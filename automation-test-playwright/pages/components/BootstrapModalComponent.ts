import { type Locator, type Page } from "@playwright/test";
import { env } from "@helpers-runtime/env";

/**
 * Encapsulates interactions with Bootstrap modal dialogs.
 * Provides wait, close, and content extraction utilities for `.modal.show` elements.
 */
export class BootstrapModalComponent {
  readonly modal: Locator;

  /**
   * Creates a modal component. Defaults to the standard Bootstrap `.modal.show` selector.
   */
  constructor(
    private readonly page: Page,
    selector = ".modal.show"
  ) {
    this.modal = this.page.locator(selector);
  }

  byId(id: string): Locator {
    return this.page.locator(`#${id}`);
  }

  visible(): Locator {
    return this.modal;
  }

  async waitForVisible(locator: Locator = this.modal): Promise<void> {
    await locator.waitFor({ state: "visible", timeout: env.expectTimeout });
  }

  async waitForHidden(locator: Locator = this.modal): Promise<void> {
    await locator.waitFor({ state: "hidden", timeout: env.expectTimeout });
  }

  /**
   * Closes the page UI element.
   */
  async close(locator: Locator = this.modal): Promise<void> {
    await locator
      .locator(".modal-header .btn-close, .modal-footer button, [data-bs-dismiss='modal']")
      .filter({ visible: true })
      .first()
      .click();
    await this.waitForHidden(locator);
  }

  async text(locator: Locator = this.modal): Promise<string> {
    return ((await locator.innerText()) ?? "").trim();
  }
}
