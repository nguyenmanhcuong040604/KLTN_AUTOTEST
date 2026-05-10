import { type Locator, type Page } from "@playwright/test";
import { CrudListPage } from "../core/CrudListPage";

export class CustomerBuildingListPage extends CrudListPage {
  protected readonly path = "/customer/building/list";
  readonly list: Locator;
  private currentModalTarget: string | null = null;
  private currentBuildingName: string | null = null;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.list = this.page.locator("#buildingList");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/building\/list/);
    await this.waitForVisible(this.list);
  }

  async waitForBuildingData(): Promise<void> {
    await this.waitForCondition(async () => {
      const hasCards = (await this.page.locator("#buildingList .building-card").count()) > 0;
      const hasEmpty = await this.page
        .locator("#buildingList .empty-state")
        .isVisible()
        .catch(() => false);
      return hasCards || hasEmpty;
    }, "Customer building list did not load cards or empty state.");
  }

  /**
   * Applies the name filter.
   */
  async filterByName(name: string): Promise<void> {
    await this.fillFilter("name", name);
  }

  /**
   * Submits the active filters.
   */
  async submitFilters(): Promise<void> {
    await this.search();
  }

  cardByBuildingName(name: string): Locator {
    return this.firstVisible(this.page.locator("#buildingList .building-card").filter({ hasText: name }));
  }

  private detailModalByName(name: string): Locator {
    if (this.currentModalTarget) {
      return this.lastVisible(this.page.locator(this.currentModalTarget).filter({ hasText: name }));
    }

    return this.lastVisible(this.page.locator("#modalContainer .modal").filter({ hasText: name }));
  }

  async openBuildingDetail(name: string): Promise<void> {
    const card = this.cardByBuildingName(name);
    await this.waitForVisible(card);
    this.currentBuildingName = name;
    this.currentModalTarget = await card.getAttribute("data-bs-target");
    await card.click();

    if (!this.currentModalTarget) {
      return;
    }
    await this.waitForVisible(this.detailModalByName(name));
  }

  async waitForDetailModalContains(name: string): Promise<void> {
    const modal = this.detailModalByName(this.currentBuildingName ?? name);
    await this.waitForVisible(modal);
    await this.waitForLocatorText(modal, name);
  }

  async waitForEmptyState(): Promise<void> {
    await this.waitForVisible(this.page.locator("#buildingList .empty-state"));
  }
}
