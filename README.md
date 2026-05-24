# SunTower Graduation Project

Repository nay chua source backend SunTower va hai framework kiem thu tu dong UI E2E bang Playwright va Selenium. Muc tieu la phuc vu khoa luan tot nghiep, dong thoi tao co so so sanh hai cong cu automation test tren cung mot ung dung, cung du lieu va cung catalog test case.

## Thanh Phan Chinh

- `.github/`: GitHub Actions workflow cho Playwright va Selenium.
- `sunTower-main/`: ung dung web SunTower bang Java Spring Boot, Thymeleaf va MySQL.
- `automation-test-playwright/`: framework UI E2E bang Playwright + TypeScript.
- `automation-test-selenium/`: framework UI E2E bang Selenium WebDriver + Java + TestNG.

## Cai Dat Tong Quan

Can cai dat:

- Java 21
- Node.js 24 cho Playwright
- MySQL 8.0
- Chrome/Chromium cho UI test

Khoi tao database local bang file:

```powershell
mysql -uroot -p < sunTower-main/estate_db_generator/sql_estate.sql
```

Moi du an con co file `.env.example`; sao chep thanh `.env` va cap nhat gia tri local khi can.

## Cach Dung Nhanh

Chay backend:

```powershell
cd sunTower-main
$env:SPRING_PROFILES_ACTIVE="mysql,local-nooauth"
.\mvnw.cmd spring-boot:run
```

Chay Playwright:

```powershell
cd automation-test-playwright
npm install
npx playwright install chromium
npm run test:regression
```

Chay Selenium:

```powershell
cd automation-test-selenium
.\mvnw.cmd test
```

Tai khoan seed mac dinh:

- Admin: `admin123` / `12345678`
- Staff: `tmq0102` / `12345678`
- Customer: `abcVietNam` / `12345678`

## CI/CD

GitHub Actions nam trong `.github/workflows`:

- `playwright.yml`: start MySQL, import database, start backend, chay Playwright regression.
- `selenium.yml`: start MySQL, import database, start backend, chay Selenium smoke.

## Dong Gop

Thay doi duoc chap nhan khi:

- Khong commit `.env`, `node_modules`, `target`, report hoac log runtime.
- Backend compile/test pass neu sua source BE.
- Playwright `npm run typecheck` pass neu sua framework Playwright.
- Selenium `.\mvnw.cmd test -DskipTests` pass neu sua framework Selenium.
- Test case E2E moi can co Test ID ro rang va cleanup du lieu tao ra.

## Loi Thuong Gap

- Backend khong start: kiem tra MySQL dang chay, database `estate` da import va `.env` dung profile.
- UI test khong login duoc: kiem tra `BASE_URL`, tai khoan seed va `DEFAULT_PASSWORD`.
- Selenium bi loi Chrome trong sandbox: chay ngoai sandbox/CI runner that, hoac bat `HEADLESS=true`.
- Playwright khong tim Chromium: chay `npx playwright install chromium`.

## Ho Tro

Lien he nhom phat trien SunTower hoac tao issue/pull request trong repository de bao loi va de xuat cai tien.
