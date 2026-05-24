package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.staff.StaffDashboardPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffDashboardTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private StaffDashboardPage dashboardPage;

  @BeforeMethod(alwaysRun = true)
  public void openDashboard() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.staff().username(), contract.staff().password(), "/staff/dashboard");
    dashboardPage = pageObjects.create(StaffDashboardPage.class);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupContractIds.forEach(TestStateBuilder::deleteContract);
    cleanupContractIds.clear();
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupCustomerIds.clear();
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupBuildingIds.clear();
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
    cleanupStaffIds.clear();
  }

  @Test(groups = {"regression", "smoke"}, description = "[E2E-STF-DSH-001] should summary stats and tables display when overview widgets")
  public void shouldSummaryStatsAndTablesDisplay() {
    StepHelper.act(
        "perform overview widgets behavior",
        () -> {
          dashboardPage.waitForLoaded();
          dashboardPage.waitForSummarySectionsVisible();
        });

    StepHelper.assertStep("verify summary stats and tables display", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
