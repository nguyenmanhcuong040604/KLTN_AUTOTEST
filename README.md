# Khoá Luận Tốt Nghiệp - Hệ Thống SunTower & Kiểm Thử Tự Động

Repository này phục vụ khoá luận tốt nghiệp, bao gồm mã nguồn ứng dụng web quản lý bất động sản SunTower và các framework kiểm thử tự động (Playwright, Selenium).

## 1. Tổng Quan

Dự án được chia thành các thành phần chính:
- **Source Code Backend (Web App)**: Mã nguồn của hệ thống SunTower (Java Spring Boot, Thymeleaf, MySQL).
- **Dự Án Test Playwright**: Framework kiểm thử tự động API sử dụng Playwright + TypeScript.
- **Dự Án Test Selenium**: Framework kiểm thử tự động sử dụng Selenium (đang được phát triển/tích hợp).

Mục tiêu của repository:
- Quản lý tập trung toàn bộ mã nguồn hệ thống và code kiểm thử.
- Hỗ trợ chạy ứng dụng local dễ dàng.
- Tích hợp kiểm thử tự động vào CI/CD (GitHub Actions).

## 2. Cấu Trúc Repository

```text
.
|-- .github/                                # Cấu hình GitHub Actions CI/CD
|-- sunTower-main/                          # Source code Backend ứng dụng SunTower
|-- automation-test-playwright/             # Dự án kiểm thử tự động với Playwright
|-- automation-test-selenium-plan.md        # Tài liệu kế hoạch/thiết kế kiểm thử Selenium
|-- (Dự án test Selenium - sắp tới)         # Thư mục mã nguồn framework Selenium
`-- README.md                               # File hướng dẫn chung (file này)
```

## 3. Chi Tiết Các Thành Phần

### 3.1. Source Code Backend (`sunTower-main`)
Ứng dụng Spring Boot cốt lõi của hệ thống SunTower. Chứa các API, logic nghiệp vụ quản lý bất động sản, controller, service, repository và giao diện Thymeleaf.
- **Công nghệ**: Java 21, Spring Boot, Spring Data JPA, MySQL.
- **Cách chạy**: Xem hướng dẫn chi tiết tại `sunTower-main/README.md`.

### 3.2. Dự Án Test Playwright (`automation-test-playwright`)
Bộ framework kiểm thử tự động được xây dựng chuyên biệt bằng Playwright để kiểm thử giao diện E2E và API.
- **Công nghệ**: Playwright, TypeScript, Node.js.
- **Tính năng**: Page Object Model, tự động tạo báo cáo HTML, chụp ảnh/quay video khi test thất bại.
- **Cách chạy**: Xem hướng dẫn tại `automation-test-playwright/README.md`.

### 3.3. Dự Án Test Selenium
Thành phần kiểm thử tự động sử dụng trình duyệt điều khiển qua Selenium. Phục vụ việc đánh giá, so sánh hiệu năng và độ ổn định so với Playwright trong phạm vi khoá luận.
- **Tài liệu tham khảo**: Xem `automation-test-selenium-plan.md`.

## 4. Tài Khoản Test Mặc Định

Sử dụng với database local đã seed dữ liệu:
- **Admin**: `admin123` / `12345678`
- **Nhân viên (Staff)**: `tmq0102` / `12345678`
- **Khách hàng (Customer)**: `abcVietNam` / `12345678`

## 5. Tài Liệu Chi Tiết Hơn

Vui lòng tham khảo các file `README.md` nằm ở bên trong từng thư mục dự án con để biết thêm các lệnh cài đặt, chạy ứng dụng, và chạy test cụ thể.
