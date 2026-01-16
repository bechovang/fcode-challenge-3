# 📋 REQUIREMENTS - Shop Mua Bán Tài Khoản Game
## Phiên bản NEWBIE (Java + HTML/CSS/JS)

---

## 1. 🎯 MỤC TIÊU DỰ ÁN

Xây dựng một **hệ thống web đơn giản** cho phép:
- ✅ Người bán đăng tài khoản game
- ✅ Admin duyệt tài khoản
- ✅ Người mua tìm kiếm và mua tài khoản
- ✅ Quản lý trạng thái tài khoản (chưa bán / đã bán)

**Lưu ý**: Không yêu cầu bảo mật phức tạp, tập trung vào **luồng client-server** và **REST API**

---

## 2. 👥 ĐỐI TƯỢNG SỬ DỤNG (Actors)

### 2.1 Guest (Khách)
- Xem danh sách tài khoản game
- Tìm kiếm tài khoản
- Đăng ký tài khoản mới

### 2.2 User (Người dùng)
- Đăng nhập / đăng xuất
- **Người bán**:
  - Đăng bán tài khoản game
  - Xem danh sách tài khoản của mình
- **Người mua**:
  - Xem danh sách tài khoản
  - Mua tài khoản game
  - Xem lịch sử giao dịch

### 2.3 Admin
- Đăng nhập admin
- Duyệt / từ chối tài khoản game
- Quản lý danh sách user
- Quản lý tất cả tài khoản game

---

## 3. ⚙️ CHỨC NĂNG CHI TIẾT (Functional Requirements)

### 3.1 🔐 Authentication (Xác thực người dùng)

#### FR-001: Đăng ký tài khoản
- **Input**: Username, Password, Email, Full Name
- **Process**:
  - Validate username chưa tồn tại
  - Mã hóa password (BCrypt)
  - Lưu vào database với role = USER
- **Output**: Thông báo đăng ký thành công

#### FR-002: Đăng nhập
- **Input**: Username, Password
- **Process**:
  - Kiểm tra username/password
  - Tạo session
  - Lưu thông tin user vào session
- **Output**: Redirect về trang chủ (User) hoặc Admin dashboard (Admin)

#### FR-003: Đăng xuất
- **Process**: Xóa session
- **Output**: Redirect về trang login

---

### 3.2 🎮 Quản lý tài khoản game

#### FR-004: Đăng bán tài khoản game (User)
- **Input**:
  - Tên game (ví dụ: Liên Minh Huyền Thoại)
  - Tên tài khoản
  - Level (số)
  - Nhân vật (text)
  - Vật phẩm (textarea)
  - Giá bán (số)
- **Process**:
  - Lưu vào database
  - Trạng thái ban đầu: **PENDING**
  - Lưu seller_id = user hiện tại
- **Output**: Thông báo "Tài khoản đã được đăng, chờ admin duyệt"

#### FR-005: Xem danh sách tài khoản (User/Guest)
- **Output**:
  - Hiển thị tất cả tài khoản có status = **APPROVED**
  - Thông tin: Tên game, Level, Giá, Trạng thái
  - Nút "Xem chi tiết"

#### FR-006: Xem chi tiết tài khoản
- **Output**:
  - Tất cả thông tin của tài khoản
  - Nút "Mua" (nếu chưa SOLD)

#### FR-007: Mua tài khoản
- **Input**: Game account ID
- **Process**:
  - Kiểm tra user đã đăng nhập
  - Kiểm tra tài khoản còn APPROVED (chưa bán)
  - Cập nhật:
    - status = **SOLD**
    - buyer_id = user hiện tại
    - sold_at = timestamp hiện tại
- **Output**: "Mua thành công! Liên hệ admin để nhận tài khoản"

---

#### FR-008: Duyệt tài khoản (Admin)
- **Input**: Danh sách tài khoản PENDING
- **Action**:
  - Nút "Duyệt" → status = **APPROVED**
  - Nút "Từ chối" → status = **REJECTED**
