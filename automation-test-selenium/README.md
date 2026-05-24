# SunTower Selenium Automation

Framework kiem thu tu dong UI E2E cho SunTower, xay dung bang Java 21, Selenium WebDriver 4 va TestNG. Du an mirror Playwright E2E catalog de so sanh hai framework trong khoa luan.

## Mo Ta

Selenium project tap trung vao UI E2E. API, DB va OTP helper duoc dung de setup, verify va cleanup du lieu cho test UI, khong tao thanh API test suite rieng.

Framework ho tro:

- Page Object Model theo module.
- Role session cho admin, staff va customer.
- TestNG groups: `smoke`, `critical`, `regression`.
- Screenshot, current URL, title va Allure attachment khi test fail.
- JUnit/Surefire report de dung cho CI.

## Phu Thuoc

- Java 21
- Maven Wrapper co san trong repo
- Selenium WebDriver 4
- TestNG
- AssertJ
- REST Assured
- MySQL Connector/J
- dotenv-java
- Chrome/Chromium cho browser test
- Backend SunTower dang chay tai `BASE_URL`

## Cai Dat

```powershell
cd automation-test-selenium
.\mvnw.cmd test -DskipTests
```

Tao `.env` tu `.env.example` neu can chay local:

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
- `BROWSER`
- `HEADLESS`
- `WORKERS`, `CI_WORKERS`
- `E2E_RETRIES`

## Cach Dung

Compile khong chay browser:

```powershell
.\mvnw.cmd test -DskipTests
```

Chay full suite:

```powershell
.\mvnw.cmd test
```

Chay theo group:

```powershell
.\mvnw.cmd test -Dgroups=smoke
.\mvnw.cmd test -Dgroups=critical
.\mvnw.cmd test -Dgroups=regression
```

Chay mot class:

```powershell
.\mvnw.cmd test "-Dtest=AuthLoginTest"
```

Sinh Allure report:

```powershell
.\mvnw.cmd allure:report
```

## Cau Truc

```text
automation-test-selenium/
|-- src/main/java/org/suntower/core/       # config, driver, base page
|-- src/main/java/org/suntower/pages/      # Page Object Model
|-- src/main/java/org/suntower/helpers/    # helper ky thuat
|-- src/test/java/org/suntower/fixtures/   # base test, auth, state, data
|-- src/test/java/org/suntower/tests/e2e/  # E2E test classes
|-- pom.xml
`-- testng.xml
```

## Quy Mo Test

- 30 E2E test classes.
- 122 unique `[E2E-*]` IDs, khop Playwright catalog.
- Full suite da duoc verify local: `122/122`, `0` failures, `0` errors, `0` skipped.
- Default suite nam ro trong `testng.xml` de discovery on dinh.

## Dong Gop

Thay doi duoc chap nhan khi:

- `.\mvnw.cmd test -DskipTests` pass.
- Neu sua E2E behavior, chay class/group lien quan.
- Test moi co Test ID khop catalog neu port tu Playwright.
- Page object chi chua thao tac UI va locator.
- Fixture tao du lieu phai co cleanup.
- Khong commit `.env`, `target`, `.runtime`, Allure report hoac browser artifact.

## Khac Phuc Loi Thuong Gap

- Chrome headless crash trong sandbox: chay ngoai sandbox hoac tren CI runner that.
- Login fail: kiem tra account seed va `DEFAULT_PASSWORD`.
- DB assertion fail: kiem tra `DB_JDBC_URL`, user/password va database `estate`.
- OTP fail: kiem tra backend co profile `local-nooauth`/test support va token khop.
- Maven khong co global `mvn`: dung Maven Wrapper `.\mvnw.cmd`.

## Ho Tro

Lien he nhom phat trien SunTower hoac tao issue/pull request trong repository de bao loi Selenium, de xuat test case hoac cai tien framework.
