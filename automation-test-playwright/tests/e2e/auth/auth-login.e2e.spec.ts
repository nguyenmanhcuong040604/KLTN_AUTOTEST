import { expect, test } from "@fixtures/base.fixture";
import { LoginPage } from "@pages/auth/LoginPage";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import {
  cleanupCustomerProfileScenario,
  createCustomerProfileScenario,
  type CustomerProfileState
} from "@test-data-scenarios/profileScenario";

test.describe("Auth - Login @regression @smoke @critical", () => {
  let tempUser: CustomerProfileState | null = null;

  test.afterEach(async ({ testState }) => {
    await cleanupCustomerProfileScenario(testState, tempUser);
    tempUser = null;
  });

  test("[E2E-AUTH-LOGIN-001] should registration and forgot password navigation when login navigation", async ({
    page,
    pageObjects,
    steps
  }) => {
    const loginPage = await steps.arrange("open login page", async () => {
      const pageObject = pageObjects.create(LoginPage);
      await pageObject.open();
      await pageObject.assertLoaded();
      return pageObject;
    });

    await steps.act("navigate to registration page", async () => {
      await loginPage.clickRegister();
      await page.waitForURL(/\/register/);
    });

    await steps.assert("registration route is opened", async () => {
      await expect(page).toHaveURL(/\/register/);
    });

    await steps.act("navigate to forgot password page", async () => {
      await loginPage.open();
      await loginPage.clickForgotPassword();
      await page.waitForURL(/\/forgot-password/);
    });

    await steps.assert("forgot password route is opened", async () => {
      await expect(page).toHaveURL(/\/forgot-password/);
    });
  });

  test("[E2E-AUTH-LOGIN-002] should invalid credentials error popup when login credentials", async ({
    page,
    pageObjects,
    steps
  }) => {
    const loginPage = await steps.arrange("open login page", async () => {
      const pageObject = pageObjects.create(LoginPage);
      await pageObject.open();
      await pageObject.assertLoaded();
      return pageObject;
    });

    await steps.act("submit invalid credentials", async () => {
      await loginPage.login(
        TestDataFactory.authIdentity.unknownUsername,
        TestDataFactory.authPassword.invalidLoginPassword
      );
      await page.waitForURL(/\/login\?errorMessage=/);
    });

    await steps.assert("error popup is displayed", async () => {
      await loginPage.waitForPopupContains(
        /dang nhap that bai|sai tai khoan hoac mat khau|tai khoan khong ton tai|login failed/i
      );
    });
  });

  test("[E2E-AUTH-LOGIN-003] should valid local customer redirect when login submission", async ({
    page,
    pageObjects,
    testState,
    steps
  }) => {
    tempUser = await steps.arrange("create local customer account", async () =>
      createCustomerProfileScenario(testState)
    );

    await steps.act("login with scenario customer", async () => {
      const loginPage = pageObjects.create(LoginPage);
      await loginPage.open();
      await loginPage.login(tempUser!.username, tempUser!.password);
      await page.waitForURL(/\/customer\/home/);
    });

    await steps.assert("customer is redirected to home", async () => {
      await expect(page).toHaveURL(/\/customer\/home/);
    });
  });
});
