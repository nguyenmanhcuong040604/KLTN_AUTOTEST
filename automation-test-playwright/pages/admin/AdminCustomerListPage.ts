import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminCustomerListPage extends CrudListPage {
  protected readonly path = "/admin/customer/list";
  readonly addButton: Locator;
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addButton = this.page.locator(".btn-add");
    this.tableBody = this.page.locator("#customerTableBody");
    this.table = new TableComponent(page, "#customerTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/customer\/(list|search)/);
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  async openAddForm(): Promise<void> {
    await this.addButton.click();
  }

  rowByCustomerName(name: string): Locator {
    return this.table.rowByText(name);
  }

  /**
   * Applies the full name filter.
   */
  async filterByFullName(fullName: string): Promise<void> {
    await this.fillFilter("fullName", fullName);
  }

  async openDetail(customerText: string): Promise<void> {
    await this.clickRowLink(customerText, "/admin/customer/");
  }

  /**
   * Deletes customer through the UI.
   */
  async deleteCustomer(customerText: string): Promise<void> {
    await this.actionButton(this.rowByCustomerName(customerText), "delete").click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
