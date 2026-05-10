# Plan: Xay Dung `automation-test-selenium`

## Summary

Tao project Java + Selenium moi ten `automation-test-selenium` nam cung cap voi `automation-test-playwright`, phuc vu so sanh cong bang giua Playwright va Selenium trong do an. Project Selenium se mirror kien truc hien co cua Playwright: POM, test data, test state setup/cleanup, role sessions, reporting, group execution va CI.

File ke hoach nay duoc luu tai root workspace de giu context thong suot qua cac buoc trien khai tiep theo.

## Key Implementation Changes

- Tao Maven project thu cong tai `automation-test-selenium`, khong dung archetype de tranh sinh code thua.
- Dung Java 21, Selenium WebDriver 4.x, TestNG, AssertJ, Allure, REST Assured hoac Java HttpClient, MySQL Connector/J, dotenv/config loader.
- Cau truc chinh:
  - `src/main/java/org/suntower/core`: driver, base page, waits, config, page factory.
  - `src/main/java/org/suntower/pages`: page objects theo nhom `auth`, `admin`, `customer`, `staff`, `public`, `components`, `core`.
  - `src/main/java/org/suntower/helpers`: runtime, auth, browser, text, validation, observability.
  - `src/test/java/org/suntower/tests/e2e`: test classes theo nhom nhu Playwright.
  - `src/test/java/org/suntower/fixtures`: base test, steps, test context, test state, data scenarios.
- Implement core framework:
  - `DriverManager` dung `ThreadLocal<WebDriver>` de ho tro chay song song.
  - `BaseTest` quan ly setup/teardown browser bang TestNG `@BeforeMethod` va `@AfterMethod`.
  - `BasePage` boc explicit waits, click/fill/get text/wait URL/wait table/wait toast.
  - `StepHelper` dung Allure step de mirror `steps.arrange`, `steps.act`, `steps.assert`.
  - `PageObjectFactory` de mirror fixture `pageObjects.create(...)`.
- Implement runtime/config:
  - Doc `.env` tuong duong Playwright `env.ts`.
  - Validate `BASE_URL`, credentials, timeout, worker count, retry count, DB config.
  - Default local config lay theo Playwright project hien tai.
- Implement session/test state:
  - Port `AuthSessionHelper` sang Java cho `adminSession`, `staffSession`, `customerSession`.
  - Port account resolver, API test context, test state builder, OTP helper, DB helper.
  - Dung API/database setup-cleanup de giu test on dinh, khong chi thao tac UI.
- Implement reporting:
  - Allure report voi step logs.
  - Screenshot on failure.
  - Attach current URL, page title, browser logs neu kha dung.
  - JUnit XML output cho CI.
  - Retry analyzer tuong duong Playwright retries.
- Port theo thu tu an toan:
  - Core/components/auth truoc.
  - Sau do public + smoke tests.
  - Sau do admin/customer/staff/payment.
  - Moi nhom test chi port khi POM va helper phu thuoc da san sang.

## Public Interfaces / Commands

- Maven commands:
  - `mvn test` chay suite mac dinh.
  - `mvn test -Dgroups=smoke`
  - `mvn test -Dgroups=critical`
  - `mvn test -Dgroups=regression`
  - `mvn test -DsuiteXmlFile=testng.xml`
- TestNG groups mirror Playwright tags:
  - `smoke`
  - `critical`
  - `regression`
- Environment variables mirror Playwright:
  - `BASE_URL`
  - `APP_ENV`
  - `ADMIN_USERNAME` / `ADMIN_USERNAMES`
  - `STAFF_USERNAME` / `STAFF_USERNAMES`
  - `CUSTOMER_USERNAME` / `CUSTOMER_USERNAMES`
  - `DEFAULT_PASSWORD`
  - `ACTION_TIMEOUT`
  - `NAVIGATION_TIMEOUT`
  - `EXPECT_TIMEOUT`
  - `WORKERS`
  - `E2E_RETRIES`
  - `DB_JDBC_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`

## Test Plan

- Verify project compiles with `mvn test -DskipTests`.
- Run auth/public smoke tests first to validate driver, waits, config, Allure, screenshot failure handling.
- Run grouped suites:
  - `smoke`
  - `critical`
  - `regression`
- Compare Selenium results with existing Playwright catalog:
  - same test IDs
  - same feature groups
  - same role coverage
  - equivalent setup/cleanup behavior
- Validate CI:
  - Maven test execution.
  - JUnit XML artifact.
  - Allure results artifact.
  - screenshots/logs retained on failure.

## Assumptions

- Project root is `d:\Documents\HaUI_KHOALUAN_SELEN+PLAYWRIGHT`.
- New Selenium project will be created at `automation-test-selenium`.
- Plan file is saved as `automation-test-selenium-plan.md` in the workspace root.
- TestNG is chosen over JUnit 5 because it maps more directly to Playwright tags/groups and suite execution.
- Allure is chosen for step reporting, but Selenium must also produce JUnit XML and failure artifacts to stay comparable with Playwright.
- Test data and test state code belong under test-side packages unless reused by framework runtime.
- Selenium implementation must include API/database setup-cleanup; otherwise the comparison with Playwright is not fair.
