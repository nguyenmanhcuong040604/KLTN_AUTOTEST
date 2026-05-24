package org.suntower.tests.e2e.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.OtpAccessHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.pages.auth.LoginPage;
import org.suntower.pages.auth.RegisterCompletePage;
import org.suntower.pages.auth.RegisterPage;
import org.suntower.pages.auth.RegisterVerifyPage;
import org.testng.annotations.Test;

public class AuthRegistrationTest extends BaseTest {
  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-REG-001] should local registration via OTP when registration flow")
  public void shouldLocalRegistrationViaOtp() {
    RegistrationUser user = registrationUser("sel-register");
    try {
      StepHelper.act(
          "perform registration flow behavior",
          () -> completeRegistration(user));

      StepHelper.assertStep(
          "verify local registration via OTP",
          () -> {
            assertThat(TestStateBuilder.customerAccountExists(user.username(), user.email())).isTrue();
            assertThat(TestStateBuilder.latestVerificationStatus(user.email(), "REGISTER")).isEqualTo("USED");
          });
    } finally {
      TestStateBuilder.deleteCustomerByIdentity(user.username(), user.email());
    }
  }

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
      groups = {"regression", "critical"},
      description = "[E2E-AUTH-REG-003] should registered account login from login page when post-registration login")
  public void shouldRegisteredAccountLoginFromLoginPage() {
    RegistrationUser user = registrationUser("sel-register-login");
    LoginPage loginPage = pageObjects.create(LoginPage.class);
    try {
      StepHelper.act(
          "perform post-registration login behavior",
          () -> {
            completeRegistration(user);
            driver.manage().deleteAllCookies();
            loginPage.open();
            loginPage.assertLoaded();
            loginPage.login(user.username(), user.password());
            new org.openqa.selenium.support.ui.WebDriverWait(driver, config.expectTimeout())
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/customer/home"));

            driver.manage().deleteAllCookies();
            loginPage.open();
            loginPage.assertLoaded();
            loginPage.login(user.username(), TestDataFactory.AUTH_PASSWORD.invalidRegisteredPassword);
            loginPage.waitForLoginErrorRoute();
            loginPage.waitForPopupContains("dang nhap that bai|sai tai khoan hoac mat khau|login failed");
          });
      StepHelper.assertStep("verify registered account login from login page", () -> assertThat(TestStateBuilder.customerAccountExists(user.username(), user.email())).isTrue());
    } finally {
      TestStateBuilder.deleteCustomerByIdentity(user.username(), user.email());
    }
  }

  private void completeRegistration(RegistrationUser user) {
    RegisterPage registerPage = pageObjects.create(RegisterPage.class);
    RegisterVerifyPage verifyPage = pageObjects.create(RegisterVerifyPage.class);
    RegisterCompletePage completePage = pageObjects.create(RegisterCompletePage.class);

    registerPage.open();
    registerPage.waitForLoaded();
    registerPage.requestRegistrationCode(user.email());
    verifyPage.waitForLoaded(user.email());
    verifyPage.verifyOtp(OtpAccessHelper.latestOtp(user.email(), "REGISTER"));
    String setupToken = waitForSetupToken(user.email());
    completePage.open(setupToken, user.email());
    completePage.waitForLoaded(user.email());
    completePage.completeRegistration(user.fullName(), user.username(), user.password());
    new org.openqa.selenium.support.ui.WebDriverWait(driver, config.expectTimeout())
        .until(
            ignored ->
                driver.getCurrentUrl().contains("/login?successMessage=")
                    || driver.getCurrentUrl().contains("/customer/home"));
  }

  private String waitForSetupToken(String email) {
    long deadline = System.currentTimeMillis() + config.expectTimeout().toMillis();
    String token = "";
    while (System.currentTimeMillis() < deadline) {
      token = TestStateBuilder.latestVerificationSetupToken(email, "REGISTER");
      if (token != null && !token.isBlank()) {
        return token;
      }
      try {
        Thread.sleep(250);
      } catch (InterruptedException error) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    return token;
  }

  private RegistrationUser registrationUser(String prefix) {
    return new RegistrationUser(
        "SEL Registration " + TestDataFactory.uniqueCode(prefix),
        TestDataFactory.uniqueUsername("selreg"),
        TestDataFactory.AUTH_PASSWORD.registrationDefault,
        TestDataFactory.uniqueEmail(prefix));
  }

  private record RegistrationUser(String fullName, String username, String password, String email) {}
}
