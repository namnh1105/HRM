# HRM System

Hệ thống quản lý nhân sự đa nền tảng cho doanh nghiệp, gồm backend quản trị nghiệp vụ, dịch vụ nhận diện khuôn mặt, web quản trị và ứng dụng di động.

## Tổng quan

Repository này là một mono-repo với 4 thành phần chính:

- `HRM_BE` — Backend Spring Boot cung cấp API, xác thực, phân quyền, quản lý nhân sự và các nghiệp vụ lõi.
- `HRM_FACE_SERVICE` — Dịch vụ FastAPI xử lý nhận diện khuôn mặt, đăng ký khuôn mặt và chấm công bằng ảnh/video.
- `HRM_FE_WEB` — Frontend web với Next.js cho màn hình quản trị và vận hành.
- `HRM_FE_APP` — Ứng dụng mobile với Expo / React Native cho nhân sự sử dụng trên điện thoại.

## Tính năng chính

- Đăng nhập, xác thực và làm mới token bằng JWT.
- Quản lý người dùng, nhân viên, phòng ban, chức vụ, hợp đồng, lương, nghỉ phép, thông báo và phân quyền.
- Chấm công bằng khuôn mặt, đăng ký khuôn mặt từ video và tra cứu trạng thái xử lý job.
- Tìm kiếm / so khớp vector khuôn mặt với FAISS, có hỗ trợ Redis queue để xử lý bất đồng bộ.
- Giao diện web cho quản trị viên và ứng dụng mobile cho người dùng cuối.
- Hỗ trợ triển khai bằng Docker và từng service có thể chạy độc lập.

## Kiến trúc hệ thống

```text
Web / Mobile
   │
   ├── HRM_FE_WEB  (Next.js)
   ├── HRM_FE_APP  (Expo React Native)
   │
   ├──── gọi API ───► HRM_BE (Spring Boot)
   │                     │
   │                     ├── PostgreSQL
   │                     ├── Redis
   │                     └── Swagger / OpenAPI
   │
   └──── gọi API ───► HRM_FACE_SERVICE (FastAPI)
                         │
                         ├── PostgreSQL
                         ├── Redis Queue
                         ├── FAISS index
                         └── InsightFace / OpenCV
```

## Công nghệ sử dụng

- **Backend:** Spring Boot 3.2, Spring Security, Spring Data JPA, Redis, PostgreSQL, JWT, MapStruct, Swagger/OpenAPI, Testcontainers.
- **Face service:** FastAPI, SQLAlchemy async, InsightFace, OpenCV, FAISS, Redis, Pydantic.
- **Web:** Next.js, React 19, Redux Toolkit, TypeScript, Tailwind CSS / Next App Router.
- **Mobile:** Expo, React Native 0.81, TypeScript, Redux Toolkit, React Navigation.

## Cấu trúc thư mục

```text
HRM/
├── HRM_BE/
├── HRM_FACE_SERVICE/
├── HRM_FE_APP/
└── HRM_FE_WEB/
```

## Yêu cầu môi trường

- Java 17 cho backend.
- Node.js 20+ cho web và mobile.
- Python 3.10+ cho face service.
- PostgreSQL 15+.
- Redis 7+.
- Docker và Docker Compose nếu muốn chạy theo môi trường container.

## Cấu hình từng service

### 1) Backend `HRM_BE`

File cấu hình chính:

- `HRM_BE/src/main/resources/application.properties`
- `HRM_BE/src/main/resources/application-development.properties`
- `HRM_BE/src/main/resources/application-production.properties`

Tài liệu chi tiết hơn có thể xem trong [HRM_BE/README.md](HRM_BE/README.md).

### 2) Face service `HRM_FACE_SERVICE`

File cấu hình chính:

- `HRM_FACE_SERVICE/.env.example`
- `HRM_FACE_SERVICE/requirements.txt`
- `HRM_FACE_SERVICE/app.py`

Service này dùng:

- FAISS để lưu và tìm embedding khuôn mặt.
- InsightFace để trích xuất embedding.
- Redis queue để xử lý đăng ký khuôn mặt bất đồng bộ.

### 3) Web frontend `HRM_FE_WEB`

File cấu hình chính:

