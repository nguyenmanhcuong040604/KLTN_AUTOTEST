package org.suntower.tests.e2e.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.pages.auth.RegisterCompletePage;
import org.suntower.pages.auth.RegisterPage;
import org.suntower.pages.auth.RegisterVerifyPage;
import org.testng.annotations.Test;

public class AuthRegistrationTest extends BaseTest {
  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-REG-002] should invalid OTP error popup when OTP verification")
  public void shouldInvalidOtpErrorPopup() {
    String email = TestDataFactory.uniqueEmail("sel-register-invalid");

    RegisterVerifyPage verifyPage =
        StepHelper.act(
            "request registration code",
            () -> {
              RegisterPage registerPage = pageObjects.create(RegisterPage.class);
              registerPage.open();
              registerPage.waitForLoaded();
              registerPage.requestRegistrationCode(email);
              RegisterVerifyPage page = pageObjects.create(RegisterVerifyPage.class);
              page.waitForLoaded(email);
              return page;
            });

    StepHelper.act(
        "submit invalid otp",
        () -> {
          verifyPage.verifyOtp("000000");
          verifyPage.waitForPopupContains("xac thuc that bai|otp khong hop le|ma otp khong hop le|verification failed");
        });

    StepHelper.assertStep(
        "user remains on verify page",
        () -> assertThat(driver.getCurrentUrl()).contains("/register/verify"));
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-AUTH-REG-ROUTE] should registration auth pages load directly")
  public void shouldRegistrationPagesLoadDirectly() {
    String email = TestDataFactory.uniqueEmail("sel-route");

    StepHelper.act(
        "open register page",
        () -> {
          RegisterPage registerPage = pageObjects.create(RegisterPage.class);
          registerPage.open();
          registerPage.waitForLoaded();
        });

    StepHelper.act(
        "open verify page",
        () -> {
          RegisterVerifyPage verifyPage = pageObjects.create(RegisterVerifyPage.class);
          verifyPage.open(email);
          verifyPage.waitForLoaded(email);
        });

    StepHelper.act(
        "open complete page",
        () -> {
          RegisterCompletePage completePage = pageObjects.create(RegisterCompletePage.class);
          completePage.open("invalid-ticket-for-route-check", email);
          completePage.waitForLoaded(email);
        });
  }
}
