"""
Job xử lý đăng ký khuôn mặt từ hàng đợi Redis.
Worker chạy trong background thread cùng process với FastAPI,
nên có thể cập nhật trực tiếp in-memory FAISS index.
"""
import os
from sqlalchemy import create_engine, select
from sqlalchemy.orm import Session, sessionmaker

from database.config import get_settings
from models.employee import Employee
from services.face_core import (
    VALID_ANGLES, MIN_ANGLES_REQUIRED,
    extract_embeddings_from_frames, validate_angles,
)

# ---- Sync DB session cho worker process ----
_sync_engine = None
_SessionLocal = None


def _get_sync_session() -> Session:
    """
    Tạo synchronous DB session cho worker process.
    Worker chạy đồng bộ nên dùng sync engine.
    """
    global _sync_engine, _SessionLocal
    if _sync_engine is None:
        settings = get_settings()
        sync_url = settings.database_url.replace("postgresql+asyncpg", "postgresql+psycopg2")
        _sync_engine = create_engine(sync_url)
        _SessionLocal = sessionmaker(bind=_sync_engine)
    return _SessionLocal()


def process_face_registration(task: dict) -> dict:
    """
    Xử lý job đăng ký khuôn mặt.

    Args:
        task: dict chứa employee_id và video_path

    Returns:
        dict kết quả (thành công hoặc lỗi)
    """
    employee_id = task["employee_id"]
    video_path = task["video_path"]

    print(f"[Worker] Bắt đầu xử lý đăng ký khuôn mặt cho nhân viên {employee_id}")

    if not os.path.exists(video_path):
        return {"error": f"Không tìm thấy file video: {video_path}"}

    db = _get_sync_session()

    try:
        employee = db.execute(
            select(Employee).where(
                Employee.id == employee_id,
                Employee.is_deleted == False,
            )
        ).scalar_one_or_none()

        if not employee:
            return {"error": "Không tìm thấy nhân viên"}

        if employee.embedding is not None and bool(employee.embedding):
            return {"error": "Khuôn mặt đã được đăng ký. Mỗi nhân viên chỉ được đăng ký một lần."}

        face_embeddings = extract_embeddings_from_frames(video_path)

        if not face_embeddings:
            return {"error": "Không phát hiện khuôn mặt hợp lệ trong video"}

        is_valid, captured, missing = validate_angles(face_embeddings)
        if not is_valid:
            return {
                "error": (
                    f"Cần tối thiểu {MIN_ANGLES_REQUIRED} góc khuôn mặt để đăng ký. "
                    f"Chỉ nhận được {len(captured)} góc: {captured}. "
                    f"Các góc còn thiếu: {missing}"
                )
            }

        employee.embedding = face_embeddings
        db.commit()

        # Cập nhật trực tiếp in-memory FAISS index (cùng process với FastAPI)
        # Import tại đây để tránh circular import
        from services.face_recognition import add_to_index
        index_updated = add_to_index(employee_id, face_embeddings)

        print(f"[Worker] Đăng ký khuôn mặt thành công cho nhân viên {employee_id} (index updated: {index_updated})")

        return {
            "message": "Đăng ký khuôn mặt thành công",
            "employeeId": str(employee_id),
            "indexUpdated": index_updated,
            "anglesCaptured": list(face_embeddings.keys()),
        }

    except Exception as e:
        db.rollback()
        print(f"[Worker] Lỗi khi xử lý đăng ký khuôn mặt: {e}")
        return {"error": f"Lỗi hệ thống khi xử lý đăng ký khuôn mặt: {str(e)}"}

    finally:
        db.close()
        try:
            if os.path.exists(video_path):
                os.unlink(video_path)
                print(f"[Worker] Đã xoá file video tạm: {video_path}")
        except OSError:
            pass
