import { type Locator, type Page } from "@playwright/test";
import { CrudDetailPage } from "../core/CrudDetailPage";
import { SweetAlertComponent } from "../components/SweetAlertComponent";

export class AdminPropertyRequestDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/property-request";
  readonly rejectButton: Locator;
  private readonly sweetAlert: SweetAlertComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.rejectButton = this.page.locator(".btn-reject");
    this.sweetAlert = new SweetAlertComponent(page);
  }

  async waitForLoaded(requestId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/property-request/${requestId}$`));
    await this.waitForLocatorText(this.page.locator("h2"), `#${requestId}`);
  }

  async waitForPendingActionsVisible(): Promise<void> {
    await this.waitForVisible(this.page.locator(".btn-reject"));
    await this.waitForVisible(this.page.locator(".btn-approve"));
  }

  async rejectRequest(reason: string): Promise<void> {
    await this.rejectButton.click();
    await this.sweetAlert.fillTextarea(reason);
    await this.sweetAlert.confirm();
  }

  async waitForRejectAlertVisible(): Promise<void> {
    await this.sweetAlert.waitForVisible();
  }

  async waitForPrefilledCustomer(customerId: number): Promise<void> {
    await this.waitForLocatorCount(this.page.locator("[name='customerId_disabled']"), 1);
    await this.waitForLocatorValue(this.page.locator("[name='customerId']"), String(customerId));
  }

  async waitForCreateContractLink(requestId: number): Promise<void> {
    await this.waitForVisible(this.page.locator(`a[href="/admin/contract/add?fromRequestId=${requestId}"]`));
  }

  async waitForCreateSaleContractLink(requestId: number): Promise<void> {
    await this.waitForVisible(this.page.locator(`a[href="/admin/sale-contract/add?fromRequestId=${requestId}"]`));
  }

  async openCreateContractLink(requestId: number): Promise<void> {
    await this.page.locator(`a[href="/admin/contract/add?fromRequestId=${requestId}"]`).click();
  }

  async openCreateSaleContractLink(requestId: number): Promise<void> {
    await this.page.locator(`a[href="/admin/sale-contract/add?fromRequestId=${requestId}"]`).click();
  }

  async waitForProcessedContractLink(contractId: number): Promise<void> {
    await this.waitForVisible(this.page.locator(`a[href="/admin/contract/${contractId}"]`));
  }

  async waitForProcessedSaleContractLink(contractId: number): Promise<void> {
    await this.waitForVisible(this.page.locator(`a[href="/admin/sale-contract/${contractId}"]`));
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
