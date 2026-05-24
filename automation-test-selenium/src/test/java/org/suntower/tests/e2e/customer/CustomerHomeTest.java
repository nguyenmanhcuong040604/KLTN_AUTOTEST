package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.customer.CustomerHomePage;
import org.suntower.pages.core.NavigationPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerHomeTest extends BaseTest {
  private CreatedStaff manager;
  private CreatedCustomer customer;
  private CustomerHomePage homePage;

  @BeforeMethod(alwaysRun = true)
  public void openHome() {
    manager = TestStateBuilder.createStaff("STAFF");
    customer = TestStateBuilder.createCustomer(manager.id().intValue());
    AuthSessionHelper.loginUiAndOpen(driver, customer.username(), customer.password(), "/customer/home");
    homePage = pageObjects.create(CustomerHomePage.class);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (customer != null) TestStateBuilder.deleteCustomer(customer.id());
    if (manager != null) TestStateBuilder.deleteStaff(manager.id());
  }

  @Test(groups = {"regression", "smoke"}, description = "[E2E-CUS-HOME-001] should dashboard sections display when dashboard overview")
  public void shouldDashboardSectionsDisplay() {
    StepHelper.act(
        "perform dashboard overview behavior",
        () -> {
          homePage.waitForLoaded();
          homePage.waitForDashboardSectionsVisible();
        });
    StepHelper.assertStep("verify dashboard sections display", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }

  @Test(groups = {"regression", "smoke"}, description = "[E2E-CUS-HOME-002] should contracts and buildings navigation when quick navigation")
  public void shouldNavigateContractsAndBuildings() {
    StepHelper.act(
        "perform quick navigation behavior",
        () -> {
          homePage.waitForLoaded();
          homePage.openContracts();
          homePage.waitForContractsRoute();
          new NavigationPage(driver).open("/customer/home");
          homePage.openBuildings();
          homePage.waitForBuildingsRoute();
        });
    StepHelper.assertStep("verify contracts and buildings navigation", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }
}
