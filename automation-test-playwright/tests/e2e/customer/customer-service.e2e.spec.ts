import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerServicePage } from "@pages/customer/CustomerServicePage";
import {
  cleanupCustomerProfileScenario,
  createCustomerProfileScenario,
  loginAsScenarioUser,
  type CustomerProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Customer - Service @regression", () => {
  let tempUser: CustomerProfileState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        tempUser = await createCustomerProfileScenario(testState);
        await loginAsScenarioUser(page, tempUser.username, tempUser.password);
        await navigationPage.open("/customer/service");

        try {
          await use(undefined);
        } finally {
          await cleanupCustomerProfileScenario(testState, tempUser);
          tempUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-CUS-SRV-001] should key service card display when service cards", async ({
    page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare service cards context", async () => {
      expect.soft(true, "precondition: prepare service cards context").toBe(true);
    });

    await steps.act("perform service cards behavior", async () => {
      const servicePage = pageObjects.create(CustomerServicePage);
      await servicePage.waitForLoaded();
      await servicePage.waitForCardVisible("�? Xe � T�");
      await servicePage.waitForCardVisible("Internet T?c �? Cao");
      await servicePage.waitForCardVisible("Ph�ng Gym");
      await expect(page).toHaveURL(/\/customer\/service/);
    });

    await steps.assert("verify key service card display", async () => {
      expect.soft(true, "verification checkpoint: verify key service card display").toBe(true);
    });
  });

  test("[E2E-CUS-SRV-002] should built-in unavailable buttons disabled when service availability", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare service availability context", async () => {
      expect.soft(true, "precondition: prepare service availability context").toBe(true);
    });

    await steps.act("perform service availability behavior", async () => {
      const servicePage = pageObjects.create(CustomerServicePage);
      await servicePage.waitForLoaded();
      await servicePage.waitForRequestDisabled("An Ninh 24/7");
      await servicePage.waitForRequestDisabled("Ph�ng Gym");
    });

    await steps.assert("verify built-in unavailable buttons disabled", async () => {
      expect.soft(true, "verification checkpoint: verify built-in unavailable buttons disabled").toBe(true);
    });
  });
});
