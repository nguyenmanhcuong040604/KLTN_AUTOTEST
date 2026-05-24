package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.PropertyRequestScenario;
import org.suntower.pages.core.NavigationPage;
import org.suntower.pages.customer.CustomerPropertyRequestListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerPropertyRequestTest extends BaseTest {
  private PropertyRequestScenario scenario;
  private Long linkedContractId;
  private CustomerPropertyRequestListPage requestPage;

  @BeforeMethod(alwaysRun = true)
  public void openRequestList() {
    scenario = TestStateBuilder.createPropertyRequestScenario("RENT");
    AuthSessionHelper.loginUiAndOpen(driver, scenario.customer().username(), scenario.customer().password(), "/customer/property-request/list");
    requestPage = pageObjects.create(CustomerPropertyRequestListPage.class);
    requestPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (scenario != null) {
      TestStateBuilder.deletePropertyRequest(scenario.id());
      TestStateBuilder.deleteContract(linkedContractId);
      TestStateBuilder.deleteCustomer(scenario.customer().id());
      TestStateBuilder.deleteBuilding(scenario.building().id());
      TestStateBuilder.deleteStaff(scenario.staff().id());
      scenario = null;
      linkedContractId = null;
    }
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-REQ-001] should pending request display when property request list")
  public void shouldPendingRequestDisplay() {
    StepHelper.act(
        "perform property request list behavior",
        () -> {
          requestPage.waitForRequestVisible(scenario.id());
          requestPage.waitForRequestContains(scenario.id(), scenario.building().name());
          requestPage.waitForRequestContains(scenario.id(), "Cho xu ly");
          requestPage.waitForCancelButtonVisible(scenario.id());
        });
    StepHelper.assertStep("verify pending request display", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).status()).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-REQ-002] should pending request cancellation when property request cancellation")
  public void shouldCancelPendingRequest() {
    StepHelper.act(
        "perform property request cancellation behavior",
        () -> {
          requestPage.cancelRequest(scenario.id());
          requestPage.waitForSweetAlertContainsText("thanh cong|da huy yeu cau|success");
          new NavigationPage(driver).open("/customer/property-request/list");
          requestPage.waitForLoaded();
          requestPage.waitForRequestVisible(scenario.id());
          requestPage.waitForRequestContains(scenario.id(), "Da huy");
          requestPage.waitForCancelButtonHidden(scenario.id());
        });
    StepHelper.assertStep("verify pending request cancellation", () -> assertThat(TestStateBuilder.readPropertyRequestState(scenario.id()).status()).isEqualTo("CANCELLED"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-REQ-003] should approved request without cancellation action when property request visibility")
  public void shouldDisplayApprovedRequestWithoutCancellationAction() {
    linkedContractId = TestStateBuilder.approveRentPropertyRequest(scenario);
    StepHelper.act(
        "perform property request visibility behavior",
        () -> {
          new NavigationPage(driver).open("/customer/property-request/list");
          requestPage.waitForLoaded();
          requestPage.waitForRequestVisible(scenario.id());
          requestPage.waitForRequestContains(scenario.id(), "Da duyet");
          requestPage.waitForCancelButtonHidden(scenario.id());
        });
    StepHelper.assertStep(
        "verify approved request without cancellation action",
        () -> {
          TestStateBuilder.PropertyRequestState state = TestStateBuilder.readPropertyRequestState(scenario.id());
          assertThat(state.status()).isEqualTo("APPROVED");
          assertThat(state.contractId()).isEqualTo(linkedContractId);
        });
  }
}
