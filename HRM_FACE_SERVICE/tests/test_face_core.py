"""
Tests cho services/face_core.py
Kiểm tra các utility functions không phụ thuộc vào GPU/camera.
"""
import pytest
import json
import numpy as np
from services.face_core import (
    EMBEDDING_DIM,
    VALID_ANGLES,
    MIN_ANGLES_REQUIRED,
    validate_angles,
    parse_embedding_data,
    create_index,
    index_to_cpu,
)


class TestConstants:
    """Test các constants hợp lệ."""

    def test_embedding_dim(self):
        assert EMBEDDING_DIM == 512

    def test_valid_angles(self):
        assert set(VALID_ANGLES) == {"front", "left", "right", "up", "down"}

    def test_min_angles_required(self):
        assert MIN_ANGLES_REQUIRED == 5


class TestValidateAngles:
    """Test validate_angles()."""

    def test_all_angles_valid(self):
        embeddings = {a: [0.1] * EMBEDDING_DIM for a in VALID_ANGLES}
        is_valid, captured, missing = validate_angles(embeddings)
        assert is_valid is True
        assert len(captured) == 5
        assert len(missing) == 0

    def test_missing_one_angle(self):
        embeddings = {a: [0.1] * EMBEDDING_DIM for a in ["front", "left", "right", "up"]}
        is_valid, captured, missing = validate_angles(embeddings)
        assert is_valid is False
        assert "down" in missing

    def test_empty_embeddings(self):
        is_valid, captured, missing = validate_angles({})
        assert is_valid is False
        assert len(missing) == 5

    def test_extra_unknown_angles_ignored(self):
        embeddings = {a: [0.1] * EMBEDDING_DIM for a in VALID_ANGLES}
        embeddings["diagonal"] = [0.1] * EMBEDDING_DIM
        is_valid, captured, missing = validate_angles(embeddings)
        assert is_valid is True
        assert "diagonal" not in captured


class TestParseEmbeddingData:
    """Test parse_embedding_data() xử lý nhiều format."""

    def test_dict_input(self):
        data = {"front": [0.1, 0.2], "left": [0.3, 0.4]}
        result = parse_embedding_data(data)
        assert result == data

    def test_json_string_input(self):
        data = {"front": [0.1, 0.2]}
        json_str = json.dumps(data)
        result = parse_embedding_data(json_str)
        assert result == data

    def test_double_encoded_json(self):
        """Double-encoded JSON: parse_embedding_data strips one layer."""
        data = {"front": [0.1]}
        json_str = json.dumps(json.dumps(data))  # '"{\\"front\\": [0.1]}"'
        result = parse_embedding_data(json_str)
        # parse_embedding_data handles escaped quotes → returns dict
        assert isinstance(result, (dict, str))
        # If it returns a string, it decoded one layer; if dict, fully decoded
        if isinstance(result, str):
            assert json.loads(result) == data
        else:
            assert result == data

    def test_invalid_string_returns_none(self):
        result = parse_embedding_data("not valid json at all {{{")
        assert result is None

    def test_none_input_returns_none(self):
        result = parse_embedding_data(None)
        assert result is None

    def test_integer_input_returns_none(self):
        result = parse_embedding_data(12345)
        assert result is None


class TestCreateIndex:
    """Test FAISS index creation."""

    def test_creates_empty_index(self):
        idx = create_index()
        assert idx.ntotal == 0

    def test_index_accepts_correct_dimension(self):
        idx = create_index()
        vector = np.random.randn(1, EMBEDDING_DIM).astype(np.float32)
        idx.add(vector)
        assert idx.ntotal == 1

    def test_index_search_works(self):
        idx = create_index()
        vectors = np.random.randn(10, EMBEDDING_DIM).astype(np.float32)
        idx.add(vectors)

        query = np.random.randn(1, EMBEDDING_DIM).astype(np.float32)
        distances, indices = idx.search(query, k=3)
        assert distances.shape == (1, 3)
        assert indices.shape == (1, 3)

    def test_index_to_cpu_roundtrip(self):
        idx = create_index()
        vectors = np.random.randn(5, EMBEDDING_DIM).astype(np.float32)
        idx.add(vectors)

        cpu_idx = index_to_cpu(idx)
        assert cpu_idx.ntotal == 5
