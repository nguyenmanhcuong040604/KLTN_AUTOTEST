import { type Locator, type Page } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { WaitHelper } from "@helpers-browser/WaitHelper";
import { TextNormalizeHelper } from "@helpers-text/TextNormalizeHelper";
import { SweetAlertComponent } from "../components/SweetAlertComponent";

/**
 * Foundation layer for all Page Objects in the framework.
 *
 * Provides common utilities for locator creation, element waits, SweetAlert
 * interaction, text normalization, and page navigation that every page object
 * can inherit and reuse.
 */
export class BasePage {
  protected readonly page: Page;
  protected readonly sweetAlertComponent: SweetAlertComponent;

  /**
   * Creates a new page object bound to the given Playwright Page instance.
   */
  constructor(page: Page) {
    this.page = page;
    this.sweetAlertComponent = new SweetAlertComponent(page);
  }

  /**
   * Navigates to the requested path.
   */
  async visit(path: string): Promise<void> {
    await this.page.goto(path, { waitUntil: "domcontentloaded", timeout: env.navigationTimeout });
  }

  locator(selector: string): Locator {
    return this.page.locator(selector);
  }

  anyLocator(...selectors: string[]): Locator {
    return this.page.locator(selectors.join(", "));
  }

  visible(locator: Locator): Locator {
    return locator.filter({ visible: true });
  }

  firstVisible(locator: Locator): Locator {
    return this.visible(locator).nth(0);
  }

  lastVisible(locator: Locator): Locator {
    return this.visible(locator).nth(-1);
  }

  testId(id: string): Locator {
    return this.page.getByTestId(id);
  }

  inputByName(name: string): Locator {
    return this.page.locator(`[name="${name}"]`);
  }

  inputById(id: string): Locator {
    return this.page.locator(`#${id}`);
  }

  buttonByText(text: string): Locator {
    return this.page.getByRole("button", { name: new RegExp(this.escapeRegExp(text), "i") });
  }

  actionButton(scope: Locator, action: "view" | "edit" | "delete" | "pay" | "approve" | "reject"): Locator {
    const accessibleNameByAction: Record<typeof action, RegExp> = {
      view: /view|detail|xem|chi tiet/i,
      edit: /edit|update|sua|chinh/i,
      delete: /delete|remove|xoa/i,
      pay: /pay|payment|thanh toan/i,
      approve: /approve|duyet/i,
      reject: /reject|tu choi/i
    };
    const cssSelectorByAction: Record<typeof action, string> = {
      view: ".btn-view, .btn-action.btn-view, [data-action='view'], [title*='Xem'], [title*='Chi tiet'], [title*='detail']",
      edit: ".btn-edit, .btn-action.btn-edit, [data-action='edit'], [title*='Chinh sua'], [title*='Sua'], [title*='Edit']",
      delete: ".btn-delete, .btn-action.btn-delete, [data-action='delete'], [title*='Xoa'], [title*='Delete']",
      pay: ".btn-pay, .btn-action.btn-pay, [data-action='pay'], [title*='Thanh toan'], [title*='Payment']",
      approve: ".btn-approve, .btn-action.btn-approve, [data-action='approve'], [title*='Duyet'], [title*='Approve']",
      reject: ".btn-reject, .btn-action.btn-reject, [data-action='reject'], [title*='Tu choi'], [title*='Reject']"
    };
    return scope
      .getByTestId(new RegExp(`${action}|${action}-button|button-${action}`, "i"))
      .or(scope.locator(cssSelectorByAction[action]))
      .or(scope.getByRole("button", { name: accessibleNameByAction[action] }))
      .or(scope.getByRole("link", { name: accessibleNameByAction[action] }))
      .filter({ visible: true })
      .first();
  }

  linkByText(text: string): Locator {
    return this.page.getByRole("link", { name: new RegExp(this.escapeRegExp(text), "i") });
  }

  linkByHref(href: string): Locator {
    return this.page.locator(`a[href="${href}"]`);
  }

  modalById(id: string): Locator {
    return this.page.locator(`#${id}`);
  }

  toastPopup(): Locator {
    return this.page.locator(".swal2-popup").filter({ visible: true }).first();
  }

