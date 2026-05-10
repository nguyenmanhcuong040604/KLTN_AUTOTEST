import { type Locator, type Page } from "@playwright/test";
import { CustomerShellPage } from "../core/CustomerShellPage";

export class CustomerServicePage extends CustomerShellPage {
  protected readonly path = "/customer/service";
  readonly categories: Locator;
  private readonly cards: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.categories = this.page.locator(".category-section");
    this.cards = this.page.locator(".service-card");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/service/);
    await this.waitForTitleContainsLoose("dich vu", "suntower");
    await this.waitForLocatorCount(this.categories, 2);
  }

  async cardByTitle(title: string): Promise<Locator> {
    const expectedWords = this.serviceTitleWords(title);
    const count = await this.cards.count();

    for (let index = 0; index < count; index += 1) {
      const card = this.cards.nth(index);
      const normalizedText = this.normalizeLooseText(await card.innerText());
      if (expectedWords.every((word) => normalizedText.includes(word))) {
        return card;
      }
    }

    return this.firstVisible(this.cards.filter({ hasText: title }));
  }

  async waitForCardVisible(title: string): Promise<void> {
    await this.waitForVisible(await this.cardByTitle(title));
  }

  async waitForRequestDisabled(title: string): Promise<void> {
    await this.waitForDisabled((await this.cardByTitle(title)).locator(".btn-request"));
  }

  private serviceTitleWords(title: string): string[] {
    const normalizedTitle = this.normalizeLooseText(title);
    const aliases: Array<{ includes: string[]; words: string[] }> = [
      { includes: ["xe"], words: ["xe", "to"] },
      { includes: ["internet"], words: ["internet"] },
      { includes: ["gym"], words: ["gym"] },
      { includes: ["an ninh"], words: ["an", "ninh"] }
    ];
    return (
      aliases.find((alias) => alias.includes.every((word) => normalizedTitle.includes(word)))?.words ??
      normalizedTitle.split(" ")
    );
  }
}
