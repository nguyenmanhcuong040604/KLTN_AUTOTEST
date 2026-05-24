package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.OtpAccessHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.profile.StaffProfilePage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerProfileTest extends BaseTest {
  private CreatedCustomer customer;
  private CreatedStaff staff;
  private StaffProfilePage profilePage;

  @BeforeMethod(alwaysRun = true)
  public void openProfile() {
    staff = TestStateBuilder.createStaff("STAFF");
    customer = TestStateBuilder.createCustomer(staff.id().intValue());
    AuthSessionHelper.loginUiAndOpen(driver, customer.username(), customer.password(), "/customer/profile");
    profilePage = pageObjects.create(StaffProfilePage.class);
    profilePage.forRole("customer");
    profilePage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (customer != null) {
      TestStateBuilder.deleteCustomer(customer.id());
      customer = null;
    }
    if (staff != null) {
      TestStateBuilder.deleteStaff(staff.id());
      staff = null;
    }
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-001] should current account information display when profile overview")
  public void shouldCurrentAccountInformationDisplay() {
    StepHelper.act(
        "perform profile overview behavior",
        () -> {
          StaffProfilePage.ProfileValues values = profilePage.readProfileValues();
          assertThat(values.username()).isEqualTo(customer.username());
          assertThat(values.email()).isEqualTo(customer.email());
          assertThat(values.phone()).isEqualTo(TestStateBuilder.readProfilePhone("customer", customer.id()));
        });
    StepHelper.assertStep("verify current account information display", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-002] should success sweetalert display when success message")
  public void shouldSuccessSweetAlertDisplay() {
    StepHelper.act(
        "perform success message behavior",
        () -> {
          driver.navigate().to(config.baseUrl() + "/customer/profile?successMessage=Cap%20nhat%20thanh%20cong");
          profilePage.waitForSweetAlertContains("cap nhat thanh cong|thanh cong|success");
          profilePage.confirmSweetAlertIfPresent();
        });
    StepHelper.assertStep("verify success sweetalert display", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-003] should rejection without password confirmation when username update")
  public void shouldRejectUsernameUpdateWithInvalidOtp() {
    String originalUsername = profilePage.readProfileValues().username();
    StepHelper.act(
        "perform username update behavior",
        () -> {
          profilePage.openUsernameModal();
          profilePage.submitUsernameChange(TestDataFactory.uniqueUsername("cust"), "000000");
          profilePage.waitForSweetAlertContains("loi|that bai|error|mat khau|otp");
          profilePage.confirmSweetAlertIfPresent();
        });
    StepHelper.assertStep(
        "verify rejection without password confirmation",
        () -> {
          assertThat(TestStateBuilder.readProfileUsername("customer", customer.id())).isEqualTo(customer.username());
          assertThat(profilePage.readProfileValues().username()).isEqualTo(originalUsername);
        });
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-004] should successful update with valid OTP when phone number update")
  public void shouldUpdatePhoneWithValidOtp() {
    String nextPhone = TestDataFactory.uniquePhoneNumber();
    StepHelper.act(
        "perform phone number update behavior",
        () -> {
          profilePage.openPhoneModal();
          profilePage.sendOtpFromModal("phone");
          profilePage.waitForSweetAlertContains("OTP|gui ma|success");
          String otp = OtpAccessHelper.latestOtp(customer.email(), "PROFILE_PHONE");
          profilePage.submitPhoneChange(nextPhone, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify successful update with valid OTP", () -> assertThat(TestStateBuilder.readProfilePhone("customer", customer.id())).isEqualTo(nextPhone));
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-005] should client-side mismatch validation when password confirmation")
  public void shouldValidatePasswordConfirmationMismatch() {
    StepHelper.act(
        "perform password confirmation behavior",
        () -> {
          profilePage.submitPasswordChange("ValidPass1!", "DifferentPass1!", "000000");
          profilePage.waitForSweetAlertContains("khong khop|mismatch");
          profilePage.confirmSweetAlertIfPresent();
        });
    StepHelper.assertStep("verify client-side mismatch validation", () -> assertThat(TestStateBuilder.customerExists(customer.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-PRO-006] should successful update with valid OTP and re-login when password update")
  public void shouldUpdatePasswordWithValidOtpAndRelogin() {
    String newPassword = "NewCustomerPwd1!";
    String oldHash = TestStateBuilder.readProfilePasswordHash("customer", customer.id());
    StepHelper.act(
        "perform password update behavior",
        () -> {
          profilePage.openPasswordModal();
          profilePage.sendOtpFromModal("password");
          profilePage.waitForSweetAlertContains("OTP|gui ma|success");
          String otp = OtpAccessHelper.latestOtp(customer.email(), "PROFILE_PASSWORD");
          profilePage.submitPasswordChange(newPassword, newPassword, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep(
        "verify successful update with valid OTP and re-login",
        () -> assertThat(TestStateBuilder.readProfilePasswordHash("customer", customer.id())).isNotEqualTo(oldHash));
  }
}
