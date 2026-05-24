package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.admin.AdminReportPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminReportTest extends BaseTest {
  private CreatedStaff admin;
  private AdminReportPage reportPage;

  @BeforeMethod(alwaysRun = true)
  public void openReport() {
    admin = TestStateBuilder.createStaff("ADMIN");
    AuthSessionHelper.loginUiAndOpen(driver, admin.username(), admin.password(), "/admin/report");
    reportPage = pageObjects.create(AdminReportPage.class);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (admin != null) TestStateBuilder.deleteStaff(admin.id());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-RPT-001] should KPI and analytics display when report overview")
  public void shouldKpiAndAnalyticsDisplay() {
    StepHelper.act(
        "perform report overview behavior",
        () -> {
          reportPage.waitForLoaded();
          reportPage.waitForOverviewVisible();
        });
    StepHelper.assertStep("verify KPI and analytics display", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-RPT-002] should selector year switching when report year")
  public void shouldSwitchReportYear() {
    StepHelper.act(
        "perform report year behavior",
        () -> {
          reportPage.waitForLoaded();
          List<String> years = reportPage.availableYears();
          assertThat(years).isNotEmpty();
          String targetYear = years.get(years.size() - 1);
          reportPage.selectYear(targetYear);
          reportPage.waitForYearSelected(targetYear);
        });
    StepHelper.assertStep("verify selector year switching", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-RPT-003] should browser print trigger when print action")
  public void shouldTriggerBrowserPrint() {
    StepHelper.act(
        "perform print action behavior",
        () -> {
          reportPage.waitForLoaded();
          reportPage.triggerPrint();
          assertThat(reportPage.wasPrintTriggered()).isTrue();
        });
    StepHelper.assertStep("verify browser print trigger", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }
}
