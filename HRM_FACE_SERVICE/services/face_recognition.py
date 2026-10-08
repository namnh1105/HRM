import json
import tempfile
import cv2
import numpy as np
import faiss
import os
from datetime import datetime, date, time as time_type
from io import BytesIO
from fastapi import UploadFile
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy.orm.attributes import flag_modified
from models.employee import Employee
from services.face_core import (
    EMBEDDING_DIM, INDEX_FILE, VALID_ANGLES, MIN_ANGLES_REQUIRED,
    get_face_analyzer, create_index, index_to_cpu, index_to_gpu,
    extract_embeddings_from_frames, validate_angles,
    add_embeddings_to_memory_index, parse_embedding_data,
    load_employee_id_map, save_employee_id_map,
)

face_analyzer = get_face_analyzer()

# In-memory FAISS index (GPU nếu có) dùng cho API process
index = create_index()

if os.path.exists(INDEX_FILE):
    try:
        temp_index = faiss.read_index(INDEX_FILE)
        index = index_to_gpu(temp_index)
        print(f"Loaded FAISS index with {index.ntotal} faces")
    except Exception as e:
        print(f"Error loading index: {e}")

employee_id_map = {}


async def rebuild_index(db):
    """Rebuild FAISS index from employee embeddings"""
    global index, employee_id_map
    
    index = create_index()
    employee_id_map = {}
    
    print("Đang khởi tạo FAISS index trực tiếp từ cột embedding trong bảng Employee...")
    
    query = select(Employee.id, Employee.embedding).where(
        Employee.embedding != None,
        Employee.is_deleted == False
    )
    result = await db.execute(query)
    employee_data = result.all()
    
    print(f"Tìm thấy {len(employee_data)} nhân viên có dữ liệu embedding")
    
    fix_needed = []
    count = 0
    
    for employee_id, embedding_data in employee_data:
        try:
            embeddings_dict = parse_embedding_data(embedding_data, employee_id)
            
            if embeddings_dict is None or not isinstance(embeddings_dict, dict):
                fix_needed.append(employee_id)
                continue
                
            for angle, embedding in embeddings_dict.items():
                if angle not in VALID_ANGLES:
                    continue
                    
                if not isinstance(embedding, list) or len(embedding) != EMBEDDING_DIM:
                    continue
                
                vector = np.array([embedding], dtype=np.float32)
                index.add(vector)
                employee_id_map[count] = (str(employee_id), angle)
                count += 1
                
        except Exception as e:
            print(f"Error processing employee {employee_id}: {str(e)}")
            fix_needed.append(employee_id)
    
    if count > 0:
        cpu_index = index_to_cpu(index)
        faiss.write_index(cpu_index, INDEX_FILE)
        save_employee_id_map(employee_id_map)
        print(f"Đã lưu FAISS index với {count} vector vào {INDEX_FILE}")
    else:
        print("CẢNH BÁO: Không tìm thấy dữ liệu embedding hợp lệ nào!")
        
    print(f"Đã xây dựng xong FAISS index với {index.ntotal} vectors")
    
    return count


async def process_video(video):
    """Process video (UploadFile) to extract face embeddings from different angles."""
    contents = await video.read()

    with tempfile.NamedTemporaryFile(delete=False, suffix=".mp4") as temp_video:
        temp_video.write(contents)
        temp_video_path = temp_video.name

    try:
        return extract_embeddings_from_frames(temp_video_path)
    finally:
        os.unlink(temp_video_path)


def add_to_index(employee_id, embeddings):
    """Add employee face embeddings to in-memory FAISS index (API process)."""
    global index, employee_id_map
    return add_embeddings_to_memory_index(employee_id, embeddings, index, employee_id_map)


