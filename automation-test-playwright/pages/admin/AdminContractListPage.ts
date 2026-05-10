import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminContractListPage extends CrudListPage {
  protected readonly path = "/admin/contract/list";
  readonly addButton: Locator;
  readonly updateStatusesButton: Locator;
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addButton = this.page.locator(".btn-add");
    this.updateStatusesButton = this.page.locator(".btn-update-status");
    this.tableBody = this.page.locator("#contractTableBody");
    this.table = new TableComponent(page, "#contractTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/contract\/(list|search)/);
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
  async filterByStatus(status: "ACTIVE" | "EXPIRED"): Promise<void> {
    await this.selectFilter("status", status);
  }

  async fillRentPriceRange(rentPriceFrom?: number, rentPriceTo?: number): Promise<void> {
    if (typeof rentPriceFrom === "number") {
      await this.fillFilter("rentPriceFrom", String(rentPriceFrom));
    }
    if (typeof rentPriceTo === "number") {
      await this.fillFilter("rentPriceTo", String(rentPriceTo));
    }
  }

  rowByContractText(text: string): Locator {
    return this.table.rowByText(text);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.search();
  }

  async openDetail(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/contract/");
  }

  async openEdit(text: string): Promise<void> {
    await this.clickRowLink(text, "/admin/contract/edit/");
  }

  /**
   * Deletes contract through the UI.
   */
  async deleteContract(text: string): Promise<void> {
    await this.actionButton(this.rowByContractText(text), "delete").click();
  }

  async updateStatuses(): Promise<void> {
    await this.updateStatusesButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
