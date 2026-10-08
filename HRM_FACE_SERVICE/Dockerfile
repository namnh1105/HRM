# Build stage
FROM python:3.10-slim AS builder

WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --user \
    fastapi \
    uvicorn[standard] \
    sqlalchemy[asyncio] \
    asyncpg \
    pydantic[email] \
    pydantic-settings \
    python-multipart \
    opencv-python-headless \
    numpy \
    geopy \
    insightface \
    onnxruntime \
    faiss-cpu \
    redis \
    psycopg2-binary \
    tzdata

# Runtime stage  
FROM python:3.10-slim

# Install runtime dependencies for OpenCV and face recognition
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -g 1001 faceapi && \
    useradd -u 1001 -g faceapi -s /bin/bash -m faceapi

# Create application directory
RUN mkdir -p /opt/faceapi/logs && \
    chown -R faceapi:faceapi /opt/faceapi && \
    chmod -R 755 /opt/faceapi

WORKDIR /opt/faceapi

# Copy installed packages from builder
COPY --from=builder /root/.local /home/faceapi/.local

# Copy application code
COPY --chown=faceapi:faceapi . .

# Set environment path
ENV PATH=/home/faceapi/.local/bin:$PATH
ENV PYTHONPATH=/opt/faceapi

# Change to non-root user
USER faceapi

# Expose the port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/')" || exit 1

# Run the application
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
