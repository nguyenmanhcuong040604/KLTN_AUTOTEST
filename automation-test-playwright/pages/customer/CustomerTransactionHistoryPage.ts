import { type Locator } from "@playwright/test";
import { CrudListPage } from "../core/CrudListPage";

export class CustomerTransactionHistoryPage extends CrudListPage {
  protected readonly path = "/customer/transaction/history";

  private readonly monthSelect = this.page.locator('[name="month"]');
  private readonly yearSelect = this.page.locator('[name="year"]');
  private readonly transactionFilterForm = this.page.locator("#filterForm");
  private readonly transactionTableBody = this.page.locator("#transactionTableBody");
  private readonly resultBanner = this.page.locator("#totalTransactionSearch");
  private readonly pagination = this.page.locator("#transactionPagination");

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/transaction\/history/);
    await this.waitForVisible(this.transactionFilterForm);
    await this.waitForVisible(this.transactionTableBody);
  }

  async waitForSummaryVisible(): Promise<void> {
    await this.waitForVisible(this.page.locator(".stats-bar"));
    await this.waitForLocatorCount(this.page.locator(".stats-bar .stat-item"), 2);
  }

  async waitForSummaryValues(totalTransactions: number, totalAmountText: string): Promise<void> {
    const stats = this.page.locator(".stats-bar .stat-item");
    await this.waitForLocatorText(stats.nth(0), String(totalTransactions));
    await this.waitForLocatorText(stats.nth(1), totalAmountText);
  }

  /**
   * Applies the month filter.
   */
  async filterByMonth(month: number | string): Promise<void> {
    await this.monthSelect.selectOption(String(month));
  }

  /**
   * Applies the year filter.
   */
  async filterByYear(year: number | string): Promise<void> {
    await this.yearSelect.selectOption(String(year));
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.firstVisible(this.transactionFilterForm.locator(".btn-filter, button[type='submit']")).click();
  }

  /**
   * Resets the active filters.
   */
  async resetFilters(): Promise<void> {
    await this.firstVisible(this.transactionFilterForm.locator(".btn-reset, button[type='button']")).click();
  }

  async waitForResultCountBanner(total: number): Promise<void> {
    await this.waitForVisible(this.resultBanner);
    await this.waitForLocatorText(this.resultBanner, String(total));
  }

  rowByBuildingName(buildingName: string): Locator {
    return this.firstVisible(this.transactionTableBody.locator("tr").filter({ hasText: buildingName }));
  }

  async openTransactionDetail(buildingName: string): Promise<void> {
    await this.rowByBuildingName(buildingName).click();
  }

  async waitForDetailModalContains(text: string | RegExp): Promise<void> {
    const modal = this.page.locator(".modal.show");
    await this.waitForVisible(modal);
    const modalText = await this.locatorLooseText(modal);
    if (typeof text === "string") {
      if (!modalText.includes(this.normalizeLooseText(text))) {
        throw new Error(`Transaction detail modal did not contain expected text: ${text}`);
      }
      return;
    }

    const normalizedPattern = new RegExp(this.normalizeLooseText(text.source), text.flags.replace("g", ""));
    if (!normalizedPattern.test(modalText)) {
      throw new Error(`Transaction detail modal did not match expected text: ${String(text)}`);
    }
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
      async () => /khong co giao dich nao/i.test(await this.locatorLooseText(this.transactionTableBody)),
      "Transaction empty state was not displayed."
    );
  }

  async waitForPaginationHidden(): Promise<void> {
    await this.waitForEmpty(this.pagination);
  }
}
