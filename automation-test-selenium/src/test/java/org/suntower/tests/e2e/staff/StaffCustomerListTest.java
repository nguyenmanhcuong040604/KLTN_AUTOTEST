package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.staff.StaffCustomerListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffCustomerListTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private StaffCustomerListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openCustomerList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.staff().username(), contract.staff().password(), "/staff/customers");
    listPage = pageObjects.create(StaffCustomerListPage.class);
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

  @Test(groups = {"regression"}, description = "[E2E-STF-CUS-001] should assigned customer display when assigned customers")
  public void shouldAssignedCustomerDisplay() {
    StepHelper.act(
        "perform assigned customers behavior",
        () -> {
          listPage.waitForTableData();
          assertThat(listPage.rowByCustomerName(contract.customer().fullName()).isDisplayed()).isTrue();
        });

    StepHelper.assertStep("verify assigned customer display", () -> assertThat(TestStateBuilder.staffRentContractAssignmentExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-CUS-002] should search and details modal when customer search")
  public void shouldSearchAndOpenDetailsModal() {
    StepHelper.act(
        "perform customer search behavior",
        () -> {
          listPage.filterByFullName(contract.customer().fullName());
          listPage.submitFilters();
          listPage.openCustomerDetail(contract.customer().fullName());
          listPage.waitForDetailModalContains(contract.customer().fullName());
        });

    StepHelper.assertStep("verify search and details modal", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
