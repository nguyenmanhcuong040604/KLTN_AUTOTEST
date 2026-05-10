# SunTower Playwright Automation Framework

Project nay dung Playwright + TypeScript de kiem thu tu dong giao dien E2E he thong SunTower. Muc tieu chinh trong khoa luan la chung minh cach thiet ke, to chuc va chay bo test tu dong mot cach co cau truc.

Neu moi doc project, khong nen doc theo thu tu alphabet. Hay doc theo 4 lop sau:

1. `tests/e2e/`: doc truoc de hieu test case va luong nghiep vu.
2. `pages/`: doc sau de hieu Page Object Model va thao tac UI duoc dong goi nhu the nao.
3. `fixtures/`, `helpers/runtime/`, `fixtures/test-data/`: doc de hieu cach cau hinh, khoi tao va tao du lieu test.
4. `helpers/`, `helpers/runtime/global-teardown.ts`: phan nang cao, chi can trinh bay khi noi ve cleanup, DB, OTP, session helper va bao cao.

## 1. Muc Tieu

- Xay dung framework automation test co cau truc ro rang.
- Tach rieng test layer, page layer, fixture, helper va test data.
- Ho tro kiem thu giao dien E2E tren cac vai tro admin, staff, customer va public user.
- Co co che tao va don du lieu test an toan.
- Co report HTML, JUnit, screenshot, video va trace khi loi.
- Co the chay local va tren GitHub Actions CI.

## 2. Duong Doc Cho Khoa Luan

### 2.1. Can hieu de bao ve

- `tests/e2e/`: noi chua cac spec E2E, moi file tuong ung mot nhom chuc nang.
- `pages/`: Page Object Model, giup test ngan gon va giam lap locator.
- `fixtures/base.fixture.ts`: mo rong fixture Playwright, tao `testState` va `publicPage`.
- `playwright.config.ts`: cau hinh browser, timeout, retry, reporter va thu muc output.
- `fixtures/test-data/factories/TestDataFactory.ts`: sinh du lieu test dong de tranh trung lap.

### 2.2. Chi can biet vai tro

- `fixtures/test-data/scenarios/`: dong goi setup/assert/cleanup theo tung kich ban nghiep vu; test E2E khong import builder ha tang truc tiep.
- `helpers/auth/`, `helpers/accounts/`, `helpers/test-state/`: dang nhap, resolve account, OTP, DB setup va cleanup.
- `docs/`: tai lieu ho tro viet bao cao va bao ve.

### 2.3. Khong can trinh bay nhu source code

- `node_modules/`: thu vien npm duoc cai bang `npm install`.
- `.runtime/`: report, screenshot, video, trace va ket qua sinh ra khi chay test.
- `playwright-report/`, `test-results/`, `reports/`, `artifacts/`, `blob-report/`: output sinh tu qua trinh chay test neu co.
- `.env`: cau hinh local co the chua mat khau, khong nen nop kem source; dung `.env.example` lam mau.

Xem them ban do file ngan gon tai `docs/project-file-map.md`.

## 3. Cau Truc Thu Muc

```text
automation-test-playwright/
|-- helpers/runtime/
|   |-- env.ts
|   |-- global-setup.ts
|   `-- global-teardown.ts
|-- docs/
|   |-- framework-overview.md
|   |-- project-file-map.md
|   |-- test-strategy.md
|   `-- execution-reporting-ci.md
|-- fixtures/
|   `-- base.fixture.ts
|-- pages/
|   |-- admin/
|   |-- auth/
|   |-- components/
|   |-- core/
|   |-- customer/
|   |-- public/
|   `-- staff/
|-- fixtures/test-data/
|   |-- environments/
|   |-- factories/
|   |-- files/
|   `-- scenarios/
|-- tests/
|   `-- e2e/
|-- helpers/
|   |-- accounts/
|   |-- auth/
|   |-- browser/
|   |-- runtime/
|   |-- test-state/
|   |-- text/
|   `-- validation/
|-- .env.example
|-- package.json
|-- playwright.config.ts
`-- tsconfig.json
```

## 4. Thanh Phan Chinh

- `tests/e2e/`: kiem thu luong nguoi dung tren giao dien.
- `pages/`: Page Object Model theo man hinh va module nghiep vu.
- `fixtures/`: khoi tao fixture dung chung, page object public, metadata va `testState` ho tro tao du lieu E2E.
- `helpers/accounts/`: resolve tai khoan test theo role, dung chung cho UI va setup data.
- `helpers/auth/`: helper dang nhap/dang xuat qua UI.
- `helpers/test-state/`: setup session, truy van MySQL va cleanup co scope cho du lieu E2E.
- `helpers/browser/`, `helpers/text/`, `helpers/validation/`: helper ky thuat dung chung khong gan nghiep vu.
- `fixtures/test-data/`: seed theo moi truong, factory data dong, scenario setup va file upload test.
- `helpers/runtime/`: cau hinh moi truong, global setup, global teardown va runtime path.
- `.runtime/`: output sinh ra khi chay test, khong commit vao repository.

## 5. Quy Mo Kiem Thu Hien Tai

- Khoang 30 spec file E2E.
- Khoang 122 E2E test.
- Cac nhom test chinh: admin, staff, customer, auth, public, payment.
- Cac test hien tai dung tag `@regression`; lenh `test:demo` chay nhanh nhom auth + public de demo.

## 6. Cai Dat Va Chay Test

```powershell
cd automation-test-playwright
npm install
npx playwright install chromium
npm run typecheck
```

Chay cac bo test thuong dung:

