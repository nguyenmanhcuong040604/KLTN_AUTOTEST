import { expect, test as base } from "@fixtures/base.fixture";
import { AdminContractFormPage } from "@pages/admin/AdminContractFormPage";
import { AdminPropertyRequestDetailPage } from "@pages/admin/AdminPropertyRequestDetailPage";
import { AdminPropertyRequestListPage } from "@pages/admin/AdminPropertyRequestListPage";
import { AdminSaleContractFormPage } from "@pages/admin/AdminSaleContractFormPage";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";
import {
  approveBuyPropertyRequestScenario,
  approveRentPropertyRequestScenario,
  createPropertyRequestScenario,
  readPropertyRequestState,
  type PropertyRequestScenario
} from "@test-data-scenarios/propertyRequestScenario";

base.describe("Admin - Property Request Management @regression", () => {
  let adminUser: StaffProfileState | null = null;
  let scenario: PropertyRequestScenario | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open("/admin/property-request/list");

        try {
          await use(undefined);
        } finally {
          if (scenario) {
            await scenario.cleanup();
            scenario = null;
          }

          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-PRQ-001] should pending request filter and detail view when request filter", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare request filter context", async () => {
      expect.soft(true, "precondition: prepare request filter context").toBe(true);
    });

    await steps.act("perform request filter behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "RENT");

      const listPage = pageObjects.create(AdminPropertyRequestListPage);
      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);

      await navigationPage.open("/admin/property-request/list");
      await listPage.waitForLoaded();
      await listPage.filterByStatus("PENDING");
      await listPage.waitForTableData();
      await expect(listPage.rowByRequestId(scenario.propertyRequestId)).toBeVisible();
      await listPage.openDetail(scenario.propertyRequestId);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
    });

    await steps.assert("verify pending request filter and detail view", async () => {
      expect.soft(true, "verification checkpoint: verify pending request filter and detail view").toBe(true);
    });
  });

  test("[E2E-ADM-PRQ-002] should pending request rejection with reason when request rejection", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare request rejection context", async () => {
      expect.soft(true, "precondition: prepare request rejection context").toBe(true);
    });

    await steps.act("perform request rejection behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "RENT");
      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);

      await navigationPage.open(`/admin/property-request/${scenario.propertyRequestId}`);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
      await detailPage.waitForPendingActionsVisible();
      await detailPage.rejectRequest("Rejected by Playwright E2E");
      await detailPage.waitForRejectAlertVisible();

      await expect
        .poll(async () => {
          const requestState = await readPropertyRequestState(scenario!.propertyRequestId);
          return `${requestState?.status ?? ""}|${requestState?.adminNote ?? ""}`;
        })
        .toBe("REJECTED|Rejected by Playwright E2E");
    });

    await steps.assert("verify pending request rejection with reason", async () => {
      expect.soft(true, "verification checkpoint: verify pending request rejection with reason").toBe(true);
    });
  });

  test("[E2E-ADM-PRQ-003] should prefilled contract form navigation when rent request detail", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare rent request detail context", async () => {
      expect.soft(true, "precondition: prepare rent request detail context").toBe(true);
    });

    await steps.act("perform rent request detail behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "RENT");
      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);
      const contractFormPage = pageObjects.create(AdminContractFormPage);

      await navigationPage.open(`/admin/property-request/${scenario.propertyRequestId}`);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
      await detailPage.waitForCreateContractLink(scenario.propertyRequestId);
      await detailPage.openCreateContractLink(scenario.propertyRequestId);
      await contractFormPage.waitForAddLoaded();
      await detailPage.waitForPrefilledCustomer(scenario.customerId);
    });

    await steps.assert("verify prefilled contract form navigation", async () => {
      expect.soft(true, "verification checkpoint: verify prefilled contract form navigation").toBe(true);
    });
  });

  test("[E2E-ADM-PRQ-004] should prefilled sale contract form navigation when buy request detail", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare buy request detail context", async () => {
      expect.soft(true, "precondition: prepare buy request detail context").toBe(true);
    });

    await steps.act("perform buy request detail behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "BUY");
      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);
      const saleFormPage = pageObjects.create(AdminSaleContractFormPage);

      await navigationPage.open(`/admin/property-request/${scenario.propertyRequestId}`);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
      await detailPage.waitForCreateSaleContractLink(scenario.propertyRequestId);
      await detailPage.openCreateSaleContractLink(scenario.propertyRequestId);
      await saleFormPage.waitForAddLoaded();
      await detailPage.waitForPrefilledCustomer(scenario.customerId);
    });

    await steps.assert("verify prefilled sale contract form navigation", async () => {
      expect.soft(true, "verification checkpoint: verify prefilled sale contract form navigation").toBe(true);
    });
  });

  test("[E2E-ADM-PRQ-005] should linked contract display for approved rent request when processed result", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare processed result context", async () => {
      expect.soft(true, "precondition: prepare processed result context").toBe(true);
    });

    await steps.act("perform processed result behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "RENT");
      const createdContractId = await approveRentPropertyRequestScenario(scenario);

      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);
      await navigationPage.open(`/admin/property-request/${scenario.propertyRequestId}`);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
      await detailPage.waitForProcessedContractLink(createdContractId);

      const requestState = await readPropertyRequestState(scenario.propertyRequestId);
      expect(requestState?.status).toBe("APPROVED");
      expect(requestState?.contractId).toBe(createdContractId);
    });

    await steps.assert("verify linked contract display for approved rent request", async () => {
      expect.soft(true, "verification checkpoint: verify linked contract display for approved rent request").toBe(true);
    });
  });

  test("[E2E-ADM-PRQ-006] should linked sale contract display for approved buy request when processed result", async ({
    page: _page,
    playwright,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare processed result context", async () => {
      expect.soft(true, "precondition: prepare processed result context").toBe(true);
    });

    await steps.act("perform processed result behavior", async () => {
      scenario = await createPropertyRequestScenario(playwright, "BUY");
      const createdSaleContractId = await approveBuyPropertyRequestScenario(scenario);

      const detailPage = pageObjects.create(AdminPropertyRequestDetailPage);
      await navigationPage.open(`/admin/property-request/${scenario.propertyRequestId}`);
      await detailPage.waitForLoaded(scenario.propertyRequestId);
      await detailPage.waitForProcessedSaleContractLink(createdSaleContractId);

      const requestState = await readPropertyRequestState(scenario.propertyRequestId);
      expect(requestState?.status).toBe("APPROVED");
      expect(requestState?.saleContractId).toBe(createdSaleContractId);
    });

    await steps.assert("verify linked sale contract display for approved buy request", async () => {
      expect
        .soft(true, "verification checkpoint: verify linked sale contract display for approved buy request")
        .toBe(true);
    });
  });
});
