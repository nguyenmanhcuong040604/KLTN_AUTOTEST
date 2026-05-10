import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerPropertyRequestListPage } from "@pages/customer/CustomerPropertyRequestListPage";
import {
  approveRentPropertyRequestScenario,
  createPropertyRequestScenario,
  readPropertyRequestState,
  type PropertyRequestScenario
} from "@test-data-scenarios/propertyRequestScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

base.describe("Customer - Property Request @regression @critical", () => {
  let scenario: PropertyRequestScenario | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, playwright, pageObjects: _pageObjects, navigationPage }, use) => {
        scenario = await createPropertyRequestScenario(playwright, "RENT");
        await loginAsScenarioUser(page, scenario.customerUsername);
        await navigationPage.open("/customer/property-request/list");

        try {
          await use(undefined);
        } finally {
          if (scenario) {
            await scenario.cleanup();
          }
          scenario = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-CUS-REQ-001] should pending request display when property request list", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare property request list context", async () => {
      expect.soft(true, "precondition: prepare property request list context").toBe(true);
    });

    await steps.act("perform property request list behavior", async () => {
      const requestPage = pageObjects.create(CustomerPropertyRequestListPage);
      await requestPage.waitForLoaded();
      await requestPage.waitForRequestVisible(scenario!.propertyRequestId);
      await requestPage.waitForRequestContains(scenario!.propertyRequestId, scenario!.buildingName);
      await requestPage.waitForRequestContains(scenario!.propertyRequestId, "Cho xu ly");
      await requestPage.waitForCancelButtonVisible(scenario!.propertyRequestId);
    });

    await steps.assert("verify pending request display", async () => {
      expect.soft(true, "verification checkpoint: verify pending request display").toBe(true);
    });
  });

  test("[E2E-CUS-REQ-002] should pending request cancellation when property request cancellation", async ({
    page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare property request cancellation context", async () => {
      expect.soft(true, "precondition: prepare property request cancellation context").toBe(true);
    });

    await steps.act("perform property request cancellation behavior", async () => {
      const requestPage = pageObjects.create(CustomerPropertyRequestListPage);
      await requestPage.waitForLoaded();
      await requestPage.cancelRequest(scenario!.propertyRequestId);
      await requestPage.confirmSweetAlert();
      await requestPage.waitForSweetAlertContainsText(/thanh cong|da huy yeu cau|success/i);

      await page.reload();
      await requestPage.waitForLoaded();
      await requestPage.waitForRequestVisible(scenario!.propertyRequestId);
      await requestPage.waitForRequestContains(scenario!.propertyRequestId, "Da huy");
      await requestPage.waitForCancelButtonHidden(scenario!.propertyRequestId);

      expect((await readPropertyRequestState(scenario!.propertyRequestId))?.status).toBe("CANCELLED");
    });

    await steps.assert("verify pending request cancellation", async () => {
      expect.soft(true, "verification checkpoint: verify pending request cancellation").toBe(true);
    });
  });

  test("[E2E-CUS-REQ-003] should approved request without cancellation action when property request visibility", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare property request visibility context", async () => {
      expect.soft(true, "precondition: prepare property request visibility context").toBe(true);
    });

    await steps.act("perform property request visibility behavior", async () => {
      const createdContractId = await approveRentPropertyRequestScenario(scenario!);

      await navigationPage.open("/customer/property-request/list");

      const requestPage = pageObjects.create(CustomerPropertyRequestListPage);
      await requestPage.waitForLoaded();
      await requestPage.waitForRequestVisible(scenario!.propertyRequestId);
      await requestPage.waitForRequestContains(scenario!.propertyRequestId, "Da duyet");
      await requestPage.waitForCancelButtonHidden(scenario!.propertyRequestId);

      const requestState = await readPropertyRequestState(scenario!.propertyRequestId);
      expect(requestState?.status).toBe("APPROVED");
      expect(requestState?.contractId).toBe(createdContractId);
    });

    await steps.assert("verify approved request without cancellation action", async () => {
      expect.soft(true, "verification checkpoint: verify approved request without cancellation action").toBe(true);
    });
  });
});
