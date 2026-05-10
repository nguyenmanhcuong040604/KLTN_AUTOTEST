import { BasePage } from "./BasePage";

export class AdminShellPage extends BasePage {
  protected readonly path?: string;

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    if (!this.path) {
      throw new Error("This admin page does not define a path.");
    }

    await this.visit(this.path);
  }

  /**
   * Navigates to buildings.
   */
  async goToBuildings(): Promise<void> {
    await this.linkByHref("/admin/building/list").click();
  }

  /**
   * Navigates to customers.
   */
  async goToCustomers(): Promise<void> {
    await this.linkByHref("/admin/customer/list").click();
  }

  /**
   * Navigates to contracts.
   */
  async goToContracts(): Promise<void> {
    await this.linkByHref("/admin/contract/list").click();
  }

  /**
   * Navigates to sale contracts.
   */
  async goToSaleContracts(): Promise<void> {
    await this.linkByHref("/admin/sale-contract/list").click();
  }

  /**
   * Navigates to invoices.
   */
  async goToInvoices(): Promise<void> {
    await this.linkByHref("/admin/invoice/list").click();
  }

  /**
   * Navigates to reports.
   */
  async goToReports(): Promise<void> {
    await this.linkByHref("/admin/report").click();
  }

  /**
   * Navigates to staffs.
   */
  async goToStaffs(): Promise<void> {
    await this.linkByHref("/admin/staff/list").click();
  }

  /**
   * Navigates to profile.
   */
  async goToProfile(): Promise<void> {
    await this.linkByHref("/admin/profile").click();
  }
}
