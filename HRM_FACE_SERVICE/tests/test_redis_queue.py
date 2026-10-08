"""
Tests cho redis_queue/ (queue operations, worker, jobs).
Mock Redis để chạy mà không cần Redis server.
"""
import pytest
import json
from unittest.mock import patch, MagicMock, PropertyMock
from datetime import datetime


# ===================== Queue tests =====================


class TestEnqueueTask:
    """Test enqueue_task() đưa job vào Redis queue."""

    @patch("redis_queue.queue.get_redis_client")
    def test_enqueue_returns_job_id(self, mock_get_client):
        from redis_queue.queue import enqueue_task

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        job_id = enqueue_task("queue:test", {"type": "test_task", "data": "hello"})

        assert isinstance(job_id, str)
        assert len(job_id) == 36  # UUID format

    @patch("redis_queue.queue.get_redis_client")
    def test_enqueue_pushes_to_redis(self, mock_get_client):
        from redis_queue.queue import enqueue_task

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        enqueue_task("queue:test", {"type": "test_task"})

        # Verify rpush was called with queue name
        mock_client.rpush.assert_called_once()
        args = mock_client.rpush.call_args
        assert args[0][0] == "queue:test"

        # Verify the data contains task
        pushed_data = json.loads(args[0][1])
        assert pushed_data["task"]["type"] == "test_task"
        assert pushed_data["status"] == "pending"

    @patch("redis_queue.queue.get_redis_client")
    def test_enqueue_sets_job_status(self, mock_get_client):
        from redis_queue.queue import enqueue_task, JOB_STATUS_PREFIX

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        job_id = enqueue_task("queue:test", {"type": "test"})

        # Verify set was called for job status
        mock_client.set.assert_called_once()
        set_args = mock_client.set.call_args
        assert set_args[0][0] == f"{JOB_STATUS_PREFIX}{job_id}"
        status_data = json.loads(set_args[0][1])
        assert status_data["status"] == "pending"


class TestDequeueTask:
    """Test dequeue_task() lấy job từ Redis queue."""

    @patch("redis_queue.queue.get_redis_client")
    def test_dequeue_empty_queue(self, mock_get_client):
        from redis_queue.queue import dequeue_task

        mock_client = MagicMock()
        mock_client.lpop.return_value = None
        mock_get_client.return_value = mock_client

        result = dequeue_task("queue:test", timeout=0)
        assert result is None

    @patch("redis_queue.queue.get_redis_client")
    def test_dequeue_returns_job_data(self, mock_get_client):
        from redis_queue.queue import dequeue_task

        job_data = {
            "job_id": "test-123",
            "task": {"type": "register_face"},
            "status": "pending",
        }
        mock_client = MagicMock()
        mock_client.lpop.return_value = json.dumps(job_data).encode("utf-8")
        mock_get_client.return_value = mock_client

        result = dequeue_task("queue:test", timeout=0)
        assert result is not None
        assert result["job_id"] == "test-123"
        assert result["task"]["type"] == "register_face"

    @patch("redis_queue.queue.get_redis_client")
    def test_dequeue_with_timeout_uses_blpop(self, mock_get_client):
        from redis_queue.queue import dequeue_task

        job_data = {
            "job_id": "test-456",
            "task": {"type": "test"},
            "status": "pending",
        }
        mock_client = MagicMock()
        mock_client.blpop.return_value = (
            b"queue:test",
            json.dumps(job_data).encode("utf-8"),
        )
        mock_get_client.return_value = mock_client

        result = dequeue_task("queue:test", timeout=5)
        assert result is not None
        assert result["job_id"] == "test-456"
        mock_client.blpop.assert_called_once_with("queue:test", timeout=5)


class TestUpdateJobStatus:
    """Test update_job_status()."""

    @patch("redis_queue.queue.get_redis_client")
    def test_update_status_sets_redis_key(self, mock_get_client):
        from redis_queue.queue import update_job_status, JOB_STATUS_PREFIX

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        update_job_status("job-123", "completed", {"message": "success"})

        mock_client.set.assert_called_once()
        args = mock_client.set.call_args
        assert args[0][0] == f"{JOB_STATUS_PREFIX}job-123"
        data = json.loads(args[0][1])
        assert data["status"] == "completed"
        assert data["result"]["message"] == "success"

    @patch("redis_queue.queue.get_redis_client")
    def test_update_status_without_result(self, mock_get_client):
        from redis_queue.queue import update_job_status

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        update_job_status("job-123", "processing")

        data = json.loads(mock_client.set.call_args[0][1])
        assert data["status"] == "processing"
        assert "result" not in data


