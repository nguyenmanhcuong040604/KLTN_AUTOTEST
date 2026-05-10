import { type APIRequestContext } from "@playwright/test";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import { cleanupTestStateScope } from "@helpers-test-state/TestStateCleanup";
import { CleanupHelper } from "@helpers-test-state/TestStateCleanup";
import { Invariant } from "@helpers-validation/Invariant";
import { TestEntityLookup } from "@test-data-scenarios/TestEntityLookup";
import type {
  EntityRecord,
  PaginatedList,
  BuildingState,
  ContractState,
  CustomerState,
  PropertyRequestState,
  SaleContractState,
  StaffState
} from "@test-data-scenarios/TestEntityTypes";

type InvoiceSeedRecord = {
  id: number;
  contractId: number;
  customerId: number;
  month: number;
  year: number;
  status: string;
};

type AdditionalInfoType = "legal" | "amenity" | "planning" | "supplier";

const additionalInfoEndpoints: Record<AdditionalInfoType, string> = {
  legal: "legal-authorities",
  amenity: "nearby-amenities",
  planning: "planning-maps",
  supplier: "suppliers"
};

/**
 * Creates and deletes reusable test-state records through application test-support paths.
 * Browser assertions stay in E2E specs; this support layer only prepares deterministic data.
 */
export class TestStateBuilder {
  static async findExistingStaffId(request: APIRequestContext): Promise<number> {
    const response = await request.get("/api/v1/admin/staff", {
      params: { page: 1, size: 1000, role: "STAFF" }
    });
    const responseData = await TestEntityLookup.json<PaginatedList<EntityRecord>>(response);
    const staff = TestEntityLookup.listContent(responseData).find((item) => typeof item.id === "number");

    Invariant.ensure(Boolean(staff?.id), "Khong tim thay staff co san de gan cho customer test");
    return Number(staff!.id);
  }

  static async createStaff(request: APIRequestContext, role: "STAFF" | "ADMIN" = "STAFF"): Promise<StaffState> {
    const payload = TestDataFactory.buildAdminStaffPayload({}, role);
    const fullName = String(payload.fullName);
    const username = String(payload.username);

    const createResponse = await request.post("/api/v1/admin/staff", {
      data: payload
    });
    Invariant.equal(createResponse.status(), 200, "API response status was not 200");

    const searchResponse = await request.get("/api/v1/admin/staff", {
      params: { page: 1, size: 1000, fullName, role }
    });
    const responseData = await TestEntityLookup.json<PaginatedList<EntityRecord>>(searchResponse);
    const staff = TestEntityLookup.listContent(responseData).find(
      (item) => item.fullName === fullName || item.username === username
    );

    Invariant.ensure(Boolean(staff?.id), "Khong tim thay id cua staff vua tao");
    return { id: Number(staff!.id), username, fullName };
  }

  static async deleteStaff(request: APIRequestContext, id?: number): Promise<void> {
    if (!id) return;
    const email = await TestEntityLookup.emailForId("staff", id).catch(() => undefined);
    await CleanupHelper.safe(async () => {
      await CleanupHelper.cleanupThroughAppOrDataStore(request, `/api/v1/admin/staff/${id}`, [200, 204, 404], {
        staffIds: [id]
      });

      await cleanupTestStateScope({ staffIds: [id], emails: email ? [email] : [] });
    }, `Staff ${id}`);
  }

  static async createCustomer(request: APIRequestContext, staffId?: number): Promise<CustomerState> {
    const managerStaffId = staffId ?? (await this.findExistingStaffId(request));
    const payload = TestDataFactory.buildCustomerPayload({ staffIds: [managerStaffId] });
    const fullName = String(payload.fullName);
    const username = String(payload.username);

    const createResponse = await request.post("/api/v1/admin/customers", {
      data: payload
    });
    Invariant.equal(createResponse.status(), 200, "API response status was not 200");

    const searchResponse = await request.get("/api/v1/admin/customers", {
      params: { page: 1, size: 1000, fullName }
    });
    const responseData = await TestEntityLookup.json<PaginatedList<EntityRecord>>(searchResponse);
    const customer = TestEntityLookup.listContent(responseData).find(
      (item) => item.fullName === fullName || item.username === username
    );

    Invariant.ensure(Boolean(customer?.id), "Khong tim thay id cua customer vua tao");
    return { id: Number(customer!.id), username, fullName, staffId: managerStaffId };
  }

