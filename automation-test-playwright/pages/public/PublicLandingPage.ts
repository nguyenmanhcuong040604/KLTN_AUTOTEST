import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";
import { OptionalActionHelper } from "@helpers-browser/OptionalActionHelper";

/**
 * Page object for the public building search page (/suntower).
 *
 * Covers three UI regions:
 * 1. **Filter panel** – search fields, dropdowns, toggle, reset
 * 2. **Building results** – cards, pagination, empty state, summary
 * 3. **Detail modal** – building info sections, close action
 */
export class PublicLandingPage extends BasePage {
  // ── Filter panel ──────────────────────────────────────────────

  readonly filterForm: Locator;
  readonly filterBody: Locator;
  readonly toggleFilterButton: Locator;
  readonly buildingNameInput: Locator;
  readonly searchButton: Locator;
  readonly resetButton: Locator;

  // ── Building results ──────────────────────────────────────────

  readonly totalBuilding: Locator;
  readonly buildingList: Locator;
  readonly buildingCards: Locator;
  readonly emptyState: Locator;
  readonly paginationContainer: Locator;
  readonly paginationButtons: Locator;

  // ── Detail modal ──────────────────────────────────────────────

  readonly detailModal: Locator;
  readonly detailModalTitle: Locator;
  readonly detailModalBody: Locator;
  readonly detailModalCloseButton: Locator;

  constructor(page: Page) {
    super(page);

    // Filter panel locators
    this.filterForm = this.anyLocator('[data-testid="public-filter-form"]', "#filterForm", "form");
    this.filterBody = this.anyLocator('[data-testid="public-filter-body"]', "#filterBody");
    this.toggleFilterButton = this.anyLocator('[data-testid="public-filter-toggle"]', ".btn-toggle-filter");
    this.buildingNameInput = this.firstVisible(
      this.anyLocator('[data-testid="public-building-name"]', '[name="name"]')
    );
    this.searchButton = this.firstVisible(this.anyLocator('[data-testid="public-search"]', 'button[type="submit"]'));
    this.resetButton = this.firstVisible(this.anyLocator('[data-testid="public-reset"]', ".btn-reset"));

    // Building results locators
    this.totalBuilding = this.anyLocator('[data-testid="public-total-building"]', "#totalBuilding");
    this.buildingList = this.anyLocator('[data-testid="public-building-list"]', "#buildingList");
    this.buildingCards = this.page.locator('[data-testid="building-card"], .building-card');
    this.emptyState = this.page.locator(".empty-state");
    this.paginationContainer = this.page.locator("#paginationContainer");
    this.paginationButtons = this.paginationContainer.locator("button");

    // Detail modal locators
    this.detailModal = this.page.locator("#modalContainer .modal.show");
    this.detailModalTitle = this.detailModal.locator(".modal-title");
    this.detailModalBody = this.detailModal.locator(".modal-body");
    this.detailModalCloseButton = this.firstVisible(this.detailModal.locator(".btn-close, .modal-footer button"));
  }

  // ── Page navigation ───────────────────────────────────────────

  /**
   * Opens the public building search page with an optional query string.
   */
  async open(query = ""): Promise<void> {
    const normalizedQuery = query ? (query.startsWith("?") ? query : `?${query}`) : "";
    await this.visit(`/suntower${normalizedQuery}`);
  }

  // ── Filter panel actions ──────────────────────────────────────

  /**
   * Types a building name into the search input and triggers a search.
   */
  async searchByBuildingName(name: string): Promise<void> {
    await this.buildingNameInput.fill(name);
    await this.search();
  }

  /**
   * Alias for {@link searchByBuildingName}.
   */
  async searchBuilding(name: string): Promise<void> {
    await this.searchByBuildingName(name);
  }

  filterSelect(name: string): Locator {
    return this.inputByName(name);
  }

  filterInput(name: string): Locator {
    return this.inputByName(name);
  }

  /**
   * Clicks the search button and waits for results to settle.
   */
  async search(): Promise<void> {
    await this.searchButton.click();
    await this.waitForResultsSettled();
  }

