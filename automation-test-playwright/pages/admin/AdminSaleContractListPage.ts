import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminSaleContractListPage extends CrudListPage {
  protected readonly path = "/admin/sale-contract/list";
  readonly addButton: Locator;
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addButton = this.page.locator(".btn-add");
    this.tableBody = this.page.locator("#saleContractTableBody");
    this.table = new TableComponent(page, "#saleContractTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/sale-contract\/(list|search)/);
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  async openAddForm(): Promise<void> {
    await this.addButton.click();
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
   * Applies the staff filter.
   */
  async filterByStaff(staffId: number | string): Promise<void> {
    await this.selectFilter("staffId", String(staffId));
  }

  /**
   * Applies the status filter.
   */
  async filterByStatus(status: "0" | "1"): Promise<void> {
    await this.selectFilter("status", status);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.search();
  }

  rowBySaleContractText(text: string): Locator {
    return this.table.rowByText(text);
  }

  async openDetail(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/sale-contract/");
  }

  async openEdit(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/sale-contract/edit/");
  }

  /**
   * Deletes sale contract through the UI.
   */
  async deleteSaleContract(text: string): Promise<void> {
    await this.actionButton(this.rowBySaleContractText(text), "delete").click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
