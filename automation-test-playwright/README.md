# SunTower Playwright Automation

Framework kiem thu tu dong UI E2E cho he thong SunTower, xay dung bang Playwright va TypeScript. Du an mirror catalog E2E cua Selenium de phuc vu so sanh trong khoa luan.

## Mo Ta

Framework tap trung vao UI E2E, khong phai bo unit/API/performance/visual/accessibility test rieng. API, DB va OTP helper chi duoc dung de setup, doi chieu va cleanup du lieu cho E2E.

Phu hop de kiem thu:

- Auth: login, registration, password reset.
- Public browsing.
- Admin: dashboard, building, customer, staff, contract, sale contract, invoice, property request, report, profile.
- Staff: dashboard, building, customer, contract, sale contract, invoice, profile.
- Customer: home, building, contract, service, transaction, payment, property request, profile.

## Phu Thuoc

- Node.js 24
- npm
- Playwright `@playwright/test`
- TypeScript
- MySQL 8.0
- Backend SunTower dang chay tai `BASE_URL`

Cac package chinh nam trong `package.json`: `@playwright/test`, `typescript`, `dotenv`, `mysql2`, `@types/node`.

## Cai Dat

```powershell
cd automation-test-playwright
npm install
npx playwright install chromium
```

Tao file `.env` tu `.env.example` neu can cau hinh local:

```powershell
Copy-Item .env.example .env
```

Bien moi truong quan trong:

- `BASE_URL`
- `APP_ENV`
- `ADMIN_USERNAME`, `STAFF_USERNAME`, `CUSTOMER_USERNAME`
- `DEFAULT_PASSWORD`
- `DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `TEST_SUPPORT_OTP_TOKEN`
- `CI_WORKERS`

## Cach Dung

Kiem tra TypeScript:

```powershell
npm run typecheck
```

Liet ke test:

```powershell
npm run test:list
```

Chay theo nhom:

```powershell
npm run test:demo
npm run test:smoke
npm run test:critical
npm run test:regression
```

Chay theo module:

```powershell
npm run test:auth
npm run test:public
npm run test:admin
npm run test:customer
npm run test:staff
npm run test:payment
```

Mo report:

```powershell
npm run report
```

Neu PowerShell chan `npm`, dung `npm.cmd`, vi du:

```powershell
npm.cmd run typecheck
```

## Cau Truc

```text
automation-test-playwright/
|-- tests/e2e/                 # E2E specs
|-- pages/                     # Page Object Model
|-- fixtures/                  # Playwright fixtures va test data
|-- helpers/                   # auth, runtime, DB, OTP, browser helpers
|-- scripts/                   # export catalog, quality gate, report helper
|-- docs/                      # tai lieu framework
|-- playwright.config.ts       # cau hinh runner
|-- package.json
`-- tsconfig.json
```

## Quy Mo Test

- 30 E2E spec files.
- 122 unique `[E2E-*]` IDs.
- Tags chinh: `@smoke`, `@critical`, `@regression`.
- Report sinh trong `.runtime/`.

## Dong Gop

Thay doi duoc chap nhan khi:

- `npm run typecheck` pass.
- Test moi co Test ID trong title, vi du `[E2E-AUTH-LOGIN-001]`.
- Page object chi chua locator/thao tac UI, khong chua business assertion phuc tap.
- Du lieu tao moi phai co cleanup bang run token/scenario helper.
- Khong commit `.env`, `node_modules`, `.runtime`, report hoac test artifacts.

## Khac Phuc Loi Thuong Gap

- `playwright` khong tim browser: chay `npx playwright install chromium`.
- Test fail vi backend: kiem tra `BASE_URL` va backend `/suntower`.
- Test fail vi DB: kiem tra `DB_JDBC_URL`, user/password va database `estate`.
- OTP khong lay duoc: kiem tra `TEST_SUPPORT_OTP_TOKEN` khop voi backend.

## Ho Tro

Lien he nhom phat trien SunTower hoac tao issue/pull request trong repository de bao loi, de xuat test case hoac cai tien framework.
