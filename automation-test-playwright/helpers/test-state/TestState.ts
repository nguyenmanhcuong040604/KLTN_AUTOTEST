import type { APIRequestContext, APIResponse } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { TestAccountResolver, type TestUserRole } from "@helpers-accounts/TestAccountResolver";
import { TestStateBuilder } from "@helpers-test-state/TestStateBuilder";
import { OtpAccessHelper } from "@helpers-test-state/TestStateOtp";


type RequestContextFactory = {
  request: {
    newContext: (options?: Record<string, unknown>) => Promise<APIRequestContext>;
  };
};

export type TestStateFixture = {
  createStaff: (role?: "STAFF" | "ADMIN") => ReturnType<typeof TestStateBuilder.createStaff>;
  deleteStaff: (id?: number) => ReturnType<typeof TestStateBuilder.deleteStaff>;
  createCustomer: (staffId?: number) => ReturnType<typeof TestStateBuilder.createCustomer>;
  deleteCustomer: (id?: number) => ReturnType<typeof TestStateBuilder.deleteCustomer>;
  createBuilding: (transactionType?: "FOR_RENT" | "FOR_SALE") => ReturnType<typeof TestStateBuilder.createBuilding>;
  deleteBuilding: (id?: number) => ReturnType<typeof TestStateBuilder.deleteBuilding>;
  updateStaffBuildingAssignments: (
    staffId: number,
    buildingIds: number[]
  ) => ReturnType<typeof TestStateBuilder.updateStaffBuildingAssignments>;
  updateStaffCustomerAssignments: (
    staffId: number,
    customerIds: number[]
  ) => ReturnType<typeof TestStateBuilder.updateStaffCustomerAssignments>;
  createContract: () => ReturnType<typeof TestStateBuilder.createContract>;
  deleteContract: (
    state?: Awaited<ReturnType<typeof TestStateBuilder.createContract>>
  ) => ReturnType<typeof TestStateBuilder.deleteContract>;
  createSaleContract: () => ReturnType<typeof TestStateBuilder.createSaleContract>;
  deleteSaleContract: (
    state?: Awaited<ReturnType<typeof TestStateBuilder.createSaleContract>>
  ) => ReturnType<typeof TestStateBuilder.deleteSaleContract>;
  deleteContractById: (id?: number) => ReturnType<typeof TestStateBuilder.deleteContractById>;
  deleteSaleContractById: (id?: number) => ReturnType<typeof TestStateBuilder.deleteSaleContractById>;
  createInvoice: (
    contract: Awaited<ReturnType<typeof TestStateBuilder.createContract>>,
    payload: Record<string, unknown>
  ) => ReturnType<typeof TestStateBuilder.createInvoice>;
  deleteInvoice: (id?: number) => ReturnType<typeof TestStateBuilder.deleteInvoice>;
  deleteAdditionalInfoRecord: (
    type: "legal" | "amenity" | "planning" | "supplier",
    id?: number
  ) => ReturnType<typeof TestStateBuilder.deleteAdditionalInfoRecord>;
  latestOtp: (email: string, purpose: string) => Promise<string>;
};

export class TestStateSession {
  static usernameCandidates(role: TestUserRole): string[] {
    return TestAccountResolver.usernameCandidates(role);
  }

  static async usernameMatchesRole(role: TestUserRole, username: string): Promise<boolean> {
    return TestAccountResolver.usernameMatchesRole(role, username);
  }

  static async login(
    request: APIRequestContext,
    username: string,
    password = env.defaultPassword
  ): Promise<APIResponse> {
    return request.post("/api/v1/auth/login", {
      failOnStatusCode: false,
      data: { username, password }
    });
  }

  static async loginAsRole(
    request: APIRequestContext,
    role: TestUserRole,
    password = env.defaultPassword
  ): Promise<{ response: APIResponse; username: string }> {
    const candidates = this.usernameCandidates(role);

    for (const username of candidates) {
      if (!(await this.usernameMatchesRole(role, username))) {
        continue;
      }

      const response = await this.login(request, username, password);
      if (response.status() === 200) {
        TestAccountResolver.remember(role, username);
        return { response, username };
      }
    }

    throw new Error(`Khong tao duoc request context dang nhap voi role ${role}. Da thu: ${candidates.join(", ")}.`);
  }

  static async newContext(playwright: RequestContextFactory, role?: TestUserRole): Promise<APIRequestContext> {
    const context = await playwright.request.newContext({
      baseURL: env.baseUrl,
      extraHTTPHeaders: { Accept: "application/json" }
    });

    if (role) {
      await this.loginAsRole(context, role);
    }

    return context;
  }

  static async newContextForUser(
    playwright: RequestContextFactory,
    username: string,
    password = env.defaultPassword
  ): Promise<APIRequestContext> {
    const context = await this.newContext(playwright);
    const response = await this.login(context, username, password);

    if (response.status() !== 200) {
      await context.dispose();
      throw new Error(`Khong tao duoc request context cho user ${username}. HTTP ${response.status()}.`);
    }

    return context;
  }
}

export function attachTestStateHelpers(context: APIRequestContext): TestStateFixture {
  return {
    createStaff: (role = "STAFF") => TestStateBuilder.createStaff(context, role),
    deleteStaff: (id) => TestStateBuilder.deleteStaff(context, id),
    createCustomer: (staffId) => TestStateBuilder.createCustomer(context, staffId),
    deleteCustomer: (id) => TestStateBuilder.deleteCustomer(context, id),
    createBuilding: (transactionType) => TestStateBuilder.createBuilding(context, transactionType),
    deleteBuilding: (id) => TestStateBuilder.deleteBuilding(context, id),
    updateStaffBuildingAssignments: (staffId, buildingIds) =>
      TestStateBuilder.updateStaffBuildingAssignments(context, staffId, buildingIds),
    updateStaffCustomerAssignments: (staffId, customerIds) =>
      TestStateBuilder.updateStaffCustomerAssignments(context, staffId, customerIds),
    createContract: () => TestStateBuilder.createContract(context),
    deleteContract: (state) => TestStateBuilder.deleteContract(context, state),
    createSaleContract: () => TestStateBuilder.createSaleContract(context),
    deleteSaleContract: (state) => TestStateBuilder.deleteSaleContract(context, state),
    deleteContractById: (id) => TestStateBuilder.deleteContractById(context, id),
    deleteSaleContractById: (id) => TestStateBuilder.deleteSaleContractById(context, id),
    createInvoice: (contract, payload) => TestStateBuilder.createInvoice(context, contract, payload),
    deleteInvoice: (id) => TestStateBuilder.deleteInvoice(context, id),
    deleteAdditionalInfoRecord: (type, id) => TestStateBuilder.deleteAdditionalInfoRecord(context, type, id),
    latestOtp: (email, purpose) => OtpAccessHelper.latestOtp(context, email, purpose)
  };
}
