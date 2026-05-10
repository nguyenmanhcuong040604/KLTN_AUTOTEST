import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

/**
 * Page object for the /login page.
 * Handles credential input, social login, and navigation to registration / forgot password.
 */
export class LoginPage extends BasePage {
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly googleLoginButton: Locator;
  readonly forgotPasswordLink: Locator;
  readonly registerLink: Locator;

  /**
   * Creates locators for all login form elements and navigation links.
   */
  constructor(page: Page) {
    super(page);
    this.usernameInput = this.anyLocator('[data-testid="login-username"]', "#username", '[name="username"]');
    this.passwordInput = this.anyLocator('[data-testid="login-password"]', "#password", '[name="password"]');
    this.submitButton = this.anyLocator('[data-testid="login-submit"]', ".login-button", 'button[type="submit"]');
    this.googleLoginButton = this.anyLocator('[data-testid="login-google"]', 'a[href="/oauth2/authorization/google"]');
    this.forgotPasswordLink = this.anyLocator('[data-testid="forgot-password-link"]', 'a[href="/forgot-password"]');
    this.registerLink = this.anyLocator('[data-testid="register-link"]', 'a[href="/register"]');
  }

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    await this.visit("/login");
  }

  async login(username: string, password: string): Promise<void> {
    await this.dismissSweetAlertIfPresent();
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }

  async clickForgotPassword(): Promise<void> {
    await this.forgotPasswordLink.click();
  }

  async clickRegister(): Promise<void> {
    await this.registerLink.click();
  }

  /**
   * Verifies that the page is loaded.
   */
  async assertLoaded(): Promise<void> {
    await this.page.waitForURL(/\/login/);
    await this.waitForVisible(this.usernameInput);
    await this.waitForVisible(this.passwordInput);
    await this.waitForVisible(this.submitButton);
    await this.dismissSweetAlertIfPresent();
  }

  async waitForPopupContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
