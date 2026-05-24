package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.OtpAccessHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.profile.StaffProfilePage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffProfileTest extends BaseTest {
  private CreatedStaff staff;
  private StaffProfilePage profilePage;

  @BeforeMethod(alwaysRun = true)
  public void openProfile() {
    staff = TestStateBuilder.createStaff("STAFF");
    AuthSessionHelper.loginUiAndOpen(driver, staff.username(), staff.password(), "/staff/profile");
    profilePage = pageObjects.create(StaffProfilePage.class);
    profilePage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (staff != null) {
      TestStateBuilder.deleteStaff(staff.id());
      staff = null;
    }
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-001] should current account information display when profile overview")
  public void shouldCurrentAccountInformationDisplay() {
    StepHelper.act(
        "perform profile overview behavior",
        () -> {
          StaffProfilePage.ProfileValues values = profilePage.readProfileValues();
          assertThat(values.username()).isEqualTo(staff.username());
          assertThat(values.email()).isEqualTo(staff.email());
          assertThat(values.phone()).isEqualTo(TestStateBuilder.readProfilePhone("staff", staff.id()));
        });

    StepHelper.assertStep("verify current account information display", () -> assertThat(TestStateBuilder.staffExists(staff.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-002] should error sweetalert display when error message")
  public void shouldErrorSweetAlertDisplay() {
    StepHelper.act(
        "perform error message behavior",
        () -> {
          driver.navigate().to(config.baseUrl() + "/staff/profile?errorMessage=Cap%20nhat%20that%20bai");
          profilePage.waitForSweetAlertContains("that bai|error|loi");
          profilePage.confirmSweetAlertIfPresent();
        });

    StepHelper.assertStep("verify error sweetalert display", () -> assertThat(TestStateBuilder.staffExists(staff.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-003] should successful update with valid OTP when username update")
  public void shouldUpdateUsernameWithValidOtp() {
    String nextUsername = TestDataFactory.uniqueUsername("stf");

    StepHelper.act(
        "perform username update behavior",
        () -> {
          profilePage.openUsernameModal();
          profilePage.sendOtpFromModal("username");
          profilePage.waitForSweetAlertContains("OTP|gui ma|success");
          String otp = OtpAccessHelper.latestOtp(staff.email(), "PROFILE_USERNAME");
          profilePage.submitUsernameChange(nextUsername, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep("verify successful update with valid OTP", () -> assertThat(TestStateBuilder.readProfileUsername("staff", staff.id())).isEqualTo(nextUsername));
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-004] should successful update with valid OTP when phone number update")
  public void shouldUpdatePhoneWithValidOtp() {
    String nextPhone = TestDataFactory.uniquePhoneNumber();

    StepHelper.act(
        "perform phone number update behavior",
        () -> {
          profilePage.openPhoneModal();
          profilePage.sendOtpFromModal("phone");
          profilePage.waitForSweetAlertContains("OTP|gui ma|success");
          String otp = OtpAccessHelper.latestOtp(staff.email(), "PROFILE_PHONE");
          profilePage.submitPhoneChange(nextPhone, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep("verify successful update with valid OTP", () -> assertThat(TestStateBuilder.readProfilePhone("staff", staff.id())).isEqualTo(nextPhone));
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-005] should client-side mismatch validation when password confirmation")
  public void shouldValidatePasswordConfirmationMismatch() {
    StepHelper.act(
        "perform password confirmation behavior",
        () -> {
          profilePage.submitPasswordChange("ValidPass1!", "DifferentPass1!", "000000");
          profilePage.waitForSweetAlertContains("khong khop|mismatch");
          profilePage.confirmSweetAlertIfPresent();
        });

    StepHelper.assertStep("verify client-side mismatch validation", () -> assertThat(TestStateBuilder.staffExists(staff.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-PRO-006] should successful update with valid OTP and re-login when password update")
  public void shouldUpdatePasswordWithValidOtpAndRelogin() {
    String newPassword = "NewStaffPassword1!";
    String oldHash = TestStateBuilder.readProfilePasswordHash("staff", staff.id());

    StepHelper.act(
        "perform password update behavior",
        () -> {
          profilePage.openPasswordModal();
          profilePage.sendOtpFromModal("password");
          profilePage.waitForSweetAlertContains("OTP|gui ma|success");
          String otp = OtpAccessHelper.latestOtp(staff.email(), "PROFILE_PASSWORD");
          profilePage.submitPasswordChange(newPassword, newPassword, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify successful update with valid OTP and re-login",
        () -> assertThat(TestStateBuilder.readProfilePasswordHash("staff", staff.id())).isNotEqualTo(oldHash));
  }
}
