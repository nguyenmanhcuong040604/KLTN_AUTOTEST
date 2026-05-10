import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class ForgotPasswordPage extends BasePage {
  readonly emailInput: Locator;
  readonly submitButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.emailInput = this.firstVisible(
      this.anyLocator('[data-testid="forgot-password-email"]', 'input[name="email"]', 'input[type="email"]')
    );
    this.submitButton = this.firstVisible(
      this.anyLocator('[data-testid="forgot-password-submit"]', 'button[type="submit"]')
    );
  }

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    await this.visit("/forgot-password");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/forgot-password/);
    await this.waitForVisible(this.emailInput);
    await this.waitForVisible(this.submitButton);
  }

  async submitEmail(email: string): Promise<void> {
    await this.emailInput.fill(email);
    await this.submitButton.click();
  }

  async waitForPopupContains(text: string | RegExp): Promise<void> {
    await this.waitForVisible(this.toastPopup());
    await this.waitForLocatorText(this.toastPopup(), text);
  }
}
