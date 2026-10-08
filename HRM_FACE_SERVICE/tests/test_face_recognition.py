"""
Tests cho services/face_recognition.py
"""
import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from datetime import date, time, datetime
import uuid
import numpy as np


# ===================== is_face_registered tests =====================


class TestIsFaceRegistered:
    """Test is_face_registered() với mocked FAISS index."""

    def test_returns_false_when_index_empty(self):
        """Index trống → không tìm thấy khuôn mặt."""
        from services.face_recognition import is_face_registered
        import services.face_recognition as module

        original_index = module.index
        try:
            mock_index = MagicMock()
            mock_index.ntotal = 0
            module.index = mock_index

            result = is_face_registered([0.1] * 512)
            assert result is False
        finally:
            module.index = original_index

    def test_returns_true_when_distance_within_threshold(self):
        """Khoảng cách < threshold → khớp."""
        from services.face_recognition import is_face_registered
        import services.face_recognition as module

        original_index = module.index
        original_map = module.employee_id_map
        try:
            mock_index = MagicMock()
            mock_index.ntotal = 5
            mock_index.search.return_value = (
                np.array([[0.3, 0.5, 0.7, 0.9, 1.1]], dtype=np.float32),
                np.array([[0, 1, 2, 3, 4]]),
            )
            module.index = mock_index
            module.employee_id_map = {
                0: ("emp-1", "front"),
                1: ("emp-1", "left"),
                2: ("emp-2", "front"),
                3: ("emp-2", "right"),
                4: ("emp-3", "front"),
            }

            result = is_face_registered([0.1] * 512, employee_id="emp-1", threshold=0.8)
            assert result is True
        finally:
            module.index = original_index
            module.employee_id_map = original_map

    def test_returns_false_when_employee_not_matched(self):
        """Khuôn mặt gần nhưng không khớp employee_id."""
        from services.face_recognition import is_face_registered
        import services.face_recognition as module

        original_index = module.index
        original_map = module.employee_id_map
        try:
            mock_index = MagicMock()
            mock_index.ntotal = 3
            mock_index.search.return_value = (
                np.array([[0.3, 0.5, 0.7]], dtype=np.float32),
                np.array([[0, 1, 2]]),
            )
            module.index = mock_index
            module.employee_id_map = {
                0: ("emp-1", "front"),
                1: ("emp-1", "left"),
                2: ("emp-1", "right"),
            }

            result = is_face_registered([0.1] * 512, employee_id="emp-999", threshold=0.8)
            assert result is False
        finally:
            module.index = original_index
            module.employee_id_map = original_map

    def test_returns_false_when_all_distances_exceed_threshold(self):
        """Tất cả khoảng cách > threshold → không khớp."""
        from services.face_recognition import is_face_registered
        import services.face_recognition as module

        original_index = module.index
        original_map = module.employee_id_map
        try:
            mock_index = MagicMock()
            mock_index.ntotal = 2
            mock_index.search.return_value = (
                np.array([[1.5, 2.0]], dtype=np.float32),
                np.array([[0, 1]]),
            )
            module.index = mock_index
            module.employee_id_map = {
                0: ("emp-1", "front"),
                1: ("emp-1", "left"),
            }

            result = is_face_registered([0.1] * 512, threshold=1.0)
            assert result is False
        finally:
            module.index = original_index
            module.employee_id_map = original_map


# ===================== get_face_status_service tests =====================


class TestGetFaceStatusService:
    """Test get_face_status_service()."""

    @pytest.mark.asyncio
    async def test_employee_not_found(self):
        from services.face_recognition import get_face_status_service

        db = AsyncMock()
        mock_result = MagicMock()
        mock_result.scalar_one_or_none.return_value = None
        db.execute.return_value = mock_result

        result = await get_face_status_service("non-existent-id", db)
        assert result["registered"] is False
        assert "Không tìm thấy nhân viên" in result["message"]

    @pytest.mark.asyncio
    async def test_employee_without_embedding(self):
        from services.face_recognition import get_face_status_service

        db = AsyncMock()
        mock_employee = MagicMock()
        mock_employee.embedding = None
        mock_result = MagicMock()
        mock_result.scalar_one_or_none.return_value = mock_employee
        db.execute.return_value = mock_result

        result = await get_face_status_service("emp-123", db)
        assert result["registered"] is False

    @pytest.mark.asyncio
    async def test_employee_with_embedding(self):
        from services.face_recognition import get_face_status_service

        db = AsyncMock()
        mock_employee = MagicMock()
        mock_employee.embedding = {"front": [0.1] * 512}
        mock_result = MagicMock()
        mock_result.scalar_one_or_none.return_value = mock_employee
        db.execute.return_value = mock_result

        result = await get_face_status_service("emp-123", db)
        assert result["registered"] is True
        assert result["employeeId"] == "emp-123"


# ===================== add_to_index tests =====================


class TestAddToIndex:
    """Test add_to_index() updates in-memory index."""

    def test_adds_embeddings_to_index(self):
        import services.face_recognition as module
        from services.face_core import create_index, EMBEDDING_DIM

        original_index = module.index
        original_map = module.employee_id_map
        try:
            module.index = create_index()
            module.employee_id_map = {}

            embeddings = {
                "front": [0.1] * EMBEDDING_DIM,
                "left": [0.2] * EMBEDDING_DIM,
            }

            result = module.add_to_index("emp-test", embeddings)
            assert result is True
            assert module.index.ntotal == 2
            assert len(module.employee_id_map) == 2
            assert module.employee_id_map[0] == ("emp-test", "front")
            assert module.employee_id_map[1] == ("emp-test", "left")
        finally:
            module.index = original_index
            module.employee_id_map = original_map