  static async deleteCustomer(request: APIRequestContext, id?: number): Promise<void> {
    if (!id) return;
    const email = await TestEntityLookup.emailForId("customer", id).catch(() => undefined);
    await CleanupHelper.safe(async () => {
      await CleanupHelper.cleanupThroughAppOrDataStore(request, `/api/v1/admin/customers/${id}`, [200, 204, 404], {
        customerIds: [id]
      });

      await cleanupTestStateScope({ customerIds: [id], emails: email ? [email] : [] });
    }, `Customer ${id}`);
  }

  static async createBuilding(
    request: APIRequestContext,
    transactionType: "FOR_RENT" | "FOR_SALE" = "FOR_RENT"
  ): Promise<BuildingState> {
    const payload = TestDataFactory.buildBuildingPayload({}, transactionType);
    const name = String(payload.name);

    const createResponse = await request.post("/api/v1/admin/buildings", { data: payload });
    Invariant.equal(createResponse.status(), 200, "API response status was not 200");

    const searchResponse = await request.get("/api/v1/admin/buildings", {
      params: { page: 1, size: 1000, name }
    });
    const responseData = await TestEntityLookup.json<PaginatedList<EntityRecord>>(searchResponse);
    const building = TestEntityLookup.listContent(responseData).find((item) => item.name === name);

    Invariant.ensure(Boolean(building?.id), "Khong tim thay id cua building vua tao");
    return { id: Number(building!.id), name, transactionType };
  }

  static async deleteBuilding(request: APIRequestContext, id?: number): Promise<void> {
    if (!id) return;
    await CleanupHelper.safe(async () => {
      await CleanupHelper.cleanupThroughAppOrDataStore(request, `/api/v1/admin/buildings/${id}`, [200, 204, 404], {
        buildingIds: [id]
      });
    }, `Building ${id}`);
  }

  static async updateStaffBuildingAssignments(
    request: APIRequestContext,
    staffId: number,
    buildingIds: number[]
  ): Promise<void> {
    const response = await request.put(`/api/v1/admin/staff/${staffId}/assignments/buildings`, { data: buildingIds });
    Invariant.ensure(
      (buildingIds.length === 0 ? [200, 204, 400, 404] : [200, 204]).includes(response.status()),
      "API response status was not accepted."
    );
  }

  static async updateStaffCustomerAssignments(
    request: APIRequestContext,
    staffId: number,
    customerIds: number[]
  ): Promise<void> {
    const response = await request.put(`/api/v1/admin/staff/${staffId}/assignments/customers`, { data: customerIds });
    Invariant.ensure(
      (customerIds.length === 0 ? [200, 204, 400, 404] : [200, 204]).includes(response.status()),
      "API response status was not accepted."
    );
  }

  static async createContract(request: APIRequestContext): Promise<ContractState> {
    const staff = await this.createStaff(request);
    const building = await this.createBuilding(request, "FOR_RENT");
    await this.updateStaffBuildingAssignments(request, staff.id, [building.id]);

    const customer = await this.createCustomer(request, staff.id);
    await this.updateStaffCustomerAssignments(request, staff.id, [customer.id]);

    const payload = TestDataFactory.buildContractPayload({
      customerId: customer.id,
      buildingId: building.id,
      staffId: staff.id
    });

    const createResponse = await request.post("/api/v1/admin/contracts", { data: payload });
    Invariant.equal(createResponse.status(), 200, "API response status was not 200");

    const searchResponse = await request.get("/api/v1/admin/contracts", {
      params: { page: 1, size: 1000, customerId: customer.id }
    });
    const responseData = await TestEntityLookup.json<PaginatedList<EntityRecord>>(searchResponse);
    const contract = TestEntityLookup.listContent(responseData).find((item) => item.building === building.name);

    Invariant.ensure(Boolean(contract?.id), "Khong tim thay id cua contract vua tao");
    return { id: Number(contract!.id), staff, customer, building };
  }

