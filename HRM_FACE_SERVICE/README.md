# HRM Face Service

Dịch vụ nhận diện khuôn mặt cho hệ thống HRM, xây bằng FastAPI và được thiết kế để xử lý đăng ký khuôn mặt, xác thực khuôn mặt và quản lý FAISS index.

## Mục đích

Service này phụ trách các tác vụ liên quan đến khuôn mặt:

- Trích xuất embedding từ ảnh / video.
- Xác thực khuôn mặt với nhân viên đã đăng ký.
- Đăng ký khuôn mặt bất đồng bộ thông qua Redis queue.
- Rebuild FAISS index khi cần đồng bộ lại dữ liệu.

## Công nghệ chính

- FastAPI
- SQLAlchemy async
- PostgreSQL
- Redis
- InsightFace
- OpenCV
- FAISS
- Pydantic

## Cấu trúc chính

- `app.py` — entrypoint của API.
- `routers/face_recognition.py` — các route xử lý face API.
- `services/face_core.py` — logic lõi về embedding, video processing và FAISS.
- `services/face_recognition.py` — nghiệp vụ xác thực / cập nhật khuôn mặt.
- `redis_queue/` — queue, worker và job xử lý đăng ký khuôn mặt.
- `database/` — cấu hình kết nối DB và session async.
- `utils/response.py` — chuẩn hóa response API.

## Biến môi trường

File mẫu: `.env.example`

| Biến | Mô tả |
|---|---|
| `DB_HOST` | Host PostgreSQL |
| `DB_PORT` | Port PostgreSQL |
| `DB_NAME` | Tên database |
| `DB_USER` | Tài khoản database |
| `DB_PASSWORD` | Mật khẩu database |
| `REDIS_HOST` | Host Redis |
| `REDIS_PORT` | Port Redis |
| `REDIS_DB` | Chỉ số database Redis |
| `REDIS_PASSWORD` | Mật khẩu Redis nếu có |
| `APP_ENV` | Môi trường chạy |
| `DEBUG` | Bật/tắt debug |
| `APP_PORT` | Cổng service, mặc định `8001` |

## Chạy local

```bash
pip install -r requirements.txt
python app.py
```

Hoặc chạy bằng Uvicorn:

```bash
uvicorn app:app --host 0.0.0.0 --port 8001 --reload
```

## API chính

- `POST /face/verify` — xác thực khuôn mặt.
- `PUT /face/register` — đăng ký khuôn mặt từ video.
- `GET /face/register/status/{job_id}` — xem trạng thái job.
- `GET /face/register/queue-info` — xem số job đang chờ.
- `GET /face/status/{employee_id}` — xem trạng thái khuôn mặt của nhân viên.
- `POST /face/rebuild-index` — rebuild FAISS index.

## Ghi chú

- Service dùng FAISS để lưu vector khuôn mặt và `employee_id_map.npy` để map vector với nhân viên.
- Khi khởi động, service có thể tự rebuild index từ dữ liệu trong database.
- Redis worker sẽ được khởi chạy cùng tiến trình API để xử lý job nền.
