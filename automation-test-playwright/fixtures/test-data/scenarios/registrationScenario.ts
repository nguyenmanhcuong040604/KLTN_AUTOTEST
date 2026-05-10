import type { Page } from "@playwright/test";
import { Invariant } from "@helpers-validation/Invariant";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import { cleanupTestStateScope } from "@helpers-test-state/TestStateCleanup";
import type { TestStateFixture } from "@helpers-test-state/TestState";
import { latestVerificationStatus } from "@test-data-scenarios/profileScenario";
import { RegisterCompletePage } from "@pages/auth/RegisterCompletePage";
import { RegisterPage } from "@pages/auth/RegisterPage";
import { RegisterVerifyPage } from "@pages/auth/RegisterVerifyPage";

export type RegistrationUser = {
  email: string;
  fullName: string;
  username: string;
  password: string;
};

export function buildRegistrationUser(prefix: string): RegistrationUser {
  return {
    email: TestDataFactory.uniqueEmail(prefix),
    fullName: "E2E Register User",
    username: TestDataFactory.uniqueUsername(prefix),
    password: TestDataFactory.authPassword.registrationDefault
  };
}

export async function completeRegistrationScenario(
  page: Page,
  testState: TestStateFixture,
  user: RegistrationUser
): Promise<void> {
  const registerPage = new RegisterPage(page);
  const verifyPage = new RegisterVerifyPage(page);
  const completePage = new RegisterCompletePage(page);

  await registerPage.open();
  await registerPage.waitForLoaded();
  await registerPage.requestRegistrationCode(user.email);
  await page.waitForURL(new RegExp(`/register/verify\\?email=${encodeURIComponent(user.email)}`));

  const otp = await testState.latestOtp(user.email, "REGISTER");
  await verifyPage.waitForLoaded(user.email);
  await verifyPage.verifyOtp(otp);
  await page.waitForURL(/\/register\/complete\?/);

  await completePage.waitForLoaded(user.email);
  await completePage.completeRegistration(user.fullName, user.username, user.password);
  await page.waitForURL(/\/customer\/home/);
}

export async function cleanupRegistrationUser(user: RegistrationUser): Promise<void> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM customer WHERE email = ? OR username = ?", [
    user.email,
    user.username
  ]);

  await cleanupTestStateScope({
    customerIds: rows.map((row) => row.id),
    emails: [user.email]
  });
}

export async function verifyRegisteredCustomer(user: RegistrationUser): Promise<void> {
  const rows = await MySqlDbClient.query<{ username: string }>(
    "SELECT username FROM customer WHERE email = ? AND username = ?",
    [user.email, user.username]
  );

  Invariant.equal(rows.length, 1, "Registered customer was not found.");
}

export async function verifyNoRegisteredCustomer(user: RegistrationUser): Promise<void> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM customer WHERE email = ? OR username = ?",
    [user.email, user.username]
  );

  Invariant.equal(Number(rows[0]?.count ?? 0), 0, "Customer account still exists.");
}

export async function verifyRegistrationOtpStatus(email: string, status: "PENDING" | "USED"): Promise<void> {
  Invariant.equal(await latestVerificationStatus(email, "REGISTER"), status, "Registration OTP status was unexpected.");
}
