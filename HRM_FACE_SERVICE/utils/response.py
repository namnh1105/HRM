"""
Standard API response wrapper matching Java backend ApiResponse<T> format.
"""
from utils.timezone import now_local


def api_success(data: dict = None, message: str = None) -> dict:
    """Wrap a successful response in the standard ApiResponse format."""
    return {
        "success": True,
        "code": None,
        "message": message,
        "data": data,
        "errors": None,
        "timestamp": now_local().isoformat(timespec='milliseconds'),
    }


def api_error(message: str, errors: list = None) -> dict:
    """Wrap an error response in the standard ApiResponse format."""
    return {
        "success": False,
        "code": None,
        "message": message,
        "data": None,
        "errors": errors or [message],
        "timestamp": now_local().isoformat(timespec='milliseconds'),
    }


def wrap_service_result(result: dict) -> dict:
    """
    Automatically wrap a service result dict into ApiResponse format.
    If the dict has an 'error' key, treat it as an error response.
    Otherwise treat it as a success response.
    """
    if "error" in result:
        return api_error(result["error"])
    
    message = result.pop("message", None)
    return api_success(data=result, message=message)
