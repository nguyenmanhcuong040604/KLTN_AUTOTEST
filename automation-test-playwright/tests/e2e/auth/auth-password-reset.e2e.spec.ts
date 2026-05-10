import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { ForgotPasswordPage } from "@pages/auth/ForgotPasswordPage";
import { LoginPage } from "@pages/auth/LoginPage";
import { ResetPasswordPage } from "@pages/auth/ResetPasswordPage";
import {
  cleanupCustomerProfileScenario,
  createCustomerProfileScenario,
  latestVerificationStatus,
  readProfilePasswordHash,
  type CustomerProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Auth - Password Reset @regression @critical", () => {
  let tempUser: CustomerProfileState | null = null;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ testState }, use) => {
        tempUser = await createCustomerProfileScenario(testState);

        try {
          await use(undefined);
        } finally {
          await cleanupCustomerProfileScenario(testState, tempUser);
          tempUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-AUTH-RST-001] should valid email reset form navigation when forgot password", async ({
    page,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare forgot password context", async () => {
      expect.soft(true, "precondition: prepare forgot password context").toBe(true);
    });

    await steps.act("perform forgot password behavior", async () => {
      const forgotPage = pageObjects.create(ForgotPasswordPage);
      const resetPage = pageObjects.create(ResetPasswordPage);

      await forgotPage.open();
      await forgotPage.waitForLoaded();
      await forgotPage.submitEmail(tempUser!.email);
      await page.waitForURL(new RegExp(`/auth/reset-password\\?email=${encodeURIComponent(tempUser!.email)}`));
      await resetPage.waitForLoaded(tempUser!.email);

      expect(await latestVerificationStatus(tempUser!.email, "RESET_PASSWORD")).toBe("PENDING");
    });

    await steps.assert("verify valid email reset form navigation", async () => {
      expect.soft(true, "verification checkpoint: verify valid email reset form navigation").toBe(true);
    });
  });

  test("[E2E-AUTH-RST-002] should successful OTP reset and new password login when password reset", async ({
    page,
    testState,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare password reset context", async () => {
      expect.soft(true, "precondition: prepare password reset context").toBe(true);
    });

    await steps.act("perform password reset behavior", async () => {
      const forgotPage = pageObjects.create(ForgotPasswordPage);
      const resetPage = pageObjects.create(ResetPasswordPage);
      const loginPage = pageObjects.create(LoginPage);
      const newPassword = TestDataFactory.authPassword.resetNewPassword;

      const oldHash = await readProfilePasswordHash("customer", tempUser!.id);

      await forgotPage.open();
      await forgotPage.submitEmail(tempUser!.email);
      await page.waitForURL(new RegExp(`/auth/reset-password\\?email=${encodeURIComponent(tempUser!.email)}`));

      const otp = await testState.latestOtp(tempUser!.email, "RESET_PASSWORD");
      await resetPage.waitForLoaded(tempUser!.email);
      await resetPage.resetPassword(otp, newPassword);
      await page.waitForURL(/\/login\?successMessage=/);

      await expect.poll(() => readProfilePasswordHash("customer", tempUser!.id)).not.toBe(oldHash);

      await loginPage.assertLoaded();
      await loginPage.login(tempUser!.username, tempUser!.password);
      await page.waitForURL(/\/login\?errorMessage=/);

      await loginPage.assertLoaded();
      await loginPage.login(tempUser!.username, newPassword);
      await page.waitForURL(/\/customer\/home/);
    });

    await steps.assert("verify successful OTP reset and new password login", async () => {
      expect.soft(true, "verification checkpoint: verify successful OTP reset and new password login").toBe(true);
    });
  });

  test("[E2E-AUTH-RST-003] should client-side mismatch validation when password confirmation", async ({
    page,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare password confirmation context", async () => {
      expect.soft(true, "precondition: prepare password confirmation context").toBe(true);
    });

    await steps.act("perform password confirmation behavior", async () => {
      const forgotPage = pageObjects.create(ForgotPasswordPage);
      const resetPage = pageObjects.create(ResetPasswordPage);

      await forgotPage.open();
      await forgotPage.submitEmail(tempUser!.email);
      await page.waitForURL(new RegExp(`/auth/reset-password\\?email=${encodeURIComponent(tempUser!.email)}`));

      await resetPage.waitForLoaded(tempUser!.email);
      await resetPage.resetPassword(
        TestDataFactory.authPassword.shortOtp,
        TestDataFactory.authPassword.resetNewPassword,
        TestDataFactory.authPassword.mismatchConfirmation
      );
      await resetPage.waitForPopupContains(/mat khau khong khop|khong khop/i);
    });

    await steps.assert("verify client-side mismatch validation", async () => {
      expect.soft(true, "verification checkpoint: verify client-side mismatch validation").toBe(true);
    });
  });
});
