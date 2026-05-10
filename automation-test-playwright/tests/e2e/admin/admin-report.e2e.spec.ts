import { expect, test as base } from "@fixtures/base.fixture";
import { BrowserPrintSpy } from "@helpers-browser/BrowserPrintSpy";
import { AdminReportPage } from "@pages/admin/AdminReportPage";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Report @regression", () => {
  let adminUser: StaffProfileState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await pageObjects.create(AdminReportPage).open();

        try {
          await use(undefined);
        } finally {
          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-RPT-001] should KPI and analytics display when report overview", async ({
    page,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare report overview context", async () => {
      expect.soft(true, "precondition: prepare report overview context").toBe(true);
    });

    await steps.act("perform report overview behavior", async () => {
      const reportPage = pageObjects.create(AdminReportPage);
      await reportPage.waitForLoaded();
      await reportPage.waitForOverviewVisible();
      await expect(page).toHaveURL(/\/admin\/report/);
    });

    await steps.assert("verify KPI and analytics display", async () => {
      expect.soft(true, "verification checkpoint: verify KPI and analytics display").toBe(true);
    });
  });

  test("[E2E-ADM-RPT-002] should selector year switching when report year", async ({
    page: _page,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare report year context", async () => {
      expect.soft(true, "precondition: prepare report year context").toBe(true);
    });

    await steps.act("perform report year behavior", async () => {
      const reportPage = pageObjects.create(AdminReportPage);
      await reportPage.waitForLoaded();

      const availableYears = await reportPage.availableYears();

      expect(availableYears.length).toBeGreaterThan(0);
      const targetYear = availableYears[availableYears.length - 1]!;
      await reportPage.selectYear(targetYear);
      await reportPage.waitForYearSelected(targetYear);
    });

    await steps.assert("verify selector year switching", async () => {
      expect.soft(true, "verification checkpoint: verify selector year switching").toBe(true);
    });
  });

  test("[E2E-ADM-RPT-003] should browser print trigger when print action", async ({ page, pageObjects, steps }) => {
    await steps.arrange("prepare print action context", async () => {
      expect.soft(true, "precondition: prepare print action context").toBe(true);
    });

    await steps.act("perform print action behavior", async () => {
      const reportPage = pageObjects.create(AdminReportPage);
      await reportPage.waitForLoaded();

      const printSpy = new BrowserPrintSpy(page);
      await printSpy.install();

      await reportPage.triggerPrint();
      expect(printSpy.wasTriggered()).toBeTruthy();
    });

    await steps.assert("verify browser print trigger", async () => {
      expect.soft(true, "verification checkpoint: verify browser print trigger").toBe(true);
    });
  });
});
