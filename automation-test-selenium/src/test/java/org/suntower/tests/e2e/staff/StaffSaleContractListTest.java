package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedSaleContract;
import org.suntower.pages.staff.StaffSaleContractListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffSaleContractListTest extends BaseTest {
  private final Set<Long> cleanupSaleContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedSaleContract saleContract;
  private StaffSaleContractListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openSaleContractList() {
    saleContract = TestStateBuilder.createSaleContract();
    cleanupSaleContractIds.add(saleContract.id());
    cleanupStaffIds.add(saleContract.staff().id());
    cleanupCustomerIds.add(saleContract.customer().id());
    cleanupBuildingIds.add(saleContract.building().id());

    AuthSessionHelper.loginUiAndOpen(driver, saleContract.staff().username(), saleContract.staff().password(), "/staff/sale-contracts");
    listPage = pageObjects.create(StaffSaleContractListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupSaleContractIds.forEach(TestStateBuilder::deleteSaleContract);
    cleanupSaleContractIds.clear();
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupCustomerIds.clear();
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupBuildingIds.clear();
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
    cleanupStaffIds.clear();
    saleContract = null;
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-SALE-001] should assigned sale contract display when assigned sale contracts")
  public void shouldAssignedSaleContractDisplay() {
    StepHelper.act(
        "perform assigned sale contracts behavior",
        () -> {
          listPage.waitForTableData();
          listPage.waitForRowVisible(saleContract.building().name());
        });

    StepHelper.assertStep(
        "verify assigned sale contract display",
        () -> assertThat(TestStateBuilder.staffSaleContractAssignmentExists(saleContract)).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-SALE-002] should customer and building filtering when sale contract filter")
  public void shouldFilterByCustomerAndBuilding() {
    StepHelper.act(
        "perform sale contract filter behavior",
        () -> {
          listPage.filterByCustomerId(saleContract.customer().id());
          listPage.filterByBuildingId(saleContract.building().id());
          listPage.submitFilters();
          listPage.waitForRowVisible(saleContract.building().name());
        });

    StepHelper.assertStep("verify customer and building filtering", () -> assertThat(TestStateBuilder.saleContractExists(saleContract.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-SALE-003] should details modal display when sale contract details")
  public void shouldOpenDetailsModal() {
    StepHelper.act(
        "perform sale contract details behavior",
        () -> {
          listPage.waitForTableData();
          listPage.openDetail(saleContract.building().name());
          listPage.waitForDetailModalContains(saleContract.customer().fullName());
          listPage.waitForDetailModalContains(saleContract.building().name());
          listPage.closeDetailModal();
        });

    StepHelper.assertStep("verify details modal display", () -> assertThat(TestStateBuilder.saleContractExists(saleContract.id())).isTrue());
  }
}
