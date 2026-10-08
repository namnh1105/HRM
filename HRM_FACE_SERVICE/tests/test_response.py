"""
Tests cho utils/response.py
Kiểm tra response wrapper trả về đúng format và timezone.
"""
import pytest
from datetime import timedelta


class TestApiSuccess:
    """Test api_success() response wrapper."""

    def test_returns_success_true(self):
        from utils.response import api_success
        result = api_success(data={"key": "value"})
        assert result["success"] is True

    def test_contains_data(self):
        from utils.response import api_success
        result = api_success(data={"id": 123})
        assert result["data"] == {"id": 123}

    def test_contains_message(self):
        from utils.response import api_success
        result = api_success(message="OK")
        assert result["message"] == "OK"

    def test_errors_is_none(self):
        from utils.response import api_success
        result = api_success()
        assert result["errors"] is None

    def test_timestamp_has_vietnam_offset(self):
        from utils.response import api_success
        result = api_success()
        ts = result["timestamp"]
        assert "+07:00" in ts

    def test_timestamp_is_iso_format(self):
        from utils.response import api_success
        from datetime import datetime
        result = api_success()
        ts = result["timestamp"]
        # Should be parseable as ISO datetime
        parsed = datetime.fromisoformat(ts)
        assert parsed is not None


class TestApiError:
    """Test api_error() response wrapper."""

    def test_returns_success_false(self):
        from utils.response import api_error
        result = api_error("Something went wrong")
        assert result["success"] is False

    def test_contains_message(self):
        from utils.response import api_error
        result = api_error("Lỗi hệ thống")
        assert result["message"] == "Lỗi hệ thống"

    def test_data_is_none(self):
        from utils.response import api_error
        result = api_error("error")
        assert result["data"] is None

    def test_errors_defaults_to_message_list(self):
        from utils.response import api_error
        result = api_error("error msg")
        assert result["errors"] == ["error msg"]

    def test_custom_errors_list(self):
        from utils.response import api_error
        result = api_error("main error", errors=["err1", "err2"])
        assert result["errors"] == ["err1", "err2"]

    def test_timestamp_has_vietnam_offset(self):
        from utils.response import api_error
        result = api_error("test")
        assert "+07:00" in result["timestamp"]


class TestWrapServiceResult:
    """Test wrap_service_result() auto-detect success/error."""

    def test_wraps_error_result(self):
        from utils.response import wrap_service_result
        result = wrap_service_result({"error": "Không tìm thấy"})
        assert result["success"] is False
        assert result["message"] == "Không tìm thấy"

    def test_wraps_success_result(self):
        from utils.response import wrap_service_result
        result = wrap_service_result({"employeeId": "123", "message": "Thành công"})
        assert result["success"] is True
        assert result["data"]["employeeId"] == "123"
        # message is popped from data and put in response message
        assert result["message"] == "Thành công"
        assert "message" not in result["data"]
