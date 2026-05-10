import { type Locator, type Page } from "@playwright/test";
import { CrudListPage } from "../core/CrudListPage";

export class CustomerContractListPage extends CrudListPage {
  protected readonly path = "/customer/contract/list";
  readonly list: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.list = this.page.locator("#contractList");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/contract\/list|\/customer\/contracts/);
    await this.waitForTitleContainsLoose("hop dong", "contract");
    await this.waitForVisible(this.list);
  }

  async waitForContractData(): Promise<void> {
    await this.waitForCondition(async () => {
      const hasCards = (await this.page.locator("#contractList .contract-container").count()) > 0;
      const hasEmpty = await this.page
        .locator("#contractList .empty-state")
        .isVisible()
        .catch(() => false);
      return hasCards || hasEmpty;
    }, "Contract list did not load cards or empty state.");
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

  cardByBuildingName(name: string): Locator {
    return this.firstVisible(this.page.locator("#contractList .contract-container").filter({ hasText: name }));
  }

  async waitForEmptyState(): Promise<void> {
    await this.waitForVisible(this.page.locator("#contractList .empty-state"));
  }
}
