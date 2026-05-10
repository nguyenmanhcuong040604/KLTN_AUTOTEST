import { type Locator, type Page } from "@playwright/test";
import { StaffShellPage } from "../core/StaffShellPage";

export class StaffDashboardPage extends StaffShellPage {
  protected readonly path = "/staff/dashboard";
  readonly overdueInvoicesBody: Locator;
  readonly expiringContractsBody: Locator;
  readonly expiringInvoicesBody: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.overdueInvoicesBody = this.page.locator("#overdueInvoicesBody");
    this.expiringContractsBody = this.page.locator("#expiringContractsBody");
    this.expiringInvoicesBody = this.page.locator("#expiringInvoicesBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/staff\/dashboard/);
    await this.waitForTitleContainsLoose("bang dieu khien nhan vien", "dashboard");
    await this.waitForVisible(this.page.locator('a.nav-link.active[href="/staff/dashboard"]'));
  }

  async waitForSummarySectionsVisible(): Promise<void> {
    await this.waitForVisible(this.page.locator("#buildingCntStat"));
    await this.waitForVisible(this.page.locator("#contractCntStat"));
    await this.waitForVisible(this.page.locator("#customerCntStat"));
    await this.waitForVisible(this.page.locator("#unpaidInvoiceCntStat"));
    await this.waitForVisible(this.overdueInvoicesBody);
    await this.waitForVisible(this.expiringContractsBody);
    await this.waitForVisible(this.expiringInvoicesBody);
  }
}
