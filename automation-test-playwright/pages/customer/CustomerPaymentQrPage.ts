import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

export class CustomerPaymentQrPage extends BasePage {
  readonly invoicePill: Locator;
  readonly amountBox: Locator;
  readonly qrImage: Locator;
  readonly metaGrid: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.invoicePill = this.page.locator(".invoice-pill");
    this.amountBox = this.page.locator(".amount-box");
    this.qrImage = this.page.locator(".qr-box img");
    this.metaGrid = this.page.locator(".meta-grid");
  }

  async waitForLoaded(invoiceId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/payment-demo/qr/${invoiceId}$`));
    await this.waitForVisible(this.firstVisible(this.page.locator("h1, h2")));
    await this.waitForLocatorText(this.invoicePill, String(invoiceId));
    await this.waitForVisible(this.qrImage);
  }

  async waitForMetaContains(text: string | RegExp): Promise<void> {
    await this.waitForLocatorText(this.metaGrid, text);
  }

  async confirmPayment(): Promise<void> {
    await this.firstVisible(
      this.page
        .locator("button.btn-confirm, button.btn-primary, button[type='submit']")
        .or(this.page.getByRole("button", { name: /toi da thanh toan|confirm|paid/i }))
    ).click();
  }

  async goBackToInvoiceList(): Promise<void> {
    await this.firstVisible(
      this.page
        .locator('a[href*="/customer/invoice"]')
        .or(this.page.getByRole("link", { name: /quay lai|hoa don|invoice/i }))
    ).click();
  }
}
