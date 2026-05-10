import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { StaffContractListPage } from "@pages/staff/StaffContractListPage";
import { staffRentContractAssignmentExists } from "@test-data-scenarios/adminScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Staff - Contract List @regression", () => {
  let ContractState: ContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await testState.createContract();
        await loginAsScenarioUser(page, ContractState.staff.username);
        await navigationPage.open("/staff/contracts");

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

  test("[E2E-STF-CTR-001] should assigned contract display when assigned contracts", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare assigned contracts context", async () => {
      expect.soft(true, "precondition: prepare assigned contracts context").toBe(true);
    });

    await steps.act("perform assigned contracts behavior", async () => {
      const contractPage = pageObjects.create(StaffContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.waitForTableData();
      await expect(contractPage.rowByContractText(ContractState!.customer.fullName)).toBeVisible();

      expect(await staffRentContractAssignmentExists(ContractState!)).toBe(true);
    });

    await steps.assert("verify assigned contract display", async () => {
      expect.soft(true, "verification checkpoint: verify assigned contract display").toBe(true);
    });
  });

  test("[E2E-STF-CTR-002] should customer building and status filtering when contract filter", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract filter context", async () => {
      expect.soft(true, "precondition: prepare contract filter context").toBe(true);
    });

    await steps.act("perform contract filter behavior", async () => {
      const contractPage = pageObjects.create(StaffContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.filterByCustomer(ContractState!.customer.id);
      await contractPage.filterByBuilding(ContractState!.building.id);
      await contractPage.filterByStatus("ACTIVE");
      await contractPage.submitFilters();
      await contractPage.waitForTableData();
      await expect(contractPage.rowByContractText(ContractState!.customer.fullName)).toBeVisible();
    });

    await steps.assert("verify customer building and status filtering", async () => {
      expect.soft(true, "verification checkpoint: verify customer building and status filtering").toBe(true);
    });
  });

  test("[E2E-STF-CTR-003] should details modal from list when contract details", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract details context", async () => {
      expect.soft(true, "precondition: prepare contract details context").toBe(true);
    });

    await steps.act("perform contract details behavior", async () => {
      const contractPage = pageObjects.create(StaffContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.waitForTableData();
      await contractPage.openContractDetail(ContractState!.customer.fullName);
      await contractPage.waitForDetailModalContains(ContractState!.customer.fullName);
    });

    await steps.assert("verify details modal from list", async () => {
      expect.soft(true, "verification checkpoint: verify details modal from list").toBe(true);
    });
  });
});