  static async deleteContract(request: APIRequestContext, temp?: ContractState): Promise<void> {
    if (!temp) return;

    await CleanupHelper.safe(async () => {
      await CleanupHelper.cleanupThroughAppOrDataStore(request, `/api/v1/admin/contracts/${temp.id}`, [200, 204, 404], {
        contractIds: [temp.id],
        customerIds: [temp.customer.id],
        buildingIds: [temp.building.id],
        staffIds: [temp.staff.id]
      });
    }, `Contract ${temp.id}`);

    await CleanupHelper.safe(
      () => this.updateStaffCustomerAssignments(request, temp.staff.id, []),
      "Reset Staff Customer Assignment"
    );
    await CleanupHelper.safe(
      () => this.updateStaffBuildingAssignments(request, temp.staff.id, []),
      "Reset Staff Building Assignment"
    );
    await this.deleteCustomer(request, temp.customer.id);
    await this.deleteBuilding(request, temp.building.id);
    await this.deleteStaff(request, temp.staff.id);
  }

  static async createSaleContract(request: APIRequestContext): Promise<SaleContractState> {
    const staff = await this.createStaff(request);
    const building = await this.createBuilding(request, "FOR_SALE");
    await this.updateStaffBuildingAssignments(request, staff.id, [building.id]);

    const customer = await this.createCustomer(request, staff.id);
    await this.updateStaffCustomerAssignments(request, staff.id, [customer.id]);

    const payload = TestDataFactory.buildSaleContractPayload({
      buildingId: building.id,
      customerId: customer.id,
      staffId: staff.id
    });

    const createResponse = await request.post("/api/v1/admin/sale-contracts", { data: payload });
    Invariant.equal(createResponse.status(), 200, "API response status was not 200");

    const saleContractId = await TestEntityLookup.saleContractIdByParties(building.id, customer.id, staff.id);
    return { id: saleContractId, staff, customer, building };
  }

  static async deleteSaleContract(request: APIRequestContext, temp?: SaleContractState): Promise<void> {
    if (!temp) return;

    await CleanupHelper.safe(async () => {
      await CleanupHelper.cleanupThroughAppOrDataStore(
        request,
        `/api/v1/admin/sale-contracts/${temp.id}`,
        [200, 204, 404],
        {
          saleContractIds: [temp.id],
          customerIds: [temp.customer.id],
          buildingIds: [temp.building.id],
          staffIds: [temp.staff.id]
        }
      );
    }, `SaleContract ${temp.id}`);

    await CleanupHelper.safe(
      () => this.updateStaffCustomerAssignments(request, temp.staff.id, []),
      "Reset Staff Customer Assignment"
    );
    await CleanupHelper.safe(
      () => this.updateStaffBuildingAssignments(request, temp.staff.id, []),
      "Reset Staff Building Assignment"
    );
    await this.deleteCustomer(request, temp.customer.id);
    await this.deleteBuilding(request, temp.building.id);
    await this.deleteStaff(request, temp.staff.id);
  }

  static async deleteContractById(request: APIRequestContext, contractId?: number): Promise<void> {
    if (!contractId) return;
    await CleanupHelper.cleanupThroughAppOrDataStore(
      request,
      `/api/v1/admin/contracts/${contractId}`,
      [200, 204, 404],
      { contractIds: [contractId] }
    );
  }

  static async deleteSaleContractById(request: APIRequestContext, saleContractId?: number): Promise<void> {
    if (!saleContractId) return;
    await CleanupHelper.cleanupThroughAppOrDataStore(
      request,
      `/api/v1/admin/sale-contracts/${saleContractId}`,
      [200, 204, 404],
      { saleContractIds: [saleContractId] }
    );
  }

