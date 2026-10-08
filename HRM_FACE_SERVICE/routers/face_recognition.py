import os
import tempfile
from fastapi import APIRouter, File, Form, UploadFile, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from database import get_db
from services.face_recognition import (
    verify_face_service,
    get_face_status_service,
    rebuild_index
)
from redis_queue.queue import enqueue_face_registration, get_job_status, get_queue_length, FACE_REGISTRATION_QUEUE
from utils.response import api_success, api_error

router = APIRouter(prefix="/face", tags=["Face Recognition (Internal)"])

# Thư mục lưu video tạm cho worker xử lý
TEMP_VIDEO_DIR = os.path.join(tempfile.gettempdir(), "face_registration_videos")
os.makedirs(TEMP_VIDEO_DIR, exist_ok=True)


@router.post("/verify")
async def verify_face(
    file: UploadFile = File(..., description="Ảnh khuôn mặt để xác thực"),
    employee_id: str = Form(..., description="ID của nhân viên"),
    db: AsyncSession = Depends(get_db)
):
    """
    Internal API: Xác thực khuôn mặt so với nhân viên đã đăng ký.
    Trả về matched=true nếu khuôn mặt khớp.
    """
    result = await verify_face_service(file, employee_id, db)
    if "error" in result:
        return api_error(result["error"])
    return api_success(data=result)


@router.put("/register")
async def register_face(
    employee_id: str = Form(...),
    video: UploadFile = File(...),
):
    """
    Đăng ký khuôn mặt cho nhân viên từ video.
    Video được lưu tạm và đưa vào hàng đợi Redis để worker xử lý bất đồng bộ.
    Trả về job_id để theo dõi trạng thái.
    """
    video_filename = f"{employee_id}_{video.filename}"
    video_path = os.path.join(TEMP_VIDEO_DIR, video_filename)

    contents = await video.read()
    with open(video_path, "wb") as f:
        f.write(contents)

    job_id = enqueue_face_registration(employee_id, video_path)

    return api_success(
        data={
            "jobId": job_id,
            "employeeId": employee_id,
            "message": "Yêu cầu đăng ký khuôn mặt đã được đưa vào hàng đợi xử lý",
        },
        message="Đăng ký khuôn mặt đang được xử lý"
    )


@router.get("/register/status/{job_id}")
async def get_registration_status(job_id: str):
    """
    Kiểm tra trạng thái job đăng ký khuôn mặt.
    """
    status = get_job_status(job_id)
    if status is None:
        return api_error("Không tìm thấy job với ID này")
    return api_success(data={"jobId": job_id, **status})


@router.get("/register/queue-info")
async def get_queue_info():
    """
    Lấy thông tin hàng đợi đăng ký khuôn mặt.
    """
    length = get_queue_length(FACE_REGISTRATION_QUEUE)
    return api_success(data={"queueName": FACE_REGISTRATION_QUEUE, "pendingJobs": length})


@router.get("/status/{employee_id}")
async def get_face_status(
    employee_id: str,
    db: AsyncSession = Depends(get_db)
):
    """
    Kiểm tra trạng thái đăng ký khuôn mặt của nhân viên.
    """
    result = await get_face_status_service(employee_id, db)
    return api_success(data=result)


@router.post("/rebuild-index")
async def rebuild_face_index(db: AsyncSession = Depends(get_db)):
    """
    Rebuild FAISS index từ dữ liệu embedding của nhân viên.
    Sử dụng khi cần đồng bộ lại index với database.
    """
    count = await rebuild_index(db)
    return api_success(
        data={"vectorsAdded": count},
        message="Face index rebuilt successfully"
    )
