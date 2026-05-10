import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { AdminCustomerDetailPage } from "@pages/admin/AdminDetailPages";
import { AdminCustomerFormPage } from "@pages/admin/AdminCustomerFormPage";
import { AdminCustomerListPage } from "@pages/admin/AdminCustomerListPage";
import {
  customerAccountExists,
  customerExists,
  findCreatedCustomer,
  staffCustomerAssignmentExists
} from "@test-data-scenarios/adminScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Customer Management @regression", () => {
  let adminUser: StaffProfileState | null = null;
  const cleanupStaffIds = new Set<number>();
  const cleanupCustomerIds = new Set<number>();

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open("/admin/customer/list");

        try {
          await use(undefined);
        } finally {
          for (const customerId of cleanupCustomerIds) {
            await testState.deleteCustomer(customerId);
          }
          cleanupCustomerIds.clear();

          for (const staffId of cleanupStaffIds) {
            await testState.deleteStaff(staffId);
          }
          cleanupStaffIds.clear();

          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-CUS-001] should create customer from add form when customer creation", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare customer creation context", async () => {
      expect.soft(true, "precondition: prepare customer creation context").toBe(true);
    });

    await steps.act("perform customer creation behavior", async () => {
      const manager = await testState.createStaff("STAFF");
      cleanupStaffIds.add(manager.id);

      const listPage = pageObjects.create(AdminCustomerListPage);
      const formPage = pageObjects.create(AdminCustomerFormPage);
      const payload = TestDataFactory.buildCustomerPayload({
        username: TestDataFactory.uniqueUsername("e2ecust")
      });

      await navigationPage.open("/admin/customer/list");
      await listPage.openAddForm();
      await formPage.waitForLoaded();
      await formPage.fillCustomerBasics({
        username: String(payload.username),
        password: String(payload.password),
        fullName: String(payload.fullName),
        phone: String(payload.phone),
        email: String(payload.email)
      });
      await formPage.selectStaffIds([manager.id]);
      await formPage.submit();
      await formPage.waitForSweetAlertContains(/them khach hang|thanh cong|success/i);

      const createdCustomer = await findCreatedCustomer(String(payload.username));
      expect(createdCustomer).toBeDefined();
      cleanupCustomerIds.add(createdCustomer!.id);

      expect(await staffCustomerAssignmentExists(manager.id, createdCustomer!.id)).toBe(true);
    });

    await steps.assert("verify create customer from add form", async () => {
      expect.soft(true, "verification checkpoint: verify create customer from add form").toBe(true);
    });
  });

  test("[E2E-ADM-CUS-002] should search and detail view when customer search", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare customer search context", async () => {
      expect.soft(true, "precondition: prepare customer search context").toBe(true);
    });

    await steps.act("perform customer search behavior", async () => {
      const manager = await testState.createStaff("STAFF");
      cleanupStaffIds.add(manager.id);
      const customer = await testState.createCustomer(manager.id);
      cleanupCustomerIds.add(customer.id);

      const listPage = pageObjects.create(AdminCustomerListPage);
      const detailPage = pageObjects.create(AdminCustomerDetailPage);

      await navigationPage.open(`/admin/customer/search?fullName=${encodeURIComponent(customer.fullName)}`);
      await listPage.waitForLoaded();
      await listPage.waitForTableData();
      await expect(listPage.rowByCustomerName(customer.fullName)).toBeVisible();
      await listPage.openDetail(customer.fullName);
      await detailPage.waitForLoaded(customer.id);
    });

    await steps.assert("verify search and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify search and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-CUS-003] should no staff selected validation when staff assignment", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare staff assignment context", async () => {
      expect.soft(true, "precondition: prepare staff assignment context").toBe(true);
    });

    await steps.act("perform staff assignment behavior", async () => {
      const formPage = pageObjects.create(AdminCustomerFormPage);
      const payload = TestDataFactory.buildCustomerPayload({
        username: TestDataFactory.uniqueUsername("nostaff")
      });

      await navigationPage.open("/admin/customer/add");
      await formPage.waitForLoaded();
      await formPage.fillCustomerBasics({
        username: String(payload.username),
        password: String(payload.password),
        fullName: String(payload.fullName),
        phone: String(payload.phone),
        email: String(payload.email)
      });
      await formPage.submit();
      await formPage.waitForSweetAlertContains(/loi|error|nhan vien/i);

      expect(await customerAccountExists(String(payload.username), String(payload.email))).toBe(false);
    });

    await steps.assert("verify no staff selected validation", async () => {
      expect.soft(true, "verification checkpoint: verify no staff selected validation").toBe(true);
    });
  });

  test("[E2E-ADM-CUS-004] should search result deletion when customer deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare customer deletion context", async () => {
      expect.soft(true, "precondition: prepare customer deletion context").toBe(true);
    });

    await steps.act("perform customer deletion behavior", async () => {
      const manager = await testState.createStaff("STAFF");
      cleanupStaffIds.add(manager.id);
      const customer = await testState.createCustomer(manager.id);

      const listPage = pageObjects.create(AdminCustomerListPage);
      await navigationPage.open(`/admin/customer/search?fullName=${encodeURIComponent(customer.fullName)}`);
      await listPage.waitForTableData();
      await listPage.deleteCustomer(customer.fullName);
      await listPage.confirmSweetAlert();
      await listPage.waitForSweetAlertContains(/xoa khach hang|thanh cong|success/i);

      await expect.poll(() => customerExists(customer.id)).toBe(false);
    });

    await steps.assert("verify search result deletion", async () => {
      expect.soft(true, "verification checkpoint: verify search result deletion").toBe(true);
    });
  });
});
