import { BasePage } from "./BasePage";

export class CustomerShellPage extends BasePage {
  protected readonly path?: string;

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    if (!this.path) {
      throw new Error("This customer page does not define a path.");
    }

    await this.visit(this.path);
  }

  async openHome(): Promise<void> {
    await this.visit("/customer/home");
  }

  /**
   * Navigates to contracts.
   */
  async goToContracts(): Promise<void> {
    await this.visit("/customer/contract/list");
  }

  /**
   * Navigates to invoices.
   */
  async goToInvoices(): Promise<void> {
    await this.visit("/customer/invoice/list");
  }

  /**
   * Navigates to buildings.
   */
  async goToBuildings(): Promise<void> {
    await this.visit("/customer/building/list");
  }

  /**
   * Navigates to transactions.
   */
  async goToTransactions(): Promise<void> {
    await this.visit("/customer/transaction/history");
  }

  /**
   * Navigates to services.
   */
  async goToServices(): Promise<void> {
    await this.visit("/customer/service");
  }

  /**
   * Navigates to profile.
   */
  async goToProfile(): Promise<void> {
    await this.visit("/customer/profile");
  }
}
