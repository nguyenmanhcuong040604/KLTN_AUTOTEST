package org.suntower.tests.e2e.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.auth.LoginPage;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AuthLoginTest extends BaseTest {
  @Test(
      groups = {"regression", "smoke", "critical"},
      description = "[E2E-AUTH-LOGIN-001] should registration and forgot password navigation when login navigation")
  public void shouldRegistrationAndForgotPasswordNavigation() {
    LoginPage loginPage =
        StepHelper.arrange(
            "open login page",
            () -> {
              LoginPage page = pageObjects.create(LoginPage.class);
              page.open();
              page.assertLoaded();
              return page;
            });

    StepHelper.act(
        "navigate to registration page",
        () -> {
          loginPage.clickRegister();
          loginPage.waitForRegistrationRoute();
        });

    StepHelper.assertStep(
        "registration route is opened",
        () -> assertThat(driver.getCurrentUrl()).contains("/register"));

    StepHelper.act(
        "navigate to forgot password page",
        () -> {
          loginPage.open();
          loginPage.clickForgotPassword();
          loginPage.waitForForgotPasswordRoute();
        });

    StepHelper.assertStep(
        "forgot password route is opened",
        () -> assertThat(driver.getCurrentUrl()).contains("/forgot-password"));
  }

  @Test(
      groups = {"regression", "smoke", "critical"},
      description = "[E2E-AUTH-LOGIN-002] should invalid credentials error popup when login credentials")
  public void shouldInvalidCredentialsErrorPopup() {
    LoginPage loginPage =
        StepHelper.arrange(
            "open login page",
            () -> {
              LoginPage page = pageObjects.create(LoginPage.class);
              page.open();
              page.assertLoaded();
              return page;
            });

    StepHelper.act(
        "submit invalid credentials",
        () -> {
          loginPage.login(
              TestDataFactory.AUTH_IDENTITY.unknownUsername,
              TestDataFactory.AUTH_PASSWORD.invalidLoginPassword);
          loginPage.waitForLoginErrorRoute();
        });

    StepHelper.assertStep(
        "error popup is displayed",
        () -> loginPage.waitForPopupContains("dang nhap that bai|sai tai khoan hoac mat khau|tai khoan khong ton tai|login failed"));
  }

  @Test(
      groups = {"regression", "smoke", "critical"},
      description = "[E2E-AUTH-LOGIN-003] should valid local customer redirect when login submission")
  public void shouldValidLocalCustomerRedirect() {
    CreatedStaff staff = TestStateBuilder.createStaff("STAFF");
    CreatedCustomer customer = TestStateBuilder.createCustomer(staff.id().intValue());
    try {
      StepHelper.act(
          "login with scenario customer",
          () -> {
            LoginPage loginPage = pageObjects.create(LoginPage.class);
            loginPage.open();
            loginPage.assertLoaded();
            loginPage.login(customer.username(), customer.password());
            new WebDriverWait(driver, config.expectTimeout()).until(ExpectedConditions.urlContains("/customer/home"));
          });

      StepHelper.assertStep("customer is redirected to home", () -> assertThat(driver.getCurrentUrl()).contains("/customer/home"));
    } finally {
      TestStateBuilder.deleteCustomer(customer.id());
      TestStateBuilder.deleteStaff(staff.id());
    }
  }
}
