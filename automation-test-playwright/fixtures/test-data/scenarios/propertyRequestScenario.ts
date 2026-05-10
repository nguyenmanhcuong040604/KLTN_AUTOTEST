import type { APIRequestContext } from "@playwright/test";
import { Invariant } from "@helpers-validation/Invariant";
import { TestStateSession } from "@helpers-test-state/TestState";
import { CleanupHelper, type CleanupRegistryLike } from "@helpers-test-state/TestStateCleanup";
import { TestStateBuilder } from "@helpers-test-state/TestStateBuilder";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

type RequestContextFactory = {
  request: {
    newContext: (options?: Record<string, unknown>) => Promise<APIRequestContext>;
  };
};

export type PropertyRequestScenario = {
  customerUsername: string;
  customerId: number;
  buildingId: number;
  buildingName: string;
  staffId: number;
  propertyRequestId: number;
  linkedContractIds: Set<number>;
  linkedSaleContractIds: Set<number>;
  cleanup: () => Promise<void>;
};

type InternalPropertyRequestScenario = PropertyRequestScenario & {
  admin: APIRequestContext;
  customer: APIRequestContext;
};

function internalScenario(scenario: PropertyRequestScenario): InternalPropertyRequestScenario {
  return scenario as InternalPropertyRequestScenario;
}

export async function createPropertyRequestScenario(
  playwright: RequestContextFactory,
  requestType: "RENT" | "BUY" = "RENT",
  cleanupRegistry?: CleanupRegistryLike
): Promise<PropertyRequestScenario> {
  const admin = await TestStateSession.newContext(playwright, "admin");
  const BuildingState = await TestStateBuilder.createBuilding(admin, requestType === "BUY" ? "FOR_SALE" : "FOR_RENT");
  const StaffState = await TestStateBuilder.createStaff(admin);
  await TestStateBuilder.updateStaffBuildingAssignments(admin, StaffState.id, [BuildingState.id]);
  const CustomerState = await TestStateBuilder.createCustomer(admin, StaffState.id);
  await TestStateBuilder.updateStaffCustomerAssignments(admin, StaffState.id, [CustomerState.id]);
  const customer = await TestStateSession.newContextForUser(playwright, CustomerState.username);
  const propertyRequest = await TestStateBuilder.createPropertyRequest(
    customer,
    CustomerState.username,
    BuildingState.id,
    requestType
  );

  let cleaned = false;
  const scenario: InternalPropertyRequestScenario = {
    admin,
    customer,
    customerUsername: CustomerState.username,
    customerId: CustomerState.id,
    buildingId: BuildingState.id,
    buildingName: BuildingState.name,
    staffId: StaffState.id,
    propertyRequestId: propertyRequest.id,
    linkedContractIds: new Set<number>(),
    linkedSaleContractIds: new Set<number>(),
    cleanup: async () => {
      if (cleaned) {
        return;
      }

      cleaned = true;
      const linkedContractIds = [...scenario.linkedContractIds];
      const linkedSaleContractIds = [...scenario.linkedSaleContractIds];
      await CleanupHelper.run([
        { label: "Dispose customer request context", action: () => customer.dispose() },
        {
          label: `Delete property request ${propertyRequest.id}`,
          action: () => TestStateBuilder.deletePropertyRequest(propertyRequest.id)
        },
        ...linkedContractIds.map((id) => ({
          label: `Delete linked contract ${id}`,
          action: () => admin.delete(`/api/v1/admin/contracts/${id}`, { failOnStatusCode: false })
        })),
        ...linkedSaleContractIds.map((id) => ({
          label: `Delete linked sale contract ${id}`,
          action: () => admin.delete(`/api/v1/admin/sale-contracts/${id}`, { failOnStatusCode: false })
        })),
        {
          label: `Reset customer assignments for staff ${StaffState.id}`,
          action: () => TestStateBuilder.updateStaffCustomerAssignments(admin, StaffState.id, [])
        },
        {
          label: `Reset building assignments for staff ${StaffState.id}`,
          action: () => TestStateBuilder.updateStaffBuildingAssignments(admin, StaffState.id, [])
        },
        {
          label: `Delete customer ${CustomerState.id}`,
          action: () => TestStateBuilder.deleteCustomer(admin, CustomerState.id)
        },
        {
          label: `Delete building ${BuildingState.id}`,
          action: () => TestStateBuilder.deleteBuilding(admin, BuildingState.id)
        },
        { label: `Delete staff ${StaffState.id}`, action: () => TestStateBuilder.deleteStaff(admin, StaffState.id) },
        { label: "Dispose admin setup context", action: () => admin.dispose() }
      ]);
    }
  };

  cleanupRegistry?.addLabeled(`Cleanup property request scenario ${propertyRequest.id}`, () => scenario.cleanup());
  return scenario;
}

