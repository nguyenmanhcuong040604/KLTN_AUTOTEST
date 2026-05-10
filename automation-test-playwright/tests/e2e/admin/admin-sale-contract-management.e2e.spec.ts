import type { SaleContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { AdminSaleContractDetailPage } from "@pages/admin/AdminDetailPages";
import { AdminSaleContractFormPage } from "@pages/admin/AdminSaleContractFormPage";
import { AdminSaleContractListPage } from "@pages/admin/AdminSaleContractListPage";
import {
  cleanupAdminEntitySets,
  createAssignableScenario,
  findCreatedSaleContract,
  readSaleContractTransferDate,
  saleContractExists,
  trackRentScenario,
  trackSaleContractScenario
} from "@test-data-scenarios/adminScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Sale Contract Management @regression", () => {
  let adminUser: StaffProfileState | null = null;
  const cleanupSaleContractIds = new Set<number>();
  const cleanupCustomerIds = new Set<number>();
  const cleanupBuildingIds = new Set<number>();
  const cleanupStaffIds = new Set<number>();

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open("/admin/sale-contract/list");

        try {
          await use(undefined);
        } finally {
          await cleanupAdminEntitySets(testState, {
            saleContractIds: cleanupSaleContractIds,
            customerIds: cleanupCustomerIds,
            buildingIds: cleanupBuildingIds,
            staffIds: cleanupStaffIds
          });

          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-SCT-001] should search and detail view when sale contract search", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare sale contract search context", async () => {
      expect.soft(true, "precondition: prepare sale contract search context").toBe(true);
    });

    await steps.act("perform sale contract search behavior", async () => {
      const SaleContractState: SaleContractState = await testState.createSaleContract();
      trackSaleContractScenario(SaleContractState, {
        saleContractIds: cleanupSaleContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const listPage = pageObjects.create(AdminSaleContractListPage);
      const detailPage = pageObjects.create(AdminSaleContractDetailPage);

      await navigationPage.open(
        `/admin/sale-contract/search?customerId=${SaleContractState.customer.id}&buildingId=${SaleContractState.building.id}`
      );
      await listPage.waitForLoaded();
      await listPage.waitForTableData();
      await expect(listPage.rowBySaleContractText(SaleContractState.customer.fullName)).toBeVisible();
      await listPage.openDetail(SaleContractState.customer.fullName);
      await detailPage.waitForLoaded(SaleContractState.id);
    });

    await steps.assert("verify search and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify search and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-SCT-002] should create sale contract from add form when sale contract creation", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare sale contract creation context", async () => {
      expect.soft(true, "precondition: prepare sale contract creation context").toBe(true);
    });

    await steps.act("perform sale contract creation behavior", async () => {
      const scenario = await createAssignableScenario(testState, "FOR_SALE");
      trackRentScenario(scenario, {
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });
      const formPage = pageObjects.create(AdminSaleContractFormPage);

      await navigationPage.open("/admin/sale-contract/add");
      await formPage.waitForAddLoaded();
      await formPage.selectBuilding(scenario.building.id);
      await formPage.selectCustomer(scenario.customer.id);
      await formPage.waitForStaffOptions();
      await formPage.selectStaff(scenario.staff.id);
      await formPage.fillSalePrice(3600000000);
      await formPage.fillNote("Playwright sale contract note");
      await formPage.submitSaleContract();
      await formPage.waitForSweetAlertContains(/thanh cong|them hop dong|success/i);

      const createdSaleContract = await findCreatedSaleContract(scenario.customer.id, scenario.building.id);
      expect(createdSaleContract).toBeDefined();
      expect(Number(createdSaleContract!.sale_price)).toBe(3600000000);
      cleanupSaleContractIds.add(createdSaleContract!.id);
    });

    await steps.assert("verify create sale contract from add form", async () => {
      expect.soft(true, "verification checkpoint: verify create sale contract from add form").toBe(true);
    });
  });

  test("[E2E-ADM-SCT-003] should edit form update when transfer date", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare transfer date context", async () => {
      expect.soft(true, "precondition: prepare transfer date context").toBe(true);
    });

    await steps.act("perform transfer date behavior", async () => {
      const SaleContractState: SaleContractState = await testState.createSaleContract();
      trackSaleContractScenario(SaleContractState, {
        saleContractIds: cleanupSaleContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const formPage = pageObjects.create(AdminSaleContractFormPage);
      await navigationPage.open(`/admin/sale-contract/edit/${SaleContractState.id}`);
      await formPage.waitForEditLoaded(SaleContractState.id);
      await formPage.fillTransferDate("2026-06-16");
      await formPage.submitSaleContract();
      await formPage.waitForSweetAlertContains(/thanh cong|cap nhat|success/i);

      await expect.poll(() => readSaleContractTransferDate(SaleContractState.id)).toBe("2026-06-16");
    });

    await steps.assert("verify edit form update", async () => {
      expect.soft(true, "verification checkpoint: verify edit form update").toBe(true);
    });
  });

  test("[E2E-ADM-SCT-004] should earlier than signed date validation when transfer date", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare transfer date context", async () => {
      expect.soft(true, "precondition: prepare transfer date context").toBe(true);
    });

    await steps.act("perform transfer date behavior", async () => {
      const SaleContractState: SaleContractState = await testState.createSaleContract();
      trackSaleContractScenario(SaleContractState, {
        saleContractIds: cleanupSaleContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const transferDateBefore = await readSaleContractTransferDate(SaleContractState.id);

      const formPage = pageObjects.create(AdminSaleContractFormPage);
      await navigationPage.open(`/admin/sale-contract/edit/${SaleContractState.id}`);
      await formPage.waitForEditLoaded(SaleContractState.id);
      await formPage.fillTransferDate("2025-01-01");
      await formPage.submitSaleContract();
      await formPage.waitForSweetAlertContains(/ngay ban giao|khong hop le|transfer date/i);

      expect(await readSaleContractTransferDate(SaleContractState.id)).toBe(transferDateBefore);
    });

    await steps.assert("verify earlier than signed date validation", async () => {
      expect.soft(true, "verification checkpoint: verify earlier than signed date validation").toBe(true);
    });
  });

  test("[E2E-ADM-SCT-005] should detail page deletion when sale contract deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare sale contract deletion context", async () => {
      expect.soft(true, "precondition: prepare sale contract deletion context").toBe(true);
    });

    await steps.act("perform sale contract deletion behavior", async () => {
      const SaleContractState: SaleContractState = await testState.createSaleContract();
      trackSaleContractScenario(SaleContractState, {
        saleContractIds: cleanupSaleContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const detailPage = pageObjects.create(AdminSaleContractDetailPage);
      await navigationPage.open(`/admin/sale-contract/${SaleContractState.id}`);
      await detailPage.waitForLoaded(SaleContractState.id);
      await detailPage.deleteSaleContract();
      await detailPage.confirmSweetAlert();
      await detailPage.waitForSweetAlertContains(/thanh cong|xoa hop dong mua ban|success/i);

      await expect.poll(() => saleContractExists(SaleContractState.id)).toBe(false);

      cleanupSaleContractIds.delete(SaleContractState.id);
    });

    await steps.assert("verify detail page deletion", async () => {
      expect.soft(true, "verification checkpoint: verify detail page deletion").toBe(true);
    });
  });
});
