package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.fixtures.state.TestStateBuilder.PropertyRequestScenario;
import org.suntower.pages.admin.AdminContractFormPage;
import org.suntower.pages.admin.AdminPropertyRequestDetailPage;
import org.suntower.pages.admin.AdminPropertyRequestListPage;
import org.suntower.pages.admin.AdminSaleContractFormPage;
import org.suntower.pages.core.NavigationPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminPropertyRequestManagementTest extends BaseTest {
  private CreatedStaff admin;
  private PropertyRequestScenario scenario;
  private Long linkedContractId;
  private Long linkedSaleContractId;
  private NavigationPage navigationPage;

  @BeforeMethod(alwaysRun = true)
  public void openRequestList() {
    admin = TestStateBuilder.createStaff("ADMIN");
    AuthSessionHelper.loginUiAndOpen(driver, admin.username(), admin.password(), "/admin/property-request/list");
    navigationPage = new NavigationPage(driver);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (scenario != null) {
      TestStateBuilder.deletePropertyRequest(scenario.id());
      TestStateBuilder.deleteContract(linkedContractId);
      TestStateBuilder.deleteSaleContract(linkedSaleContractId);
      TestStateBuilder.deleteCustomer(scenario.customer().id());
      TestStateBuilder.deleteBuilding(scenario.building().id());
      TestStateBuilder.deleteStaff(scenario.staff().id());
      scenario = null;
      linkedContractId = null;
      linkedSaleContractId = null;
    }
    if (admin != null) {
      TestStateBuilder.deleteStaff(admin.id());
      admin = null;
    }
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-001] should pending request filter and detail view when request filter")
  public void shouldFilterPendingRequestAndOpenDetail() {
    scenario = TestStateBuilder.createPropertyRequestScenario("RENT");
    AdminPropertyRequestListPage listPage = pageObjects.create(AdminPropertyRequestListPage.class);
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    StepHelper.act(
        "perform request filter behavior",
        () -> {
          navigationPage.open("/admin/property-request/list");
          listPage.waitForLoaded();
          listPage.filterByStatus("PENDING");
          listPage.waitForRowVisible(scenario.id());
          listPage.openDetail(scenario.id());
          detailPage.waitForLoaded(scenario.id());
        });
    StepHelper.assertStep("verify pending request filter and detail view", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).status()).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-002] should pending request rejection with reason when request rejection")
  public void shouldRejectPendingRequestWithReason() {
    scenario = TestStateBuilder.createPropertyRequestScenario("RENT");
    String reason = "Rejected by Selenium E2E";
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    StepHelper.act(
        "perform request rejection behavior",
        () -> {
          navigationPage.open("/admin/property-request/" + scenario.id());
          detailPage.waitForLoaded(scenario.id());
          detailPage.waitForPendingActionsVisible();
          detailPage.rejectRequest(scenario.id(), reason);
          detailPage.waitForRejectAlertVisible();
        });
    StepHelper.assertStep(
        "verify pending request rejection with reason",
        () -> {
          TestStateBuilder.PropertyRequestState state = TestStateBuilder.readPropertyRequestState(scenario.id());
          assertThat(state.status()).isEqualTo("REJECTED");
          assertThat(state.adminNote()).isEqualTo(reason);
        });
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-003] should prefilled contract form navigation when rent request detail")
  public void shouldNavigateToPrefilledRentContractForm() {
    scenario = TestStateBuilder.createPropertyRequestScenario("RENT");
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    AdminContractFormPage contractFormPage = pageObjects.create(AdminContractFormPage.class);
    StepHelper.act(
        "perform rent request detail behavior",
        () -> {
          navigationPage.open("/admin/property-request/" + scenario.id());
          detailPage.waitForLoaded(scenario.id());
          detailPage.waitForCreateContractLink(scenario.id());
          detailPage.openCreateContractLink(scenario.id());
          contractFormPage.waitForAddLoaded();
          detailPage.waitForPrefilledCustomer(scenario.customer().id());
        });
    StepHelper.assertStep("verify prefilled contract form navigation", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).status()).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-004] should prefilled sale contract form navigation when buy request detail")
  public void shouldNavigateToPrefilledSaleContractForm() {
    scenario = TestStateBuilder.createPropertyRequestScenario("BUY");
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    AdminSaleContractFormPage saleFormPage = pageObjects.create(AdminSaleContractFormPage.class);
    StepHelper.act(
        "perform buy request detail behavior",
        () -> {
          navigationPage.open("/admin/property-request/" + scenario.id());
          detailPage.waitForLoaded(scenario.id());
          detailPage.waitForCreateSaleContractLink(scenario.id());
          detailPage.openCreateSaleContractLink(scenario.id());
          saleFormPage.waitForAddLoaded();
          detailPage.waitForPrefilledCustomer(scenario.customer().id());
        });
    StepHelper.assertStep("verify prefilled sale contract form navigation", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).status()).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-005] should linked contract display for approved rent request when processed result")
  public void shouldDisplayLinkedContractForApprovedRentRequest() {
    scenario = TestStateBuilder.createPropertyRequestScenario("RENT");
    linkedContractId = TestStateBuilder.approveRentPropertyRequest(scenario);
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    StepHelper.act(
        "perform processed result behavior",
        () -> {
          navigationPage.open("/admin/property-request/" + scenario.id());
          detailPage.waitForLoaded(scenario.id());
          detailPage.waitForProcessedContractLink(linkedContractId);
        });
    StepHelper.assertStep("verify linked contract display for approved rent request", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).contractId()).isEqualTo(linkedContractId));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRQ-006] should linked sale contract display for approved buy request when processed result")
  public void shouldDisplayLinkedSaleContractForApprovedBuyRequest() {
    scenario = TestStateBuilder.createPropertyRequestScenario("BUY");
    linkedSaleContractId = TestStateBuilder.approveBuyPropertyRequest(scenario);
    AdminPropertyRequestDetailPage detailPage = pageObjects.create(AdminPropertyRequestDetailPage.class);
    StepHelper.act(
        "perform processed result behavior",
        () -> {
          navigationPage.open("/admin/property-request/" + scenario.id());
          detailPage.waitForLoaded(scenario.id());
          detailPage.waitForProcessedSaleContractLink(linkedSaleContractId);
        });
    StepHelper.assertStep("verify linked sale contract display for approved buy request", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).saleContractId()).isEqualTo(linkedSaleContractId));
  }
}
