import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { StaffCustomerListPage } from "@pages/staff/StaffCustomerListPage";
import { staffRentContractAssignmentExists } from "@test-data-scenarios/adminScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Staff - Customer List @regression", () => {
  let ContractState: ContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await testState.createContract();
        await loginAsScenarioUser(page, ContractState.staff.username);
        await navigationPage.open("/staff/customers");

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

  test("[E2E-STF-CUS-001] should assigned customer display when assigned customers", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare assigned customers context", async () => {
      expect.soft(true, "precondition: prepare assigned customers context").toBe(true);
    });

    await steps.act("perform assigned customers behavior", async () => {
      const customerPage = pageObjects.create(StaffCustomerListPage);
      await customerPage.waitForLoaded();
      await customerPage.waitForTableData();
      await expect(customerPage.rowByCustomerName(ContractState!.customer.fullName)).toBeVisible();

      expect(await staffRentContractAssignmentExists(ContractState!)).toBe(true);
    });

    await steps.assert("verify assigned customer display", async () => {
      expect.soft(true, "verification checkpoint: verify assigned customer display").toBe(true);
    });
  });

  test("[E2E-STF-CUS-002] should search and details modal when customer search", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare customer search context", async () => {
      expect.soft(true, "precondition: prepare customer search context").toBe(true);
    });

    await steps.act("perform customer search behavior", async () => {
      const customerPage = pageObjects.create(StaffCustomerListPage);
      await customerPage.waitForLoaded();
      await customerPage.filterByFullName(ContractState!.customer.fullName);
      await customerPage.submitFilters();
      await customerPage.waitForTableData();
      await customerPage.openCustomerDetail(ContractState!.customer.fullName);
      await customerPage.waitForDetailModalContains(ContractState!.customer.fullName);
    });

    await steps.assert("verify search and details modal", async () => {
      expect.soft(true, "verification checkpoint: verify search and details modal").toBe(true);
    });
  });
});
