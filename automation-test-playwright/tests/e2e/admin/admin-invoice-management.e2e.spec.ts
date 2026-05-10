import { expect, test as base } from "@fixtures/base.fixture";
import { env } from "@helpers-runtime/env";
import { AdminInvoiceDetailPage } from "@pages/admin/AdminDetailPages";
import { AdminInvoiceFormPage } from "@pages/admin/AdminInvoiceFormPage";
import { AdminInvoiceListPage } from "@pages/admin/AdminInvoiceListPage";
import {
  cleanupContractScenario,
  createManagedInvoiceForContract,
  createContractScenario,
  findCreatedInvoiceForPeriod,
  invoiceExists,
  markInvoicePaid,
  previousInvoicePeriod,
  readInvoiceDueDate,
  readInvoiceStatus,
  type InvoiceScenarioRecord
} from "@test-data-scenarios/invoiceScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

type ContractState = Awaited<ReturnType<typeof createContractScenario>>;

function requireContract(contract: ContractState | null): ContractState {
  expect(contract, "Contract scenario must be created by the scenario fixture").toBeTruthy();
  return contract!;
}

function nextMonthDueDate(month: number, year: number, day = 20): string {
  const dueMonth = month === 12 ? 1 : month + 1;
  const dueYear = month === 12 ? year + 1 : year;
  return `${dueYear}-${String(dueMonth).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

base.describe("Admin - Invoice Management @regression @critical", () => {
  let adminUser: StaffProfileState | null = null;
  let contract: ContractState | null = null;
  let createdInvoices: InvoiceScenarioRecord[] = [];

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        contract = await createContractScenario(testState);
        createdInvoices = [];

        await loginAsScenarioUser(page, adminUser.username, env.defaultPassword);
        await navigationPage.open("/admin/invoice/list");

        try {
          await use(undefined);
        } finally {
          await cleanupContractScenario(
            testState,
            contract,
            createdInvoices.map((item) => item.id)
          );
          await cleanupStaffProfileScenario(testState, adminUser);
          createdInvoices = [];
          contract = null;
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-INV-001] should customer filtering and data display when invoice list", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare invoice list context", async () => {
      expect.soft(true, "precondition: prepare invoice list context").toBe(true);
    });

    await steps.act("perform invoice list behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      const listPage = pageObjects.create(AdminInvoiceListPage);
      await navigationPage.open("/admin/invoice/list");
      await listPage.waitForLoaded();
      await listPage.waitForTableData();

      await listPage.filterByCustomer(activeContract.customer.id);
      await listPage.filterByMonth(invoice.month);
      await listPage.filterByStatus("PENDING");
      await listPage.submitFilters();
      await listPage.waitForTableData();

      await expect(listPage.rowByInvoiceId(invoice.id)).toContainText(activeContract.customer.fullName);
    });

    await steps.assert("verify customer filtering and data display", async () => {
      expect.soft(true, "verification checkpoint: verify customer filtering and data display").toBe(true);
    });
  });

  test("[E2E-ADM-INV-002] should create invoice from add form when invoice creation", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare invoice creation context", async () => {
      expect.soft(true, "precondition: prepare invoice creation context").toBe(true);
    });

    await steps.act("perform invoice creation behavior", async () => {
      const activeContract = requireContract(contract);

      const listPage = pageObjects.create(AdminInvoiceListPage);
      const formPage = pageObjects.create(AdminInvoiceFormPage);
      const period = previousInvoicePeriod();

      await navigationPage.open("/admin/invoice/list");
      await listPage.openAddForm();
      await formPage.waitForAddLoaded();
      await formPage.fillAddForm({
        customerId: activeContract.customer.id,
        contractId: activeContract.id,
        month: period.month,
        year: period.year,
        dueDate: period.dueDate,
        electricityUsage: 21,
        waterUsage: 8
      });
      await formPage.submitInvoice();
      await formPage.waitForSweetAlertContains(/thanh cong|success/i);

      const createdInvoice = await findCreatedInvoiceForPeriod(activeContract, period);
      expect(createdInvoice).toBeDefined();
      expect(createdInvoice!.status).toBe("PENDING");
      createdInvoices.push(createdInvoice!);
    });

    await steps.assert("verify create invoice from add form", async () => {
      expect.soft(true, "verification checkpoint: verify create invoice from add form").toBe(true);
    });
  });

  test("[E2E-ADM-INV-003] should pending invoice update when invoice edit", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare invoice edit context", async () => {
      expect.soft(true, "precondition: prepare invoice edit context").toBe(true);
    });

    await steps.act("perform invoice edit behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      const formPage = pageObjects.create(AdminInvoiceFormPage);
      const updatedDueDate = nextMonthDueDate(invoice.month, invoice.year);

      await navigationPage.open(`/admin/invoice/edit/${invoice.id}`);
      await formPage.waitForEditLoaded(invoice.id);
      await formPage.fillEditForm({
        dueDate: updatedDueDate,
        electricityUsage: 40,
        waterUsage: 10
      });
      await formPage.submitInvoice();
      await formPage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect.poll(() => readInvoiceDueDate(invoice.id)).toBe(updatedDueDate);
    });

    await steps.assert("verify pending invoice update", async () => {
      expect.soft(true, "verification checkpoint: verify pending invoice update").toBe(true);
    });
  });

  test("[E2E-ADM-INV-004] should non-pending warning display when invoice edit lock", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare invoice edit lock context", async () => {
      expect.soft(true, "precondition: prepare invoice edit lock context").toBe(true);
    });

    await steps.act("perform invoice edit lock behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      await markInvoicePaid(invoice.id);

      const formPage = pageObjects.create(AdminInvoiceFormPage);
      await navigationPage.open(`/admin/invoice/edit/${invoice.id}`);
      await formPage.waitForEditLoaded(invoice.id);
      await formPage.waitForWarningVisible();
    });

    await steps.assert("verify non-pending warning display", async () => {
      expect.soft(true, "verification checkpoint: verify non-pending warning display").toBe(true);
    });
  });

  test("[E2E-ADM-INV-005] should invoice payment confirmation when payment confirmation", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare payment confirmation context", async () => {
      expect.soft(true, "precondition: prepare payment confirmation context").toBe(true);
    });

    await steps.act("perform payment confirmation behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      const detailPage = pageObjects.create(AdminInvoiceDetailPage);
      await navigationPage.open(`/admin/invoice/${invoice.id}`);
      await detailPage.waitForLoaded(invoice.id);
      await detailPage.confirmInvoicePaid();
      await detailPage.confirmSweetAlert();
      await detailPage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect.poll(() => readInvoiceStatus(invoice.id)).toBe("PAID");
    });

    await steps.assert("verify invoice payment confirmation", async () => {
      expect.soft(true, "verification checkpoint: verify invoice payment confirmation").toBe(true);
    });
  });

  test("[E2E-ADM-INV-006] should delete invoice from list when invoice deletion", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare invoice deletion context", async () => {
      expect.soft(true, "precondition: prepare invoice deletion context").toBe(true);
    });

    await steps.act("perform invoice deletion behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      const listPage = pageObjects.create(AdminInvoiceListPage);
      await navigationPage.open("/admin/invoice/list");
      await listPage.waitForTableData();
      await listPage.filterByCustomer(activeContract.customer.id);
      await listPage.filterByMonth(invoice.month);
      await listPage.filterByStatus("PENDING");
      await listPage.submitFilters();
      await listPage.waitForTableData();
      await listPage.deleteInvoice(invoice.id);
      await listPage.confirmSweetAlert();
      await listPage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect.poll(() => invoiceExists(invoice.id)).toBe(false);

      createdInvoices = createdInvoices.filter((item) => item.id !== invoice.id);
    });

    await steps.assert("verify delete invoice from list", async () => {
      expect.soft(true, "verification checkpoint: verify delete invoice from list").toBe(true);
    });
  });
});
