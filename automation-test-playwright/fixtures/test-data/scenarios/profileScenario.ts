import type { Page } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import type { TestStateFixture } from "@helpers-test-state/TestState";
import { LoginPage } from "@pages/auth/LoginPage";
import { AuthSessionHelper } from "@helpers-auth/AuthSessionHelper";

export type CustomerProfileState = {
  id: number;
  username: string;
  email: string;
  phone: string;
  password: string;
  managerStaffId: number;
};

export type StaffProfileState = {
  id: number;
  username: string;
  email: string;
  phone: string;
  password: string;
};

const defaultPassword = env.defaultPassword;
type ProfileAccountTable = "staff" | "customer";

async function fetchCustomerIdentity(id: number): Promise<{ email: string; phone: string }> {
  const rows = await MySqlDbClient.query<{ email: string; phone: string }>(
    "SELECT email, phone FROM customer WHERE id = ? LIMIT 1",
    [id]
  );
  const row = rows[0];
  if (!row) {
    throw new Error(`Khong tim thay customer temp voi id=${id}.`);
  }

  return row;
}

async function fetchStaffIdentity(id: number): Promise<{ email: string; phone: string }> {
  const rows = await MySqlDbClient.query<{ email: string; phone: string }>(
    "SELECT email, phone FROM staff WHERE id = ? LIMIT 1",
    [id]
  );
  const row = rows[0];
  if (!row) {
    throw new Error(`Khong tim thay staff temp voi id=${id}.`);
  }

  return row;
}

export async function createCustomerProfileScenario(testState: TestStateFixture): Promise<CustomerProfileState> {
  const manager = await testState.createStaff("STAFF");
  const customer = await testState.createCustomer(manager.id);
  const identity = await fetchCustomerIdentity(customer.id);

  return {
    id: customer.id,
    username: customer.username,
    email: identity.email,
    phone: identity.phone,
    password: defaultPassword,
    managerStaffId: manager.id
  };
}

export async function cleanupCustomerProfileScenario(
  testState: TestStateFixture,
  tempUser: CustomerProfileState | null
): Promise<void> {
  if (!tempUser) {
    return;
  }

  await testState.deleteCustomer(tempUser.id);
  await testState.deleteStaff(tempUser.managerStaffId);
}

export async function createStaffProfileScenario(
  testState: TestStateFixture,
  role: "STAFF" | "ADMIN"
): Promise<StaffProfileState> {
  const staff = await testState.createStaff(role);
  const identity = await fetchStaffIdentity(staff.id);

  return {
    id: staff.id,
    username: staff.username,
    email: identity.email,
    phone: identity.phone,
    password: defaultPassword
  };
}

export async function cleanupStaffProfileScenario(
  testState: TestStateFixture,
  tempUser: StaffProfileState | null
): Promise<void> {
  if (!tempUser) {
    return;
  }

  await testState.deleteStaff(tempUser.id);
}

export async function loginAsScenarioUser(page: Page, username: string, password = defaultPassword): Promise<void> {
  const loginPage = new LoginPage(page);
  await loginPage.open();
  await loginPage.login(username, password);
}

export async function logoutScenarioUser(page: Page): Promise<void> {
  await AuthSessionHelper.logoutUi(page);
}

export async function readProfileUsername(table: ProfileAccountTable, userId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ username: string }>(`SELECT username FROM ${table} WHERE id = ?`, [userId]);
  return rows[0]?.username ?? "";
}

export async function readProfilePhone(table: ProfileAccountTable, userId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ phone: string }>(`SELECT phone FROM ${table} WHERE id = ?`, [userId]);
  return rows[0]?.phone ?? "";
}

export async function readProfilePasswordHash(table: ProfileAccountTable, userId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ password: string }>(`SELECT password FROM ${table} WHERE id = ?`, [userId]);
  return rows[0]?.password ?? "";
}

export async function latestVerificationStatus(email: string, purpose: string): Promise<string> {
  const rows = await MySqlDbClient.query<{ status: string }>(
    `
      SELECT status
      FROM email_verification
      WHERE email = ? AND purpose = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [email, purpose]
  );

  return rows[0]?.status ?? "";
}
