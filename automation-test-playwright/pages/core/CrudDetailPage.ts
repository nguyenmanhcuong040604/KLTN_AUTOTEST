import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "./BasePage";

export class CrudDetailPage extends BasePage {
  protected readonly detailPath?: string;
  readonly pageHeader: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.pageHeader = this.firstVisible(page.locator("h1, h2"));
  }

  /**
   * Opens the page.
   */
  async open(id: number): Promise<void> {
    if (!this.detailPath) {
      throw new Error("This detail page does not define a detail path.");
    }

    await this.visit(`${this.detailPath}/${id}`);
  }

  async waitForHeaderContains(text: string): Promise<void> {
    await this.waitForLocatorText(this.pageHeader, text);
  }
}
