"""
Timezone utility – single source of truth cho toàn bộ project.

- Windows cần package `tzdata` (pip install tzdata).
- Linux / Docker: zoneinfo đã có sẵn.
- Fallback: nếu không tìm thấy IANA tz data, dùng UTC+7 thủ công.
"""
from datetime import datetime, timezone, timedelta

try:
    from zoneinfo import ZoneInfo

    # Kiểm tra xem IANA tz data có sẵn không (Windows cần tzdata package)
    _test = ZoneInfo("Asia/Ho_Chi_Minh")
    _USE_ZONEINFO = True
except (ImportError, KeyError):
    _USE_ZONEINFO = False


def get_timezone(tz_name: str = "Asia/Ho_Chi_Minh"):
    """
    Trả về timezone object.
    Ưu tiên dùng ZoneInfo (chính xác, hỗ trợ DST).
    Fallback sang UTC+7 nếu không có tzdata.
    """
    if _USE_ZONEINFO:
        try:
            return ZoneInfo(tz_name)
        except KeyError:
            pass

    # Fallback: UTC+7 cho Asia/Ho_Chi_Minh
    _KNOWN_OFFSETS = {
        "Asia/Ho_Chi_Minh": 7,
        "Asia/Bangkok": 7,
        "Asia/Tokyo": 9,
        "Asia/Seoul": 9,
        "Asia/Shanghai": 8,
        "Asia/Singapore": 8,
        "UTC": 0,
    }
    offset_hours = _KNOWN_OFFSETS.get(tz_name, 7)
    return timezone(timedelta(hours=offset_hours))


# Singleton timezone – dùng setting từ config
from database.config import get_settings

LOCAL_TZ = get_timezone(get_settings().TIMEZONE)


def now_local() -> datetime:
    """Trả về thời gian hiện tại theo timezone đã cấu hình."""
    return datetime.now(LOCAL_TZ)
