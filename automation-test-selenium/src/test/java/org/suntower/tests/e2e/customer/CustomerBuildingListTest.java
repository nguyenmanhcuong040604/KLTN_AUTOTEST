package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.customer.CustomerBuildingListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerBuildingListTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private CustomerBuildingListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openBuildingList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.customer().username(), contract.customer().password(), "/customer/building/list");
    listPage = pageObjects.create(CustomerBuildingListPage.class);
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

  @Test(groups = {"regression"}, description = "[E2E-CUS-BLD-001] should assigned building display when assigned buildings")
  public void shouldAssignedBuildingDisplay() {
    StepHelper.act(
        "perform assigned buildings behavior",
        () -> {
          listPage.waitForBuildingData();
          assertThat(listPage.cardByBuildingName(contract.building().name()).isDisplayed()).isTrue();
        });
    StepHelper.assertStep("verify assigned building display", () -> assertThat(TestStateBuilder.customerRentContractAssignmentExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-BLD-002] should name filter and details modal when building search")
  public void shouldFilterByNameAndOpenDetailsModal() {
    StepHelper.act(
        "perform building search behavior",
        () -> {
          listPage.filterByName(contract.building().name());
          listPage.submitFilters();
          listPage.openBuildingDetail(contract.building().name());
          listPage.waitForDetailModalContains(contract.building().name());
        });
    StepHelper.assertStep("verify name filter and details modal", () -> assertThat(TestStateBuilder.buildingExists(contract.building().id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-BLD-003] should empty state for unmatched search when building search")
  public void shouldShowEmptyStateForUnmatchedSearch() {
    StepHelper.act(
        "perform building search behavior",
        () -> {
          listPage.filterByName(TestDataFactory.uniqueCode("no-match"));
          listPage.submitFilters();
          listPage.waitForEmptyState();
        });
    StepHelper.assertStep("verify empty state for unmatched search", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
