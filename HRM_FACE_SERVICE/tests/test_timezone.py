"""
Tests cho utils/timezone.py
Kiểm tra timezone utility hoạt động đúng trên cả Windows và Linux/Docker.
"""
import pytest
from datetime import datetime, timezone, timedelta


class TestGetTimezone:
    """Test get_timezone() trả về timezone object đúng."""

    def test_returns_timezone_object(self):
        from utils.timezone import get_timezone
        tz = get_timezone("Asia/Ho_Chi_Minh")
        assert tz is not None

    def test_vietnam_timezone_offset_is_utc_plus_7(self):
        from utils.timezone import get_timezone
        tz = get_timezone("Asia/Ho_Chi_Minh")
        dt = datetime(2026, 6, 15, 12, 0, 0, tzinfo=tz)
        offset = dt.utcoffset()
        assert offset == timedelta(hours=7)

    def test_utc_timezone(self):
        from utils.timezone import get_timezone
        tz = get_timezone("UTC")
        dt = datetime(2026, 1, 1, 0, 0, 0, tzinfo=tz)
        assert dt.utcoffset() == timedelta(hours=0)

    def test_fallback_for_unknown_timezone(self):
        """Nếu timezone không hợp lệ, fallback sang UTC+7."""
        from utils.timezone import get_timezone
        tz = get_timezone("Invalid/Timezone")
        dt = datetime(2026, 1, 1, 12, 0, 0, tzinfo=tz)
        # Fallback = UTC+7
        assert dt.utcoffset() == timedelta(hours=7)


class TestNowLocal:
    """Test now_local() trả về thời gian đúng timezone."""

    def test_returns_datetime(self):
        from utils.timezone import now_local
        result = now_local()
        assert isinstance(result, datetime)

    def test_is_timezone_aware(self):
        from utils.timezone import now_local
        result = now_local()
        assert result.tzinfo is not None

    def test_utc_offset_is_7_hours(self):
        from utils.timezone import now_local
        result = now_local()
        assert result.utcoffset() == timedelta(hours=7)

    def test_difference_from_utc_now(self):
        """now_local() nên cách UTC khoảng 7 giờ."""
        from utils.timezone import now_local
        local = now_local()
        utc = datetime.now(timezone.utc)
        # So sánh bằng timestamp (epoch) – chênh lệch < 2 giây
        diff = abs(local.timestamp() - utc.timestamp())
        assert diff < 2

    def test_isoformat_contains_offset(self):
        from utils.timezone import now_local
        iso = now_local().isoformat()
        # Must contain +07:00
        assert "+07:00" in iso


class TestLocalTzSingleton:
    """Test LOCAL_TZ singleton khớp với settings."""

    def test_local_tz_is_not_none(self):
        from utils.timezone import LOCAL_TZ
        assert LOCAL_TZ is not None

    def test_local_tz_matches_settings(self):
        from utils.timezone import LOCAL_TZ
        from database.config import get_settings
        settings = get_settings()
        assert settings.TIMEZONE == "Asia/Ho_Chi_Minh"
        # Offset phải là +7
        dt = datetime(2026, 1, 1, 0, 0, 0, tzinfo=LOCAL_TZ)
        assert dt.utcoffset() == timedelta(hours=7)
