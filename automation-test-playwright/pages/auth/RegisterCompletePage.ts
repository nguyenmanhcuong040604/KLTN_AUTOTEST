import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class RegisterCompletePage extends BasePage {
  readonly fullNameInput: Locator;
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly completeButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.fullNameInput = this.firstVisible(page.locator('input[name="fullName"]'));
    this.usernameInput = this.firstVisible(page.locator('input[name="username"]'));
    this.passwordInput = this.firstVisible(page.locator('input[name="password"]'));
    this.confirmPasswordInput = this.firstVisible(page.locator('input[name="confirmPassword"]'));
    this.completeButton = this.firstVisible(
      this.anyLocator(
        '[data-testid="register-complete-submit"]',
        'form[action="/auth/register/complete"] button[type="submit"]',
        "button.submit",
        'button:has-text("Tạo tài khoản")'
      )
    );
  }

  /**
   * Opens the page.
   */
  async open(ticket: string, email: string): Promise<void> {
    await this.visit(`/register/complete?ticket=${encodeURIComponent(ticket)}&email=${encodeURIComponent(email)}`);
  }

  async waitForLoaded(email?: string): Promise<void> {
    await this.page.waitForURL(/\/register\/complete/);
    await this.waitForVisible(this.fullNameInput);
    await this.waitForVisible(this.usernameInput);
    await this.waitForVisible(this.completeButton);
    await this.dismissSweetAlertIfPresent();
    if (email) {
      await this.waitForLocatorText(this.page.locator("body"), email);
    }
  }

  async completeRegistration(
    fullName: string,
    username: string,
    password: string,
    confirmPassword = password
  ): Promise<void> {
    await this.dismissSweetAlertIfPresent();
    await this.fullNameInput.fill(fullName);
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.confirmPasswordInput.fill(confirmPassword);
    await this.completeButton.click();
  }

  async waitForPopupContains(text: string | RegExp): Promise<void> {
    await this.waitForVisible(this.toastPopup());
    await this.waitForLocatorText(this.toastPopup(), text);
  }
}
