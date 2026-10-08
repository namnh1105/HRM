"""
Redis connection configuration.
Sử dụng settings từ database/config.py để khởi tạo Redis client.
"""
import redis
from database.config import get_settings


_redis_client = None


def get_redis_client() -> redis.Redis:
    """
    Trả về Redis client singleton.
    Lazy-init để tránh kết nối ngay khi import module.
    """
    global _redis_client
    if _redis_client is None:
        settings = get_settings()
        _redis_client = redis.Redis(
            host=settings.REDIS_HOST,
            port=settings.REDIS_PORT,
            db=settings.REDIS_DB,
            password=settings.REDIS_PASSWORD,
            decode_responses=False,
        )
    return _redis_client


def close_redis():
    """Đóng kết nối Redis."""
    global _redis_client
    if _redis_client is not None:
        _redis_client.close()
        _redis_client = None
