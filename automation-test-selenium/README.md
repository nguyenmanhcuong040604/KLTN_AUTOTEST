# SunTower Selenium Automation

Java 17 + Selenium WebDriver 4 + TestNG automation framework for SunTower.

This project mirrors the Playwright framework at `../automation-test-playwright` so the two implementations can be compared in the graduation thesis.

## Stack

- Java 17
- Selenium WebDriver 4
- TestNG
- AssertJ
- Allure TestNG
- REST Assured
- MySQL Connector/J
- dotenv-java

## Commands

```powershell
mvn test
mvn test -Dgroups=smoke
mvn test -Dgroups=critical
mvn test -Dgroups=regression
mvn test -DsuiteXmlFile=testng.xml
```

Compile without executing browser tests:

```powershell
mvn test -DskipTests
```

Generate Allure report after a run:

```powershell
mvn allure:report
```

## Environment

Copy `.env.example` to `.env` and adjust credentials for the target environment.

Important variables:

- `BASE_URL`
- `APP_ENV`
- `ADMIN_USERNAME` / `ADMIN_USERNAMES`
- `STAFF_USERNAME` / `STAFF_USERNAMES`
- `CUSTOMER_USERNAME` / `CUSTOMER_USERNAMES`
- `DEFAULT_PASSWORD`
- `EXPECT_TIMEOUT`
- `ACTION_TIMEOUT`
- `NAVIGATION_TIMEOUT`
- `E2E_RETRIES`
- `WORKERS`
- `CI_WORKERS`
- `DB_JDBC_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `BROWSER`
- `HEADLESS`

## Current Coverage

Initial Selenium baseline includes:

- core config and driver lifecycle
- explicit wait based `BasePage`
- `PageObjectFactory`
- SweetAlert and optional browser action helpers
- shared Bootstrap modal, table, CRUD, and role shell page objects
- Allure step helper
- TestNG retry analyzer
- failure screenshot/current URL/page title attachments
- auth login, registration, and password reset baseline tests
- public building browsing smoke tests
- foundational admin page objects for dashboard, building, customer, staff, and contract flows

Admin, customer, staff, payment, and full test-state scenario ports should be added incrementally using the same package structure.
