"""
Face Recognition Core - Shared logic giữa API service và Redis worker.
Chứa: constants, face analyzer, video processing, FAISS index management.
"""
import json
import os
import cv2
import numpy as np
import faiss
from insightface.app import FaceAnalysis


# ===================== Constants =====================

EMBEDDING_DIM = 512
INDEX_FILE = "face_index.bin"
ID_MAP_FILE = "employee_id_map.npy"
VALID_ANGLES = ["front", "left", "right", "up", "down"]
MIN_ANGLES_REQUIRED = 5


# ===================== Face Analyzer =====================

_face_analyzer = None


def get_face_analyzer() -> FaceAnalysis:
    """Trả về FaceAnalysis singleton, lazy-init."""
    global _face_analyzer
    if _face_analyzer is None:
        _face_analyzer = FaceAnalysis(providers=["CUDAExecutionProvider"])
        _face_analyzer.prepare(ctx_id=0, det_size=(640, 640))
    return _face_analyzer


# ===================== GPU FAISS setup =====================

try:
    _gpu_res = faiss.StandardGpuResources()
    USE_GPU_FAISS = True
    print("[FaceCore] Using GPU FAISS index")
except AttributeError:
    _gpu_res = None
    USE_GPU_FAISS = False
    print("[FaceCore] GPU FAISS not available, falling back to CPU")


def create_index():
    """Tạo FAISS index mới (GPU nếu có, fallback CPU)."""
    if USE_GPU_FAISS:
        return faiss.GpuIndexFlatL2(_gpu_res, EMBEDDING_DIM)
    return faiss.IndexFlatL2(EMBEDDING_DIM)


def index_to_cpu(idx):
    """Chuyển FAISS index về CPU (để lưu file)."""
    if USE_GPU_FAISS:
        return faiss.index_gpu_to_cpu(idx)
    return idx


def index_to_gpu(idx):
    """Chuyển FAISS index lên GPU (nếu có)."""
    if USE_GPU_FAISS:
        return faiss.index_cpu_to_gpu(_gpu_res, 0, idx)
    return idx


# ===================== Video Processing =====================

def extract_embeddings_from_frames(video_path: str) -> dict | None:
    """
    Xử lý video từ đường dẫn file, trích xuất face embeddings từ các góc.
    Đây là logic CORE dùng chung cho cả async service và sync worker.

    Returns:
        dict {"front": [...], "left": [...], ...} hoặc None nếu không tìm thấy mặt.
    """
    analyzer = get_face_analyzer()

    cap = cv2.VideoCapture(video_path)
    face_angles = {angle: False for angle in VALID_ANGLES}
    face_embeddings = {}

    while not all(face_angles.values()):
        ret, frame = cap.read()
        if not ret:
            break

        faces = analyzer.get(frame)
        for face in faces:
            embedding = face.normed_embedding.tolist()
            yaw, pitch, roll = face.pose

            if -10 < yaw < 10 and -10 < pitch < 10 and not face_angles["front"]:
                face_angles["front"], face_embeddings["front"] = True, embedding
            elif yaw > 20 and -10 < pitch < 10 and not face_angles["right"]:
                face_angles["right"], face_embeddings["right"] = True, embedding
            elif yaw < -20 and -10 < pitch < 10 and not face_angles["left"]:
                face_angles["left"], face_embeddings["left"] = True, embedding
            elif -10 < yaw < 10 and pitch > 15 and not face_angles["up"]:
                face_angles["up"], face_embeddings["up"] = True, embedding
            elif -10 < yaw < 10 and pitch < -15 and not face_angles["down"]:
                face_angles["down"], face_embeddings["down"] = True, embedding

    cap.release()

    if not face_embeddings:
        return None
    return face_embeddings


def validate_angles(face_embeddings: dict) -> tuple[bool, list[str], list[str]]:
    """
    Kiểm tra xem face_embeddings có đủ số góc yêu cầu không.

    Returns:
        (is_valid, captured_angles, missing_angles)
    """
    captured = [a for a in face_embeddings.keys() if a in VALID_ANGLES]
    missing = [a for a in VALID_ANGLES if a not in captured]
    return len(captured) >= MIN_ANGLES_REQUIRED, captured, missing


# ===================== FAISS Index I/O =====================

def load_employee_id_map() -> dict:
    """Load employee_id_map từ file .npy."""
    if os.path.exists(ID_MAP_FILE):
        return np.load(ID_MAP_FILE, allow_pickle=True).item()
    return {}


def save_employee_id_map(id_map: dict):
    """Lưu employee_id_map ra file .npy."""
    np.save(ID_MAP_FILE, id_map)


def add_embeddings_to_disk_index(employee_id: str, embeddings: dict) -> bool:
    """
    Thêm embeddings vào FAISS index trên DISK (đọc file → thêm → ghi lại).
    Dùng cho worker process chạy riêng, không chia sẻ bộ nhớ với API process.
    """
    try:
        if os.path.exists(INDEX_FILE):
            disk_index = faiss.read_index(INDEX_FILE)
        else:
            disk_index = faiss.IndexFlatL2(EMBEDDING_DIM)

        id_map = load_employee_id_map()

        idx_start = disk_index.ntotal
        for angle, embedding in embeddings.items():
            vector = np.array([embedding], dtype=np.float32)
            disk_index.add(vector)
            id_map[idx_start] = (str(employee_id), angle)
            idx_start += 1

        faiss.write_index(disk_index, INDEX_FILE)
        save_employee_id_map(id_map)
        print(f"[FaceCore] Đã thêm {len(embeddings)} vectors cho nhân viên {employee_id} vào FAISS index (disk)")
        return True
    except Exception as e:
        print(f"[FaceCore] Lỗi khi thêm vào FAISS index (disk): {e}")
        return False


def add_embeddings_to_memory_index(employee_id: str, embeddings: dict,
                                    mem_index, id_map: dict) -> bool:
    """
    Thêm embeddings vào FAISS index trong BỘ NHỚ (in-memory, GPU nếu có).
    Dùng cho API process giữ index trong RAM để search nhanh.
    Đồng thời persist xuống file.
    """
    try:
        idx_start = mem_index.ntotal
        for angle, embedding in embeddings.items():
            vector = np.array([embedding], dtype=np.float32)
            mem_index.add(vector)
            id_map[idx_start] = (str(employee_id), angle)
            idx_start += 1

        cpu_index = index_to_cpu(mem_index)
        faiss.write_index(cpu_index, INDEX_FILE)
        save_employee_id_map(id_map)
        return True
    except Exception as e:
        print(f"[FaceCore] Lỗi khi thêm vào memory index: {e}")
        return False


def parse_embedding_data(embedding_data, employee_id=None) -> dict | None:
    """
    Parse embedding data từ DB (có thể là dict hoặc JSON string).
    Trả về dict hoặc None nếu lỗi.
    """
    if isinstance(embedding_data, dict):
        return embedding_data
    if isinstance(embedding_data, str):
        try:
            return json.loads(embedding_data)
        except json.JSONDecodeError:
            try:
                cleaned = embedding_data.replace('\\"', '"')
                if cleaned.startswith('"') and cleaned.endswith('"'):
                    cleaned = cleaned[1:-1]
                return json.loads(cleaned)
            except json.JSONDecodeError as e:
                print(f"[FaceCore] Lỗi JSON cho nhân viên {employee_id}: {e}")
                return None
    return None
