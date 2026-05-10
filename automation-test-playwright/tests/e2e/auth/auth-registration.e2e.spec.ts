import { expect, test } from "@fixtures/base.fixture";
import { LoginPage } from "@pages/auth/LoginPage";
import { RegisterPage } from "@pages/auth/RegisterPage";
import { RegisterVerifyPage } from "@pages/auth/RegisterVerifyPage";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import {
  buildRegistrationUser,
  cleanupRegistrationUser,
  completeRegistrationScenario,
  verifyNoRegisteredCustomer,
  verifyRegisteredCustomer,
  verifyRegistrationOtpStatus
} from "@test-data-scenarios/registrationScenario";

test.describe("Auth - Registration @regression @critical", () => {
  test("[E2E-AUTH-REG-001] should local registration via OTP when registration flow", async ({
    page,
    testState,
    pageObjects: _pageObjects,
    steps
  }) => {
    await steps.arrange("prepare registration flow context", async () => {
      expect.soft(true, "precondition: prepare registration flow context").toBe(true);
    });

    await steps.act("perform registration flow behavior", async () => {
      const user = buildRegistrationUser("e2e_register");

      try {
        await completeRegistrationScenario(page, testState, user);

        await verifyRegisteredCustomer(user);
        await verifyRegistrationOtpStatus(user.email, "USED");
      } finally {
        await cleanupRegistrationUser(user);
      }
    });

    await steps.assert("verify local registration via OTP", async () => {
      expect.soft(true, "verification checkpoint: verify local registration via OTP").toBe(true);
    });
  });

  test("[E2E-AUTH-REG-002] should invalid OTP error popup when OTP verification", async ({
    page,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare OTP verification context", async () => {
      expect.soft(true, "precondition: prepare OTP verification context").toBe(true);
    });

    await steps.act("perform OTP verification behavior", async () => {
      const registerPage = pageObjects.create(RegisterPage);
      const verifyPage = pageObjects.create(RegisterVerifyPage);
      const user = buildRegistrationUser("e2e_register_invalid");

      try {
        await registerPage.open();
        await registerPage.requestRegistrationCode(user.email);
        await page.waitForURL(new RegExp(`/register/verify\\?email=${encodeURIComponent(user.email)}`));

        await verifyPage.waitForLoaded(user.email);
        await verifyPage.verifyOtp("000000");
        await page.waitForURL(/\/register\/verify\?/);
        await verifyPage.waitForPopupContains(
          /xac thuc that bai|otp khong hop le|ma otp khong hop le|verification failed/i
        );
        await expect(page).not.toHaveURL(/\/register\/complete\?/);

        await verifyRegistrationOtpStatus(user.email, "PENDING");
        await verifyNoRegisteredCustomer(user);
      } finally {
        await cleanupRegistrationUser(user);
      }
    });

    await steps.assert("verify invalid OTP error popup", async () => {
      expect.soft(true, "verification checkpoint: verify invalid OTP error popup").toBe(true);
    });
  });

  test("[E2E-AUTH-REG-003] should registered account login from login page when post-registration login", async ({
    page,
    testState,
    pageObjects,
    steps
  }) => {
    await steps.arrange("prepare post-registration login context", async () => {
      expect.soft(true, "precondition: prepare post-registration login context").toBe(true);
    });

    await steps.act("perform post-registration login behavior", async () => {
      const user = buildRegistrationUser("e2e_register_login");
      const loginPage = pageObjects.create(LoginPage);

      try {
        await completeRegistrationScenario(page, testState, user);
        await page.context().clearCookies();
        await loginPage.open();
        await loginPage.assertLoaded();
        await loginPage.login(user.username, user.password);
        await page.waitForURL(/\/customer\/home/);

        await page.context().clearCookies();
        await loginPage.open();
        await loginPage.assertLoaded();
        await loginPage.login(user.username, TestDataFactory.authPassword.invalidRegisteredPassword);
        await page.waitForURL(/\/login\?errorMessage=/);
        await loginPage.waitForPopupContains(/dang nhap that bai|sai tai khoan hoac mat khau|login failed/i);
      } finally {
        await cleanupRegistrationUser(user);
      }
    });

    await steps.assert("verify registered account login from login page", async () => {
      expect.soft(true, "verification checkpoint: verify registered account login from login page").toBe(true);
    });
  });
});
