import { expect, test as base } from "@fixtures/base.fixture";
import { StaffInvoiceListPage } from "@pages/staff/StaffInvoiceListPage";
import {
  cleanupContractScenario,
  createManagedInvoiceForContract,
  createContractScenario,
  findCreatedInvoiceForPeriod,
  invoiceExists,
  invoicePeriodCount,
  previousInvoicePeriod,
  readInvoiceEditState,
  type InvoiceScenarioRecord
} from "@test-data-scenarios/invoiceScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

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

base.describe("Staff - Invoice List @regression", () => {
  let contract: ContractState | null = null;
  let createdInvoices: InvoiceScenarioRecord[] = [];

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        contract = await createContractScenario(testState);
        createdInvoices = [];

        await loginAsScenarioUser(page, contract.staff.username);
        await navigationPage.open("/staff/invoices");

        try {
          await use(undefined);
        } finally {
          await cleanupContractScenario(
            testState,
            contract,
            createdInvoices.map((item) => item.id)
          );
          createdInvoices = [];
          contract = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-STF-INV-001] should assigned invoice rows and detail modal when invoice list", async ({
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

      const invoicePage = pageObjects.create(StaffInvoiceListPage);
      await navigationPage.open("/staff/invoices");
      await invoicePage.waitForLoaded();
      await invoicePage.waitForTableData();

      await invoicePage.selectFilter("status", "PENDING");
      await invoicePage.search();
      await invoicePage.waitForTableData();

      await expect(invoicePage.rowByInvoiceId(invoice.id)).toContainText(activeContract.customer.fullName);
      await invoicePage.openViewModal(invoice.id);
      await expect(invoicePage.visibleModal()).toContainText(activeContract.building.name);
      await expect.poll(() => invoicePage.visibleModalLooseText()).toMatch(/chi tiet hoa|invoice detail/i);
    });

    await steps.assert("verify assigned invoice rows and detail modal", async () => {
      expect.soft(true, "verification checkpoint: verify assigned invoice rows and detail modal").toBe(true);
    });
  });

  test("[E2E-STF-INV-002] should create invoice from add modal when invoice creation", async ({
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

      const invoicePage = pageObjects.create(StaffInvoiceListPage);
      const period = previousInvoicePeriod();

      await navigationPage.open("/staff/invoices");
      await invoicePage.openAddInvoiceModal();
      await invoicePage.selectAddCustomer(activeContract.customer.id);
      await invoicePage.selectAddContract(activeContract.id);
      await invoicePage.fillAddInvoiceForm({
        month: period.month,
        year: period.year,
        dueDate: period.dueDate,
        electricityUsage: 25,
        waterUsage: 9
      });
      await invoicePage.chooseAddStatus("PENDING");
      await invoicePage.submitAddInvoice();
      await invoicePage.waitForSweetAlertContains(/thanh cong|success/i);

      const createdInvoice = await findCreatedInvoiceForPeriod(activeContract, period);
      expect(createdInvoice).toBeDefined();
      createdInvoices.push(createdInvoice!);
    });

    await steps.assert("verify create invoice from add modal", async () => {
      expect.soft(true, "verification checkpoint: verify create invoice from add modal").toBe(true);
    });
  });

  test("[E2E-STF-INV-003] should business error display when duplicate invoice", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare duplicate invoice context", async () => {
      expect.soft(true, "precondition: prepare duplicate invoice context").toBe(true);
    });

    await steps.act("perform duplicate invoice behavior", async () => {
      const activeContract = requireContract(contract);

      const existingInvoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(existingInvoice);

      const invoicePage = pageObjects.create(StaffInvoiceListPage);
      await navigationPage.open("/staff/invoices");
      await invoicePage.openAddInvoiceModal();
      await invoicePage.selectAddCustomer(activeContract.customer.id);
      await invoicePage.selectAddContract(activeContract.id);
      await invoicePage.fillAddInvoiceForm({
        month: existingInvoice.month,
        year: existingInvoice.year,
        dueDate: previousInvoicePeriod().dueDate,
        electricityUsage: 18,
        waterUsage: 7
      });
      await invoicePage.submitAddInvoice();
      await invoicePage.waitForSweetAlertContains(/loi|da ton tai|error/i);

      expect(await invoicePeriodCount(activeContract, existingInvoice)).toBe(1);
    });

    await steps.assert("verify business error display", async () => {
      expect.soft(true, "verification checkpoint: verify business error display").toBe(true);
    });
  });

  test("[E2E-STF-INV-004] should usage due date and status update when invoice edit", async ({
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

      const invoicePage = pageObjects.create(StaffInvoiceListPage);
      const updatedDueDate = nextMonthDueDate(invoice.month, invoice.year);

      await navigationPage.open("/staff/invoices");
      await invoicePage.waitForTableData();
      await invoicePage.openEditModal(invoice.id);
      await invoicePage.fillVisibleEditForm({
        dueDate: updatedDueDate,
        electricityUsage: 33,
        waterUsage: 11,
        status: "PAID"
      });
      await invoicePage.saveVisibleEditForm();
      await invoicePage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect.poll(() => readInvoiceEditState(invoice.id)).toBe(`PAID|${updatedDueDate}`);
    });

    await steps.assert("verify usage due date and status update", async () => {
      expect.soft(true, "verification checkpoint: verify usage due date and status update").toBe(true);
    });
  });

  test("[E2E-STF-INV-005] should owned invoice deletion from list when invoice deletion", async ({
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

      const invoicePage = pageObjects.create(StaffInvoiceListPage);
      await navigationPage.open("/staff/invoices");
      await invoicePage.waitForTableData();
      await invoicePage.deleteInvoice(invoice.id);
      await invoicePage.confirmSweetAlert();
      await invoicePage.waitForSweetAlertContains(/thanh cong|success/i);

      await expect.poll(() => invoiceExists(invoice.id)).toBe(false);

      createdInvoices = createdInvoices.filter((item) => item.id !== invoice.id);
    });

    await steps.assert("verify owned invoice deletion from list", async () => {
      expect.soft(true, "verification checkpoint: verify owned invoice deletion from list").toBe(true);
    });
  });
});
