# Pottel Reporting

- **Frontend:** React + Vite (SPA) — thư mục `frontend/`
- **Backend:** Spring Boot (chỉ REST API + JWT) — thư mục `src/`
- **Database:** PostgreSQL (`pottel_bss`)

Giao diện duy nhất là React. Backend không còn Thymeleaf.

## Chạy dự án

### 1. Backend (cổng 8080)

Cần Java 21 và PostgreSQL đang chạy. Đặt biến môi trường trước khi chạy:

| Biến | Bắt buộc | Ý nghĩa |
|---|---|---|
| `JWT_SECRET` | Có | Khóa JWT dạng Base64, tối thiểu 32 byte. Tạo bằng: `openssl rand -base64 48` |
| `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD` | Không | Nếu có, tự tạo tài khoản ADMIN lần đầu |
| `CORS_ALLOWED_ORIGINS` | Không | Địa chỉ React được gọi API (mặc định `http://localhost:5173,http://127.0.0.1:5173`) |

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

### 2. Frontend (cổng 5173)

```bash
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173 . Nếu Backend không ở `localhost:8080`, copy `frontend/.env.example` thành `frontend/.env` và sửa `VITE_API_URL`.

## REST API

| Method | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/auth/dang-nhap` | Công khai | Đăng nhập, trả JWT |
| GET | `/api/me` | Đã đăng nhập | Thông tin tài khoản hiện tại |
| GET | `/api/reports/bc-ds-01` | Đã đăng nhập | Báo cáo BC-DS-01 (bảng 1A/1B/1C) |
| GET | `/api/admin/accounts` | ADMIN | Danh sách tài khoản |
| POST | `/api/admin/accounts` | ADMIN | Tạo tài khoản (vai trò USER) |
| POST | `/api/admin/accounts/{id}/toggle` | ADMIN | Khóa / mở khóa |

Tham số `/api/reports/bc-ds-01`: `bc` (1B|1C), `fromDate`, `toDate` (yyyy-MM-dd), `msisdn`, `kitStatus`, `reconciliationStatus`, `goiMoiStatus` (HAS|NONE), `page`, `pageSize` (10|20|50|100), `includeMeta` (false: bỏ qua bảng 1A và danh sách lọc).
Lỗi trả JSON `{"message": "..."}`.

## Thêm báo cáo mới

1. Thêm file SQL vào `src/main/resources/sql/reports/` và đăng ký trong `DanhMucBaoCao`.
2. Thêm API trong `controller/`.
3. Thêm mục trong `frontend/src/config/reports.js` và route trong `frontend/src/App.jsx`.
