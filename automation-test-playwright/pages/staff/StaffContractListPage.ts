import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class StaffContractListPage extends CrudListPage {
  protected readonly path = "/staff/contracts";
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.tableBody = this.page.locator("#contractTableBody");
    this.table = new TableComponent(page, "#contractTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.waitForVisible(this.page.locator('a.nav-link.active[href="/staff/contracts"]'));
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  /**
   * Applies the customer filter.
   */
  async filterByCustomer(customerId: number | string): Promise<void> {
    await this.selectFilter("customerId", String(customerId));
  }

  /**
   * Applies the building filter.
   */
  async filterByBuilding(buildingId: number | string): Promise<void> {
    await this.selectFilter("buildingId", String(buildingId));
  }

  /**
   * Applies the status filter.
   */
  async filterByStatus(status: "ACTIVE" | "EXPIRED"): Promise<void> {
    await this.selectFilter("status", status);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.search();
  }

  rowByContractText(text: string): Locator {
    return this.table.rowByText(text);
  }

  async openContractDetail(text: string): Promise<void> {
    await this.rowByContractText(text).locator(".btn-view").click();
  }

  async waitForDetailModalContains(text: string): Promise<void> {
    await this.waitForLocatorText(this.page.locator(".modal.show"), text);
  }
}
