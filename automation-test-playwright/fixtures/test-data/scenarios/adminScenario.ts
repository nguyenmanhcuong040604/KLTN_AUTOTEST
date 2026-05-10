import type { Page } from "@playwright/test";
import type { TestStateFixture } from "@helpers-test-state/TestState";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";
import type { BuildingState, ContractState, CustomerState, SaleContractState, StaffState } from "./TestEntityTypes";

export type AdminE2ESession = {
  user: StaffProfileState;
  cleanup: () => Promise<void>;
};

export async function createAdminE2ESession(
  page: Page,
  testState: TestStateFixture,
  landingPath: string
): Promise<AdminE2ESession> {
  const user = await createStaffProfileScenario(testState, "ADMIN");
  let cleaned = false;

  const cleanup = async () => {
    if (cleaned) {
      return;
    }

    cleaned = true;
    await cleanupStaffProfileScenario(testState, user);
  };

  try {
    await loginAsScenarioUser(page, user.username, user.password);
    await page.goto(landingPath);
    return { user, cleanup };
  } catch (error) {
    await cleanup();
    throw error;
  }
}

export async function cleanupContractScenarios(
  testState: TestStateFixture,
  contracts: Array<Awaited<ReturnType<TestStateFixture["createContract"]>>>
): Promise<void> {
  for (const contract of contracts.splice(0)) {
    await testState.deleteContract(contract);
  }
}

export async function cleanupBuildingIdSet(testState: TestStateFixture, buildingIds: Set<number>): Promise<void> {
  for (const buildingId of buildingIds) {
    await testState.deleteBuilding(buildingId);
  }

  buildingIds.clear();
}

export async function createBuildingScenario(
  testState: TestStateFixture,
  transactionType: "FOR_RENT" | "FOR_SALE" = "FOR_RENT"
): Promise<BuildingState> {
  return testState.createBuilding(transactionType);
}

export async function createBuildingLockedByContractScenario(testState: TestStateFixture): Promise<ContractState> {
  return testState.createContract();
}

