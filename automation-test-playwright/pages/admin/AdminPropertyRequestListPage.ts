import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminPropertyRequestListPage extends CrudListPage {
  protected readonly path = "/admin/property-request/list";
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.tableBody = this.page.locator("#requestTableBody");
    this.table = new TableComponent(page, "#requestTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/property-request\/list/);
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  /**
   * Applies the status filter.
   */
  async filterByStatus(status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED" | ""): Promise<void> {
    await this.page.locator("#statusFilter").selectOption(status);
  }

  rowByRequestId(requestId: number): Locator {
    return this.firstVisible(this.page.locator("#requestTableBody tr").filter({ hasText: `#${requestId}` }));
  }

  async openDetail(requestId: number): Promise<void> {
    await this.rowByRequestId(requestId).locator(`a[href="/admin/property-request/${requestId}"]`).click();
  }
}
