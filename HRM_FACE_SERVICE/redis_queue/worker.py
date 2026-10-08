"""
Redis Queue Worker.
Lắng nghe các hàng đợi Redis và xử lý jobs.
Chạy trong một process riêng biệt: python -m redis_queue.worker
"""
import json
import signal
import sys
import time

from redis_queue.config import get_redis_client, close_redis
from redis_queue.queue import (
    FACE_REGISTRATION_QUEUE,
    STATUS_PROCESSING,
    STATUS_COMPLETED,
    STATUS_FAILED,
    dequeue_task,
    update_job_status,
)
from redis_queue.jobs.face_registration import process_face_registration

JOB_HANDLERS = {
    "register_face": process_face_registration,
}

_running = True


def _signal_handler(signum, frame):
    """Xử lý tín hiệu dừng worker."""
    global _running
    print("\n[Worker] Nhận tín hiệu dừng, đang tắt worker...")
    _running = False


def process_job(job_data: dict):
    """
    Xử lý một job từ queue.

    Args:
        job_data: dict chứa job_id, task, status, created_at
    """
    job_id = job_data.get("job_id", "unknown")
    task = job_data.get("task", {})
    task_type = task.get("type", "unknown")

    print(f"[Worker] Đang xử lý job {job_id} (type: {task_type})")

    update_job_status(job_id, STATUS_PROCESSING)

    handler = JOB_HANDLERS.get(task_type)
    if handler is None:
        error_msg = f"Không tìm thấy handler cho task type: {task_type}"
        print(f"[Worker] {error_msg}")
        update_job_status(job_id, STATUS_FAILED, {"error": error_msg})
        return

    try:
        result = handler(task)

        if "error" in result:
            print(f"[Worker] Job {job_id} thất bại: {result['error']}")
            update_job_status(job_id, STATUS_FAILED, result)
        else:
            print(f"[Worker] Job {job_id} hoàn thành thành công")
            update_job_status(job_id, STATUS_COMPLETED, result)

    except Exception as e:
        error_msg = f"Lỗi không mong đợi khi xử lý job: {str(e)}"
        print(f"[Worker] {error_msg}")
        update_job_status(job_id, STATUS_FAILED, {"error": error_msg})


def _worker_loop(queues: list[str], poll_interval: int = 1):
    """
    Vòng lặp chính của worker, dùng chung cho cả standalone và background thread.
    """
    global _running

    try:
        client = get_redis_client()
        client.ping()
        print("[Worker] Kết nối Redis thành công")
    except Exception as e:
        print(f"[Worker] Không thể kết nối Redis: {e}")
        return

    while _running:
        try:
            for queue_name in queues:
                job_data = dequeue_task(queue_name, timeout=0)
                if job_data:
                    process_job(job_data)

            time.sleep(poll_interval)

        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"[Worker] Lỗi trong vòng lặp chính: {e}")
            time.sleep(poll_interval)

    print("[Worker] Worker đã dừng")
    close_redis()


def start_worker(queues: list[str] = None, poll_interval: int = 1):
    """
    Khởi chạy worker (standalone mode - chạy ở main thread).
    Dùng khi chạy: python -m redis_queue
    """
    global _running

    if queues is None:
        queues = [FACE_REGISTRATION_QUEUE]

    signal.signal(signal.SIGINT, _signal_handler)
    signal.signal(signal.SIGTERM, _signal_handler)

    print(f"[Worker] Khởi động worker (standalone), lắng nghe queues: {queues}")
    print(f"[Worker] Nhấn Ctrl+C để dừng worker")

    _worker_loop(queues, poll_interval)


def start_worker_thread(queues: list[str] = None, poll_interval: int = 1):
    """
    Khởi chạy worker trong background daemon thread.
    Dùng khi tích hợp vào FastAPI lifespan - tự động chạy cùng app.
    Thread sẽ tự dừng khi main process tắt (daemon=True).
    """
    import threading

    global _running
    _running = True

    if queues is None:
        queues = [FACE_REGISTRATION_QUEUE]

    print(f"[Worker] Khởi động worker (background thread), lắng nghe queues: {queues}")

    worker_thread = threading.Thread(
        target=_worker_loop,
        args=(queues, poll_interval),
        daemon=True,
        name="redis-queue-worker",
    )
    worker_thread.start()
    return worker_thread


def stop_worker():
    """Dừng worker (dùng cho cả standalone và background thread)."""
    global _running
    _running = False
    print("[Worker] Đang dừng worker...")


if __name__ == "__main__":
    start_worker()