```powershell
npm run test:e2e
npm run test:list
npm run test:catalog
npm run test:demo
npm run test:regression
npm run test:auth
npm run test:public
npm run test:admin
npm run test:customer
npm run test:staff
npm run test:payment
npm run test:ci
```

Tren Windows, neu PowerShell chan `npm` do Execution Policy, co the dung:

```powershell
npm.cmd run typecheck
npm.cmd run test:ci
```

## 7. Che Do An Toan Du Lieu

Framework khong dung tag rieng cho destructive/safe test. An toan du lieu duoc quan ly bang moi truong test rieng, `TestDataFactory`, `runToken`, cleanup registry va `global-teardown.ts`.

Khong chay automation test tren database that hoac tai khoan that. Cac test co tao/sua/xoa du lieu phai tao du lieu dong co `runToken` va co co che cleanup.

Seed data toi thieu cho moi environment gom:

| Nhom seed           | Bien cau hinh                                                                        | Muc dich                                            |
| ------------------- | ------------------------------------------------------------------------------------ | --------------------------------------------------- |
| Tai khoan theo role | `ADMIN_USERNAME(S)`, `STAFF_USERNAME(S)`, `CUSTOMER_USERNAME(S)`, `DEFAULT_PASSWORD` | Dang nhap UI va tao request context dung role       |
| Entity nghiep vu    | `TEST_BUILDING_ID`, `TEST_CONTRACT_ID`, `TEST_CUSTOMER_ID`, `TEST_STAFF_ID`          | Lam moc cho hop dong, hoa don, phan cong va request |
| Dia chi toa nha     | `TEST_DISTRICT_ID`, `TEST_BUILDING_WARD`, `TEST_BUILDING_STREET`                     | Tao building moi on dinh theo environment           |
| Toa do              | `TEST_BUILDING_LATITUDE`, `TEST_BUILDING_LONGITUDE`                                  | Kiem thu form/toa do building                       |
| OTP test            | `TEST_SUPPORT_OTP_TOKEN`                                                             | Ho tro lay OTP trong moi truong test                |
| Database            | `DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_POOL_LIMIT`                         | Lookup, cleanup va doi chieu trang thai du lieu     |

## 8. Bao Cao

Ket qua test duoc luu tai:

```text
.runtime/playwright-report
.runtime/test-results
.runtime/reports/junit
```

Mo HTML report:

```powershell
npm run report
```

Khi test loi, framework luu screenshot, video va trace theo cau hinh E2E.

Xuat danh muc test case cho phu luc:

```powershell
npm run test:catalog
```

File Markdown/CSV duoc sinh trong `.runtime/reports/test-catalog`, khong commit vao source.

## 9. Source Nop Khoa Luan

| Nhom                 | Xu ly     | Ghi chu                                                                                                           |
| -------------------- | --------- | ----------------------------------------------------------------------------------------------------------------- |
| Source framework     | Nop       | `tests/`, `pages/`, `fixtures/`, `helpers/`, `scripts/`, `docs/`                                                  |
| Config can tai lap   | Nop       | `package.json`, `package-lock.json`, `playwright.config.ts`, `tsconfig.json`, `eslint.config.mjs`, `.env.example` |
| Test asset can thiet | Nop       | File upload mau trong `fixtures/test-data/files/`                                                                 |
| Cau hinh local       | Khong nop | `.env` co the chua password local                                                                                 |
| Dependency cai dat   | Khong nop | `node_modules/`, cai lai bang `npm ci` hoac `npm install`                                                         |
| Runtime output       | Khong nop | `.runtime/`, `playwright-report/`, `test-results/`, `reports/`, `artifacts/`, `blob-report/`                      |

## 10. Quy Uoc Viet Test

- Moi test case nen co Test ID trong title, vi du `[E2E-AUTH-LOGIN-001]`.
- Chi su dung tag that su co trong title; hien tai la `@regression`, `@smoke`, `@critical`.
- E2E spec nam trong `tests/e2e` va chay bang Playwright project `e2e`.
- Test data dong nen tao qua `fixtures/test-data/factories/TestDataFactory.ts`.
- Setup phuc tap nen dong goi trong `fixtures/test-data/scenarios`.
- E2E spec khong import truc tiep `helpers/test-state`; moi DB assertion, request setup va cleanup nen di qua `testState` fixture, `fixtures/test-data/scenarios` hoac helper chuyen dung.
- Test khong viet SQL, khong goi raw API request trong body test, tru truong hop dac biet co ghi chu ro ly do.
- Page object chi chua thao tac giao dien va locator, khong chua business assertion phuc tap.
- Uu tien locator on dinh: `data-testid`, `id`, `name`, sau do moi dung fallback text/CSS.
- Moi E2E test phai co `steps.arrange`, `steps.act`, `steps.assert` voi title co nghia; khong dung comment placeholder de qua quality gate.

## 11. Gioi Han Hien Tai

- Chua co visual regression.
- Chua co performance/load test.
- Chua co accessibility test.
- Coverage duoc quan ly bang test catalog va convention, chua co dashboard tu dong.
- Test tao/sua/xoa du lieu yeu cau moi truong test rieng va seed data on dinh.

## 12. Tai Lieu Trong Docs

- `docs/framework-overview.md`: kien truc framework va vai tro tung thanh phan.
- `docs/project-file-map.md`: ban do file ngan gon de doc va bao ve khoa luan.
- `docs/test-strategy.md`: chien luoc test, pham vi bao phu va quy uoc tag.
- `docs/execution-reporting-ci.md`: cach chay test, doc report va luong CI.

Danh muc test case cho phu luc duoc sinh tu source bang `npm run test:catalog`, thay vi giu file catalog tinh trong repo.
