# Project File Map

Tai lieu nay giup doc project test-auto theo dung thu tu, tranh bi roi boi cac file framework va output sinh tu dong.

## 1. Can hieu de bao ve

| Khu vuc                                           | Vai tro                                | Ly do can hieu                                               |
| ------------------------------------------------- | -------------------------------------- | ------------------------------------------------------------ |
| `tests/e2e/`                                      | Chua test case E2E theo nhom chuc nang | Day la bang chung chinh cua khoa luan                        |
| `pages/`                                          | Page Object Model cho cac man hinh     | Giai thich cach tach locator/thao tac UI khoi test           |
| `fixtures/base.fixture.ts`                        | Mo rong fixture Playwright             | Cho thay cach dung chung setup va request context            |
| `playwright.config.ts`                            | Cau hinh Playwright                    | Trinh bay browser, timeout, retry, reporter va output        |
| `fixtures/test-data/factories/TestDataFactory.ts` | Sinh du lieu test dong                 | Giai thich cach tranh trung du lieu va lam test lap lai duoc |

## 2. Chi can biet vai tro

| Khu vuc                                                 | Vai tro                                      | Cach trinh bay ngan gon                                                           |
| ------------------------------------------------------- | -------------------------------------------- | --------------------------------------------------------------------------------- |
| `helpers/runtime/env.ts`                                | Doc bien moi truong va cau hinh local/CI     | "Tap trung cau hinh de test chay duoc tren nhieu moi truong"                      |
| `helpers/runtime/global-setup.ts`                       | Tao/don thu muc report truoc khi chay        | "Chuan bi output cho moi lan test"                                                |
| `helpers/runtime/global-teardown.ts`                    | Don du lieu test va file upload sau khi chay | "Co che an toan du lieu sau test"                                                 |
| `fixtures/test-data/scenarios/`                         | Tao va don du lieu theo kich ban nghiep vu   | "An setup phuc tap de test body ngan gon; test goi qua testState/scenario facade" |
| `helpers/auth/`                                         | Dang nhap/dang xuat qua UI                   | "Giup test vao dung role bang luong nguoi dung"                                   |
| `helpers/test-state/`                                   | Setup session, MySQL lookup, OTP va cleanup  | "Ho tro setup/verify/cleanup cho E2E, khong phai layer API/DB test rieng"         |
| `helpers/browser`, `helpers/text`, `helpers/validation` | Helper ky thuat dung chung                   | "Dung lai cac thao tac ky thuat lap lai"                                          |

## 3. File sinh tu dong, khong can trinh bay

| Khu vuc                      | Nguon sinh ra                            | Xu ly                              |
| ---------------------------- | ---------------------------------------- | ---------------------------------- |
| `node_modules/`              | `npm install`                            | Khong dua vao source nop           |
| `.runtime/`                  | Chay Playwright test                     | Khong dua vao source nop           |
| `playwright-report/`         | HTML report neu cau hinh output mac dinh | Chi dung de xem ket qua            |
| `test-results/`              | Screenshot/video/trace neu test loi      | Chi dung khi debug                 |
| `reports/`                   | JUnit/CI report neu co                   | Chi nop khi can minh chung ket qua |
| `artifacts/`, `blob-report/` | Output CI/report nang cao                | Khong can trinh bay trong source   |
| `.env`                       | Cau hinh local                           | Khong nop neu co username/password |

## 4. File Nen Nop Va Khong Nen Nop

| Nhom              | Nen nop? | Vi du                                                                                        |
| ----------------- | -------- | -------------------------------------------------------------------------------------------- |
| Source automation | Co       | `tests/`, `pages/`, `fixtures/`, `helpers/`, `scripts/`, `docs/`                             |
| Config tai lap    | Co       | `package.json`, `package-lock.json`, `playwright.config.ts`, `tsconfig.json`, `.env.example` |
| Output runtime    | Khong    | `.runtime/`, `playwright-report/`, `test-results/`, `reports/`, `artifacts/`, `blob-report/` |
| Dependency        | Khong    | `node_modules/`                                                                              |
| Secret local      | Khong    | `.env`                                                                                       |

## 5. Cach giai thich ngan gon truoc hoi dong

1. Bat dau tu `tests/e2e/auth/auth-login.e2e.spec.ts` de cho thay mot test case doc duoc nhu kich ban nguoi dung.
2. Mo `pages/auth/LoginPage.ts` de giai thich Page Object Model qua mot page cu the.
3. Mo `fixtures/base.fixture.ts` de giai thich fixture dung chung.
4. Mo `fixtures/base.fixture.ts` va `fixtures/test-data/scenarios/` neu can noi ve tao du lieu test dong.
5. Mo `helpers/test-state/` va `helpers/runtime/global-teardown.ts` neu hoi ve an toan du lieu va cleanup sau khi test.

## 6. Nguyen tac giu project gon

- Khong commit `node_modules/` va output runtime.
- Khong xoa `pages/`, `fixtures/`, `fixtures/test-data/`, `helpers/` khi con test dang import chung.
- Khong dua `.env` co thong tin local vao goi source nop; dung `.env.example` de huong dan cau hinh.
- Neu can giam do phuc tap trong bao cao, hay dua `helpers/` va `global-teardown.ts` vao phan nang cao/phu luc.

## 7. Kien truc da duoc rut gon

- `pages/core/` chi con cac base page that su dung truc tiep: `BasePage`, `CrudListPage`, `CrudFormPage`, `CrudDetailPage`, shell page theo role, navigation va profile base.
- `helpers/accounts/` resolve tai khoan test theo role de dung chung cho UI login va setup data.
- `helpers/test-state/` gom `testState` fixture helper, session setup, cleanup task va fallback cleanup qua DB.
- `fixtures/test-data/environments/` chi con `index.ts`, tranh lap 4 file JSON co cung noi dung.
