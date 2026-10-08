"""
Entry point để chạy worker: python -m redis_queue
"""
from redis_queue.worker import start_worker

if __name__ == "__main__":
    start_worker()
