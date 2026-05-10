import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class RegisterPage extends BasePage {
  readonly emailInput: Locator;
  readonly submitButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.emailInput = this.firstVisible(
      this.anyLocator('[data-testid="register-email"]', 'input[name="email"]', 'input[type="email"]')
    );
    this.submitButton = this.firstVisible(this.anyLocator('[data-testid="register-submit"]', 'button[type="submit"]'));
  }

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    await this.visit("/register");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/register/);
    await this.waitForVisible(this.emailInput);
    await this.waitForVisible(this.submitButton);
  }

  async requestRegistrationCode(email: string): Promise<void> {
    await this.emailInput.fill(email);
    await this.submitButton.click();
  }
}
