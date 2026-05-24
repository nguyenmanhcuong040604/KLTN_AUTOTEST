package org.suntower.tests.e2e.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.OtpAccessHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.auth.ForgotPasswordPage;
import org.suntower.pages.auth.LoginPage;
import org.suntower.pages.auth.ResetPasswordPage;
import org.testng.annotations.Test;

public class AuthPasswordResetTest extends BaseTest {
  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-RST-001] should valid email reset form navigation when forgot password")
  public void shouldValidEmailResetFormNavigation() {
    CreatedStaff staff = TestStateBuilder.createStaff("STAFF");
    CreatedCustomer customer = TestStateBuilder.createCustomer(staff.id().intValue());
    try {
      StepHelper.act(
          "perform forgot password behavior",
          () -> {
            ForgotPasswordPage forgotPage = pageObjects.create(ForgotPasswordPage.class);
            ResetPasswordPage resetPage = pageObjects.create(ResetPasswordPage.class);
            forgotPage.open();
            forgotPage.waitForLoaded();
            forgotPage.submitEmail(customer.email());
            resetPage.waitForLoaded(customer.email());
          });
      StepHelper.assertStep("verify valid email reset form navigation", () -> assertThat(TestStateBuilder.latestVerificationStatus(customer.email(), "RESET_PASSWORD")).isEqualTo("PENDING"));
    } finally {
      TestStateBuilder.deleteCustomer(customer.id());
      TestStateBuilder.deleteStaff(staff.id());
    }
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-RST-002] should successful OTP reset and new password login when password reset")
  public void shouldSuccessfulOtpResetAndNewPasswordLogin() {
    CreatedStaff staff = TestStateBuilder.createStaff("STAFF");
    CreatedCustomer customer = TestStateBuilder.createCustomer(staff.id().intValue());
    String newPassword = TestDataFactory.AUTH_PASSWORD.resetNewPassword;
    String oldHash = TestStateBuilder.readProfilePasswordHash("customer", customer.id());
    try {
      StepHelper.act(
          "perform password reset behavior",
          () -> {
            ForgotPasswordPage forgotPage = pageObjects.create(ForgotPasswordPage.class);
            ResetPasswordPage resetPage = pageObjects.create(ResetPasswordPage.class);
            LoginPage loginPage = pageObjects.create(LoginPage.class);
            forgotPage.open();
            forgotPage.waitForLoaded();
            forgotPage.submitEmail(customer.email());
            resetPage.waitForLoaded(customer.email());
            resetPage.resetPassword(OtpAccessHelper.latestOtp(customer.email(), "RESET_PASSWORD"), newPassword);
            loginPage.assertLoaded();

            loginPage.login(customer.username(), customer.password());
            loginPage.waitForLoginErrorRoute();
            loginPage.waitForPopupContains("dang nhap that bai|sai tai khoan hoac mat khau|login failed");

            loginPage.open();
            loginPage.assertLoaded();
            loginPage.login(customer.username(), newPassword);
            new org.openqa.selenium.support.ui.WebDriverWait(driver, config.expectTimeout())
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/customer/home"));
          });
      StepHelper.assertStep("verify successful OTP reset and new password login", () -> assertThat(TestStateBuilder.readProfilePasswordHash("customer", customer.id())).isNotEqualTo(oldHash));
    } finally {
      TestStateBuilder.deleteCustomer(customer.id());
      TestStateBuilder.deleteStaff(staff.id());
    }
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-RST-003] should client-side mismatch validation when password confirmation")
  public void shouldClientSideMismatchValidation() {
    String email = TestDataFactory.uniqueEmail("sel-reset-mismatch");

    ResetPasswordPage resetPage =
        StepHelper.arrange(
            "open reset password page",
            () -> {
              ResetPasswordPage page = pageObjects.create(ResetPasswordPage.class);
              page.open(email);
              page.waitForLoaded(email);
              return page;
            });

    StepHelper.act(
        "submit mismatched passwords",
        () ->
            resetPage.resetPassword(
                TestDataFactory.AUTH_PASSWORD.shortOtp,
                TestDataFactory.AUTH_PASSWORD.resetNewPassword,
                TestDataFactory.AUTH_PASSWORD.mismatchConfirmation));

    StepHelper.assertStep(
        "mismatch validation is displayed",
        () -> resetPage.waitForPopupContains("mat khau khong khop|khong khop|passwords do not match"));
  }
}
