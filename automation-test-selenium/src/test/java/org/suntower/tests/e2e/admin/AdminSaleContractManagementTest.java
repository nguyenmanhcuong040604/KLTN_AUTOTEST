package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.AssignableScenario;
import org.suntower.fixtures.state.TestStateBuilder.CreatedSaleContract;
import org.suntower.fixtures.state.TestStateBuilder.SaleContractRecord;
import org.suntower.pages.admin.AdminSaleContractDetailPage;
import org.suntower.pages.admin.AdminSaleContractFormPage;
import org.suntower.pages.admin.AdminSaleContractListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminSaleContractManagementTest extends BaseTest {
  private final Set<Long> cleanupSaleContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private AdminSaleContractListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openList() {
    adminSession.open("/admin/sale-contract/list");
    listPage = pageObjects.create(AdminSaleContractListPage.class);
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
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-SCT-001] should search and detail view when sale contract search")
  public void shouldSearchAndDetailView() {
    CreatedSaleContract saleContract = arrangeSaleContract();
    StepHelper.act(
        "perform sale contract search behavior",
        () -> {
          adminSession.open(
              "/admin/sale-contract/search?customerId=" + saleContract.customer().id() + "&buildingId=" + saleContract.building().id());
          listPage.waitForLoaded();
          listPage.waitForTableData();
          assertThat(listPage.rowBySaleContractText(saleContract.customer().fullName()).isDisplayed()).isTrue();
          listPage.openDetail(saleContract.customer().fullName());
          pageObjects.create(AdminSaleContractDetailPage.class).waitForLoaded(saleContract.id());
        });
    StepHelper.assertStep("verify search and detail view", () -> assertThat(TestStateBuilder.saleContractExists(saleContract.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-SCT-002] should create sale contract from add form when sale contract creation")
  public void shouldCreateSaleContractFromAddForm() {
    AssignableScenario scenario = arrangeAssignable();
    StepHelper.act(
        "perform sale contract creation behavior",
        () -> {
          AdminSaleContractFormPage formPage = pageObjects.create(AdminSaleContractFormPage.class);
          formPage.openAdd();
          formPage.waitForAddLoaded();
          formPage.selectBuilding(scenario.building().id());
          formPage.selectCustomer(scenario.customer().id());
          formPage.waitForStaffOptions();
          formPage.selectStaff(scenario.staff().id());
          formPage.fillSalePrice(3_600_000_000L);
          formPage.fillNote("Selenium sale contract note");
          formPage.submitSaleContract();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep(
        "verify create sale contract from add form",
        () -> {
          SaleContractRecord created = TestStateBuilder.findCreatedSaleContract(scenario.customer().id(), scenario.building().id());
          assertThat(created).isNotNull();
          cleanupSaleContractIds.add(created.id());
          assertThat(created.salePrice()).isEqualTo(3_600_000_000L);
        });
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-SCT-003] should edit form update when transfer date")
  public void shouldUpdateTransferDate() {
    CreatedSaleContract saleContract = arrangeSaleContract();
    StepHelper.act(
        "perform transfer date behavior",
        () -> {
          AdminSaleContractFormPage formPage = pageObjects.create(AdminSaleContractFormPage.class);
          formPage.openEdit(saleContract.id().intValue());
          formPage.waitForEditLoaded(saleContract.id());
          formPage.fillTransferDate("2026-06-16");
          formPage.submitSaleContract();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep(
        "verify edit form update",
        () -> assertThat(TestStateBuilder.readSaleContractTransferDate(saleContract.id())).isEqualTo("2026-06-16"));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-SCT-004] should earlier than signed date validation when transfer date")
  public void shouldValidateTransferDate() {
    CreatedSaleContract saleContract = arrangeSaleContract();
    String before = TestStateBuilder.readSaleContractTransferDate(saleContract.id());
    StepHelper.act(
        "perform transfer date behavior",
        () -> {
          AdminSaleContractFormPage formPage = pageObjects.create(AdminSaleContractFormPage.class);
          formPage.openEdit(saleContract.id().intValue());
          formPage.waitForEditLoaded(saleContract.id());
          formPage.fillTransferDate("2025-01-01");
          formPage.submitSaleContract();
          formPage.waitForSweetAlertContains("ngay ban giao|khong hop le|transfer date");
        });
    StepHelper.assertStep("verify earlier than signed date validation", () -> assertThat(TestStateBuilder.readSaleContractTransferDate(saleContract.id())).isEqualTo(before));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-SCT-005] should detail page deletion when sale contract deletion")
  public void shouldDetailPageDeletion() {
    CreatedSaleContract saleContract = arrangeSaleContract();
    StepHelper.act(
        "perform sale contract deletion behavior",
        () -> {
          AdminSaleContractDetailPage detailPage = pageObjects.create(AdminSaleContractDetailPage.class);
          detailPage.open(saleContract.id().intValue());
          detailPage.waitForLoaded(saleContract.id());
          detailPage.deleteSaleContract();
          detailPage.confirmSweetAlert();
          detailPage.waitForSweetAlertContains("thanh cong|xoa hop dong mua ban|success");
        });
    StepHelper.assertStep(
        "verify detail page deletion",
        () -> {
          assertThat(TestStateBuilder.saleContractExists(saleContract.id())).isFalse();
          cleanupSaleContractIds.remove(saleContract.id());
        });
  }

  private CreatedSaleContract arrangeSaleContract() {
    CreatedSaleContract saleContract = TestStateBuilder.createSaleContract();
    cleanupSaleContractIds.add(saleContract.id());
    cleanupStaffIds.add(saleContract.staff().id());
    cleanupCustomerIds.add(saleContract.customer().id());
    cleanupBuildingIds.add(saleContract.building().id());
    return saleContract;
  }

  private AssignableScenario arrangeAssignable() {
    AssignableScenario scenario = TestStateBuilder.createAssignableScenario("FOR_SALE");
    cleanupStaffIds.add(scenario.staff().id());
    cleanupCustomerIds.add(scenario.customer().id());
    cleanupBuildingIds.add(scenario.building().id());
    return scenario;
  }
}
