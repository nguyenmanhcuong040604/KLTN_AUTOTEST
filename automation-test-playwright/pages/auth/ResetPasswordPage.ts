import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class ResetPasswordPage extends BasePage {
  readonly otpInput: Locator;
  readonly newPasswordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly submitButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.otpInput = this.firstVisible(this.anyLocator('[data-testid="reset-password-otp"]', 'input[name="otp"]'));
    this.newPasswordInput = this.firstVisible(
      this.anyLocator('[data-testid="reset-password-new"]', 'input[name="newPassword"]')
    );
    this.confirmPasswordInput = this.firstVisible(
      this.anyLocator('[data-testid="reset-password-confirm"]', 'input[name="confirmPassword"]')
    );
    this.submitButton = this.firstVisible(
      this.anyLocator('[data-testid="reset-password-submit"]', 'button[type="submit"]')
    );
  }

  /**
   * Opens the page.
   */
  async open(email: string): Promise<void> {
    await this.visit(`/auth/reset-password?email=${encodeURIComponent(email)}`);
  }

  async waitForLoaded(email?: string): Promise<void> {
    await this.page.waitForURL(/\/auth\/reset-password/);
    await this.waitForVisible(this.otpInput);
    await this.waitForVisible(this.newPasswordInput);
    await this.waitForVisible(this.confirmPasswordInput);
    await this.waitForVisible(this.submitButton);
    if (email) {
      await this.waitForLocatorValue(this.page.locator("#emailDisplay"), email);
    }
  }

  async resetPassword(otp: string, password: string, confirmPassword = password): Promise<void> {
    await this.otpInput.fill(otp);
    await this.newPasswordInput.fill(password);
    await this.confirmPasswordInput.fill(confirmPassword);
    await this.submitButton.click();
  }

  async resendOtp(): Promise<void> {
    await this.page.getByRole("button", { name: /Gửi lại mã/i }).click();
  }

  async waitForPopupContains(text: string | RegExp): Promise<void> {
    const popup = this.toastPopup();
    await this.waitForVisible(popup);
    const rawText = ((await popup.textContent()) ?? "").trim();
    const normalize = (value: string): string =>
      value
        .normalize("NFD")
        .replace(/\p{Diacritic}/gu, "")
        .replace(/\s+/g, " ")
        .trim()
        .toLowerCase();

    const normalizedText = normalize(rawText);
    if (typeof text === "string") {
      if (!rawText.includes(text) && !normalizedText.includes(normalize(text))) {
        throw new Error(`Reset password popup did not contain expected text: ${text}`);
      }
      return;
    }

    const normalizedPattern = new RegExp(normalize(text.source), text.flags.replace("g", ""));
    if (!text.test(rawText) && !normalizedPattern.test(normalizedText)) {
      throw new Error(`Reset password popup did not match expected text: ${String(text)}`);
    }
  }
}
