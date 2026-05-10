import { type Locator, type Page } from "@playwright/test";
import { CustomerShellPage } from "../core/CustomerShellPage";
import { OptionalActionHelper } from "@helpers-browser/OptionalActionHelper";
import { SweetAlertComponent } from "../components/SweetAlertComponent";

export class CustomerInvoicePage extends CustomerShellPage {
  protected readonly path = "/customer/invoice/list";
  readonly emptyState: Locator;
  readonly statsValues: Locator;
  readonly visibleModal: Locator;
  readonly invoiceCards: Locator;
  readonly invoiceSummaries: Locator;
  readonly paymentButtons: Locator;
  private readonly sweetAlert: SweetAlertComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.emptyState = this.firstVisible(this.anyLocator('[data-testid="customer-invoice-empty"]', ".empty-state"));
    this.statsValues = this.page.locator(".stat-value");
    this.visibleModal = this.page.locator(".modal.show");
    this.invoiceCards = this.anyLocator('[data-testid="customer-invoice-card"]', ".invoice-card");
    this.invoiceSummaries = this.page.locator(".invoice-summary");
    this.paymentButtons = this.anyLocator(
      '[data-testid="customer-pay-button"]',
      ".pay-btn",
      ".btn-payment",
      "[data-bs-target^='#paymentModal']"
    );
    this.sweetAlert = new SweetAlertComponent(page);
  }

  async openFirstInvoiceSummary(): Promise<void> {
    await this.firstVisible(this.invoiceSummaries).click();
  }

  async openFirstPaymentModal(): Promise<void> {
    await this.firstVisible(this.paymentButtons).click();
  }

  async openFirstPaymentModalIfAvailable(): Promise<boolean> {
    return OptionalActionHelper.clickIfPresent(this.paymentButtons);
  }

  async confirmPaymentInModal(): Promise<void> {
    await this.visibleModal.locator(".btn-payment").click();
  }

  async continueSweetAlertRedirect(): Promise<void> {
    await this.sweetAlert.confirm();
  }

  /**
   * Closes the visible modal UI element.
   */
  async closeVisibleModal(): Promise<void> {
    await this.firstVisible(this.visibleModal.locator(".modal-header .btn-close, .modal-footer button")).click();
  }

  /**
   * Reads stats from the page.
   */
  async readStats(): Promise<{ unpaidCount: string; totalPayable: string }> {
    return {
      unpaidCount: (await this.statsValues.nth(0).innerText()).trim(),
      totalPayable: (await this.statsValues.nth(1).innerText()).trim()
    };
  }

  async firstInvoiceCardText(): Promise<string> {
    return (await this.firstVisible(this.invoiceCards).innerText()).trim();
  }

  async visibleModalText(): Promise<string> {
    return (await this.visibleModal.innerText()).trim();
  }

  async visibleModalLooseText(): Promise<string> {
    return this.locatorLooseText(this.visibleModal);
  }

  async waitForEmptyState(): Promise<void> {
    await this.waitForVisible(this.emptyState);
    await this.waitForCondition(
      async () => /chua co hoa don nao/i.test(await this.locatorLooseText(this.emptyState)),
      "Invoice empty state was not displayed."
    );
  }

  async waitForLoaded(): Promise<void> {
    await this.waitForTitleContainsLoose("thanh toan", "hoa don", "invoice");
    await this.waitForVisible(this.firstVisible(this.page.locator("h1, h2")));
  }

  /**
   * Verifies that the page is loaded.
   */
  async assertLoaded(): Promise<void> {
    await this.waitForLoaded();
  }

  async waitForPaymentSuccessAlert(): Promise<void> {
    await this.sweetAlert.waitForTextLoose("thanh toan thanh cong");
  }
}
