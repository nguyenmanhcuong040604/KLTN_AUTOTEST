import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import { TestStateBuilder } from "@helpers-test-state/TestStateBuilder";
import type { TestStateFixture } from "@helpers-test-state/TestState";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";

type ContractState = Awaited<ReturnType<typeof TestStateBuilder.createContract>>;
type InvoiceStatus = (typeof TestDataFactory.invoiceStatus)[keyof typeof TestDataFactory.invoiceStatus];

export type InvoiceScenarioRecord = {
  id: number;
  contractId: number;
  customerId: number;
  month: number;
  year: number;
  status: InvoiceStatus;
};

export type InvoicePaymentState = {
  status: string;
  payment_method: string | null;
  transaction_code: string | null;
  paid_date: string | null;
} | null;

const DEFAULT_CONTRACT_VALUES = {
  rentArea: 50,
  rentPricePerSquareMeter: 1_000_000,
  serviceFee: 100_000,
  carFee: 50_000,
  motorbikeFee: 20_000,
  electricityFee: 3_500,
  waterFee: 15_000
} as const;

type InvoiceSeedOverrides = Partial<{
  month: number;
  year: number;
  dueDate: string;
  electricityUsage: number;
  waterUsage: number;
  status: InvoiceStatus;
}>;

function resolveBaseDate(baseDate?: Date): Date {
  if (baseDate) {
    return baseDate;
  }

  const configured = process.env.TEST_BASE_DATE;
  const parsed = configured ? new Date(configured) : new Date();
  return Number.isNaN(parsed.getTime()) ? new Date() : parsed;
}

export function previousInvoicePeriod(baseDate?: Date): { month: number; year: number; dueDate: string } {
  const resolvedBaseDate = resolveBaseDate(baseDate);
  const invoiceDate = new Date(resolvedBaseDate);
  invoiceDate.setMonth(invoiceDate.getMonth() - 1);

  const month = invoiceDate.getMonth() + 1;
  const year = invoiceDate.getFullYear();
  const dueDateValue = new Date(year, month, 15);
  const dueDate = `${dueDateValue.getFullYear()}-${String(dueDateValue.getMonth() + 1).padStart(2, "0")}-15`;

  return { month, year, dueDate };
}

export function buildManagedInvoicePayload(
  contract: ContractState,
  overrides: InvoiceSeedOverrides = {}
): Record<string, unknown> {
  const period = previousInvoicePeriod();
  const month = overrides.month ?? period.month;
  const year = overrides.year ?? period.year;
  const dueDate = overrides.dueDate ?? period.dueDate;
  const electricityUsage = overrides.electricityUsage ?? 18;
  const waterUsage = overrides.waterUsage ?? 7;

  const rentAmount = DEFAULT_CONTRACT_VALUES.rentArea * DEFAULT_CONTRACT_VALUES.rentPricePerSquareMeter;
  const electricityAmount = electricityUsage * DEFAULT_CONTRACT_VALUES.electricityFee;
  const waterAmount = waterUsage * DEFAULT_CONTRACT_VALUES.waterFee;

  const details = [
    { description: "Tien thue mat bang", amount: rentAmount },
    { description: "Phi dich vu", amount: DEFAULT_CONTRACT_VALUES.serviceFee },
    { description: "Phi gui o to", amount: DEFAULT_CONTRACT_VALUES.carFee },
    { description: "Phi gui xe may", amount: DEFAULT_CONTRACT_VALUES.motorbikeFee },
    { description: "Phi dien", amount: electricityAmount },
    { description: "Phi nuoc", amount: waterAmount }
  ];

  const totalAmount = details.reduce((sum, item) => sum + Number(item.amount), 0);

  return TestDataFactory.buildInvoicePayload({
    contractId: contract.id,
    customerId: contract.customer.id,
    month,
    year,
    dueDate,
    status: overrides.status ?? TestDataFactory.invoiceStatus.pending,
    totalAmount,
    details,
    electricityUsage,
    waterUsage
  });
}

export async function createContractScenario(testState: TestStateFixture): Promise<ContractState> {
  return testState.createContract();
}

export async function createManagedInvoiceForContract(
  testState: TestStateFixture,
  contract: ContractState,
  overrides: InvoiceSeedOverrides = {}
): Promise<InvoiceScenarioRecord> {
  const payload = buildManagedInvoicePayload(contract, overrides);
  const invoice = await testState.createInvoice(contract, payload);

  return {
    ...invoice,
    status: invoice.status as InvoiceScenarioRecord["status"]
  };
}

