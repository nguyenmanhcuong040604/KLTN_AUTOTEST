import { type Locator, type Page } from "@playwright/test";
import { CrudListPage } from "../core/CrudListPage";

export class StaffBuildingListPage extends CrudListPage {
  protected readonly path = "/staff/buildings";
  readonly list: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.list = this.page.locator("#buildingCardsContainer");
  }

  async waitForLoaded(): Promise<void> {
    await this.waitForPageUrl(/\/staff\/buildings/);
    await this.waitForVisible(this.page.locator('a.nav-link.active[href="/staff/buildings"]'));
    await this.waitForVisible(this.list);
  }

  async waitForBuildingData(): Promise<void> {
    await this.waitForCondition(async () => {
      const hasCards = (await this.page.locator("#buildingCardsContainer .building-card").count()) > 0;
      const hasEmpty = await this.page
        .locator("#buildingCardsContainer .empty-state")
        .isVisible()
        .catch(() => false);
      return hasCards || hasEmpty;
    }, "Staff building list did not load cards or empty state.");
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
    return this.firstVisible(this.page.locator("#buildingCardsContainer .building-card").filter({ hasText: name }));
  }

  async openBuildingDetail(name: string): Promise<void> {
    await this.cardByBuildingName(name).locator(".btn-view-detail").click();
  }

  async waitForDetailModalContains(name: string): Promise<void> {
    await this.waitForLocatorText(this.page.locator("#modalContainer .modal.show"), name);
  }
}
