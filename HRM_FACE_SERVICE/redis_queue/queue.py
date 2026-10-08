"""
Redis Queue operations.
Quản lý hàng đợi công việc (jobs) trong Redis.
"""
import json
import uuid
from datetime import datetime
from typing import Any, Dict, Optional

from redis_queue.config import get_redis_client
from utils.timezone import now_local

# Queue names
FACE_REGISTRATION_QUEUE = "queue:face_registration"
DEFAULT_QUEUE = "queue:default"

# Job status keys
JOB_STATUS_PREFIX = "job:status:"
JOB_RESULT_PREFIX = "job:result:"

# Job statuses
STATUS_PENDING = "pending"
STATUS_PROCESSING = "processing"
STATUS_COMPLETED = "completed"
STATUS_FAILED = "failed"


def _generate_job_id() -> str:
    """Tạo job ID duy nhất."""
    return str(uuid.uuid4())


def enqueue_task(queue_name: str, task: dict) -> str:
    """
    Thêm một task vào cuối hàng đợi Redis.
    Trả về job_id để theo dõi trạng thái.
    """
    client = get_redis_client()
    job_id = _generate_job_id()

    job_data = {
        "job_id": job_id,
        "task": task,
        "status": STATUS_PENDING,
        "created_at": now_local().isoformat(),
    }

    # Lưu job data vào queue
    client.rpush(queue_name, json.dumps(job_data))

    # Lưu trạng thái job
    client.set(
        f"{JOB_STATUS_PREFIX}{job_id}",
        json.dumps({"status": STATUS_PENDING, "created_at": job_data["created_at"]}),
        ex=3600,  # TTL 1 giờ
    )

    return job_id


def dequeue_task(queue_name: str, timeout: int = 0) -> Optional[Dict[str, Any]]:
    """
    Lấy task từ đầu hàng đợi Redis (blocking).
    timeout=0: chờ vô hạn, timeout>0: chờ N giây.
    """
    client = get_redis_client()

    if timeout > 0:
        result = client.blpop(queue_name, timeout=timeout)
    else:
        result = client.lpop(queue_name)
        if result:
            return json.loads(result.decode("utf-8"))
        return None

    if result:
        _, data = result
        return json.loads(data.decode("utf-8"))
    return None


def update_job_status(job_id: str, status: str, result: dict = None):
    """Cập nhật trạng thái của job."""
    client = get_redis_client()

    status_data = {
        "status": status,
        "updated_at": now_local().isoformat(),
    }
    if result:
        status_data["result"] = result

    client.set(
        f"{JOB_STATUS_PREFIX}{job_id}",
        json.dumps(status_data),
        ex=3600,
    )


def get_job_status(job_id: str) -> Optional[Dict[str, Any]]:
    """Lấy trạng thái hiện tại của job."""
    client = get_redis_client()
    data = client.get(f"{JOB_STATUS_PREFIX}{job_id}")
    if data:
        return json.loads(data.decode("utf-8"))
    return None


def get_queue_length(queue_name: str) -> int:
    """Lấy số lượng job đang chờ trong queue."""
    client = get_redis_client()
    return client.llen(queue_name)


def enqueue_face_registration(employee_id: str, video_path: str) -> str:
    """
    Đưa job đăng ký khuôn mặt vào hàng đợi.
    Video phải được lưu tạm trước khi gọi hàm này.
    Trả về job_id.
    """
    task = {
        "type": "register_face",
        "employee_id": employee_id,
        "video_path": video_path,
    }
    return enqueue_task(FACE_REGISTRATION_QUEUE, task)