- `HRM_FE_WEB/.env.local`
- `HRM_FE_WEB/package.json`
- `HRM_FE_WEB/src/store/api/baseQuery.ts`

### 4) Mobile app `HRM_FE_APP`

File cấu hình chính:

- `HRM_FE_APP/.env.example`
- `HRM_FE_APP/package.json`

## Chạy dự án ở môi trường development

### Bước 1: Khởi động PostgreSQL và Redis

Bạn có thể chạy bằng Docker hoặc dịch vụ cục bộ. Nếu dùng Docker, hãy đảm bảo các port sau sẵn sàng:

- PostgreSQL: `5432`
- Redis: `6379`

### Bước 2: Chạy backend

```bash
cd HRM_BE
./gradlew bootRun
```

Trên Windows:

```bash
cd HRM_BE
gradlew.bat bootRun
```

Swagger UI thường có tại:

- `http://localhost:8080/swagger-ui.html`

### Bước 3: Chạy face service

```bash
cd HRM_FACE_SERVICE
pip install -r requirements.txt
python app.py
```

Mặc định service chạy tại:

- `http://localhost:8001`
- Swagger docs: `http://localhost:8001/docs`

### Bước 4: Chạy web frontend

```bash
cd HRM_FE_WEB
npm install
npm run dev
```

Mặc định web chạy tại:

- `http://localhost:3000`

### Bước 5: Chạy mobile app

```bash
cd HRM_FE_APP
npm install
npm start
```

Sau đó có thể chạy Android / iOS / web theo môi trường Expo.

## Docker / triển khai

Mỗi service đều có file Docker riêng hoặc file triển khai đi kèm trong thư mục tương ứng:

- `HRM_BE/docker-compose.yml`
- `HRM_BE/docker-compose.prod.yml`
- `HRM_FACE_SERVICE/docker-compose.prod.yml`
- `HRM_FACE_SERVICE/docker-compose.gpu.yml`
- `HRM_FE_WEB` và `HRM_FE_APP` có thể containerize theo nhu cầu triển khai.

Nếu triển khai production, hãy kiểm tra thêm các tài liệu trong thư mục `deployment/` của từng service.

## API tham khảo nhanh

### Backend

- Authentication: đăng ký, đăng nhập, refresh token, logout.
- Users: hồ sơ cá nhân, cập nhật thông tin, đổi mật khẩu, vô hiệu hóa tài khoản.
- Health check: kiểm tra trạng thái service.

### Face service

- `POST /face/verify` — xác thực khuôn mặt.
- `PUT /face/register` — đăng ký khuôn mặt qua video.
- `GET /face/register/status/{job_id}` — tra cứu trạng thái job.
- `GET /face/register/queue-info` — xem trạng thái queue.
- `GET /face/status/{employee_id}` — kiểm tra trạng thái khuôn mặt của nhân viên.
- `POST /face/rebuild-index` — rebuild FAISS index.

## Ghi chú triển khai

- Backend và face service đều phụ thuộc vào PostgreSQL.
- Face service còn phụ thuộc Redis để đẩy và xử lý job bất đồng bộ.
- Web và mobile cần trỏ đúng tới backend API bằng biến môi trường.
- FAISS index và file map nhân viên được tạo tự động trong `HRM_FACE_SERVICE` khi service chạy.

## Tài liệu liên quan

- [HRM_BE/README.md](HRM_BE/README.md)
- [HRM_BE/deployment/DEPLOYMENT.md](HRM_BE/deployment/DEPLOYMENT.md)
- [HRM_FACE_SERVICE/deployment/DEPLOYMENT.md](HRM_FACE_SERVICE/deployment/DEPLOYMENT.md)
- [HRM_FE_APP/README.md](HRM_FE_APP/README.md)
- [HRM_FE_WEB/README.md](HRM_FE_WEB/README.md)

## Trạng thái dự án

Dự án đang được phát triển theo mô hình mono-repo và có thể mở rộng từng service độc lập.

---

Nếu bạn muốn, mình có thể viết tiếp một bản README ngắn gọn hơn bằng tiếng Anh hoặc bổ sung sơ đồ kiến trúc đẹp hơn bằng Mermaid.