  /**
   * Fills a single filter text field by its form name attribute.
   */
  async fillFilter(fieldName: string, value: string): Promise<void> {
    await this.filterInput(fieldName).fill(value);
  }

  /**
   * Fills a filter text field only if the element is visible on the page.
   */
  async fillFilterIfPresent(fieldName: string, value: string): Promise<boolean> {
    return OptionalActionHelper.fillIfPresent(this.filterInput(fieldName), value);
  }

  /**
   * Fills a pair of numeric range fields (e.g. areaFrom / areaTo).
   */
  async fillNumberRange(fieldNameFrom: string, fieldNameTo: string, fromValue: string, toValue: string): Promise<void> {
    await this.fillFilter(fieldNameFrom, fromValue);
    await this.fillFilter(fieldNameTo, toValue);
  }

  /**
   * Selects an option in a filter dropdown by its form name attribute.
   */
  async selectFilter(fieldName: string, value: string): Promise<void> {
    await this.filterSelect(fieldName).selectOption(value);
  }

  /**
   * Selects a district from the district dropdown.
   */
  async selectDistrict(value: string): Promise<void> {
    await this.selectFilter("districtId", value);
  }

  /**
   * Selects a district only if the dropdown is present on the page.
   */
  async selectDistrictIfAvailable(value: string): Promise<boolean> {
    return OptionalActionHelper.selectIfPresent(
      this.anyLocator('[data-testid="public-district"]', '[name="districtId"]'),
      value
    );
  }

  /**
   * Clicks the reset button and waits for the building name input to clear.
   */
  async resetFilters(): Promise<void> {
    await this.resetButton.click();
    await this.waitForLocatorValue(this.buildingNameInput, "");
  }

  /**
   * Clicks the reset button only if it is visible.
   */
  async resetFiltersIfAvailable(): Promise<boolean> {
    return OptionalActionHelper.clickIfPresent(this.resetButton);
  }

  /**
   * Toggles the collapsible filter panel open or closed.
   */
  async toggleFilterPanel(): Promise<void> {
    await this.toggleFilterButton.click();
  }

  async optionCount(fieldName: string): Promise<number> {
    return this.filterSelect(fieldName).locator("option").count();
  }

  async filterValue(fieldName: string): Promise<string> {
    return this.filterInput(fieldName).inputValue();
  }

  async selectedValue(fieldName: string): Promise<string> {
    return this.filterSelect(fieldName).inputValue();
  }

  async isFilterCollapsed(): Promise<boolean> {
    return this.filterBody.evaluate((element) => element.classList.contains("collapsed"));
  }

  async storedFilterCollapsedValue(): Promise<string | null> {
    return this.page.evaluate(() => window.localStorage.getItem("filterCollapsed"));
  }

  /**
   * Waits until all filter dropdown metadata (district, ward, street, direction, level)
   * has been loaded with more than one option each.
   */
  async waitForFilterMetadataLoaded(): Promise<void> {
    await this.waitForCondition(
      async () => (await this.optionCount("districtId")) > 1,
      "District metadata was not loaded."
    );
    await this.waitForCondition(async () => (await this.optionCount("ward")) > 1, "Ward metadata was not loaded.");
    await this.waitForCondition(async () => (await this.optionCount("street")) > 1, "Street metadata was not loaded.");
    await this.waitForCondition(
      async () => (await this.optionCount("direction")) > 1,
      "Direction metadata was not loaded."
    );
    await this.waitForCondition(async () => (await this.optionCount("level")) > 1, "Level metadata was not loaded.");
  }

  // ── Building results actions ──────────────────────────────────

  cardByName(name: string): Locator {
    return this.firstVisible(this.page.locator('[data-testid="building-card"], .building-card', { hasText: name }));
  }

  /**
   * Waits until the card for the given building shows the icon fallback
   * instead of a real image (verifies missing-image handling).
   */
  async waitForCardUsesBuildingIconFallback(name: string): Promise<void> {
    const targetCard = this.cardByName(name);
    await this.waitForVisible(targetCard);
    await this.waitForLocatorCount(targetCard.locator(".building-image img"), 0);
    await this.waitForVisible(targetCard.locator(".building-image .bi-building"));
  }

