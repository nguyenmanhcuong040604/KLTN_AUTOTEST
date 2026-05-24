package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedBuilding;
import org.suntower.pages.admin.AdminDashboardPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminDashboardTest extends BaseTest {
  private AdminDashboardPage dashboardPage;
  private CreatedBuilding recentBuilding;

  @BeforeMethod(alwaysRun = true)
  public void openDashboard() {
    recentBuilding = TestStateBuilder.createBuilding("FOR_RENT");
    adminSession.open("/admin/dashboard");
    dashboardPage = pageObjects.create(AdminDashboardPage.class);
    dashboardPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupDashboardScenario() {
    if (recentBuilding != null) {
      TestStateBuilder.deleteBuilding(recentBuilding.id());
      recentBuilding = null;
    }
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-ADM-DSH-001] should KPI analytics and ranking display when overview widgets")
  public void shouldKpiAnalyticsAndRankingDisplay() {
    StepHelper.act("load dashboard overview widgets", () -> dashboardPage.waitForOverviewVisible());

    StepHelper.assertStep(
        "admin remains on dashboard",
        () -> assertThat(driver.getCurrentUrl()).contains("/admin/dashboard"));
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-ADM-DSH-002] should management navigation when KPI cards")
  public void shouldManagementNavigationWhenKpiCards() {
    StepHelper.act(
        "open management lists from KPI cards",
        () -> {
          dashboardPage.openBuildingsFromStatCard();
          assertThat(driver.getCurrentUrl()).contains("/admin/building/list");

          dashboardPage.open();
          dashboardPage.waitForLoaded();
          dashboardPage.openCustomersFromStatCard();
          assertThat(driver.getCurrentUrl()).contains("/admin/customer/list");

          dashboardPage.open();
          dashboardPage.waitForLoaded();
          dashboardPage.openStaffsFromStatCard();
          assertThat(driver.getCurrentUrl()).contains("/admin/staff/list");

          dashboardPage.open();
          dashboardPage.waitForLoaded();
          dashboardPage.openContractsFromStatCard();
        });

    StepHelper.assertStep(
        "contract management list is opened last",
        () -> assertThat(driver.getCurrentUrl()).contains("/admin/contract/list"));
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-ADM-DSH-003] should detail navigation when recent buildings")
  public void shouldDetailNavigationWhenRecentBuildings() {
    StepHelper.arrange(
        "load dashboard with recent building",
        () -> {
          dashboardPage.waitForLoaded();
          dashboardPage.waitForRecentBuildingVisible(recentBuilding.name());
        });

    StepHelper.act("open recent building detail", () -> dashboardPage.openRecentBuilding(recentBuilding.name()));

    StepHelper.assertStep(
        "building detail page matches seeded building",
        () -> {
          assertThat(driver.getCurrentUrl()).endsWith("/admin/building/" + recentBuilding.id());
          assertThat(TestStateBuilder.buildingNameMatches(recentBuilding.id(), recentBuilding.name())).isTrue();
        });
  }
}
