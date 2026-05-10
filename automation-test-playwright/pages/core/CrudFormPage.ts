import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "./BasePage";
import { OptionalActionHelper } from "@helpers-browser/OptionalActionHelper";

/**
 * Base page object for CRUD add/edit form pages.
 * Provides submit/cancel actions, form validation, and file upload support.
 */
export class CrudFormPage extends BasePage {
  protected readonly addPath?: string;
  protected readonly editPath?: string;
  readonly form: Locator;
  readonly submitButton: Locator;
  readonly cancelButton: Locator;

  /**
   * Creates form locators for submit/cancel buttons and the main form element.
   */
  constructor(page: Page) {
    super(page);
    this.form = this.firstVisible(page.locator("form"));
    this.submitButton = this.lastVisible(
      page
        .locator(
          "form button[type='submit'], form input[type='submit'], button[type='submit'][form], button.btn-submit, button.btn-save"
        )
        .or(page.getByRole("button", { name: /xac nhan|luu|them|cap nhat|submit|save|create|update/i }))
    );
    this.cancelButton = this.lastVisible(page.getByRole("button", { name: /huy|cancel/i }));
  }

  async openAdd(): Promise<void> {
    if (!this.addPath) {
      throw new Error("This form page does not define an add path.");
    }

    await this.visit(this.addPath);
  }

  async openEdit(id: number): Promise<void> {
    if (!this.editPath) {
      throw new Error("This form page does not support edit navigation.");
    }

    await this.visit(`${this.editPath}/${id}`);
  }

  async fillTextField(fieldName: string, value: string): Promise<void> {
    await this.inputByName(fieldName).fill(value);
  }

  async fillNumberField(fieldName: string, value: number): Promise<void> {
    await this.inputByName(fieldName).fill(String(value));
  }

  async selectOption(fieldName: string, value: string): Promise<void> {
    await this.inputByName(fieldName).selectOption(value);
  }

  async submit(): Promise<void> {
    await this.submitButton.click();
  }

  async submitIfPresent(): Promise<boolean> {
    return OptionalActionHelper.clickIfPresent(this.submitButton);
  }

  async cancel(): Promise<void> {
    await this.cancelButton.click();
  }
}
