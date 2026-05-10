import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminInvoiceListPage extends CrudListPage {
  protected readonly path = "/admin/invoice/list";
  readonly addInvoiceButton: Locator;
  readonly updateStatusesButton: Locator;
  readonly invoiceTableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addInvoiceButton = this.page.locator(".btn-hd-primary");
    this.updateStatusesButton = this.page.locator(".btn-hd-green");
    this.invoiceTableBody = this.page.locator("#invoiceTableBody");
    this.table = new TableComponent(page, "#invoiceTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/invoice\/list/);
    await this.waitForVisible(this.invoiceTableBody);
  }

  rowByInvoiceId(invoiceId: number): Locator {
    return this.firstVisible(
      this.page
        .locator("#invoiceTableBody tr")
        .filter({ has: this.page.locator(".invoice-id", { hasText: String(invoiceId) }) })
    );
  }

  async waitForTableData(): Promise<void> {
    await this.waitForVisible(this.invoiceTableBody);
    await this.table.waitForDataOrEmpty();
  }

  async openAddForm(): Promise<void> {
    await this.addInvoiceButton.click();
  }

  async openDetail(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/invoice/");
  }

  async openEdit(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/invoice/edit/");
  }

  /**
   * Deletes invoice through the UI.
   */
  async deleteInvoice(invoiceId: number): Promise<void> {
    await this.actionButton(this.rowByInvoiceId(invoiceId), "delete").click();
  }

  /**
   * Applies the month filter.
   */
  async filterByMonth(month: number): Promise<void> {
    await this.fillFilter("month", String(month));
  }

  /**
   * Applies the year filter.
   */
  async filterByYear(year: number): Promise<void> {
    await this.fillFilter("year", String(year));
  }

  /**
   * Applies the status filter.
   */
  async filterByStatus(status: "PENDING" | "PAID" | "OVERDUE"): Promise<void> {
    await this.selectFilter("status", status);
  }

  /**
   * Applies the customer filter.
   */
  async filterByCustomer(customerId: number): Promise<void> {
    await this.selectFilter("customerId", String(customerId));
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    const searchForm = this.page.locator("form[action*='/admin/invoice/search']");
    await Promise.all([
      this.page.waitForURL(/\/admin\/invoice\/search(\?|$)/),
      searchForm.evaluate((form) => (form as HTMLFormElement).requestSubmit())
    ]);
  }

  async updateStatuses(): Promise<void> {
    await this.updateStatusesButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
