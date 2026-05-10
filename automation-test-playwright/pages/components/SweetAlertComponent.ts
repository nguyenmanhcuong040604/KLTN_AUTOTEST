import { type Locator, type Page } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { WaitHelper } from "@helpers-browser/WaitHelper";
import { TextNormalizeHelper } from "@helpers-text/TextNormalizeHelper";

/**
 * Encapsulates interactions with SweetAlert2 popup dialogs.
 * Handles confirm/cancel actions, text assertions, and processing-state waits.
 */
export class SweetAlertComponent {
  readonly popup: Locator;
  readonly confirmButton: Locator;
  readonly cancelButton: Locator;
  readonly textarea: Locator;

  /**
   * Creates a new SweetAlert component bound to the given page.
   */
  constructor(private readonly page: Page) {
    this.popup = this.page.locator(".swal2-popup");
    this.confirmButton = this.page.locator(".swal2-confirm");
    this.cancelButton = this.page.locator(".swal2-cancel");
    this.textarea = this.page.locator(".swal2-textarea");
  }

  async waitForVisible(): Promise<void> {
    await this.popup.waitFor({ state: "visible", timeout: env.expectTimeout });
  }

  async waitForText(text: string | RegExp): Promise<void> {
    await this.waitForVisible();
    await WaitHelper.until(
      async () => {
        const rawText = (await this.popup.textContent()) ?? "";
        return typeof text === "string" ? rawText.includes(text) : text.test(rawText);
      },
      { message: `SweetAlert did not contain expected text: ${String(text)}` }
    );
  }

  /**
   * Waits until the popup contains the expected text using loose (normalized) matching.
   * Handles both UTF-8 and ASCII-normalized comparisons.
   */
  async waitForTextLoose(text: string | RegExp): Promise<void> {
    await this.waitForVisible();
    await this.waitUntilNotProcessing();

    await WaitHelper.until(
      async () => {
        const rawText = ((await this.popup.textContent()) ?? "").trim();
        const normalizedText = TextNormalizeHelper.normalizeLooseText(rawText);

        if (typeof text === "string") {
          return rawText.includes(text) || normalizedText.includes(TextNormalizeHelper.normalizeLooseText(text));
        }

        const normalizedPattern = new RegExp(
          TextNormalizeHelper.normalizeLooseText(text.source),
          text.flags.replace("g", "")
        );
        return text.test(rawText) || normalizedPattern.test(normalizedText);
      },
      { message: `SweetAlert did not contain expected text: ${String(text)}` }
    );
  }

  /**
   * Alias for {@link waitForText}.
   */
  async waitForContains(text: string | RegExp): Promise<void> {
    await this.waitForText(text);
  }

  /**
   * Alias for {@link waitForTextLoose}.
   */
  async waitForContainsLoose(text: string | RegExp): Promise<void> {
    await this.waitForTextLoose(text);
  }

  async fillTextarea(value: string): Promise<void> {
    await this.textarea.fill(value);
  }

  /**
   * Clicks the confirm button (OK / Dong y / Xac nhan / Confirm / Yes).
   */
  async confirm(): Promise<void> {
    await this.confirmButton
      .filter({ visible: true })
      .first()
      .or(this.page.getByRole("button", { name: /ok|dong y|xac nhan|confirm|yes/i }))
      .first()
      .click();
  }

  /**
   * Clicks the cancel button (Huy / Cancel / No).
   */
  async cancel(): Promise<void> {
    await this.cancelButton
      .filter({ visible: true })
      .first()
      .or(this.page.getByRole("button", { name: /huy|cancel|no/i }))
      .first()
      .click();
  }

  /**
   * Clicks the confirm button only if the popup is currently visible.
   */
  async confirmIfPresent(): Promise<void> {
    const popupVisible = await this.popup.filter({ visible: true }).count();
    if (!popupVisible) {
      return;
    }

    const button = this.confirmButton
      .filter({ visible: true })
      .first()
      .or(this.page.getByRole("button", { name: /ok|dong y|xac nhan|confirm|yes/i }))
      .first();
    if (await button.count()) {
      await button.click();
      await this.popup.waitFor({ state: "hidden", timeout: env.expectTimeout });
    }
  }

  /**
   * Polls the popup text until it no longer indicates a processing state
   * (e.g. "Dang xu ly", "Vui long doi", "Processing", "Please wait").
   */
  async waitUntilNotProcessing(): Promise<void> {
    await WaitHelper.until(
      async () => {
        const normalizedText = TextNormalizeHelper.normalizeLooseText((await this.popup.textContent()) ?? "");
        return !/dang xu ly|vui long doi|processing|please wait/i.test(normalizedText);
      },
      { timeout: env.expectTimeout, message: "SweetAlert remained in processing state." }
    );
  }
}
