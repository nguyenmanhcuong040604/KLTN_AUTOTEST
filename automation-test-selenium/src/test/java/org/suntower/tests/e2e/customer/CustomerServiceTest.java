package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.customer.CustomerServicePage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerServiceTest extends BaseTest {
  private CreatedStaff manager;
  private CreatedCustomer customer;
  private CustomerServicePage servicePage;

  @BeforeMethod(alwaysRun = true)
  public void openService() {
    manager = TestStateBuilder.createStaff("STAFF");
    customer = TestStateBuilder.createCustomer(manager.id().intValue());
    AuthSessionHelper.loginUiAndOpen(driver, customer.username(), customer.password(), "/customer/service");
    servicePage = pageObjects.create(CustomerServicePage.class);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (customer != null) TestStateBuilder.deleteCustomer(customer.id());
    if (manager != null) TestStateBuilder.deleteStaff(manager.id());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-SRV-001] should key service card display when service cards")
  public void shouldDisplayKeyServiceCards() {
    StepHelper.act(
        "perform service cards behavior",
        () -> {
          servicePage.waitForLoaded();
          servicePage.waitForCardVisible("Xe O To");
          servicePage.waitForCardVisible("Internet");
          servicePage.waitForCardVisible("Gym");
        });
    StepHelper.assertStep("verify key service card display", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-SRV-002] should built-in unavailable buttons disabled when service availability")
  public void shouldUnavailableButtonsDisabled() {
    StepHelper.act(
        "perform service availability behavior",
        () -> {
          servicePage.waitForLoaded();
          servicePage.waitForRequestDisabled("An Ninh");
          servicePage.waitForRequestDisabled("Gym");
        });
    StepHelper.assertStep("verify built-in unavailable buttons disabled", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }
}