export async function readPropertyRequestState(
  propertyRequestId: number
): Promise<
  { status: string; adminNote: string; contractId: number | null; saleContractId: number | null } | undefined
> {
  const rows = await MySqlDbClient.query<{
    status: string;
    admin_note: string;
    contract_id: number | null;
    sale_contract_id: number | null;
  }>("SELECT status, admin_note, contract_id, sale_contract_id FROM property_request WHERE id = ?", [
    propertyRequestId
  ]);
  const row = rows[0];
  return row
    ? {
        status: row.status,
        adminNote: row.admin_note,
        contractId: row.contract_id,
        saleContractId: row.sale_contract_id
      }
    : undefined;
}

export async function createLinkedRentContractScenario(scenario: PropertyRequestScenario): Promise<number> {
  const internal = internalScenario(scenario);
  const contractPayload = TestDataFactory.buildContractPayload({
    customerId: scenario.customerId,
    buildingId: scenario.buildingId,
    staffId: scenario.staffId
  });

  const response = await internal.admin.post("/api/v1/admin/contracts", {
    failOnStatusCode: false,
    data: contractPayload
  });
  Invariant.equal(response.status(), 200, "Khong tao duoc rent property request");

  const rows = await MySqlDbClient.query<{ id: number }>(
    `
      SELECT id
      FROM contract
      WHERE customer_id = ? AND building_id = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [scenario.customerId, scenario.buildingId]
  );
  Invariant.equal(rows.length, 1, "Khong tim thay rent property request vua tao");

  const contractId = rows[0]!.id;
  scenario.linkedContractIds.add(contractId);
  return contractId;
}

export async function createLinkedSaleContractScenario(scenario: PropertyRequestScenario): Promise<number> {
  const internal = internalScenario(scenario);
  const salePayload = TestDataFactory.buildSaleContractPayload({
    buildingId: scenario.buildingId,
    customerId: scenario.customerId,
    staffId: scenario.staffId,
    transferDate: null
  });

  const response = await internal.admin.post("/api/v1/admin/sale-contracts", {
    failOnStatusCode: false,
    data: salePayload
  });
  Invariant.equal(response.status(), 200, "Khong tao duoc buy property request");

  const rows = await MySqlDbClient.query<{ id: number }>(
    `
      SELECT id
      FROM sale_contract
      WHERE customer_id = ? AND building_id = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [scenario.customerId, scenario.buildingId]
  );
  Invariant.equal(rows.length, 1, "Khong tim thay buy property request vua tao");

  const saleContractId = rows[0]!.id;
  scenario.linkedSaleContractIds.add(saleContractId);
  return saleContractId;
}

export async function approveRentPropertyRequestScenario(scenario: PropertyRequestScenario): Promise<number> {
  const internal = internalScenario(scenario);
  const contractId = await createLinkedRentContractScenario(scenario);
  const response = await internal.admin.post(`/api/v1/admin/property-requests/${scenario.propertyRequestId}/approve`, {
    failOnStatusCode: false,
    data: { contractId }
  });
  Invariant.equal(response.status(), 200, "Khong approve duoc rent property request");
  return contractId;
}

export async function approveBuyPropertyRequestScenario(scenario: PropertyRequestScenario): Promise<number> {
  const internal = internalScenario(scenario);
  const saleContractId = await createLinkedSaleContractScenario(scenario);
  const response = await internal.admin.post(`/api/v1/admin/property-requests/${scenario.propertyRequestId}/approve`, {
    failOnStatusCode: false,
    data: { saleContractId }
  });
  Invariant.equal(response.status(), 200, "Khong approve duoc buy property request");
  return saleContractId;
}
