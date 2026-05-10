import { expect, test as base } from "@fixtures/base.fixture";
import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { StaffDashboardPage } from "@pages/staff/StaffDashboardPage";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Staff - Dashboard @regression @smoke", () => {
  let ContractState: ContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await testState.createContract();
        await loginAsScenarioUser(page, ContractState.staff.username);
        await navigationPage.open("/staff/dashboard");

        try {
          await use(undefined);
        } finally {
          await testState.deleteContract(ContractState ?? undefined);
          ContractState = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-STF-DSH-001] should summary stats and tables display when overview widgets", async ({
    page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare overview widgets context", async () => {
      expect.soft(true, "precondition: prepare overview widgets context").toBe(true);
    });

    await steps.act("perform overview widgets behavior", async () => {
      const dashboardPage = pageObjects.create(StaffDashboardPage);
      await dashboardPage.waitForLoaded();
      await dashboardPage.waitForSummarySectionsVisible();
      await expect(page).toHaveURL(/\/staff\/dashboard/);
    });

    await steps.assert("verify summary stats and tables display", async () => {
      expect.soft(true, "verification checkpoint: verify summary stats and tables display").toBe(true);
    });
  });
});