  static async createInvoice(
    request: APIRequestContext,
    contract: ContractState,
    payload: Record<string, unknown>
  ): Promise<InvoiceSeedRecord> {
    const createResponse = await request.post("/api/v1/admin/invoices", {
      failOnStatusCode: false,
      data: payload
    });

    Invariant.equal(createResponse.status(), 200, "Khong tao duoc invoice tam cho E2E");

    const rows = await MySqlDbClient.query<{ id: number }>(
      `
        SELECT id
        FROM invoice
        WHERE contract_id = ? AND customer_id = ? AND month = ? AND year = ?
        ORDER BY id DESC
        LIMIT 1
      `,
      [contract.id, contract.customer.id, Number(payload.month), Number(payload.year)]
    );

    Invariant.ensure(rows.length > 0, "Khong tim thay invoice vua tao trong DB");

    return {
      id: rows[0]!.id,
      contractId: contract.id,
      customerId: contract.customer.id,
      month: Number(payload.month),
      year: Number(payload.year),
      status: String(payload.status)
    };
  }

  static async deleteInvoice(request: APIRequestContext, invoiceId?: number): Promise<void> {
    if (!invoiceId) return;

    const invoiceRows = await MySqlDbClient.query<{ id: number; contract_id: number; month: number; year: number }>(
      "SELECT id, contract_id, month, year FROM invoice WHERE id = ? LIMIT 1",
      [invoiceId]
    );

    if (!invoiceRows.length) {
      return;
    }

    const invoice = invoiceRows[0]!;
    const response = await request.delete(`/api/v1/admin/invoices/${invoiceId}`, {
      failOnStatusCode: false
    });

    if ([200, 204, 404].includes(response.status())) {
      return;
    }

    await MySqlDbClient.execute("DELETE FROM utility_meter WHERE contract_id = ? AND month = ? AND year = ?", [
      invoice.contract_id,
      invoice.month,
      invoice.year
    ]);
    await MySqlDbClient.execute("DELETE FROM invoice_detail WHERE invoice_id = ?", [invoiceId]);
    await MySqlDbClient.execute("DELETE FROM invoice WHERE id = ?", [invoiceId]);
  }

  static async deleteAdditionalInfoRecord(
    request: APIRequestContext,
    type: AdditionalInfoType,
    id?: number
  ): Promise<void> {
    if (!id) return;

    await request.delete(`/api/v1/admin/building-additional-information/${additionalInfoEndpoints[type]}/${id}`, {
      failOnStatusCode: false
    });
  }

  static async findCustomerIdByUsername(username: string): Promise<number> {
    return TestEntityLookup.customerIdByUsername(username);
  }

  static async createPropertyRequest(
    customerRequest: APIRequestContext,
    customerUsername: string,
    buildingId: number,
    requestType: "RENT" | "BUY" = "RENT"
  ): Promise<PropertyRequestState> {
    const payload = TestDataFactory.buildPropertyRequestPayload({ buildingId }, requestType);
    const submitResponse = await customerRequest.post("/api/v1/customer/property-requests", {
      data: payload,
      failOnStatusCode: false,
      maxRedirects: 0
    });
    Invariant.equal(submitResponse.status(), 200, "API response status was not 200");

    const customerId = await this.findCustomerIdByUsername(customerUsername);
    const rows = await MySqlDbClient.query<{ id: number }>(
      `
        SELECT id
        FROM property_request
        WHERE customer_id = ? AND building_id = ? AND request_type = ?
        ORDER BY id DESC
        LIMIT 1
      `,
      [customerId, buildingId, requestType]
    );

    Invariant.ensure(rows.length > 0, "Expected at least one row.");
    return { id: rows[0]!.id, buildingId, customerId, requestType };
  }

  static async deletePropertyRequest(id?: number): Promise<void> {
    if (!id) return;
    await CleanupHelper.safe(async () => {
      await MySqlDbClient.execute("DELETE FROM property_request WHERE id = ?", [id]);
    }, `PropertyRequest ${id}`);
  }
}
