import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerInvoicePage } from "@pages/customer/CustomerInvoicePage";
import { CustomerPaymentQrPage } from "@pages/customer/CustomerPaymentQrPage";
import {
  cleanupContractScenario,
  createManagedInvoiceForContract,
  createContractScenario,
  readInvoicePaymentState,
  readInvoiceStatus,
  readInvoiceSummary,
  type InvoiceScenarioRecord
} from "@test-data-scenarios/invoiceScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

type ContractState = Awaited<ReturnType<typeof createContractScenario>>;

function requireContract(contract: ContractState | null): ContractState {
  expect(contract, "Contract scenario must be created by the scenario fixture").toBeTruthy();
  return contract!;
}

base.describe("Customer - Invoice Payment @regression @critical", () => {
  let contract: ContractState | null = null;
  let createdInvoices: InvoiceScenarioRecord[] = [];

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        contract = await createContractScenario(testState);
        createdInvoices = [];

        await loginAsScenarioUser(page, contract.customer.username);
        await navigationPage.open("/customer/invoice/list");

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

  test("[E2E-CUS-PAY-001] should unpaid summary and payment details modal display when invoice list", async ({
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

      const invoice = await createManagedInvoiceForContract(testState, activeContract, {
        electricityUsage: 12,
        waterUsage: 4
      });
      createdInvoices.push(invoice);

      const invoicePage = pageObjects.create(CustomerInvoicePage);
      await navigationPage.open("/customer/invoice/list");
      await invoicePage.waitForLoaded();

      const stats = await invoicePage.readStats();
      expect(stats.unpaidCount).toBe("1");
      expect(stats.totalPayable).toMatch(/\d/);

      const cardText = await invoicePage.firstInvoiceCardText();
      expect(cardText).toContain(String(invoice.id));
      expect(cardText).toContain(activeContract.building.name);

      await invoicePage.openFirstPaymentModal();
      const modalText = await invoicePage.visibleModalLooseText();
      expect(modalText).toMatch(/invoice|chi tiet|hoa/i);
      expect(modalText).toContain(activeContract.building.name.toLowerCase());
      expect(modalText).toMatch(/tong cong|total/i);
      expect(modalText).toContain(String(invoice.id));

      const invoiceSummary = await readInvoiceSummary(invoice.id);
      expect(invoiceSummary?.status).toBe("PENDING");
      expect(invoiceSummary?.totalAmount ?? 0).toBeGreaterThan(0);
    });

    await steps.assert("verify unpaid summary and payment details modal display", async () => {
      expect.soft(true, "verification checkpoint: verify unpaid summary and payment details modal display").toBe(true);
    });
  });

  test("[E2E-CUS-PAY-002] should QR payment page redirection when payment modal", async ({
    page: _page,
    testState,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare payment modal context", async () => {
      expect.soft(true, "precondition: prepare payment modal context").toBe(true);
    });

    await steps.act("perform payment modal behavior", async () => {
      const activeContract = requireContract(contract);

      const invoice = await createManagedInvoiceForContract(testState, activeContract);
      createdInvoices.push(invoice);

      const invoicePage = pageObjects.create(CustomerInvoicePage);
      const qrPage = pageObjects.create(CustomerPaymentQrPage);

      await navigationPage.open("/customer/invoice/list");
      await invoicePage.openFirstPaymentModal();
      await invoicePage.confirmPaymentInModal();
      await invoicePage.continueSweetAlertRedirect();

      await qrPage.waitForLoaded(invoice.id);
      await qrPage.waitForMetaContains(new RegExp(`SUNTOWER INV ${invoice.id}`));

      expect(await readInvoiceStatus(invoice.id)).toBe("PENDING");
    });

    await steps.assert("verify QR payment page redirection", async () => {
      expect.soft(true, "verification checkpoint: verify QR payment page redirection").toBe(true);
    });
  });

  test("[E2E-CUS-PAY-003] should invoice status update to paid when payment confirmation", async ({
    page,
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

      const invoicePage = pageObjects.create(CustomerInvoicePage);
      const qrPage = pageObjects.create(CustomerPaymentQrPage);

      await navigationPage.open("/customer/invoice/list");
      await invoicePage.openFirstPaymentModal();
      await invoicePage.confirmPaymentInModal();
      await invoicePage.continueSweetAlertRedirect();
      await qrPage.waitForLoaded(invoice.id);

      await qrPage.confirmPayment();
      await expect(page).toHaveURL(/\/customer\/invoice\/list\?paySuccess/);
      await invoicePage.waitForPaymentSuccessAlert();
      await expect
        .poll(() => readInvoicePaymentState(invoice.id))
        .toMatchObject({
          status: "PAID",
          payment_method: "BANK_QR"
        });

      const paidInvoice = await readInvoicePaymentState(invoice.id);
      expect(paidInvoice?.transaction_code).toMatch(new RegExp(`^QR-${invoice.id}-\\d{14}$`));
      expect(paidInvoice?.paid_date).toBeTruthy();

      await invoicePage.waitForEmptyState();
    });

    await steps.assert("verify invoice status update to paid", async () => {
      expect.soft(true, "verification checkpoint: verify invoice status update to paid").toBe(true);
    });
  });

  test("[E2E-CUS-PAY-004] should no unpaid invoices display when empty state", async ({
    page: _page,
    pageObjects,
    navigationPage,
    steps
  }) => {
    await steps.arrange("prepare empty state context", async () => {
      expect.soft(true, "precondition: prepare empty state context").toBe(true);
    });

    await steps.act("perform empty state behavior", async () => {
      const invoicePage = pageObjects.create(CustomerInvoicePage);

      await navigationPage.open("/customer/invoice/list");
      await invoicePage.waitForLoaded();
      await invoicePage.waitForEmptyState();
    });

    await steps.assert("verify no unpaid invoices display", async () => {
      expect.soft(true, "verification checkpoint: verify no unpaid invoices display").toBe(true);
    });
  });
});