export async function findCreatedInvoiceForPeriod(
  contract: ContractState,
  period: { month: number; year: number }
): Promise<InvoiceScenarioRecord | undefined> {
  const rows = await MySqlDbClient.query<{ id: number; status: InvoiceStatus }>(
    `
      SELECT id, status
      FROM invoice
      WHERE contract_id = ? AND customer_id = ? AND month = ? AND year = ?
      ORDER BY id DESC
      LIMIT 1
    `,
    [contract.id, contract.customer.id, period.month, period.year]
  );

  const row = rows[0];
  if (!row) {
    return undefined;
  }

  return {
    id: row.id,
    contractId: contract.id,
    customerId: contract.customer.id,
    month: period.month,
    year: period.year,
    status: String(row.status) as InvoiceScenarioRecord["status"]
  };
}

export async function invoicePeriodCount(
  contract: ContractState,
  period: { month: number; year: number }
): Promise<number> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    `
      SELECT COUNT(*) AS count
      FROM invoice
      WHERE contract_id = ? AND customer_id = ? AND month = ? AND year = ?
    `,
    [contract.id, contract.customer.id, period.month, period.year]
  );

  return Number(rows[0]?.count ?? 0);
}

export async function invoiceExists(invoiceId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM invoice WHERE id = ?", [invoiceId]);
  return rows.length > 0;
}

export async function readInvoiceStatus(invoiceId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ status: string }>("SELECT status FROM invoice WHERE id = ?", [invoiceId]);
  return rows[0]?.status ?? "";
}

export async function readInvoiceDueDate(invoiceId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ due_date: string }>(
    "SELECT DATE_FORMAT(due_date, '%Y-%m-%d') AS due_date FROM invoice WHERE id = ?",
    [invoiceId]
  );
  return rows[0]?.due_date ?? "";
}

export async function readInvoiceEditState(invoiceId: number): Promise<string> {
  const rows = await MySqlDbClient.query<{ status: string; due_date: string }>(
    "SELECT status, DATE_FORMAT(due_date, '%Y-%m-%d') AS due_date FROM invoice WHERE id = ?",
    [invoiceId]
  );
  return `${rows[0]?.status ?? ""}|${rows[0]?.due_date ?? ""}`;
}

export async function readInvoiceSummary(
  invoiceId: number
): Promise<{ status: string; totalAmount: number } | undefined> {
  const rows = await MySqlDbClient.query<{ status: string; total_amount: number }>(
    "SELECT status, total_amount FROM invoice WHERE id = ?",
    [invoiceId]
  );
  const row = rows[0];
  return row ? { status: row.status, totalAmount: Number(row.total_amount) } : undefined;
}

export async function markInvoicePaid(invoiceId: number): Promise<void> {
  await MySqlDbClient.execute("UPDATE invoice SET status = 'PAID' WHERE id = ?", [invoiceId]);
}

export async function readInvoicePaymentState(invoiceId: number): Promise<InvoicePaymentState> {
  const rows = await MySqlDbClient.query<NonNullable<InvoicePaymentState>>(
    "SELECT status, payment_method, transaction_code, paid_date FROM invoice WHERE id = ?",
    [invoiceId]
  );
  return rows[0] ?? null;
}

export async function markInvoicePaidByBankQr(
  invoiceId: number,
  transactionCode = `E2E-TX-${invoiceId}`
): Promise<void> {
  await MySqlDbClient.execute(
    `
      UPDATE invoice
      SET status = 'PAID',
          paid_date = CURRENT_TIMESTAMP,
          payment_method = 'BANK_QR',
          transaction_code = ?
      WHERE id = ?
    `,
    [transactionCode, invoiceId]
  );
}

export async function readInvoiceTotalAmount(invoiceId: number): Promise<number> {
  const rows = await MySqlDbClient.query<{ total_amount: number }>("SELECT total_amount FROM invoice WHERE id = ?", [
    invoiceId
  ]);
  return Number(rows[0]?.total_amount ?? 0);
}

export async function paidInvoicePeriodCount(invoiceId: number, month: number, year: number): Promise<number> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    `
      SELECT COUNT(*) AS count
      FROM invoice
      WHERE id = ? AND month = ? AND year = ? AND status = 'PAID'
    `,
    [invoiceId, month, year]
  );
  return Number(rows[0]?.count ?? 0);
}

export async function deleteInvoiceIfPresent(testState: TestStateFixture, invoiceId?: number): Promise<void> {
  await testState.deleteInvoice(invoiceId);
}

export async function cleanupContractScenario(
  testState: TestStateFixture,
  contract: ContractState | null,
  invoiceIds: number[] = []
): Promise<void> {
  for (const invoiceId of invoiceIds) {
    await deleteInvoiceIfPresent(testState, invoiceId);
  }

  if (contract) {
    await testState.deleteContract(contract);
  }
}
