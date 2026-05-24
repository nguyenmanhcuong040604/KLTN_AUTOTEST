package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.customer.CustomerContractListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerContractListTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private CustomerContractListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openContractList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.customer().username(), contract.customer().password(), "/customer/contract/list");
    listPage = pageObjects.create(CustomerContractListPage.class);
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

  @Test(groups = {"regression"}, description = "[E2E-CUS-CTR-001] should current contract display when current contracts")
  public void shouldCurrentContractDisplay() {
    StepHelper.act(
        "perform current contracts behavior",
        () -> {
          listPage.waitForContractData();
          assertThat(listPage.cardByBuildingName(contract.building().name()).isDisplayed()).isTrue();
        });
    StepHelper.assertStep("verify current contract display", () -> assertThat(TestStateBuilder.customerActiveContractExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-CTR-002] should building and status filtering when contract filter")
  public void shouldFilterByBuildingAndStatus() {
    StepHelper.act(
        "perform contract filter behavior",
        () -> {
          listPage.filterByBuilding(contract.building().id());
          listPage.filterByStatus("ACTIVE");
          listPage.submitFilters();
          assertThat(listPage.cardByBuildingName(contract.building().name()).isDisplayed()).isTrue();
        });
    StepHelper.assertStep("verify building and status filtering", () -> assertThat(TestStateBuilder.customerActiveContractExists(contract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-CTR-003] should empty state for unmatched criteria when contract filter")
  public void shouldShowEmptyStateForUnmatchedCriteria() {
    StepHelper.act(
        "perform contract filter behavior",
        () -> {
          listPage.filterByStatus("EXPIRED");
          listPage.submitFilters();
          listPage.waitForEmptyState();
        });
    StepHelper.assertStep("verify empty state for unmatched criteria", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }
}
