from fastapi import FastAPI
from contextlib import asynccontextmanager
from starlette.middleware.cors import CORSMiddleware
import uvicorn
from routers import face_recognition
from services.face_recognition import rebuild_index
from database import AsyncSessionLocal
from redis_queue.worker import start_worker_thread, stop_worker

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Khởi tạo FAISS index khi khởi động
    try:
        print("Bắt đầu tải dữ liệu trực tiếp từ cột embedding trong bảng Employee...")
        async with AsyncSessionLocal() as db:
            vector_count = await rebuild_index(db)
            print(f"Đã khởi tạo FAISS index thành công với GPU: {vector_count} vector")
    except Exception as e:
        print(f"Lỗi khi khởi tạo FAISS index: {e}")

    # Tự động khởi động Redis Queue Worker trong background thread
    worker_thread = start_worker_thread()
    print("[App] Redis Queue Worker đã khởi động tự động")

    yield

    # Dừng worker khi app shutdown
    stop_worker()
    print("[App] Redis Queue Worker đã dừng")

app = FastAPI(
    title="Face Recognition Attendance API",
    description="API chấm công bằng nhận diện khuôn mặt",
    version="2.0.0",
    lifespan=lifespan
)

origins = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "*"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(face_recognition.router)

@app.get("/")
async def root():
    return {
        "message": "Face Recognition Attendance API",
        "version": "2.0.0",
        "docs": "/docs"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)


