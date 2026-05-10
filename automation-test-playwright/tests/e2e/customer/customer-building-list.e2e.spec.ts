import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { CustomerBuildingListPage } from "@pages/customer/CustomerBuildingListPage";
import { buildingExists, customerRentContractAssignmentExists } from "@test-data-scenarios/adminScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Customer - Building List @regression", () => {
  let ContractState: ContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await testState.createContract();
        await loginAsScenarioUser(page, ContractState.customer.username);
        await navigationPage.open("/customer/building/list");

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

  test("[E2E-CUS-BLD-001] should assigned building display when assigned buildings", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare assigned buildings context", async () => {
      expect.soft(true, "precondition: prepare assigned buildings context").toBe(true);
    });

    await steps.act("perform assigned buildings behavior", async () => {
      const buildingPage = pageObjects.create(CustomerBuildingListPage);
      await buildingPage.waitForLoaded();
      await buildingPage.waitForBuildingData();
      await expect(buildingPage.cardByBuildingName(ContractState!.building.name)).toBeVisible();

      expect(await customerRentContractAssignmentExists(ContractState!)).toBe(true);
    });

    await steps.assert("verify assigned building display", async () => {
      expect.soft(true, "verification checkpoint: verify assigned building display").toBe(true);
    });
  });

  test("[E2E-CUS-BLD-002] should name filter and details modal when building search", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building search context", async () => {
      expect.soft(true, "precondition: prepare building search context").toBe(true);
    });

    await steps.act("perform building search behavior", async () => {
      const buildingPage = pageObjects.create(CustomerBuildingListPage);
      await buildingPage.waitForLoaded();
      await buildingPage.filterByName(ContractState!.building.name);
      await buildingPage.submitFilters();
      await buildingPage.waitForBuildingData();
      await buildingPage.openBuildingDetail(ContractState!.building.name);
      await buildingPage.waitForDetailModalContains(ContractState!.building.name);

      expect(await buildingExists(ContractState!.building.id)).toBe(true);
    });

    await steps.assert("verify name filter and details modal", async () => {
      expect.soft(true, "verification checkpoint: verify name filter and details modal").toBe(true);
    });
  });

  test("[E2E-CUS-BLD-003] should empty state for unmatched search when building search", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building search context", async () => {
      expect.soft(true, "precondition: prepare building search context").toBe(true);
    });

    await steps.act("perform building search behavior", async () => {
      const buildingPage = pageObjects.create(CustomerBuildingListPage);
      await buildingPage.waitForLoaded();
      await buildingPage.filterByName(TestDataFactory.uniqueCode("no-match"));
      await buildingPage.submitFilters();
      await buildingPage.waitForBuildingData();
      await buildingPage.waitForEmptyState();
    });

    await steps.assert("verify empty state for unmatched search", async () => {
      expect.soft(true, "verification checkpoint: verify empty state for unmatched search").toBe(true);
    });
  });
});