class TestGetJobStatus:
    """Test get_job_status()."""

    @patch("redis_queue.queue.get_redis_client")
    def test_job_not_found(self, mock_get_client):
        from redis_queue.queue import get_job_status

        mock_client = MagicMock()
        mock_client.get.return_value = None
        mock_get_client.return_value = mock_client

        result = get_job_status("non-existent")
        assert result is None

    @patch("redis_queue.queue.get_redis_client")
    def test_job_found(self, mock_get_client):
        from redis_queue.queue import get_job_status

        status = {"status": "completed", "result": {"message": "done"}}
        mock_client = MagicMock()
        mock_client.get.return_value = json.dumps(status).encode("utf-8")
        mock_get_client.return_value = mock_client

        result = get_job_status("job-123")
        assert result is not None
        assert result["status"] == "completed"


class TestEnqueueFaceRegistration:
    """Test enqueue_face_registration() convenience function."""

    @patch("redis_queue.queue.get_redis_client")
    def test_enqueues_with_correct_task_data(self, mock_get_client):
        from redis_queue.queue import enqueue_face_registration, FACE_REGISTRATION_QUEUE

        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        job_id = enqueue_face_registration("emp-1", "/tmp/video.mp4")

        assert isinstance(job_id, str)
        # Verify the task data
        rpush_args = mock_client.rpush.call_args
        assert rpush_args[0][0] == FACE_REGISTRATION_QUEUE
        job_data = json.loads(rpush_args[0][1])
        assert job_data["task"]["type"] == "register_face"
        assert job_data["task"]["employee_id"] == "emp-1"
        assert job_data["task"]["video_path"] == "/tmp/video.mp4"


class TestGetQueueLength:
    """Test get_queue_length()."""

    @patch("redis_queue.queue.get_redis_client")
    def test_returns_length(self, mock_get_client):
        from redis_queue.queue import get_queue_length

        mock_client = MagicMock()
        mock_client.llen.return_value = 5
        mock_get_client.return_value = mock_client

        result = get_queue_length("queue:test")
        assert result == 5

    @patch("redis_queue.queue.get_redis_client")
    def test_empty_queue_returns_zero(self, mock_get_client):
        from redis_queue.queue import get_queue_length

        mock_client = MagicMock()
        mock_client.llen.return_value = 0
        mock_get_client.return_value = mock_client

        result = get_queue_length("queue:test")
        assert result == 0


# ===================== Worker tests =====================


class TestProcessJob:
    """Test worker.process_job()."""

    @patch("redis_queue.worker.update_job_status")
    @patch("redis_queue.worker.JOB_HANDLERS", {"test_type": MagicMock(return_value={"message": "ok"})})
    def test_successful_job(self, mock_update_status):
        from redis_queue.worker import process_job

        job_data = {
            "job_id": "job-1",
            "task": {"type": "test_type", "data": "hello"},
        }
        process_job(job_data)

        # Should update to processing then completed
        calls = mock_update_status.call_args_list
        assert calls[0][0] == ("job-1", "processing")
        assert calls[1][0][0] == "job-1"
        assert calls[1][0][1] == "completed"

    @patch("redis_queue.worker.update_job_status")
    @patch("redis_queue.worker.JOB_HANDLERS", {"test_type": MagicMock(return_value={"error": "something broke"})})
    def test_job_returns_error(self, mock_update_status):
        from redis_queue.worker import process_job

        job_data = {
            "job_id": "job-2",
            "task": {"type": "test_type"},
        }
        process_job(job_data)

        calls = mock_update_status.call_args_list
        assert calls[1][0][1] == "failed"

    @patch("redis_queue.worker.update_job_status")
    def test_unknown_task_type(self, mock_update_status):
        from redis_queue.worker import process_job

        job_data = {
            "job_id": "job-3",
            "task": {"type": "unknown_type"},
        }
        process_job(job_data)

        calls = mock_update_status.call_args_list
        assert calls[1][0][1] == "failed"
        assert "error" in calls[1][0][2]

    @patch("redis_queue.worker.update_job_status")
    @patch("redis_queue.worker.JOB_HANDLERS", {"crash": MagicMock(side_effect=RuntimeError("boom"))})
    def test_handler_exception(self, mock_update_status):
        from redis_queue.worker import process_job

        job_data = {
            "job_id": "job-4",
            "task": {"type": "crash"},
        }
        process_job(job_data)

        calls = mock_update_status.call_args_list
        assert calls[1][0][1] == "failed"
        assert "boom" in calls[1][0][2]["error"]
