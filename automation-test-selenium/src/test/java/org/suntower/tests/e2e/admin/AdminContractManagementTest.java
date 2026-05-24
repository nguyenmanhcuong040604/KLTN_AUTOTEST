package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.AssignableScenario;
import org.suntower.fixtures.state.TestStateBuilder.ContractEditState;
import org.suntower.fixtures.state.TestStateBuilder.ContractRecord;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.admin.AdminContractDetailPage;
import org.suntower.pages.admin.AdminContractFormPage;
import org.suntower.pages.admin.AdminContractListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminContractManagementTest extends BaseTest {
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private AdminContractListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openContractList() {
    adminSession.open("/admin/contract/list");
    listPage = pageObjects.create(AdminContractListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupScenarios() {
    cleanupContractIds.forEach(TestStateBuilder::deleteContract);
    cleanupContractIds.clear();
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupCustomerIds.clear();
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupBuildingIds.clear();
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
    cleanupStaffIds.clear();
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-001] should search and detail view when contract search")
  public void shouldSearchAndDetailView() {
    CreatedContract contract = arrangeContract();

    StepHelper.act(
        "perform contract search behavior",
        () -> {
          adminSession.open("/admin/contract/search?customerId=" + contract.customer().id() + "&buildingId=" + contract.building().id());
          listPage.waitForLoaded();
          listPage.waitForTableData();
          assertThat(listPage.rowByContractText(contract.customer().fullName()).isDisplayed()).isTrue();
          listPage.openDetail(contract.customer().fullName());
          pageObjects.create(AdminContractDetailPage.class).waitForLoaded(contract.id());
        });

    StepHelper.assertStep("verify search and detail view", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-002] should create contract from add form when contract creation")
  public void shouldCreateContractFromAddForm() {
    AssignableScenario scenario = arrangeAssignable("FOR_RENT");

    StepHelper.act(
        "perform contract creation behavior",
        () -> {
          AdminContractFormPage formPage = pageObjects.create(AdminContractFormPage.class);
          formPage.openAdd();
          formPage.waitForAddLoaded();
          formPage.selectBuilding(scenario.building().id());
          formPage.waitForRentAreaOptions();
          formPage.selectCustomer(scenario.customer().id());
          formPage.waitForStaffOptions();
          formPage.selectRentArea("50");
          formPage.selectStaff(scenario.staff().id());
          formPage.fillRentPrice(1_450_000);
          formPage.fillDates("2026-06-01", "2026-12-31");
          formPage.submitContract();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify create contract from add form",
        () -> {
          ContractRecord created = TestStateBuilder.findCreatedContract(scenario.customer().id(), scenario.building().id());
          assertThat(created).isNotNull();
          cleanupContractIds.add(created.id());
          assertThat(created.rentPrice()).isEqualTo(1_450_000);
          assertThat(created.startDate()).isEqualTo("2026-06-01");
          assertThat(created.endDate()).isEqualTo("2026-12-31");
        });
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-003] should invalid date range validation when contract dates")
  public void shouldValidateInvalidDateRange() {
    AssignableScenario scenario = arrangeAssignable("FOR_RENT");

    StepHelper.act(
        "perform contract dates behavior",
        () -> {
          AdminContractFormPage formPage = pageObjects.create(AdminContractFormPage.class);
          formPage.openAdd();
          formPage.waitForAddLoaded();
          formPage.selectBuilding(scenario.building().id());
          formPage.waitForRentAreaOptions();
          formPage.selectCustomer(scenario.customer().id());
          formPage.waitForStaffOptions();
          formPage.selectRentArea("50");
          formPage.selectStaff(scenario.staff().id());
          formPage.fillRentPrice(1_500_000);
          formPage.fillDates("2026-09-01", "2026-08-01");
          formPage.submitContract();
          formPage.waitForSweetAlertContains("ngay ket thuc|canh bao|warning");
        });

    StepHelper.assertStep(
        "verify invalid date range validation",
        () -> assertThat(TestStateBuilder.contractWithPriceExists(scenario.customer().id(), scenario.building().id(), 1_500_000)).isFalse());
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-004] should active contract update when contract edit")
  public void shouldUpdateActiveContract() {
    CreatedContract contract = arrangeContract();

    StepHelper.act(
        "perform contract edit behavior",
        () -> {
          AdminContractFormPage formPage = pageObjects.create(AdminContractFormPage.class);
          formPage.openEdit(contract.id().intValue());
          formPage.waitForEditLoaded(contract.id());
          formPage.fillDates("2026-01-15", "2026-11-30");
          formPage.fillRentPrice(2_500_000);
          formPage.selectStatus("ACTIVE");
          formPage.submitContract();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify active contract update",
        () -> {
          ContractEditState state = TestStateBuilder.readContractEditState(contract.id());
          assertThat(state.rentPrice()).isEqualTo(2_500_000);
          assertThat(state.endDate()).isEqualTo("2026-11-30");
          assertThat(state.status()).isEqualTo("ACTIVE");
        });
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-005] should expired contract lock banner display when contract edit lock")
  public void shouldExpiredContractLockBannerDisplay() {
    CreatedContract contract = arrangeContract();
    TestStateBuilder.expireContract(contract.id());

    StepHelper.act(
        "perform contract edit lock behavior",
        () -> {
          AdminContractFormPage formPage = pageObjects.create(AdminContractFormPage.class);
          formPage.openEdit(contract.id().intValue());
          formPage.waitForEditLoaded(contract.id());
          formPage.waitForExpiredBanner();
        });

    StepHelper.assertStep("verify expired contract lock banner display", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-CTR-006] should detail page deletion when contract deletion")
  public void shouldDetailPageDeletion() {
    CreatedContract contract = arrangeContract();

    StepHelper.act(
        "perform contract deletion behavior",
        () -> {
          AdminContractDetailPage detailPage = pageObjects.create(AdminContractDetailPage.class);
          detailPage.open(contract.id().intValue());
          detailPage.waitForLoaded(contract.id());
          detailPage.deleteContract();
          detailPage.confirmSweetAlert();
          detailPage.waitForSweetAlertContains("thanh cong|xoa hop dong|success");
        });

    StepHelper.assertStep(
        "verify detail page deletion",
        () -> {
          assertThat(TestStateBuilder.contractExists(contract.id())).isFalse();
          cleanupContractIds.remove(contract.id());
        });
  }

  private CreatedContract arrangeContract() {
    CreatedContract contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    return contract;
  }

  private AssignableScenario arrangeAssignable(String transactionType) {
    AssignableScenario scenario = TestStateBuilder.createAssignableScenario(transactionType);
    cleanupStaffIds.add(scenario.staff().id());
    cleanupCustomerIds.add(scenario.customer().id());
    cleanupBuildingIds.add(scenario.building().id());
    return scenario;
  }
}