def is_face_registered(embedding, employee_id=None, threshold=1):
    """Check if face is registered and optionally matches a specific employee"""
    global employee_id_map
    
    print(f"Đang so sánh khuôn mặt với ngưỡng threshold = {threshold}")
    
    if index.ntotal == 0:
        print("FAISS index trống, không có khuôn mặt nào được đăng ký")
        return False
        
    if isinstance(embedding, list):
        query = np.array([embedding], dtype=np.float32)
    else:
        query = np.array([embedding.tolist()], dtype=np.float32)
    
    try:
        distances, indices = index.search(query, k=min(5, index.ntotal))
        print(f"Distances: {distances[0]}, Indices: {indices[0]}")
        
        if distances[0][0] > threshold:
            print(f"Khoảng cách lớn nhất {distances[0][0]} vượt quá ngưỡng {threshold}")
            return False
            
        if employee_id:
            for i, idx in enumerate(indices[0]):
                if idx < 0 or idx >= len(employee_id_map):
                    continue
                    
                matched_id, angle = employee_id_map.get(int(idx), (None, None))
                if matched_id is None:
                    continue
                    
                print(f"Match: {matched_id} (angle: {angle}, distance: {distances[0][i]:.4f})")
                if matched_id == str(employee_id) and distances[0][i] <= threshold:
                    print(f"Tìm thấy khuôn mặt khớp với nhân viên {employee_id}")
                    return True
            
            print(f"Không tìm thấy khuôn mặt khớp với nhân viên {employee_id}")
            return False
        
        return True
    except Exception as e:
        print(f"Lỗi khi tìm kiếm khuôn mặt: {str(e)}")
        return False


async def get_face_status_service(employee_id: str, db: AsyncSession):
    """Check if employee has registered face"""
    result = await db.execute(
        select(Employee).where(
            Employee.id == employee_id,
            Employee.is_deleted == False
        )
    )
    employee = result.scalar_one_or_none()

    if not employee:
        return {"registered": False, "message": "Không tìm thấy nhân viên"}

    is_registered = employee.embedding is not None and bool(employee.embedding)
    return {
        "registered": is_registered,
        "employeeId": str(employee_id),
    }


async def verify_face_service(file, employee_id: str, db: AsyncSession):
    """
    Internal API: Verify if a face photo matches the registered employee.
    Returns matched=True/False with confidence info.
    """
    # Read and decode image
    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    frame = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if frame is None:
        return {"error": "Không thể giải mã ảnh"}

    # Detect face
    faces = face_analyzer.get(frame)
    if not faces:
        return {"error": "Không tìm thấy khuôn mặt trong ảnh"}

    face_embedding = faces[0].normed_embedding.tolist()

    # Check if employee has a registered face
    status = await get_face_status_service(employee_id, db)
    if not status.get("registered"):
        return {"error": "Nhân viên chưa đăng ký khuôn mặt"}

    # Verify face matches employee (threshold 0.8)
    matched = is_face_registered(face_embedding, employee_id, threshold=0.8)

    return {
        "matched": matched,
        "employeeId": str(employee_id),
    }


async def register_face_service(employee_id: str, video, db: AsyncSession):
    """Register employee face from video (async, dùng cho API trực tiếp nếu cần)."""
    # Check if face is already registered
    check_result = await db.execute(
        select(Employee.embedding).where(
            Employee.id == employee_id,
            Employee.is_deleted == False
        )
    )
    existing_embedding = check_result.scalar_one_or_none()
    if existing_embedding is not None and bool(existing_embedding):
        return {"error": "Khuôn mặt đã được đăng ký. Mỗi nhân viên chỉ được đăng ký một lần."}

    face_embeddings = await process_video(video)

    if not face_embeddings:
        return {"error": "Không phát hiện khuôn mặt hợp lệ trong video"}

    is_valid, captured, missing = validate_angles(face_embeddings)
    if not is_valid:
        return {
            "error": f"Cần tối thiểu {MIN_ANGLES_REQUIRED} góc khuôn mặt để đăng ký. "
                     f"Chỉ nhận được {len(captured)} góc: {captured}. "
                     f"Các góc còn thiếu: {missing}"
        }

    async with db.begin():
        result = await db.execute(
            select(Employee).where(
                Employee.id == employee_id,
                Employee.is_deleted == False
            )
        )
        employee = result.scalar_one_or_none()

        if not employee:
            return {"error": "Không tìm thấy nhân viên"}

        employee.embedding = face_embeddings
        success = add_to_index(employee_id, face_embeddings)

    return {
        "message": "Đăng ký khuôn mặt thành công", 
        "employeeId": str(employee_id),
        "indexUpdated": success,
        "anglesCaptured": list(face_embeddings.keys())
    }

