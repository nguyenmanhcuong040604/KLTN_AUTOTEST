package org.suntower.tests.e2e.admin;

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

public class AdminProfileTest extends BaseTest {
  private CreatedStaff admin;
  private StaffProfilePage profilePage;

  @BeforeMethod(alwaysRun = true)
  public void openProfile() {
    admin = TestStateBuilder.createStaff("ADMIN");
    AuthSessionHelper.loginUiAndOpen(driver, admin.username(), admin.password(), "/admin/profile");
    profilePage = pageObjects.create(StaffProfilePage.class);
    profilePage.forRole("admin");
    profilePage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    if (admin != null) TestStateBuilder.deleteStaff(admin.id());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-001] should current account information display when profile overview")
  public void shouldCurrentAccountInformationDisplay() {
    StepHelper.act(
        "perform profile overview behavior",
        () -> assertThat(profilePage.readProfileValues().username()).isEqualTo(admin.username()));
    StepHelper.assertStep("verify current account information display", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-002] should success sweetalert display when success message")
  public void shouldSuccessSweetAlertDisplay() {
    StepHelper.act(
        "perform success message behavior",
        () -> {
          driver.navigate().to(config.baseUrl() + "/admin/profile?successMessage=Cap%20nhat%20thanh%20cong");
          profilePage.waitForSweetAlertContains("cap nhat thanh cong|thanh cong|success");
        });
    StepHelper.assertStep("verify success sweetalert display", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-003] should successful update with valid OTP when username update")
  public void shouldUpdateUsernameWithValidOtp() {
    String nextUsername = TestDataFactory.uniqueUsername("adm");
    StepHelper.act(
        "perform username update behavior",
        () -> {
          profilePage.openUsernameModal();
          profilePage.sendOtpFromModal("username");
          String otp = OtpAccessHelper.latestOtp(admin.email(), "PROFILE_USERNAME");
          profilePage.submitUsernameChange(nextUsername, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify successful update with valid OTP", () -> assertThat(TestStateBuilder.readProfileUsername("staff", admin.id())).isEqualTo(nextUsername));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-004] should successful update with valid OTP when phone number update")
  public void shouldUpdatePhoneWithValidOtp() {
    String phone = TestDataFactory.uniquePhoneNumber();
    StepHelper.act(
        "perform phone number update behavior",
        () -> {
          profilePage.openPhoneModal();
          profilePage.sendOtpFromModal("phone");
          String otp = OtpAccessHelper.latestOtp(admin.email(), "PROFILE_PHONE");
          profilePage.submitPhoneChange(phone, otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify successful update with valid OTP", () -> assertThat(TestStateBuilder.readProfilePhone("staff", admin.id())).isEqualTo(phone));
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-005] should client-side mismatch validation when password confirmation")
  public void shouldValidatePasswordConfirmationMismatch() {
    StepHelper.act(
        "perform password confirmation behavior",
        () -> {
          profilePage.submitPasswordChange("ValidPass1!", "DifferentPass1!", "000000");
          profilePage.waitForSweetAlertContains("khong khop|mismatch");
        });
    StepHelper.assertStep("verify client-side mismatch validation", () -> assertThat(TestStateBuilder.staffExists(admin.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-ADM-PRO-006] should successful update with valid OTP and re-login when password update")
  public void shouldUpdatePasswordWithValidOtp() {
    String oldHash = TestStateBuilder.readProfilePasswordHash("staff", admin.id());
    StepHelper.act(
        "perform password update behavior",
        () -> {
          profilePage.openPasswordModal();
          profilePage.sendOtpFromModal("password");
          String otp = OtpAccessHelper.latestOtp(admin.email(), "PROFILE_PASSWORD");
          profilePage.submitPasswordChange("NewAdminPassword1!", "NewAdminPassword1!", otp);
          profilePage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify successful update with valid OTP and re-login", () -> assertThat(TestStateBuilder.readProfilePasswordHash("staff", admin.id())).isNotEqualTo(oldHash));
  }
}
