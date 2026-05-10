import { type Locator, type Page } from "@playwright/test";
import { CustomerShellPage } from "../core/CustomerShellPage";

export class CustomerHomePage extends CustomerShellPage {
  protected readonly path = "/customer/home";
  readonly welcomeSection: Locator;
  readonly contractsContainer: Locator;
  readonly pendingInvoiceContainer: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.welcomeSection = this.page.locator(".welcome-section");
    this.contractsContainer = this.page.locator("#contractsContainer");
    this.pendingInvoiceContainer = this.page.locator("#pendingInvoiceContainer");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/home/);
    await this.waitForTitleContainsLoose("trang khach hang", "customer");
    await this.waitForVisible(this.welcomeSection);
  }

  async waitForDashboardSectionsVisible(): Promise<void> {
    await this.waitForVisible(this.contractsContainer);
    await this.waitForVisible(this.pendingInvoiceContainer);
  }

  async openContracts(): Promise<void> {
    await this.firstVisible(
      this.page.locator('.view-all[href="/customer/contract/list"], .nav-link[href="/customer/contract/list"]')
    ).click();
  }

  async openBuildings(): Promise<void> {
    await this.firstVisible(this.page.locator('.nav-link[href="/customer/building/list"]')).click();
  }

  async openProfile(): Promise<void> {
    await this.firstVisible(this.page.locator('a[href="/customer/profile"]')).click();
  }
}
