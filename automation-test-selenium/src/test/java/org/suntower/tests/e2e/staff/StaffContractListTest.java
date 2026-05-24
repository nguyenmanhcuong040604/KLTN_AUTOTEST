package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.staff.StaffContractListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffContractListTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private StaffContractListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openContractList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());

    AuthSessionHelper.loginUiAndOpen(driver, contract.staff().username(), contract.staff().password(), "/staff/contracts");
    listPage = pageObjects.create(StaffContractListPage.class);
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
    contract = null;
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-CTR-001] should assigned contract display when assigned contracts")
  public void shouldAssignedContractDisplay() {
    StepHelper.act(
        "perform assigned contracts behavior",
        () -> {
          listPage.waitForTableData();
          assertThat(listPage.rowByContractText(contract.customer().fullName()).isDisplayed()).isTrue();
        });

    StepHelper.assertStep(
        "verify assigned contract display",
        () -> assertThat(TestStateBuilder.staffRentContractAssignmentExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-CTR-002] should customer building and status filtering when contract filter")
  public void shouldFilterByCustomerBuildingAndStatus() {
    StepHelper.act(
        "perform contract filter behavior",
        () -> {
          listPage.filterByCustomer(contract.customer().id());
          listPage.filterByBuilding(contract.building().id());
          listPage.filterByStatus("ACTIVE");
          listPage.submitFilters();
          assertThat(listPage.rowByContractText(contract.customer().fullName()).isDisplayed()).isTrue();
        });

    StepHelper.assertStep("verify customer building and status filtering", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-CTR-003] should details modal from list when contract details")
  public void shouldOpenDetailsModalFromList() {
    StepHelper.act(
        "perform contract details behavior",
        () -> {
          listPage.waitForTableData();
          listPage.openContractDetail(contract.customer().fullName());
          listPage.waitForDetailModalContains(contract.customer().fullName());
        });

    StepHelper.assertStep("verify details modal from list", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
