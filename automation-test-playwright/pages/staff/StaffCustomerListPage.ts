import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class StaffCustomerListPage extends CrudListPage {
  protected readonly path = "/staff/customers";
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.tableBody = this.page.locator("#customerTableBody");
    this.table = new TableComponent(page, "#customerTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/staff\/customers/);
    await this.waitForTitleContainsLoose("quan ly khach hang", "customer");
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  /**
   * Applies the full name filter.
   */
  async filterByFullName(fullName: string): Promise<void> {
    await this.fillFilter("fullName", fullName);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.search();
  }

  rowByCustomerName(name: string): Locator {
    return this.table.rowByText(name);
  }

  async openCustomerDetail(name: string): Promise<void> {
    await this.rowByCustomerName(name).locator(".btn-view").click();
  }

  async waitForDetailModalContains(name: string): Promise<void> {
    await this.waitForLocatorText(this.page.locator(".modal.show"), name);
  }
}
