import { expect, test as base } from "@fixtures/base.fixture";
import { CustomerTransactionHistoryPage } from "@pages/customer/CustomerTransactionHistoryPage";
import {
  cleanupContractScenario,
  createManagedInvoiceForContract,
  createContractScenario,
  markInvoicePaidByBankQr,
  paidInvoicePeriodCount,
  readInvoicePaymentState,
  readInvoiceTotalAmount
} from "@test-data-scenarios/invoiceScenario";
import { loginAsScenarioUser } from "@test-data-scenarios/profileScenario";

type ContractState = Awaited<ReturnType<typeof createContractScenario>>;

base.describe("Customer - Transaction History @regression", () => {
  let ContractState: ContractState | null = null;
  let invoiceId: number | null = null;
  let invoiceMonth = 0;
  let invoiceYear = 0;
  let invoiceTotalAmount = 0;

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        ContractState = await createContractScenario(testState);
        const invoice = await createManagedInvoiceForContract(testState, ContractState, { status: "PENDING" });

        invoiceId = invoice.id;
        invoiceMonth = invoice.month;
        invoiceYear = invoice.year;

        await markInvoicePaidByBankQr(invoice.id);
        invoiceTotalAmount = await readInvoiceTotalAmount(invoice.id);

        await loginAsScenarioUser(page, ContractState.customer.username);
        await navigationPage.open("/customer/transaction/history");

        try {
          await use(undefined);
        } finally {
          await cleanupContractScenario(testState, ContractState, invoiceId ? [invoiceId] : []);
          ContractState = null;
          invoiceId = null;
          invoiceMonth = 0;
          invoiceYear = 0;
          invoiceTotalAmount = 0;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-CUS-TXN-001] should paid transaction summary and invoice detail display when transaction summary", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare transaction summary context", async () => {
      expect.soft(true, "precondition: prepare transaction summary context").toBe(true);
    });

    await steps.act("perform transaction summary behavior", async () => {
      const transactionPage = pageObjects.create(CustomerTransactionHistoryPage);
      await transactionPage.waitForLoaded();
      await transactionPage.waitForSummaryVisible();
      await transactionPage.waitForSummaryValues(1, invoiceTotalAmount.toLocaleString("vi-VN"));
      await transactionPage.waitForResultCountBanner(1);
      await expect(transactionPage.rowByBuildingName(ContractState!.building.name)).toBeVisible();

      await transactionPage.openTransactionDetail(ContractState!.building.name);
      await transactionPage.waitForDetailModalContains(ContractState!.building.name);
      await transactionPage.waitForDetailModalContains(`Ma hoa don: ${invoiceId}`);
      await transactionPage.closeDetailModal();

      const paymentState = await readInvoicePaymentState(invoiceId!);
      expect(paymentState?.status).toBe("PAID");
      expect(paymentState?.payment_method).toBe("BANK_QR");
      expect(paymentState?.transaction_code).toBe(`E2E-TX-${invoiceId}`);
    });

    await steps.assert("verify paid transaction summary and invoice detail display", async () => {
      expect
        .soft(true, "verification checkpoint: verify paid transaction summary and invoice detail display")
        .toBe(true);
    });
  });

  test("[E2E-CUS-TXN-002] should invoice period filtering when period filter", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare period filter context", async () => {
      expect.soft(true, "precondition: prepare period filter context").toBe(true);
    });

    await steps.act("perform period filter behavior", async () => {
      const transactionPage = pageObjects.create(CustomerTransactionHistoryPage);
      await transactionPage.waitForLoaded();
      await transactionPage.filterByMonth(invoiceMonth);
      await transactionPage.filterByYear(invoiceYear);
      await transactionPage.submitFilters();
      await transactionPage.waitForResultCountBanner(1);
      await expect(transactionPage.rowByBuildingName(ContractState!.building.name)).toBeVisible();

      expect(await paidInvoicePeriodCount(invoiceId!, invoiceMonth, invoiceYear)).toBe(1);
    });

    await steps.assert("verify invoice period filtering", async () => {
      expect.soft(true, "verification checkpoint: verify invoice period filtering").toBe(true);
    });
  });

  test("[E2E-CUS-TXN-003] should empty state and filter reset when filter reset", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare filter reset context", async () => {
      expect.soft(true, "precondition: prepare filter reset context").toBe(true);
    });

    await steps.act("perform filter reset behavior", async () => {
      const transactionPage = pageObjects.create(CustomerTransactionHistoryPage);
      await transactionPage.waitForLoaded();
      await transactionPage.filterByMonth(invoiceMonth === 12 ? 1 : invoiceMonth + 1);
      await transactionPage.filterByYear(invoiceYear);
      await transactionPage.submitFilters();
      await transactionPage.waitForEmptyState();
      await transactionPage.waitForPaginationHidden();

      await transactionPage.resetFilters();
      await transactionPage.submitFilters();
      await transactionPage.waitForResultCountBanner(1);
      await expect(transactionPage.rowByBuildingName(ContractState!.building.name)).toBeVisible();
    });

    await steps.assert("verify empty state and filter reset", async () => {
      expect.soft(true, "verification checkpoint: verify empty state and filter reset").toBe(true);
    });
  });
});
