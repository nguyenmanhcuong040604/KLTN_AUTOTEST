import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class RegisterVerifyPage extends BasePage {
  readonly otpInput: Locator;
  readonly verifyButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.otpInput = this.firstVisible(page.locator('input[name="otp"]'));
    this.verifyButton = this.firstVisible(
      this.anyLocator(
        '[data-testid="register-verify-submit"]',
        'form[action="/auth/register/verify"] button[type="submit"]',
        "button.submit",
        'button:has-text("X�c nh?n")'
      )
    );
  }

  /**
   * Opens the page.
   */
  async open(email: string): Promise<void> {
    await this.visit(`/register/verify?email=${encodeURIComponent(email)}`);
  }

  async waitForLoaded(email?: string): Promise<void> {
    await this.page.waitForURL(/\/register\/verify/);
    await this.waitForVisible(this.otpInput);
    await this.waitForVisible(this.verifyButton);
    await this.dismissSweetAlertIfPresent();
    if (email) {
      await this.waitForLocatorText(this.page.locator("body"), email);
    }
  }

  async verifyOtp(otp: string): Promise<void> {
    await this.dismissSweetAlertIfPresent();
    await this.otpInput.fill(otp);
    await this.verifyButton.click();
  }

  async waitForPopupContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
