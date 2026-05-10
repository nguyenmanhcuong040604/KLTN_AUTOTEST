import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminStaffListPage extends CrudListPage {
  protected readonly path = "/admin/staff/list";
  readonly addButton: Locator;
  readonly adminTableBody: Locator;
  readonly staffTableBody: Locator;
  private readonly staffTable: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addButton = this.page.locator(".btn-hd-add, .btn-add");
    this.adminTableBody = this.page.locator("#adminTableBody");
    this.staffTableBody = this.page.locator("#staffTableBody");
    this.staffTable = new TableComponent(page, "#staffTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/staff\/(list|search)/);
    await this.waitForVisible(this.page.locator("#staffTableBody, #adminTableBody"));
  }

  async openAddForm(): Promise<void> {
    await this.addButton.click();
  }

  rowByStaffName(fullName: string): Locator {
    return this.firstVisible(this.page.locator("tbody tr").filter({ hasText: fullName }));
  }

  /**
   * Applies the full name filter.
   */
  async filterByFullName(fullName: string): Promise<void> {
    await this.fillFilter("fullName", fullName);
  }

  /**
   * Applies the role filter.
   */
  async filterByRole(role: "STAFF" | "ADMIN"): Promise<void> {
    await this.selectFilter("role", role);
  }

  async openDetail(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/staff/");
  }

  /**
   * Deletes staff through the UI.
   */
  async deleteStaff(text: string): Promise<void> {
    await this.actionButton(this.rowByStaffName(text), "delete").click();
  }

  async waitForSearchTableData(): Promise<void> {
    await this.staffTable.waitForDataOrEmpty();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