  async setInputValue(locator: Locator, value: string): Promise<void> {
    const isVisible = await locator.isVisible().catch(() => false);
    const isEditable = await locator.isEditable().catch(() => false);
    if (isVisible && isEditable) {
      await locator.fill(value);
      return;
    }

    await locator.evaluate((element, nextValue) => {
      const input = element as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;
      input.value = String(nextValue);
      input.dispatchEvent(new Event("input", { bubbles: true }));
      input.dispatchEvent(new Event("change", { bubbles: true }));
    }, value);
  }

  protected escapeRegExp(value: string): string {
    return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  protected normalizeLooseText(value: string): string {
    return TextNormalizeHelper.normalizeLooseText(value);
  }

  async locatorLooseText(locator: Locator): Promise<string> {
    return this.normalizeLooseText((await locator.textContent()) ?? "");
  }

  async waitForTitleContainsLoose(...expectedParts: string[]): Promise<void> {
    const normalizedExpected = expectedParts.map((part) => this.normalizeLooseText(part));
    await WaitHelper.until(
      async () => {
        const normalizedTitle = this.normalizeLooseText(await this.page.title());
        return normalizedExpected.some((part) => normalizedTitle.includes(part));
      },
      { message: `Title did not contain expected text: ${expectedParts.join(", ")}` }
    );
  }

  async waitForPath(pathPattern: RegExp | string): Promise<void> {
    if (typeof pathPattern === "string") {
      await this.waitForPageUrl(new RegExp(pathPattern.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")));
      return;
    }

    await this.waitForPageUrl(pathPattern);
  }

  async waitForPageUrl(pathPattern: RegExp): Promise<void> {
    if (pathPattern.test(this.page.url())) {
      return;
    }

    await this.page.waitForURL(pathPattern);
  }

  async waitForToastMessage(text: string): Promise<void> {
    await this.waitForLocatorText(this.toastPopup(), text);
  }

  async waitForSweetAlertContainsText(text: string | RegExp): Promise<void> {
    await this.sweetAlertComponent.waitForTextLoose(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await this.sweetAlertComponent.waitForVisible();
    await this.sweetAlertComponent.confirm();
  }

  async cancelSweetAlert(): Promise<void> {
    await this.sweetAlertComponent.waitForVisible();
    await this.sweetAlertComponent.cancel();
  }

  /**
   * Dismisses sweet alert if present when visible.
   */
  async dismissSweetAlertIfPresent(): Promise<void> {
    await this.sweetAlertComponent.confirmIfPresent();
  }

  async waitForVisible(locator: Locator): Promise<void> {
    await locator.waitFor({ state: "visible", timeout: env.expectTimeout });
  }

  async waitForHidden(locator: Locator): Promise<void> {
    await locator.waitFor({ state: "hidden", timeout: env.expectTimeout });
  }

  async waitForLocatorText(locator: Locator, text: string | RegExp): Promise<void> {
    await WaitHelper.until(
      async () => {
        const rawText = (await locator.textContent()) ?? "";
        return typeof text === "string" ? rawText.includes(text) : text.test(rawText);
      },
      { message: `Locator did not contain expected text: ${String(text)}` }
    );
  }

  async waitForLocatorValue(locator: Locator, value: string): Promise<void> {
    await WaitHelper.until(async () => (await locator.inputValue().catch(() => "")) === value, {
      message: `Locator did not have expected value: ${value}`
    });
  }

  async waitForLocatorCount(locator: Locator, count: number): Promise<void> {
    await WaitHelper.until(async () => (await locator.count()) === count, {
      message: `Locator count did not become ${count}`
    });
  }

  async waitForEnabled(locator: Locator): Promise<void> {
    await WaitHelper.until(async () => locator.isEnabled().catch(() => false), {
      message: "Locator did not become enabled."
    });
  }

  async waitForDisabled(locator: Locator): Promise<void> {
    await WaitHelper.until(async () => locator.isDisabled().catch(() => false), {
      message: "Locator did not become disabled."
    });
  }

  async waitForEmpty(locator: Locator): Promise<void> {
    await WaitHelper.until(async () => ((await locator.textContent().catch(() => "")) ?? "").trim() === "", {
      message: "Locator did not become empty."
    });
  }

  async waitForCondition(predicate: () => Promise<boolean> | boolean, message?: string): Promise<void> {
    await WaitHelper.until(predicate, { message });
  }
}
