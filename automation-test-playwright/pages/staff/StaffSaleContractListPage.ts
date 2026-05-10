import { type Locator } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { StaffShellPage } from "../core/StaffShellPage";

export class StaffSaleContractListPage extends StaffShellPage {
  protected readonly path = "/staff/sale-contracts";

  private readonly filterForm = this.page.locator("#filterForm");
  private readonly tableBody = this.page.locator("#saleContractTableBody");
  private readonly pagination = this.page.locator("#pagination");
  private readonly table = new TableComponent(this.page, "#saleContractTableBody");

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/staff\/sale-contracts/);
    await this.waitForVisible(this.filterForm);
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.waitForVisible(this.tableBody);
    await this.table.waitForDataOrEmpty();
  }

  /**
   * Applies the customer id filter.
   */
  async filterByCustomerId(customerId: number | string): Promise<void> {
    await this.filterForm.locator('[name="customerId"]').selectOption(String(customerId));
  }

  /**
   * Applies the building id filter.
   */
  async filterByBuildingId(buildingId: number | string): Promise<void> {
    await this.filterForm.locator('[name="buildingId"]').selectOption(String(buildingId));
  }

  /**
   * Applies the status filter.
   */
  async filterByStatus(status: "0" | "1"): Promise<void> {
    await this.filterForm.locator('[name="status"]').selectOption(status);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.firstVisible(this.filterForm.locator(".btn-filter.btn-search, button[type='submit']")).click();
  }

  /**
   * Resets the active filters.
   */
  async resetFilters(): Promise<void> {
    await this.firstVisible(this.filterForm.locator(".btn-filter.btn-reset, button[type='button']")).click();
  }

  rowByBuildingName(buildingName: string): Locator {
    return this.table.rowByText(buildingName);
  }

  async waitForRowVisible(buildingName: string): Promise<void> {
    await this.waitForVisible(this.rowByBuildingName(buildingName));
  }

  async openDetail(buildingName: string): Promise<void> {
    await this.rowByBuildingName(buildingName).locator(".btn-view").click();
  }

  async waitForDetailModalContains(text: string | RegExp): Promise<void> {
    const modal = this.page.locator(".modal.show");
    await this.waitForVisible(modal);
    await this.waitForLocatorText(modal, text);
  }

  /**
   * Closes the detail modal UI element.
   */
  async closeDetailModal(): Promise<void> {
    const modal = this.page.locator(".modal.show");
    await this.firstVisible(modal.locator(".modal-header .btn-close, .modal-footer button")).click();
    await this.waitForHidden(modal);
  }

  async waitForEmptyState(): Promise<void> {
    await this.waitForCondition(
      async () => /khong co du lieu/i.test(await this.locatorLooseText(this.tableBody)),
      "Sale contract empty state was not displayed."
    );
  }

  async waitForPaginationVisible(): Promise<void> {
    await this.waitForVisible(this.pagination);
  }
}