export async function findBuildingCreatedFromForm(
  buildingName: string
): Promise<{ id: number; transaction_type: string; floor_area: number; tax_code: string } | undefined> {
  const rows = await MySqlDbClient.query<{
    id: number;
    transaction_type: string;
    floor_area: number;
    tax_code: string;
  }>(
    `
      SELECT id, transaction_type, floor_area, tax_code
      FROM building
      WHERE name = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [buildingName]
  );

  return rows[0];
}

export async function findBuildingEditState(
  buildingId: number
): Promise<{ name: string; floor_area: number; rent_price: number } | undefined> {
  const rows = await MySqlDbClient.query<{ name: string; floor_area: number; rent_price: number }>(
    "SELECT name, floor_area, rent_price FROM building WHERE id = ?",
    [buildingId]
  );

  return rows[0];
}

export async function buildingExists(buildingId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM building WHERE id = ?", [buildingId]);
  return rows.length > 0;
}

export async function buildingNameMatches(buildingId: number, buildingName: string): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM building WHERE id = ? AND name = ?",
    [buildingId, buildingName]
  );
  return Number(rows[0]?.count ?? 0) === 1;
}

export type AssignableScenario = {
  staff: StaffState;
  customer: CustomerState;
  building: BuildingState;
};

/** @deprecated Use {@link AssignableScenario} instead. */
export type AssignableRentScenario = AssignableScenario;

export async function createAssignableScenario(
  testState: TestStateFixture,
  transactionType: "FOR_RENT" | "FOR_SALE" = "FOR_RENT"
): Promise<AssignableScenario> {
  const staff = await testState.createStaff();
  const building = await testState.createBuilding(transactionType);
  await testState.updateStaffBuildingAssignments(staff.id, [building.id]);
  const customer = await testState.createCustomer(staff.id);
  await testState.updateStaffCustomerAssignments(staff.id, [customer.id]);
  return { staff, customer, building };
}

/** @deprecated Use {@link createAssignableScenario} with transactionType "FOR_RENT". */
export async function createAssignableRentScenario(testState: TestStateFixture): Promise<AssignableScenario> {
  return createAssignableScenario(testState, "FOR_RENT");
}

/** @deprecated Use {@link createAssignableScenario} with transactionType "FOR_SALE". */
export async function createAssignableSaleScenario(testState: TestStateFixture): Promise<AssignableScenario> {
  return createAssignableScenario(testState, "FOR_SALE");
}

export async function cleanupAdminEntitySets(
  testState: TestStateFixture,
  entitySets: {
    contractIds?: Set<number>;
    saleContractIds?: Set<number>;
    customerIds?: Set<number>;
    buildingIds?: Set<number>;
    staffIds?: Set<number>;
  }
): Promise<void> {
  for (const contractId of entitySets.contractIds ?? []) {
    await testState.deleteContractById(contractId);
  }
  entitySets.contractIds?.clear();

  for (const saleContractId of entitySets.saleContractIds ?? []) {
    await testState.deleteSaleContractById(saleContractId);
  }
  entitySets.saleContractIds?.clear();

  for (const staffId of entitySets.staffIds ?? []) {
    await testState.updateStaffCustomerAssignments(staffId, []);
    await testState.updateStaffBuildingAssignments(staffId, []);
  }

  for (const customerId of entitySets.customerIds ?? []) {
    await testState.deleteCustomer(customerId);
  }
  entitySets.customerIds?.clear();

  for (const buildingId of entitySets.buildingIds ?? []) {
    await testState.deleteBuilding(buildingId);
  }
  entitySets.buildingIds?.clear();

  for (const staffId of entitySets.staffIds ?? []) {
    await testState.deleteStaff(staffId);
  }
  entitySets.staffIds?.clear();
}

export function trackRentScenario(
  scenario: AssignableRentScenario,
  entitySets: { staffIds: Set<number>; customerIds: Set<number>; buildingIds: Set<number> }
): void {
  entitySets.staffIds.add(scenario.staff.id);
  entitySets.customerIds.add(scenario.customer.id);
  entitySets.buildingIds.add(scenario.building.id);
}

export function trackContractScenario(
  contract: ContractState,
  entitySets: { contractIds: Set<number>; staffIds: Set<number>; customerIds: Set<number>; buildingIds: Set<number> }
): void {
  entitySets.contractIds.add(contract.id);
  entitySets.staffIds.add(contract.staff.id);
  entitySets.customerIds.add(contract.customer.id);
  entitySets.buildingIds.add(contract.building.id);
}

export function trackSaleContractScenario(
  saleContract: SaleContractState,
  entitySets: {
    saleContractIds: Set<number>;
    staffIds: Set<number>;
    customerIds: Set<number>;
    buildingIds: Set<number>;
  }
): void {
  entitySets.saleContractIds.add(saleContract.id);
  entitySets.staffIds.add(saleContract.staff.id);
  entitySets.customerIds.add(saleContract.customer.id);
  entitySets.buildingIds.add(saleContract.building.id);
}

export async function findCreatedStaff(username: string): Promise<{ id: number; role: string } | undefined> {
  const rows = await MySqlDbClient.query<{ id: number; role: string }>(
    "SELECT id, role FROM staff WHERE username = ? LIMIT 1",
    [username]
  );
  return rows[0];
}

export async function staffExists(staffId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM staff WHERE id = ?", [staffId]);
  return rows.length > 0;
}

export async function staffBuildingAssignmentExists(staffId: number, buildingId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM assignment_building WHERE staff_id = ? AND building_id = ?",
    [staffId, buildingId]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function staffCustomerAssignmentExists(staffId: number, customerId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM assignment_customer WHERE staff_id = ? AND customer_id = ?",
    [staffId, customerId]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function findCreatedCustomer(username: string): Promise<{ id: number } | undefined> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM customer WHERE username = ? LIMIT 1", [
    username
  ]);
  return rows[0];
}

export async function customerExists(customerId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM customer WHERE id = ?", [customerId]);
  return rows.length > 0;
}

export async function customerAccountExists(username: string, email: string): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM customer WHERE username = ? OR email = ?",
    [username, email]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function findCreatedContract(
  customerId: number,
  buildingId: number
): Promise<{ id: number; rent_price: number; start_date: string; end_date: string } | undefined> {
  const rows = await MySqlDbClient.query<{ id: number; rent_price: number; start_date: string; end_date: string }>(
    `
      SELECT id, rent_price,
             DATE_FORMAT(start_date, '%Y-%m-%d') AS start_date,
             DATE_FORMAT(end_date, '%Y-%m-%d') AS end_date
      FROM contract
      WHERE customer_id = ? AND building_id = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [customerId, buildingId]
  );
  return rows[0];
}