- **Output**: Danh sách được cập nhật

#### FR-009: Quản lý user (Admin)
- **Output**:
  - Danh sách tất cả user
  - Thông tin: Username, Email, Role
  - Nút "Khóa" / "Mở khóa" (Optional)

---

### 3.3 🔍 Tìm kiếm & Lọc

#### FR-010: Tìm kiếm tài khoản
- **Input**:
  - Tên game
  - Khoảng giá (min-max)
  - Level tối thiểu
- **Output**: Danh sách tài khoản thỏa mãn

#### FR-011: Sắp xếp
- Sắp xếp theo:
  - Giá (tăng dần / giảm dần)
  - Level (cao → thấp)
  - Ngày đăng (mới nhất)

---

### 3.4 📊 Báo cáo & Thống kê (Optional - nếu có thời gian)

#### FR-012: Lịch sử giao dịch (User)
- Người bán: Xem tài khoản đã bán
- Người mua: Xem tài khoản đã mua

#### FR-013: Dashboard Admin
- Tổng số user
- Tổng số tài khoản
- Tổng doanh thu (giả lập)

---

## 4. 🗄️ DATABASE DESIGN

### 4.1 Bảng `users`
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    email VARCHAR(100),
    full_name VARCHAR(100),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 Bảng `game_accounts`
```sql
CREATE TABLE game_accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    game_name VARCHAR(100) NOT NULL,
    account_name VARCHAR(100),
    level INT,
    characters TEXT,  -- Mô tả nhân vật
    items TEXT,       -- Mô tả vật phẩm
    price DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') DEFAULT 'PENDING',
    seller_id INT,
    buyer_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);
```

---

## 5. 🌐 REST API ENDPOINTS

### 5.1 Authentication APIs
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/register` | Đăng ký | `{username, password, email, fullName}` | `{success: true, message}` |
| POST | `/api/login` | Đăng nhập | `{username, password}` | `{success: true, user: {...}}` |
| POST | `/api/logout` | Đăng xuất | - | `{success: true}` |

### 5.2 Game Account APIs
| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| GET | `/api/game-accounts` | Lấy danh sách tài khoản | No | Query: `?status=APPROVED` |
| GET | `/api/game-accounts/{id}` | Chi tiết tài khoản | No | - |
| POST | `/api/game-accounts` | Đăng bán tài khoản | User | `{gameName, accountName, level, characters, items, price}` |
| PUT | `/api/game-accounts/{id}/approve` | Duyệt tài khoản | Admin | - |
| PUT | `/api/game-accounts/{id}/reject` | Từ chối | Admin | - |
| PUT | `/api/game-accounts/{id}/buy` | Mua tài khoản | User | - |

### 5.3 Admin APIs
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/admin/users` | Danh sách user | Admin |
| GET | `/api/admin/game-accounts?status=PENDING` | Tài khoản chờ duyệt | Admin |

---

## 6. 💻 CÔNG NGHỆ SỬ DỤNG

### 6.1 Backend
- **Ngôn ngữ**: Java 17+
- **Framework**: Spring Boot 3.x
  - Spring Web (REST API)
  - Spring Data JPA (ORM)
  - Spring Security (đơn giản - session based)
- **Database**: MySQL 8.0
- **Build tool**: Maven

### 6.2 Frontend
- **HTML5** + **CSS3** + **Vanilla JavaScript**
- **Bootstrap 5** (cho UI đẹp)
- **Fetch API** (gọi REST API)

### 6.3 Tools
- **IDE**: IntelliJ IDEA / Eclipse
- **Database tool**: MySQL Workbench / DBeaver
- **Testing**: Postman (test API)

---

## 7. 🏗️ KIẾN TRÚC HỆ THỐNG

