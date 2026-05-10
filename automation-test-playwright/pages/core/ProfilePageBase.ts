import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "./BasePage";
import { ProfileModalComponent, type ProfileModalKind } from "../components/ProfileModalComponent";

type ProfileInfoContainer = ".info-box" | ".info-item";

export abstract class ProfilePageBase extends BasePage {
  readonly usernameValue: Locator;
  readonly emailValue: Locator;
  readonly linkedGoogleEmailValue: Locator;
  readonly phoneValue: Locator;
  readonly usernameModal: Locator;
  readonly phoneModal: Locator;
  readonly passwordModal: Locator;
  readonly usernameAction: Locator;
  readonly phoneAction: Locator;
  readonly passwordAction: Locator;
  protected readonly profileModals: ProfileModalComponent;

  protected constructor(
    page: Page,
    private readonly profilePath: string,
    private readonly titlePattern: RegExp,
    private readonly infoContainer: ProfileInfoContainer
  ) {
    super(page);
    this.profileModals = new ProfileModalComponent(page);
    this.usernameValue = this.infoValueByIndex(0);
    this.emailValue = this.infoValueByIndex(1);
    this.linkedGoogleEmailValue = this.infoValueByIndex(2);
    this.phoneValue = this.infoValueByIndex(3);
    this.usernameModal = this.profileModals.modal("username");
    this.phoneModal = this.profileModals.modal("phone");
    this.passwordModal = this.profileModals.modal("password");
    this.usernameAction = this.profileModals.action("username");
    this.phoneAction = this.profileModals.action("phone");
    this.passwordAction = this.profileModals.action("password");
  }

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    await this.visit(this.profilePath);
  }

  async waitForLoaded(): Promise<void> {
    await this.waitForPageUrl(this.titlePattern);
    await this.waitForVisible(this.usernameValue);
    await this.waitForVisible(this.emailValue);
    await this.waitForVisible(this.phoneValue);
  }

  async openUsernameModal(): Promise<void> {
    await this.openProfileModal("username");
  }

  async openPhoneModal(): Promise<void> {
    await this.openProfileModal("phone");
  }

  async openPasswordModal(): Promise<void> {
    await this.openProfileModal("password");
  }

  /**
   * Sends otp from modal from the UI.
   */
  async sendOtpFromModal(modal: ProfileModalKind): Promise<void> {
    await this.profileModals.sendOtp(modal);
  }

  async submitUsernameChange(newUsername: string, otp: string): Promise<void> {
    const modal = await this.profileModals.open("username");
    await this.sweetAlertComponent.confirmIfPresent();
    await modal.locator('[name="newUsername"]').fill(newUsername);
    await modal.locator("#usernameOtp").fill(otp);
    await this.firstVisible(modal.locator("button.btn-primary, button[type='submit']")).click();
  }

  async submitPhoneChange(newPhoneNumber: string, otp: string): Promise<void> {
    const modal = await this.profileModals.open("phone");
    await this.sweetAlertComponent.confirmIfPresent();
    await modal.locator('[name="newPhoneNumber"]').fill(newPhoneNumber);
    await modal.locator("#phoneOtp").fill(otp);
    await this.firstVisible(modal.locator("button.btn-primary, button[type='submit']")).click();
  }

  async submitPasswordChange(newPassword: string, confirmPassword: string, otp: string): Promise<void> {
    const modal = await this.profileModals.open("password");
    await this.sweetAlertComponent.confirmIfPresent();
    await modal.locator('[name="newPassword"]').fill(newPassword);
    await modal.locator('[name="confirmPassword"]').fill(confirmPassword);
    await modal.locator("#passwordOtp").fill(otp);
    await this.firstVisible(modal.locator("button.btn-primary, button[type='submit']")).click();
  }

  async waitForSweetAlert(): Promise<void> {
    await this.sweetAlertComponent.waitForVisible();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlertIfPresent(): Promise<void> {
    await this.sweetAlertComponent.confirmIfPresent();
  }

  /**
   * Reads profile values from the page.
   */
  async readProfileValues(): Promise<{ username: string; email: string; phone: string }> {
    return {
      username: ((await this.usernameValue.textContent()) ?? "").trim(),
      email: ((await this.emailValue.textContent()) ?? "").trim(),
      phone: ((await this.phoneValue.textContent()) ?? "").trim()
    };
  }

  private async openProfileModal(modal: ProfileModalKind): Promise<void> {
    await this.profileModals.open(modal);
  }

  private infoValueByIndex(index: number): Locator {
    return this.firstVisible(this.page.locator(this.infoContainer).locator(".info-value").nth(index));
  }
}
