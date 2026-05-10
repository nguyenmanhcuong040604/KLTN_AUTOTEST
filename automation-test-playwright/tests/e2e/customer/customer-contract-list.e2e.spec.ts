import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerContractListPage } from "@pages/customer/CustomerContractListPage";
import { customerActiveContractExists } from "@test-data-scenarios/adminScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Customer - Contract List @regression", () => {
  let ContractState: ContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await testState.createContract();
        await loginAsScenarioUser(page, ContractState.customer.username);
        await navigationPage.open("/customer/contract/list");

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

  test("[E2E-CUS-CTR-001] should current contract display when current contracts", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare current contracts context", async () => {
      expect.soft(true, "precondition: prepare current contracts context").toBe(true);
    });

    await steps.act("perform current contracts behavior", async () => {
      const contractPage = pageObjects.create(CustomerContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.waitForContractData();
      await expect(contractPage.cardByBuildingName(ContractState!.building.name)).toBeVisible();

      expect(await customerActiveContractExists(ContractState!)).toBe(true);
    });

    await steps.assert("verify current contract display", async () => {
      expect.soft(true, "verification checkpoint: verify current contract display").toBe(true);
    });
  });

  test("[E2E-CUS-CTR-002] should building and status filtering when contract filter", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract filter context", async () => {
      expect.soft(true, "precondition: prepare contract filter context").toBe(true);
    });

    await steps.act("perform contract filter behavior", async () => {
      const contractPage = pageObjects.create(CustomerContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.filterByBuilding(ContractState!.building.id);
      await contractPage.filterByStatus("ACTIVE");
      await contractPage.submitFilters();
      await contractPage.waitForContractData();
      await expect(contractPage.cardByBuildingName(ContractState!.building.name)).toBeVisible();

      expect(await customerActiveContractExists(ContractState!)).toBe(true);
    });

    await steps.assert("verify building and status filtering", async () => {
      expect.soft(true, "verification checkpoint: verify building and status filtering").toBe(true);
    });
  });

  test("[E2E-CUS-CTR-003] should empty state for unmatched criteria when contract filter", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract filter context", async () => {
      expect.soft(true, "precondition: prepare contract filter context").toBe(true);
    });

    await steps.act("perform contract filter behavior", async () => {
      const contractPage = pageObjects.create(CustomerContractListPage);
      await contractPage.waitForLoaded();
      await contractPage.filterByStatus("EXPIRED");
      await contractPage.submitFilters();
      await contractPage.waitForContractData();
      await contractPage.waitForEmptyState();
    });

    await steps.assert("verify empty state for unmatched criteria", async () => {
      expect.soft(true, "verification checkpoint: verify empty state for unmatched criteria").toBe(true);
    });
  });
});
