# Tong Quan Kien Truc Framework

Tai lieu nay mo ta kien truc framework Playwright cua SunTower de dung trong van hanh va trinh bay khoa luan.

## 1. Muc Tieu Thiet Ke

Framework duoc thiet ke theo cac muc tieu:

- Tach biet test script voi chi tiet thao tac giao dien.
- Tai su dung session, admin setup context ho tro setup, page object va test data.
- Tap trung vao kiem thu giao dien E2E.
- Dam bao du lieu test co the tao moi va don sau khi chay.
- Tao report va artifact de phan tich loi.
- Co the chay local va tich hop CI.

## 2. Kien Truc Lop

```text
Test specs
  -> Fixtures
    -> Page Objects / Setup Helpers
      -> Helpers / DB Repositories / Test Data
        -> SunTower application
```

`tests/` la noi mo ta kich ban kiem thu. Test khong nen chua nhieu chi tiet locator, SQL, raw API request hoac logic tao data phuc tap.

`fixtures/` cap san cac doi tuong dung chung nhu `testState`, `publicPage` va metadata.

`pages/` dong vai tro Page Object Model. Moi class dai dien cho mot man hinh, mot modal hoac mot thanh phan giao dien.

`helpers/auth/` chi chua helper dang nhap/dang xuat qua UI.

`helpers/test-state/` gom setup session, MySQL lookup va cleanup co scope cho du lieu E2E; day la lop ho tro E2E, khong phai layer API/DB test rieng.

`fixtures/test-data/` gom seed theo moi truong, factory tao data dong, scenario setup va file upload test.

`helpers/browser`, `helpers/text` va `helpers/validation` gom helper nho khong gan nghiep vu.

## 3. Quy Tac Bien Gioi Layer

- E2E spec chi mo ta luong nguoi dung va assertion UI/ket qua nghiep vu muc cao.
- E2E spec khong import truc tiep `helpers/test-state` hoac `helpers/test-state`.
- Truy van DB, request setup, cleanup va seed lookup phai nam trong `fixtures/test-data/scenarios` hoac `helpers/*`.
- Page object khong tao data, khong cleanup, khong query DB.
- Scenario co the dung page object, factory, DB repository va request helper de dong goi tien dieu kien/phu dieu kien cho test.

## 4. Thanh Phan Noi Bat

### Fixture ho tro du lieu E2E

Framework tao `testState` trong fixture de chuan bi du lieu, lay OTP va cleanup nhanh cho cac kich ban UI. Cac request nay la tien dieu kien ky thuat, khong duoc tinh la test endpoint rieng.

### Page Object Model

Page object duoc chia theo module:

- `admin`
- `staff`
- `customer`
- `auth`
- `public`
- `components`
- `core`

Nhom `core` chua cac base class nhu BasePage, routed page va CRUD page de giam trung lap.

### Quan ly du lieu test

`TestDataFactory` tao username, email, ten toa nha, so dien thoai va payload theo dinh dang thong nhat. Moi du lieu dong nen chua `runToken` de framework nhan dien va cleanup.

### Global setup va teardown

`global-setup.ts` tao thu muc runtime va don report cu.

`global-teardown.ts` quet du lieu test theo `runToken`, xoa ban ghi phu hop trong database va don file upload nam trong whitelist.

## 5. Gia Tri Cho Khoa Luan

Kien truc nay the hien cac diem quan trong cua mot automation framework:

- Modularity: chia thanh test, fixture, page object, helper, config.
- Maintainability: thay doi UI it anh huong den test case.
- Reusability: dung lai POM, test data va cleanup.
- Reliability: co retry, timeout, artifact va cleanup.
- Traceability: test title co Test ID, metadata va report.
