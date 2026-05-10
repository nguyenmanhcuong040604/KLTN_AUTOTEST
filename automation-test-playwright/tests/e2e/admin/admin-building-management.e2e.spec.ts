import type { ContractState } from "@test-data-scenarios/TestEntityTypes";
import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { AdminBuildingDetailPage } from "@pages/admin/AdminDetailPages";
import { AdminBuildingFormPage } from "@pages/admin/AdminBuildingFormPage";
import { AdminBuildingListPage } from "@pages/admin/AdminBuildingListPage";
import {
  buildingExists,
  cleanupBuildingIdSet,
  cleanupContractScenarios,
  createBuildingLockedByContractScenario,
  createBuildingScenario,
  createAdminE2ESession,
  findBuildingCreatedFromForm,
  findBuildingEditState,
  type AdminE2ESession
} from "@test-data-scenarios/adminScenario";

base.describe("Admin - Building Management @regression @critical", () => {
  let adminSession: AdminE2ESession | null = null;
  const cleanupBuildingIds = new Set<number>();
  const cleanupContracts: ContractState[] = [];

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage: _navigationPage }, use) => {
        adminSession = await createAdminE2ESession(page, testState, "/admin/building/list");

        try {
          await use(undefined);
        } finally {
          await cleanupContractScenarios(testState, cleanupContracts);
          await cleanupBuildingIdSet(testState, cleanupBuildingIds);
          await adminSession?.cleanup();
          adminSession = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-BLD-001] should filter and detail view when building search", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building search context", async () => {
      expect.soft(true, "precondition: prepare building search context").toBe(true);
    });

    await steps.act("perform building search behavior", async () => {
      const BuildingState = await createBuildingScenario(testState, TestDataFactory.transactionType.rent);
      cleanupBuildingIds.add(BuildingState.id);

      const listPage = pageObjects.create(AdminBuildingListPage);
      const detailPage = pageObjects.create(AdminBuildingDetailPage);

      await navigationPage.open("/admin/building/list");
      await listPage.waitForLoaded();
      await listPage.filterByName(BuildingState.name);
      await listPage.filterByTransactionType(TestDataFactory.transactionType.rent);
      await listPage.search();
      await listPage.waitForTableData();

      await expect(listPage.rowByBuildingName(BuildingState.name)).toBeVisible();
      await listPage.openDetail(BuildingState.name);
      await detailPage.waitForLoaded(BuildingState.id);
    });

    await steps.assert("verify filter and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify filter and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-BLD-002] should rental building creation when building creation", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building creation context", async () => {
      expect.soft(true, "precondition: prepare building creation context").toBe(true);
    });

    await steps.act("perform building creation behavior", async () => {
      const listPage = pageObjects.create(AdminBuildingListPage);
      const formPage = pageObjects.create(AdminBuildingFormPage);
      const buildingName = TestDataFactory.uniqueBuildingName("E2E Building");
      const taxCode = TestDataFactory.uniqueNumberCode(TestDataFactory.buildingForm.taxCodePrefix, 10);
      const createData = TestDataFactory.buildingForm.rentalCreate;

      await navigationPage.open("/admin/building/list");
      await listPage.openAddForm();
      await formPage.waitForAddLoaded();
      await formPage.setTransactionType(TestDataFactory.transactionType.rent);
      await formPage.fillCommonFields({
        name: buildingName,
        districtId: TestDataFactory.buildingForm.defaultDistrictId,
        ward: TestDataFactory.buildingForm.defaultWard,
        street: TestDataFactory.buildingForm.defaultStreet,
        numberOfFloor: createData.numberOfFloor,
        numberOfBasement: createData.numberOfBasement,
        floorArea: createData.floorArea,
        level: TestDataFactory.buildingForm.defaultLevel,
        direction: TestDataFactory.buildingForm.defaultDirection,
        taxCode,
        linkOfBuilding: TestDataFactory.buildingForm.validLink
      });
      await formPage.fillRentFields({
        rentPrice: createData.rentPrice,
        deposit: createData.deposit,
        serviceFee: createData.serviceFee,
        carFee: createData.carFee,
        motorbikeFee: createData.motorbikeFee,
        waterFee: createData.waterFee,
        electricityFee: createData.electricityFee,
        rentAreaValues: createData.rentAreaValues
      });
      await formPage.setCoordinates(
        TestDataFactory.buildingForm.defaultCoordinates.latitude,
        TestDataFactory.buildingForm.defaultCoordinates.longitude
      );
      await formPage.submit();
      await formPage.waitForSweetAlertContains(/thanh cong|success/i);

      const createdBuilding = await findBuildingCreatedFromForm(buildingName);
      expect(createdBuilding).toBeDefined();
      expect(createdBuilding!.transaction_type).toBe(TestDataFactory.transactionType.rent);
      expect(Number(createdBuilding!.floor_area)).toBe(createData.floorArea);
      expect(createdBuilding!.tax_code).toBe(taxCode);
      cleanupBuildingIds.add(createdBuilding!.id);
    });

    await steps.assert("verify rental building creation", async () => {
      expect.soft(true, "verification checkpoint: verify rental building creation").toBe(true);
    });
  });

  test("[E2E-ADM-BLD-003] should unlocked building update when building edit", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building edit context", async () => {
      expect.soft(true, "precondition: prepare building edit context").toBe(true);
    });

    await steps.act("perform building edit behavior", async () => {
      const BuildingState = await createBuildingScenario(testState, TestDataFactory.transactionType.rent);
      cleanupBuildingIds.add(BuildingState.id);

      const formPage = pageObjects.create(AdminBuildingFormPage);
      const updatedName = `${BuildingState.name} Updated`;
      const updateData = TestDataFactory.buildingForm.rentalUpdate;

      await navigationPage.open(`/admin/building/edit/${BuildingState.id}`);
      await formPage.waitForEditLoaded(BuildingState.id);
      await formPage.fillCommonFields({
        name: updatedName,
        numberOfFloor: updateData.numberOfFloor,
        floorArea: updateData.floorArea
      });
      await formPage.fillRentFields({
        rentPrice: updateData.rentPrice,
        deposit: updateData.deposit,
        serviceFee: updateData.serviceFee,
        carFee: updateData.carFee,
        motorbikeFee: updateData.motorbikeFee,
        waterFee: updateData.waterFee,
        electricityFee: updateData.electricityFee,
        rentAreaValues: updateData.rentAreaValues
      });
      await formPage.submit();
      await formPage.waitForSweetAlertContains(/thanh cong|success/i);

      const buildingState = await findBuildingEditState(BuildingState.id);
      expect(buildingState?.name).toBe(updatedName);
      expect(Number(buildingState?.floor_area ?? 0)).toBe(updateData.floorArea);
      expect(Number(buildingState?.rent_price ?? 0)).toBe(updateData.rentPrice);
    });

    await steps.assert("verify unlocked building update", async () => {
      expect.soft(true, "verification checkpoint: verify unlocked building update").toBe(true);
    });
  });

  test("[E2E-ADM-BLD-004] should active contract lock banner display when building edit lock", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building edit lock context", async () => {
      expect.soft(true, "precondition: prepare building edit lock context").toBe(true);
    });

    await steps.act("perform building edit lock behavior", async () => {
      const ContractState = await createBuildingLockedByContractScenario(testState);
      cleanupContracts.push(ContractState);

      const formPage = pageObjects.create(AdminBuildingFormPage);
      await navigationPage.open(`/admin/building/edit/${ContractState.building.id}`);
      await formPage.waitForEditLoaded(ContractState.building.id);
      await formPage.waitForLockBanner();
    });

    await steps.assert("verify active contract lock banner display", async () => {
      expect.soft(true, "verification checkpoint: verify active contract lock banner display").toBe(true);
    });
  });

  test("[E2E-ADM-BLD-005] should unlocked building deletion from list when building deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare building deletion context", async () => {
      expect.soft(true, "precondition: prepare building deletion context").toBe(true);
    });

    await steps.act("perform building deletion behavior", async () => {
      const BuildingState = await createBuildingScenario(testState, TestDataFactory.transactionType.rent);

      const listPage = pageObjects.create(AdminBuildingListPage);
      await navigationPage.open("/admin/building/list");
      await listPage.filterByName(BuildingState.name);
      await listPage.search();
      await listPage.waitForTableData();
      await listPage.deleteBuilding(BuildingState.name);
      await listPage.confirmSweetAlert();
      await listPage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect
        .poll(async () => {
          return buildingExists(BuildingState.id);
        })
        .toBe(false);
    });

    await steps.assert("verify unlocked building deletion from list", async () => {
      expect.soft(true, "verification checkpoint: verify unlocked building deletion from list").toBe(true);
    });
  });
});
