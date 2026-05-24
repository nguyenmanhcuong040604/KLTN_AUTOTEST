# SunTower Main

SunTower Main la ung dung web quan ly bat dong san, xay dung bang Java 21, Spring Boot, Thymeleaf va MySQL. Ung dung cung cap giao dien theo role admin/staff/customer, trang public browsing va cac API JSON phuc vu AJAX, auth, profile, payment va test support.

## Mo Ta

Ung dung ho tro cac nghiep vu chinh:

- Public: xem va loc danh sach toa nha tai `/suntower`.
- Auth: login, register, forgot/reset password, JWT cookie auth va Google OAuth2.
- Admin: quan ly building, customer, staff, contract, sale contract, invoice, property request, dashboard, report va profile.
- Staff: xem building/customer duoc phan cong, contract, sale contract, invoice, dashboard va profile.
- Customer: home, building, contract, invoice payment, transaction history, service, property request va profile.

## Phu Thuoc

- Java 21
- Maven Wrapper co san trong repo
- Spring Boot 3.5.7
- Spring MVC + Thymeleaf
- Spring Data JPA
- Spring Security + JWT cookie auth
- MySQL 8.0
- H2 cho integration test
- Bootstrap, jQuery, AJAX
- Spring Mail

## Cai Dat

1. Cai Java 21 va MySQL 8.0.
2. Import database:

```powershell
mysql -uroot -p < estate_db_generator/sql_estate.sql
```

3. Tao `.env` tu `.env.example`:

```powershell
Copy-Item .env.example .env
```

4. Cap nhat cac bien trong `.env`, dac biet:

- `SPRING_PROFILES_ACTIVE`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `BUILDING_IMAGE_UPLOAD_DIR`
- `PLANNING_MAP_IMAGE_UPLOAD_DIR`
- `GOONG_API_KEY`
- `PAYMENT_QR_BANK_BIN`
- `PAYMENT_QR_ACCOUNT_NO`
- `PAYMENT_QR_ACCOUNT_NAME`

## Cach Dung

Chay local voi MySQL va bo qua OAuth that:

```powershell
cd sunTower-main
$env:SPRING_PROFILES_ACTIVE="mysql,local-nooauth"
.\mvnw.cmd spring-boot:run
```

Chay integration test:

```powershell
.\mvnw.cmd test
```

Neu `JAVA_HOME` chua dung:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
```

## URL Chinh

- Public page: `http://localhost:8080/suntower`
- Login: `http://localhost:8080/login`
- Admin dashboard: `http://localhost:8080/admin/dashboard`
- Staff dashboard: `http://localhost:8080/staff/dashboard`
- Customer home: `http://localhost:8080/customer/home`

Tai khoan seed local thuong dung:

- Admin: `admin123` / `12345678`
- Staff: `tmq0102` / `12345678`
- Customer: `abcVietNam` / `12345678`

## Cau Truc

```text
sunTower-main/
|-- src/main/java/com/estate/
|   |-- controller/        # Thymeleaf page controllers
|   |-- api/v1/            # JSON APIs
|   |-- service/           # business logic
|   |-- repository/        # JPA repositories va entities
|   |-- config/            # app/security config
|   `-- security/          # auth, JWT, OAuth2
|-- src/main/resources/
|   |-- templates/         # Thymeleaf views
|   |-- static/            # CSS, JS, images
|   `-- application*.properties
|-- src/test/java/         # integration tests
|-- estate_db_generator/   # SQL seed va data generator
|-- screenshots/           # ERD va hinh minh hoa
`-- pom.xml
```

## Dong Gop

Thay doi duoc chap nhan khi:

- `.\mvnw.cmd test` pass neu sua backend logic.
- Khong commit `.env`, `target`, file upload local, log runtime hoac secret.
- API moi can co validation va response loi ro rang.
- Thay doi DB/schema can cap nhat seed SQL neu automation test phu thuoc.
- Thay doi UI quan trong can thong bao cho hai project test de cap nhat Page Object.

## Khac Phuc Loi Thuong Gap

- Backend khong ket noi MySQL: kiem tra `SPRING_DATASOURCE_URL`, user/password va database `estate`.
- Port 8080 dang ban: tat process cu hoac doi `server.port`.
- Login/OAuth loi local: dung profile `local-nooauth` khi test local.
- Upload anh loi: kiem tra `BUILDING_IMAGE_UPLOAD_DIR` va `PLANNING_MAP_IMAGE_UPLOAD_DIR`.
- Mail/OTP loi: kiem tra cau hinh Spring Mail hoac dung test-support profile/token trong automation.

## Ho Tro

Lien he nhom phat trien SunTower hoac tao issue/pull request trong repository de bao loi backend, de xuat API hoac cap nhat nghiep vu.

## Tai Lieu Hinh Anh

So do ERD:

![ERD](./screenshots/estate_erd.png)