export async function contractExists(contractId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM contract WHERE id = ?", [contractId]);
  return rows.length > 0;
}

export async function staffRentContractAssignmentExists(
  contract: Pick<ContractState, "id" | "staff" | "customer" | "building">
): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND staff_id = ? AND customer_id = ? AND building_id = ?",
    [contract.id, contract.staff.id, contract.customer.id, contract.building.id]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function customerRentContractAssignmentExists(
  contract: Pick<ContractState, "id" | "customer" | "building">
): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND customer_id = ? AND building_id = ?",
    [contract.id, contract.customer.id, contract.building.id]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function customerActiveContractExists(
  contract: Pick<ContractState, "id" | "customer" | "building">
): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM contract WHERE id = ? AND customer_id = ? AND building_id = ? AND status = 'ACTIVE'",
    [contract.id, contract.customer.id, contract.building.id]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function contractWithPriceExists(
  customerId: number,
  buildingId: number,
  rentPrice: number
): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM contract WHERE customer_id = ? AND building_id = ? AND rent_price = ?",
    [customerId, buildingId, rentPrice]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function readContractEditState(
  contractId: number
): Promise<{ rent_price: number; end_date: string; status: string } | undefined> {
  const rows = await MySqlDbClient.query<{ rent_price: number; end_date: string; status: string }>(
    "SELECT rent_price, DATE_FORMAT(end_date, '%Y-%m-%d') AS end_date, status FROM contract WHERE id = ?",
    [contractId]
  );
  return rows[0];
}

export async function expireContract(contractId: number): Promise<void> {
  await MySqlDbClient.execute("UPDATE contract SET status = 'EXPIRED' WHERE id = ?", [contractId]);
}

export async function findCreatedSaleContract(
  customerId: number,
  buildingId: number
): Promise<{ id: number; sale_price: number } | undefined> {
  const rows = await MySqlDbClient.query<{ id: number; sale_price: number }>(
    `
      SELECT id, sale_price
      FROM sale_contract
      WHERE customer_id = ? AND building_id = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [customerId, buildingId]
  );
  return rows[0];
}

export async function saleContractExists(saleContractId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM sale_contract WHERE id = ?", [saleContractId]);
  return rows.length > 0;
}

export async function staffSaleContractAssignmentExists(
  saleContract: Pick<SaleContractState, "id" | "staff" | "customer" | "building">
): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    "SELECT COUNT(*) AS count FROM sale_contract WHERE id = ? AND staff_id = ? AND customer_id = ? AND building_id = ?",
    [saleContract.id, saleContract.staff.id, saleContract.customer.id, saleContract.building.id]
  );
  return Number(rows[0]?.count ?? 0) > 0;
}

export async function readSaleContractTransferDate(saleContractId: number): Promise<string | null> {
  const rows = await MySqlDbClient.query<{ transfer_date: string | null }>(
    "SELECT DATE_FORMAT(transfer_date, '%Y-%m-%d') AS transfer_date FROM sale_contract WHERE id = ?",
    [saleContractId]
  );
  return rows[0]?.transfer_date ?? null;
}
