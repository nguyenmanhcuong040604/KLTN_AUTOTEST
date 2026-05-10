package org.suntower.tests.e2e.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.pages.auth.ForgotPasswordPage;
import org.suntower.pages.auth.ResetPasswordPage;
import org.testng.annotations.Test;

public class AuthPasswordResetTest extends BaseTest {
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

  @Test(
      groups = {"regression"},
      description = "[E2E-AUTH-RST-ROUTE] should forgot and reset password pages load directly")
  public void shouldPasswordResetPagesLoadDirectly() {
    String email = TestDataFactory.uniqueEmail("sel-reset-route");

    StepHelper.act(
        "open forgot password page",
        () -> {
          ForgotPasswordPage forgotPage = pageObjects.create(ForgotPasswordPage.class);
          forgotPage.open();
          forgotPage.waitForLoaded();
        });

    StepHelper.act(
        "open reset password page",
        () -> {
          ResetPasswordPage resetPage = pageObjects.create(ResetPasswordPage.class);
          resetPage.open(email);
          resetPage.waitForLoaded(email);
        });

    StepHelper.assertStep(
        "reset route is opened",
        () -> assertThat(driver.getCurrentUrl()).contains("/auth/reset-password"));
  }
}
