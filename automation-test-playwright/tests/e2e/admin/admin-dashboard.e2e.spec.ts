import { expect, test as base } from "@fixtures/base.fixture";
import { AdminDashboardPage } from "@pages/admin/AdminDashboardPage";
import { buildingNameMatches } from "@test-data-scenarios/adminScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Dashboard @regression @smoke", () => {
  let adminUser: StaffProfileState | null = null;
  let BuildingStateId: number | null = null;
  let BuildingStateName = "";

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, navigationPage, steps }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        const BuildingState = await testState.createBuilding("FOR_RENT");
        BuildingStateId = BuildingState.id;
        BuildingStateName = BuildingState.name;

        await steps.arrange("login admin scenario user and open dashboard", async () => {
          await loginAsScenarioUser(page, adminUser!.username, adminUser!.password);
          await navigationPage.open("/admin/dashboard");
        });

        try {
          await use(undefined);
        } finally {
          if (BuildingStateId) {
            await testState.deleteBuilding(BuildingStateId);
          }
          BuildingStateId = null;
          BuildingStateName = "";

          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-DSH-001] should KPI analytics and ranking display when overview widgets", async ({
    page,
    pageObjects,
    steps
  }) => {
    const dashboardPage = pageObjects.create(AdminDashboardPage);
    await steps.arrange("open dashboard with scenario data", async () => {
      await dashboardPage.waitForLoaded();
    });

    await steps.act("load dashboard overview widgets", async () => {
      await dashboardPage.waitForOverviewVisible();
    });

    await steps.assert("admin remains on dashboard", async () => {
      await expect(page).toHaveURL(/\/admin\/dashboard/);
    });
  });

  test("[E2E-ADM-DSH-002] should management navigation when KPI cards", async ({
    page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    const dashboardPage = pageObjects.create(AdminDashboardPage);
    await steps.arrange("load dashboard", async () => {
      await dashboardPage.waitForLoaded();
    });

    await steps.act("open management lists from KPI cards", async () => {
      await dashboardPage.openBuildingsFromStatCard();
      await expect(page).toHaveURL(/\/admin\/building\/list/);

      await navigationPage.open("/admin/dashboard");
      await dashboardPage.openCustomersFromStatCard();
      await expect(page).toHaveURL(/\/admin\/customer\/list/);

      await navigationPage.open("/admin/dashboard");
      await dashboardPage.openStaffsFromStatCard();
      await expect(page).toHaveURL(/\/admin\/staff\/list/);

      await navigationPage.open("/admin/dashboard");
      await dashboardPage.openContractsFromStatCard();
    });

    await steps.assert("contract management list is opened last", async () => {
      await expect(page).toHaveURL(/\/admin\/contract\/list/);
    });
  });

  test("[E2E-ADM-DSH-003] should detail navigation when recent buildings", async ({ page, pageObjects, steps }) => {
    const dashboardPage = pageObjects.create(AdminDashboardPage);
    await steps.arrange("load dashboard with recent building", async () => {
      await dashboardPage.waitForLoaded();
      await dashboardPage.waitForRecentBuildingVisible(BuildingStateName);
    });

    await steps.act("open recent building detail", async () => {
      await dashboardPage.openRecentBuilding(BuildingStateName);
    });

    await steps.assert("building detail page matches seeded building", async () => {
      await expect(page).toHaveURL(new RegExp(`/admin/building/${BuildingStateId}$`));
      await expect.poll(() => buildingNameMatches(BuildingStateId!, BuildingStateName)).toBeTruthy();
    });
  });
});
