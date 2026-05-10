import { type Locator, type Page } from "@playwright/test";
import { CrudFormPage } from "../core/CrudFormPage";

export class AdminInvoiceFormPage extends CrudFormPage {
  protected readonly addPath = "/admin/invoice/add";
  protected readonly editPath = "/admin/invoice/edit";
  readonly customerSelect: Locator;
  readonly contractSelect: Locator;
  readonly totalAmount: Locator;
  readonly warningBox: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.customerSelect = this.page.locator("#customerSelect, [name='customerId']");
    this.contractSelect = this.page.locator("#contractSelect, [name='contractId']");
    this.totalAmount = this.page.locator("#totalAmount");
    this.warningBox = this.page.locator("#notPendingWarning");
  }

  async waitForAddLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/invoice\/add/);
    await this.waitForVisible(this.page.locator("#invoiceForm"));
  }

  async waitForEditLoaded(invoiceId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/invoice/edit/${invoiceId}$`));
    await this.waitForVisible(this.page.locator("#invoiceEditForm"));
  }

  async selectCustomer(customerId: number): Promise<void> {
    await this.customerSelect.selectOption(String(customerId));
  }

  async selectContract(contractId: number): Promise<void> {
    await this.contractSelect.selectOption(String(contractId));
  }

  async fillAddForm(input: {
    customerId: number;
    contractId: number;
    month: number;
    year: number;
    dueDate: string;
    electricityUsage: number;
    waterUsage: number;
  }): Promise<void> {
    await this.selectCustomer(input.customerId);
    await this.selectContract(input.contractId);
    await this.page.locator('[name="month"]').selectOption(String(input.month));
    await this.page.locator('[name="year"]').fill(String(input.year));
    await this.page.locator('[name="dueDate"]').fill(input.dueDate);
    await this.page.locator('[name="electricityUsage"]').fill(String(input.electricityUsage));
    await this.page.locator('[name="waterUsage"]').fill(String(input.waterUsage));
  }

  async fillEditForm(input: { dueDate: string; electricityUsage: number; waterUsage: number }): Promise<void> {
    await this.page.locator('[name="dueDate"]').fill(input.dueDate);
    await this.page.locator('[name="electricityUsage"]').fill(String(input.electricityUsage));
    await this.page.locator('[name="waterUsage"]').fill(String(input.waterUsage));
  }

  /**
   * Reads total amount text from the page.
   */
  async readTotalAmountText(): Promise<string> {
    return (await this.totalAmount.innerText()).trim();
  }

  async waitForWarningVisible(): Promise<void> {
    await this.waitForVisible(this.warningBox);
  }

  async submitInvoice(): Promise<void> {
    await this.submitButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
