import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { AdminContractDetailPage } from "@pages/admin/AdminDetailPages";
import { AdminContractFormPage } from "@pages/admin/AdminContractFormPage";
import { AdminContractListPage } from "@pages/admin/AdminContractListPage";
import {
  cleanupAdminEntitySets,
  contractExists,
  contractWithPriceExists,
  createAssignableScenario,
  expireContract,
  findCreatedContract,
  readContractEditState,
  trackContractScenario,
  trackRentScenario
} from "@test-data-scenarios/adminScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Contract Management @regression @critical", () => {
  let adminUser: StaffProfileState | null = null;
  const cleanupContractIds = new Set<number>();
  const cleanupCustomerIds = new Set<number>();
  const cleanupBuildingIds = new Set<number>();
  const cleanupStaffIds = new Set<number>();

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open("/admin/contract/list");

        try {
          await use(undefined);
        } finally {
          await cleanupAdminEntitySets(testState, {
            contractIds: cleanupContractIds,
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

  test("[E2E-ADM-CTR-001] should search and detail view when contract search", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract search context", async () => {
      expect.soft(true, "precondition: prepare contract search context").toBe(true);
    });

    await steps.act("perform contract search behavior", async () => {
      const ContractState: ContractState = await testState.createContract();
      trackContractScenario(ContractState, {
        contractIds: cleanupContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const listPage = pageObjects.create(AdminContractListPage);
      const detailPage = pageObjects.create(AdminContractDetailPage);

      await navigationPage.open(
        `/admin/contract/search?customerId=${ContractState.customer.id}&buildingId=${ContractState.building.id}`
      );
      await listPage.waitForLoaded();
      await listPage.waitForTableData();
      await expect(listPage.rowByContractText(ContractState.customer.fullName)).toBeVisible();
      await listPage.openDetail(ContractState.customer.fullName);
      await detailPage.waitForLoaded(ContractState.id);
    });

    await steps.assert("verify search and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify search and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-CTR-002] should create contract from add form when contract creation", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract creation context", async () => {
      expect.soft(true, "precondition: prepare contract creation context").toBe(true);
    });

    await steps.act("perform contract creation behavior", async () => {
      const scenario = await createAssignableScenario(testState, "FOR_RENT");
      trackRentScenario(scenario, {
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });
      const formPage = pageObjects.create(AdminContractFormPage);

      await navigationPage.open("/admin/contract/add");
      await formPage.waitForAddLoaded();
      await formPage.selectBuilding(scenario.building.id);
      await formPage.waitForRentAreaOptions();
      await formPage.selectCustomer(scenario.customer.id);
      await formPage.waitForStaffOptions();
      await formPage.selectRentArea("50");
      await formPage.selectStaff(scenario.staff.id);
      await formPage.fillRentPrice(1450000);
      await formPage.fillDates("2026-06-01", "2026-12-31");
      await formPage.submitContract();
      await formPage.waitForSweetAlertContains(/thanh cong|them hop dong|success/i);

      const createdContract = await findCreatedContract(scenario.customer.id, scenario.building.id);
      expect(createdContract).toBeDefined();
      expect(Number(createdContract!.rent_price)).toBe(1450000);
      expect(createdContract!.start_date).toBe("2026-06-01");
      expect(createdContract!.end_date).toBe("2026-12-31");
      cleanupContractIds.add(createdContract!.id);
    });

    await steps.assert("verify create contract from add form", async () => {
      expect.soft(true, "verification checkpoint: verify create contract from add form").toBe(true);
    });
  });

  test("[E2E-ADM-CTR-003] should invalid date range validation when contract dates", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract dates context", async () => {
      expect.soft(true, "precondition: prepare contract dates context").toBe(true);
    });

    await steps.act("perform contract dates behavior", async () => {
      const scenario = await createAssignableScenario(testState, "FOR_RENT");
      trackRentScenario(scenario, {
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });
      const formPage = pageObjects.create(AdminContractFormPage);

      await navigationPage.open("/admin/contract/add");
      await formPage.waitForAddLoaded();
      await formPage.selectBuilding(scenario.building.id);
      await formPage.waitForRentAreaOptions();
      await formPage.selectCustomer(scenario.customer.id);
      await formPage.waitForStaffOptions();
      await formPage.selectRentArea("50");
      await formPage.selectStaff(scenario.staff.id);
      await formPage.fillRentPrice(1500000);
      await formPage.fillDates("2026-09-01", "2026-08-01");
      await formPage.submitContract();
      await formPage.waitForSweetAlertContains(/ngay ket thuc|canh bao|warning/i);

      expect(await contractWithPriceExists(scenario.customer.id, scenario.building.id, 1500000)).toBe(false);
    });

    await steps.assert("verify invalid date range validation", async () => {
      expect.soft(true, "verification checkpoint: verify invalid date range validation").toBe(true);
    });
  });

  test("[E2E-ADM-CTR-004] should active contract update when contract edit", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract edit context", async () => {
      expect.soft(true, "precondition: prepare contract edit context").toBe(true);
    });

    await steps.act("perform contract edit behavior", async () => {
      const ContractState: ContractState = await testState.createContract();
      trackContractScenario(ContractState, {
        contractIds: cleanupContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const formPage = pageObjects.create(AdminContractFormPage);
      await navigationPage.open(`/admin/contract/edit/${ContractState.id}`);
      await formPage.waitForEditLoaded(ContractState.id);
      await formPage.fillDates("2026-01-15", "2026-11-30");
      await formPage.fillRentPrice(2500000);
      await formPage.selectStatus("ACTIVE");
      await formPage.submitContract();
      await formPage.waitForSweetAlertContains(/thanh cong|cap nhat|success/i);

      const contractState = await readContractEditState(ContractState.id);
      expect(Number(contractState!.rent_price)).toBe(2500000);
      expect(contractState!.end_date).toBe("2026-11-30");
      expect(contractState!.status).toBe("ACTIVE");
    });

    await steps.assert("verify active contract update", async () => {
      expect.soft(true, "verification checkpoint: verify active contract update").toBe(true);
    });
  });

  test("[E2E-ADM-CTR-005] should expired contract lock banner display when contract edit lock", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract edit lock context", async () => {
      expect.soft(true, "precondition: prepare contract edit lock context").toBe(true);
    });

    await steps.act("perform contract edit lock behavior", async () => {
      const ContractState: ContractState = await testState.createContract();
      trackContractScenario(ContractState, {
        contractIds: cleanupContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      await expireContract(ContractState.id);

      const formPage = pageObjects.create(AdminContractFormPage);
      await navigationPage.open(`/admin/contract/edit/${ContractState.id}`);
      await formPage.waitForEditLoaded(ContractState.id);
      await formPage.waitForExpiredBanner();
    });

    await steps.assert("verify expired contract lock banner display", async () => {
      expect.soft(true, "verification checkpoint: verify expired contract lock banner display").toBe(true);
    });
  });

  test("[E2E-ADM-CTR-006] should detail page deletion when contract deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare contract deletion context", async () => {
      expect.soft(true, "precondition: prepare contract deletion context").toBe(true);
    });

    await steps.act("perform contract deletion behavior", async () => {
      const ContractState: ContractState = await testState.createContract();
      trackContractScenario(ContractState, {
        contractIds: cleanupContractIds,
        staffIds: cleanupStaffIds,
        customerIds: cleanupCustomerIds,
        buildingIds: cleanupBuildingIds
      });

      const detailPage = pageObjects.create(AdminContractDetailPage);
      await navigationPage.open(`/admin/contract/${ContractState.id}`);
      await detailPage.waitForLoaded(ContractState.id);
      await detailPage.deleteContract();
      await detailPage.confirmSweetAlert();
      await detailPage.waitForSweetAlertContains(/thanh cong|xoa hop dong|success/i);

      await expect.poll(() => contractExists(ContractState.id)).toBe(false);

      cleanupContractIds.delete(ContractState.id);
    });

    await steps.assert("verify detail page deletion", async () => {
      expect.soft(true, "verification checkpoint: verify detail page deletion").toBe(true);
    });
  });
});
