import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { AdminStaffDetailPage } from "@pages/admin/AdminStaffDetailPage";
import { AdminStaffFormPage } from "@pages/admin/AdminStaffFormPage";
import { AdminStaffListPage } from "@pages/admin/AdminStaffListPage";
import {
  cleanupAdminEntitySets,
  findCreatedStaff,
  staffBuildingAssignmentExists,
  staffCustomerAssignmentExists,
  staffExists
} from "@test-data-scenarios/adminScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Staff Management @regression", () => {
  let adminUser: StaffProfileState | null = null;
  const cleanupStaffIds = new Set<number>();
  const cleanupBuildingIds = new Set<number>();
  const cleanupCustomerIds = new Set<number>();

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open("/admin/staff/list");

        try {
          await use(undefined);
        } finally {
          await cleanupAdminEntitySets(testState, {
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

  test("[E2E-ADM-STF-001] should create staff account from add form when staff creation", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare staff creation context", async () => {
      expect.soft(true, "precondition: prepare staff creation context").toBe(true);
    });

    await steps.act("perform staff creation behavior", async () => {
      const listPage = pageObjects.create(AdminStaffListPage);
      const formPage = pageObjects.create(AdminStaffFormPage);
      const payload = TestDataFactory.buildAdminStaffPayload({
        username: TestDataFactory.uniqueUsername("e2estf")
      });

      await navigationPage.open("/admin/staff/list");
      await listPage.openAddForm();
      await formPage.waitForLoaded();
      await formPage.fillStaffBasics({
        username: String(payload.username),
        password: String(payload.password),
        fullName: String(payload.fullName),
        phone: String(payload.phone),
        email: String(payload.email)
      });
      await formPage.selectRole("STAFF");
      await formPage.submit();
      await formPage.waitForSweetAlertContains(/them nhan vien|thanh cong|success/i);

      const createdStaff = await findCreatedStaff(String(payload.username));
      expect(createdStaff).toBeDefined();
      expect(createdStaff!.role).toBe("STAFF");
      cleanupStaffIds.add(createdStaff!.id);
    });

    await steps.assert("verify create staff account from add form", async () => {
      expect.soft(true, "verification checkpoint: verify create staff account from add form").toBe(true);
    });
  });

  test("[E2E-ADM-STF-002] should search and detail view when staff search", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare staff search context", async () => {
      expect.soft(true, "precondition: prepare staff search context").toBe(true);
    });

    await steps.act("perform staff search behavior", async () => {
      const staff = await testState.createStaff("STAFF");
      cleanupStaffIds.add(staff.id);

      const listPage = pageObjects.create(AdminStaffListPage);
      const detailPage = pageObjects.create(AdminStaffDetailPage);

      await navigationPage.open(`/admin/staff/search?role=STAFF&fullName=${encodeURIComponent(staff.fullName)}`);
      await listPage.waitForLoaded();
      await listPage.waitForSearchTableData();
      await expect(listPage.rowByStaffName(staff.fullName)).toBeVisible();
      await listPage.openDetail(staff.fullName);
      await detailPage.waitForLoaded(staff.id);
    });

    await steps.assert("verify search and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify search and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-STF-003] should customer and building assignment update when staff assignment", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare staff assignment context", async () => {
      expect.soft(true, "precondition: prepare staff assignment context").toBe(true);
    });

    await steps.act("perform staff assignment behavior", async () => {
      const targetStaff = await testState.createStaff("STAFF");
      cleanupStaffIds.add(targetStaff.id);
      const manager = await testState.createStaff("STAFF");
      cleanupStaffIds.add(manager.id);
      const building = await testState.createBuilding("FOR_RENT");
      cleanupBuildingIds.add(building.id);
      const customer = await testState.createCustomer(manager.id);
      cleanupCustomerIds.add(customer.id);

      const detailPage = pageObjects.create(AdminStaffDetailPage);
      await navigationPage.open(`/admin/staff/${targetStaff.id}`);
      await detailPage.waitForLoaded(targetStaff.id);

      await detailPage.openBuildingAssignments();
      await detailPage.setBuildingAssignment(building.id, true);
      await detailPage.saveBuildingAssignments();
      await detailPage.waitForSweetAlertContains(/cap nhat phan cong toa nha|thanh cong|success/i);

      await expect.poll(() => staffBuildingAssignmentExists(targetStaff.id, building.id)).toBe(true);

      await navigationPage.open(`/admin/staff/${targetStaff.id}`);
      await detailPage.openCustomerAssignments();
      await detailPage.setCustomerAssignment(customer.id, true);
      await detailPage.saveCustomerAssignments();
      await detailPage.waitForSweetAlertContains(/cap nhat phan cong khach hang|thanh cong|success/i);

      await expect.poll(() => staffCustomerAssignmentExists(targetStaff.id, customer.id)).toBe(true);
    });

    await steps.assert("verify customer and building assignment update", async () => {
      expect.soft(true, "verification checkpoint: verify customer and building assignment update").toBe(true);
    });
  });

  test("[E2E-ADM-STF-004] should search result deletion when staff deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare staff deletion context", async () => {
      expect.soft(true, "precondition: prepare staff deletion context").toBe(true);
    });

    await steps.act("perform staff deletion behavior", async () => {
      const staff = await testState.createStaff("STAFF");

      const listPage = pageObjects.create(AdminStaffListPage);
      await navigationPage.open(`/admin/staff/search?role=STAFF&fullName=${encodeURIComponent(staff.fullName)}`);
      await listPage.waitForSearchTableData();
      await listPage.deleteStaff(staff.fullName);
      await listPage.confirmSweetAlert();
      await listPage.waitForSweetAlertContains(/xoa nhan vien|thanh cong|success/i);

      await expect.poll(() => staffExists(staff.id)).toBe(false);
    });

    await steps.assert("verify search result deletion", async () => {
      expect.soft(true, "verification checkpoint: verify search result deletion").toBe(true);
    });
  });
});
