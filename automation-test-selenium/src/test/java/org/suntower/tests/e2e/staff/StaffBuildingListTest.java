package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.staff.StaffBuildingListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffBuildingListTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private StaffBuildingListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openBuildingList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.staff().username(), contract.staff().password(), "/staff/buildings");
    listPage = pageObjects.create(StaffBuildingListPage.class);
    listPage.waitForLoaded();
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

  @Test(groups = {"regression"}, description = "[E2E-STF-BLD-001] should assigned building display when assigned buildings")
  public void shouldAssignedBuildingDisplay() {
    StepHelper.act(
        "perform assigned buildings behavior",
        () -> {
          listPage.waitForBuildingData();
          assertThat(listPage.cardByBuildingName(contract.building().name()).isDisplayed()).isTrue();
        });

    StepHelper.assertStep("verify assigned building display", () -> assertThat(TestStateBuilder.staffRentContractAssignmentExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-BLD-002] should name filter and details modal when building search")
  public void shouldFilterByNameAndOpenDetailsModal() {
    StepHelper.act(
        "perform building search behavior",
        () -> {
          listPage.filterByName(contract.building().name());
          listPage.submitFilters();
          listPage.openBuildingDetail(contract.building().name());
          listPage.waitForDetailModalContains(contract.building().name());
        });

    StepHelper.assertStep("verify name filter and details modal", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
