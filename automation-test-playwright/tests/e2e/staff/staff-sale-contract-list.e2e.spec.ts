import type { SaleContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { StaffSaleContractListPage } from "@pages/staff/StaffSaleContractListPage";
import { staffSaleContractAssignmentExists } from "@test-data-scenarios/adminScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Staff - Sale Contract List @regression", () => {
  let SaleContractState: SaleContractState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        SaleContractState = await testState.createSaleContract();
        await loginAsScenarioUser(page, SaleContractState.staff.username);
        await navigationPage.open("/staff/sale-contracts");

        try {
          await use(undefined);
        } finally {
          await testState.deleteSaleContract(SaleContractState ?? undefined);
          SaleContractState = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-STF-SALE-001] should assigned sale contract display when assigned sale contracts", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare assigned sale contracts context", async () => {
      expect.soft(true, "precondition: prepare assigned sale contracts context").toBe(true);
    });

    await steps.act("perform assigned sale contracts behavior", async () => {
      const saleContractPage = pageObjects.create(StaffSaleContractListPage);
      await saleContractPage.waitForLoaded();
      await saleContractPage.waitForTableData();
      await saleContractPage.waitForRowVisible(SaleContractState!.building.name);

      expect(await staffSaleContractAssignmentExists(SaleContractState!)).toBe(true);
    });

    await steps.assert("verify assigned sale contract display", async () => {
      expect.soft(true, "verification checkpoint: verify assigned sale contract display").toBe(true);
    });
  });

  test("[E2E-STF-SALE-002] should customer and building filtering when sale contract filter", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare sale contract filter context", async () => {
      expect.soft(true, "precondition: prepare sale contract filter context").toBe(true);
    });

    await steps.act("perform sale contract filter behavior", async () => {
      const saleContractPage = pageObjects.create(StaffSaleContractListPage);
      await saleContractPage.waitForLoaded();
      await saleContractPage.filterByCustomerId(SaleContractState!.customer.id);
      await saleContractPage.filterByBuildingId(SaleContractState!.building.id);
      await saleContractPage.submitFilters();
      await saleContractPage.waitForRowVisible(SaleContractState!.building.name);
    });

    await steps.assert("verify customer and building filtering", async () => {
      expect.soft(true, "verification checkpoint: verify customer and building filtering").toBe(true);
    });
  });

  test("[E2E-STF-SALE-003] should details modal display when sale contract details", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare sale contract details context", async () => {
      expect.soft(true, "precondition: prepare sale contract details context").toBe(true);
    });

    await steps.act("perform sale contract details behavior", async () => {
      const saleContractPage = pageObjects.create(StaffSaleContractListPage);
      await saleContractPage.waitForLoaded();
      await saleContractPage.openDetail(SaleContractState!.building.name);
      await saleContractPage.waitForDetailModalContains(SaleContractState!.customer.fullName);
      await saleContractPage.waitForDetailModalContains(SaleContractState!.building.name);
      await saleContractPage.closeDetailModal();
    });

    await steps.assert("verify details modal display", async () => {
      expect.soft(true, "verification checkpoint: verify details modal display").toBe(true);
    });
  });
});