  /**
   * Clicks a building card by name and waits for the detail modal to appear.
   */
  async openBuildingDetailsByName(name: string): Promise<void> {
    await this.cardByName(name).click();
    await this.waitForDetailModalVisible(name);
  }

  /**
   * Clicks the first visible building card and waits for the detail modal.
   */
  async openFirstBuildingDetails(): Promise<void> {
    await this.firstVisible(this.buildingCards).click();
    await this.waitForVisible(this.detailModal);
  }

  async cardCount(): Promise<number> {
    return this.buildingCards.count();
  }

  async cardNames(): Promise<string[]> {
    const names = await this.buildingCards.locator(".building-name").allTextContents();
    return names.map((name) => name.trim()).filter(Boolean);
  }

  async resultSummaryText(): Promise<string> {
    return this.locatorLooseText(this.totalBuilding);
  }

  /**
   * Navigates to a specific pagination page and waits for results to settle.
   */
  async clickPaginationPage(pageNumber: number): Promise<void> {
    await this.paginationButtons.getByText(String(pageNumber), { exact: true }).click();
    await this.waitForResultsSettled();
  }

  async paginationCount(): Promise<number> {
    return this.paginationButtons.count();
  }

  paginationButton(pageNumber: number): Locator {
    return this.paginationButtons.getByText(String(pageNumber), { exact: true });
  }

  async activePaginationText(): Promise<string> {
    return this.paginationButtons.evaluateAll((buttons) => {
      const activeButton = buttons.find((button) => button.getAttribute("style")?.includes("font-weight:700"));
      return activeButton?.textContent?.trim() ?? "";
    });
  }

  async waitForResultsSettled(): Promise<void> {
    await this.waitForVisible(this.buildingList);
    await this.waitForCondition(async () => {
      const cards = await this.buildingCards.count();
      const emptyVisible = await this.emptyState.isVisible().catch(() => false);
      const summary = (await this.totalBuilding.textContent())?.trim() ?? "";
      return cards > 0 || emptyVisible || /tim thay/i.test(this.normalizeLooseText(summary));
    }, "Public building results did not settle.");
  }

  async waitForResultsLoaded(): Promise<void> {
    await this.waitForFilterMetadataLoaded();
    await this.waitForResultsSettled();
  }

  /**
   * Waits for the empty state message to appear when no buildings match the filter.
   */
  async waitForEmptyState(): Promise<void> {
    await this.waitForVisible(this.emptyState);
    await this.waitForCondition(
      async () => /khong tim thay bat dong san/i.test(await this.locatorLooseText(this.emptyState)),
      "Public building empty state was not displayed."
    );
  }

  async waitForHasResults(): Promise<void> {
    await this.waitForVisible(this.firstVisible(this.buildingCards));
  }

  // ── Detail modal actions ──────────────────────────────────────

  /**
   * Closes the building detail modal and waits for it to disappear.
   */
  async closeDetailModal(): Promise<void> {
    await this.detailModalCloseButton.click();
    await this.waitForHidden(this.detailModal);
  }

  detailSection(title: string): Locator {
    return this.detailModalBody.locator(".info-section", {
      has: this.page.locator(".info-section-title", { hasText: title })
    });
  }

  /**
   * Waits until the detail modal is visible and its title shows "Thong tin bat dong san".
   * Optionally asserts the modal body contains the given building name.
   */
  async waitForDetailModalVisible(buildingName?: string): Promise<void> {
    await this.waitForVisible(this.detailModal);
    await this.waitForCondition(
      async () => /thong tin bat dong san/i.test(await this.locatorLooseText(this.detailModalTitle)),
      "Building detail modal title was not displayed."
    );
    if (buildingName) {
      await this.waitForLocatorText(this.detailModalBody, buildingName);
    }
  }

  async detailModalLooseText(): Promise<string> {
    return this.locatorLooseText(this.detailModalBody);
  }

  // ── Assertions ────────────────────────────────────────────────

  /**
   * Verifies that the page is fully loaded (filter metadata + building results).
   */
  async assertLoaded(): Promise<void> {
    await this.waitForResultsLoaded();
  }
}
