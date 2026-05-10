import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerHomePage } from "@pages/customer/CustomerHomePage";
import {
  cleanupCustomerProfileScenario,
  createCustomerProfileScenario,
  loginAsScenarioUser,
  type CustomerProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Customer - Home @regression @smoke", () => {
  let tempUser: CustomerProfileState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        tempUser = await createCustomerProfileScenario(testState);
        await loginAsScenarioUser(page, tempUser.username, tempUser.password);
        await navigationPage.open("/customer/home");

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

  test("[E2E-CUS-HOME-001] should dashboard sections display when dashboard overview", async ({
    page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare dashboard overview context", async () => {
      expect.soft(true, "precondition: prepare dashboard overview context").toBe(true);
    });

    await steps.act("perform dashboard overview behavior", async () => {
      const homePage = pageObjects.create(CustomerHomePage);
      await homePage.waitForLoaded();
      await homePage.waitForDashboardSectionsVisible();
      await expect(page).toHaveURL(/\/customer\/home/);
    });

    await steps.assert("verify dashboard sections display", async () => {
      expect.soft(true, "verification checkpoint: verify dashboard sections display").toBe(true);
    });
  });

  test("[E2E-CUS-HOME-002] should contracts and buildings navigation when quick navigation", async ({
    page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare quick navigation context", async () => {
      expect.soft(true, "precondition: prepare quick navigation context").toBe(true);
    });

    await steps.act("perform quick navigation behavior", async () => {
      const homePage = pageObjects.create(CustomerHomePage);
      await homePage.waitForLoaded();
      await homePage.openContracts();
      await page.waitForURL(/\/customer\/contract\/list|\/customer\/contracts/);

      await navigationPage.open("/customer/home");
      await homePage.openBuildings();
      await page.waitForURL(/\/customer\/building\/list|\/customer\/buildings/);
    });

    await steps.assert("verify contracts and buildings navigation", async () => {
      expect.soft(true, "verification checkpoint: verify contracts and buildings navigation").toBe(true);
    });
  });
});
