import { type Locator, type Page } from "@playwright/test";
import { CrudDetailPage } from "@pages/core/CrudDetailPage";

export class AdminBuildingDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/building";
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.deleteButton = this.page.locator(".btn-hero-delete");
  }

  async waitForLoaded(buildingId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/building/${buildingId}$`));
    await this.waitForVisible(this.page.locator("h1, h2"));
  }

  /**
   * Deletes building through the UI.
   */
  async deleteBuilding(): Promise<void> {
    await this.deleteButton.click();
  }
}

export class AdminCustomerDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/customer";
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.deleteButton = this.page.locator(".btn-hd-delete");
  }

  async waitForLoaded(customerId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/customer/${customerId}$`));
    await this.waitForVisible(this.firstVisible(this.page.locator(".strip-id")));
  }

  /**
   * Deletes customer through the UI.
   */
  async deleteCustomer(): Promise<void> {
    await this.deleteButton.click();
  }
}

export class AdminContractDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/contract";
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.deleteButton = this.page.locator(".btn-hd-delete");
  }

  async waitForLoaded(contractId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/contract/${contractId}$`));
    await this.waitForVisible(this.firstVisible(this.page.locator(".contract-strip")));
  }

  /**
   * Deletes contract through the UI.
   */
  async deleteContract(): Promise<void> {
    await this.deleteButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}

export class AdminSaleContractDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/sale-contract";
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.deleteButton = this.page.locator(".btn-hd-delete");
  }

  async waitForLoaded(contractId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/sale-contract/${contractId}$`));
    await this.waitForVisible(this.firstVisible(this.page.locator(".contract-strip")));
  }

  /**
   * Deletes sale contract through the UI.
   */
  async deleteSaleContract(): Promise<void> {
    await this.deleteButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}

export class AdminInvoiceDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/invoice";
  readonly payButton: Locator;
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.payButton = this.page.locator(".btn-hd-pay");
    this.deleteButton = this.page.locator(".btn-hd-delete");
  }

  async waitForLoaded(invoiceId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/invoice/${invoiceId}$`));
    await this.waitForVisible(this.firstVisible(this.payButton.or(this.deleteButton)));
  }

  async confirmInvoicePaid(): Promise<void> {
    await this.payButton.click();
  }

  /**
   * Deletes invoice through the UI.
   */
  async deleteInvoice(): Promise<void> {
    await this.deleteButton.click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
