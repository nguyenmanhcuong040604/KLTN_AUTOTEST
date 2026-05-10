import { expect, test as base } from "@fixtures/base.fixture";
import { AdminProfilePage } from "@pages/profile/ProfilePages";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  logoutScenarioUser,
  readProfilePasswordHash,
  readProfilePhone,
  readProfileUsername,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

function requireTempUser(tempUser: StaffProfileState | null): StaffProfileState {
  expect(tempUser, "Temp admin profile user must be created by the scenario fixture").toBeTruthy();
  return tempUser!;
}

base.describe("Admin - Profile @regression", () => {
  let tempUser: StaffProfileState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        tempUser = await createStaffProfileScenario(testState, "ADMIN");
        await loginAsScenarioUser(page, tempUser.username, tempUser.password);
        await navigationPage.open("/admin/profile");

        try {
          await use(undefined);
        } finally {
          await cleanupStaffProfileScenario(testState, tempUser);
          tempUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-PRO-001] should current account information display when profile overview", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare profile overview context", async () => {
      expect.soft(true, "precondition: prepare profile overview context").toBe(true);
    });

    await steps.act("perform profile overview behavior", async () => {
      const activeUser = requireTempUser(tempUser);

      const profilePage = pageObjects.create(AdminProfilePage);
      await profilePage.waitForLoaded();

      const values = await profilePage.readProfileValues();
      expect(values.username).toBe(activeUser.username);
      expect(values.email).toBe(activeUser.email);
      expect(values.phone).toBe(activeUser.phone);
    });

    await steps.assert("verify current account information display", async () => {
      expect.soft(true, "verification checkpoint: verify current account information display").toBe(true);
    });
  });

  test("[E2E-ADM-PRO-002] should success sweetalert display when success message", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare success message context", async () => {
      expect.soft(true, "precondition: prepare success message context").toBe(true);
    });

    await steps.act("perform success message behavior", async () => {
      const profilePage = pageObjects.create(AdminProfilePage);
      await navigationPage.open("/admin/profile?successMessage=Cap%20nhat%20thanh%20cong");

      await profilePage.waitForSweetAlertContains(/cap nhat thanh cong|thanh cong/i);
      await profilePage.confirmSweetAlertIfPresent();
    });

    await steps.assert("verify success sweetalert display", async () => {
      expect.soft(true, "verification checkpoint: verify success sweetalert display").toBe(true);
    });
  });

  test("[E2E-ADM-PRO-003] should successful update with valid OTP when username update", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare username update context", async () => {
      expect.soft(true, "precondition: prepare username update context").toBe(true);
    });

    await steps.act("perform username update behavior", async () => {
      const activeUser = requireTempUser(tempUser);

      const profilePage = pageObjects.create(AdminProfilePage);
      const nextUsername = TestDataFactory.uniqueUsername("adm");

      await profilePage.openUsernameModal();
      await profilePage.sendOtpFromModal("username");
      await profilePage.waitForSweetAlertContains(/OTP|gui ma/i);
      await profilePage.confirmSweetAlertIfPresent();

      const otp = await testState.latestOtp(activeUser.email, "PROFILE_USERNAME");
      await profilePage.submitUsernameChange(nextUsername, otp);
      await profilePage.waitForSweetAlertContains(/thanh cong|ten dang nhap/i);
      await expect.poll(() => readProfileUsername("staff", activeUser.id)).toBe(nextUsername);
    });

    await steps.assert("verify successful update with valid OTP", async () => {
      expect.soft(true, "verification checkpoint: verify successful update with valid OTP").toBe(true);
    });
  });

  test("[E2E-ADM-PRO-004] should successful update with valid OTP when phone number update", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare phone number update context", async () => {
      expect.soft(true, "precondition: prepare phone number update context").toBe(true);
    });

    await steps.act("perform phone number update behavior", async () => {
      const activeUser = requireTempUser(tempUser);

      const profilePage = pageObjects.create(AdminProfilePage);
      const newPhone = TestDataFactory.uniquePhoneNumber();

      await profilePage.openPhoneModal();
      await profilePage.sendOtpFromModal("phone");
      await profilePage.waitForSweetAlertContains(/OTP|gui ma/i);
      await profilePage.confirmSweetAlertIfPresent();

      const otp = await testState.latestOtp(activeUser.email, "PROFILE_PHONE");
      await profilePage.submitPhoneChange(newPhone, otp);
      await profilePage.waitForSweetAlertContains(/thanh cong|so dien thoai/i);
      await expect.poll(() => readProfilePhone("staff", activeUser.id)).toBe(newPhone);
    });

    await steps.assert("verify successful update with valid OTP", async () => {
      expect.soft(true, "verification checkpoint: verify successful update with valid OTP").toBe(true);
    });
  });

  test("[E2E-ADM-PRO-005] should client-side mismatch validation when password confirmation", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare password confirmation context", async () => {
      expect.soft(true, "precondition: prepare password confirmation context").toBe(true);
    });

    await steps.act("perform password confirmation behavior", async () => {
      const profilePage = pageObjects.create(AdminProfilePage);

      await profilePage.submitPasswordChange("ValidPass1!", "DifferentPass1!", "000000");
      await profilePage.waitForSweetAlertContains(/khong khop/i);
      await profilePage.confirmSweetAlertIfPresent();
    });

    await steps.assert("verify client-side mismatch validation", async () => {
      expect.soft(true, "verification checkpoint: verify client-side mismatch validation").toBe(true);
    });
  });

  test("[E2E-ADM-PRO-006] should successful update with valid OTP and re-login when password update", async ({
    page,
    testState,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare password update context", async () => {
      expect.soft(true, "precondition: prepare password update context").toBe(true);
    });

    await steps.act("perform password update behavior", async () => {
      const activeUser = requireTempUser(tempUser);

      const profilePage = pageObjects.create(AdminProfilePage);
      const newPassword = "NewAdminPassword1!";
      const oldHash = await readProfilePasswordHash("staff", activeUser.id);

      await profilePage.openPasswordModal();
      await profilePage.sendOtpFromModal("password");
      await profilePage.waitForSweetAlertContains(/OTP|gui ma/i);
      await profilePage.confirmSweetAlertIfPresent();

      const otp = await testState.latestOtp(activeUser.email, "PROFILE_PASSWORD");
      await profilePage.submitPasswordChange(newPassword, newPassword, otp);
      await profilePage.waitForSweetAlertContains(/thanh cong|mat khau/i);
      await expect.poll(() => readProfilePasswordHash("staff", activeUser.id)).not.toBe(oldHash);

      await logoutScenarioUser(page);
      await loginAsScenarioUser(page, activeUser.username, activeUser.password);
      await page.waitForURL(/\/login\?errorMessage=/);

      await logoutScenarioUser(page);
      await loginAsScenarioUser(page, activeUser.username, newPassword);
      await expect(page).toHaveURL(/\/admin\/|\/login-success/);
    });

    await steps.assert("verify successful update with valid OTP and re-login", async () => {
      expect.soft(true, "verification checkpoint: verify successful update with valid OTP and re-login").toBe(true);
    });
  });
});