```
┌─────────────────┐
│   Browser       │
│  (HTML/CSS/JS)  │
└────────┬────────┘
         │ HTTP Request (REST API)
         ▼
┌─────────────────┐
│  Spring Boot    │
│  ├─ Controller  │
│  ├─ Service     │
│  ├─ Repository  │
│  └─ Entity      │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│     MySQL       │
└─────────────────┘
```

**Pattern**: MVC + Layered Architecture
- **Controller**: Nhận request, trả response
- **Service**: Xử lý logic nghiệp vụ
- **Repository**: Truy vấn database
- **Entity**: Đại diện bảng trong DB

---

## 8. 🔒 YÊU CẦU BẢO MẬT (Cơ bản)

### BR-001: Authentication
- Mật khẩu phải mã hóa bằng BCrypt
- Session timeout: 30 phút

### BR-002: Authorization
- Chỉ User đăng nhập mới được đăng bán / mua tài khoản
- Chỉ Admin mới được duyệt tài khoản
- User chỉ xem được lịch sử giao dịch của mình

### BR-003: Validation
- Username: 4-20 ký tự, không có ký tự đặc biệt
- Password: tối thiểu 6 ký tự
- Email: đúng định dạng
- Giá bán: > 0

---

## 9. 📊 YÊU CẦU PHI CHỨC NĂNG (Non-Functional)

### NFR-001: Performance
- Thời gian phản hồi API: < 2 giây
- Hỗ trợ 50-100 concurrent users

### NFR-002: Usability
- Giao diện đơn giản, dễ sử dụng
- Responsive (hoạt động trên mobile)

### NFR-003: Reliability
- Hệ thống không crash khi có lỗi
- Hiển thị thông báo lỗi rõ ràng

---

## 10. ⚠️ GIỚI HẠN (Out of Scope)

❌ **KHÔNG** làm những tính năng sau (dành cho bản nâng cao):
- Thanh toán thật (VNPay, Momo...)
- Upload ảnh tài khoản
- Chat giữa buyer - seller
- Email notification
- JWT authentication
- WebSocket real-time
- Phân trang nâng cao

---

## 11. ✅ CHECKLIST HOÀN THÀNH

### Phase 1: Setup
- [ ] Tạo project Spring Boot
- [ ] Kết nối MySQL
- [ ] Tạo database + tables

### Phase 2: Backend Core
- [ ] Entity classes (User, GameAccount)
- [ ] Repository interfaces
- [ ] Service layer
- [ ] Controller + REST APIs

### Phase 3: Frontend
- [ ] Trang đăng nhập / đăng ký
- [ ] Trang danh sách tài khoản
- [ ] Trang đăng bán (User)
- [ ] Trang duyệt tài khoản (Admin)

### Phase 4: Integration
- [ ] Frontend gọi API thành công
- [ ] Xử lý lỗi
- [ ] Test toàn bộ luồng

### Phase 5: Documentation
- [ ] Viết báo cáo
- [ ] Tạo slide demo
- [ ] Chuẩn bị video demo (nếu cần)

---

## 12. 📝 TIÊU CHÍ CHẤM ĐIỂM (Dự kiến)

| Tiêu chí | Điểm | Mô tả |
|----------|------|-------|
| Phân tích lý thuyết | 20% | Mô hình client-server, REST API, kiến trúc |
| Thiết kế hệ thống | 20% | ERD, API design, sơ đồ luồng |
| Chức năng hoàn thiện | 40% | CRUD, authentication, authorization |
| Giao diện & UX | 10% | Đẹp, dễ dùng, responsive |
| Báo cáo & Demo | 10% | Rõ ràng, đầy đủ |

---

## 🎯 KẾT LUẬN

Đây là **requirement hoàn chỉnh** cho dự án Web System dành cho **NEWBIE**, tập trung vào:
✅ Hiểu rõ mô hình client-server
✅ Làm quen REST API
✅ Thực hành Java Spring Boot
✅ HTML/CSS/JS cơ bản
