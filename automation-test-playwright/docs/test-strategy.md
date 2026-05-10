# Chien Luoc Kiem Thu

Tai lieu nay tom tat pham vi kiem thu, cach phan loai test va muc do bao phu hien tai cua framework.

## 1. Lop Kiem Thu

E2E test tap trung vao:

- Hanh trinh nguoi dung tren UI.
- Dieu huong giua cac man hinh.
- Tim kiem, loc, phan trang.
- Them/sua/xoa du lieu qua giao dien.
- Modal, alert, form validation va trang thai rong.
- Cac luong nghiep vu quan trong nhu invoice, contract, sale contract, profile va payment.

## 2. Pham Vi Module

| Nhom     | E2E spec | Ghi chu                                                                  |
| -------- | -------: | ------------------------------------------------------------------------ |
| Admin    |       11 | Bao phu CRUD va nghiep vu quan tri chinh                                 |
| Staff    |        7 | Bao phu dashboard, building, customer, contract, invoice, profile        |
| Customer |        7 | Bao phu home, profile, contract, building, service, request, transaction |
| Auth     |        3 | Bao phu login, registration, reset password va session                   |
| Public   |        1 | Bao phu public building browsing                                         |
| Payment  |        1 | Bao phu QR payment va confirm payment                                    |

## 3. Quy Mo Hien Tai

| Loai          | So luong gan dung |
| ------------- | ----------------: |
| Spec file E2E |                30 |
| E2E test case |               122 |

So lieu tren duoc thong ke tu thu muc `tests/` tai thoi diem ra soat.

## 4. Tag Va Suite

| Tag           | Y nghia                                                     |
| ------------- | ----------------------------------------------------------- |
| `@regression` | Test hoi quy                                                |
| `@smoke`      | Test nhanh cho cac luong dai dien de demo/kiem tra san sang |
| `@critical`   | Test nghiep vu quan trong can uu tien khi release           |

Framework chi cho phep 3 tag tren trong title suite. Lenh `test:demo` khong dua tren tag, ma chay truc tiep hai nhom `auth` va `public` de demo nhanh. Tat ca spec kiem thu nam trong thu muc `tests/e2e` va chay bang Playwright project `e2e`.

## 5. Cac Lenh Chay Theo Chien Luoc

```powershell
npm run test:demo
npm run test:smoke
npm run test:critical
npm run test:regression
npm run test:e2e
npm run test:ci
```

## 6. Nguyen Tac Lua Chon Test Case

- Uu tien E2E cho luong nguoi dung, UI state va tich hop nhieu thanh phan.
- Cac helper request chi dung de chuan bi du lieu, lay OTP hoac cleanup, khong duoc xem la test endpoint rieng.
- Cac case sua/xoa du lieu can co cleanup ro rang.
- Cac case phu thuoc seed data can dung `fixtures/test-data/environments`.

## 7. Mau Bang Test Case Chuan

Moi test case nen co cac cot sau de dua vao phu luc khoa luan:

| Cot             | Y nghia                                                      |
| --------------- | ------------------------------------------------------------ |
| Test ID         | Ma dinh danh duy nhat, vi du `E2E-AUTH-LOGIN-001`            |
| Module          | Nhom chuc nang, vi du Auth, Admin Building, Customer Invoice |
| Actor           | Vai tro thuc hien: Admin, Staff, Customer, Anonymous         |
| Precondition    | Dieu kien truoc khi chay test                                |
| Steps           | Cac buoc thuc hien chinh                                     |
| Expected result | Ket qua mong doi                                             |
| Layer           | `E2E`                                                        |
| Priority        | `High`, `Medium`, `Low`                                      |
| Tag             | `@regression`, `@smoke`, `@critical`                         |

Mau bang:

| Test ID            | Module                   | Actor     | Precondition                     | Steps                                                 | Expected result                                    | Layer | Priority | Tag                            |
| ------------------ | ------------------------ | --------- | -------------------------------- | ----------------------------------------------------- | -------------------------------------------------- | ----- | -------- | ------------------------------ |
| E2E-AUTH-LOGIN-003 | Auth Login               | Customer  | Backend va DB seed dang san sang | Mo trang login, nhap credential hop le, submit form   | Dieu huong vao trang customer                      | E2E   | High     | `@regression @smoke @critical` |
| E2E-PUB-BLD-001    | Public Building Browsing | Anonymous | Co du lieu toa nha trong seed DB | Mo trang public, quan sat filter va danh sach ket qua | Filter mac dinh va danh sach toa nha hien thi dung | E2E   | Medium   | `@regression @smoke`           |

Co the sinh bang phu luc tu title test hien co. Title hien co thuong co dang:

```text
[TEST-ID] - Layer Actor Module - Feature - Expected behavior
```

Vi du:

```text
[E2E-AUTH-LOGIN-001] - Auth Login - Valid Credentials Redirect to Customer Home @regression
```

Khi tach title nay ra bang:

| Thanh phan      | Gia tri                                       |
| --------------- | --------------------------------------------- |
| Test ID         | `E2E-AUTH-LOGIN-001`                          |
| Layer           | `E2E`                                         |
| Module          | `Auth Login`                                  |
| Feature         | `Login`                                       |
| Expected result | `Valid Credentials Redirect to Customer Home` |
| Tag             | `@regression`                                 |

Neu can xuat bang day du, chay `npm run test:catalog` de sinh Markdown/CSV trong `.runtime/reports/test-catalog`, sau do bo sung thu cong `Precondition`, `Steps`, `Expected result` va `Priority` cho cac test case quan trong.

## 8. Khoang Trong Con Lai

Framework hien phu hop de trinh bay khoa luan automation testing. Cac huong co the mo rong sau:

- Visual regression testing.
- Performance/load testing.
- Accessibility testing.
- Dashboard thong ke coverage tu dong.
- Dashboard thong ke flakiness/coverage tu dong tren CI.
