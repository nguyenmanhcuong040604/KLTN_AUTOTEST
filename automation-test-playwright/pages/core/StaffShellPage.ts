import { BasePage } from "./BasePage";

export class StaffShellPage extends BasePage {
  protected readonly path?: string;

  /**
   * Opens the page.
   */
  async open(): Promise<void> {
    if (!this.path) {
      throw new Error("This staff page does not define a path.");
    }

    await this.visit(this.path);
  }

  async openDashboard(): Promise<void> {
    await this.visit("/staff/dashboard");
  }

  /**
   * Navigates to buildings.
   */
  async goToBuildings(): Promise<void> {
    await this.visit("/staff/buildings");
  }

  /**
   * Navigates to customers.
   */
  async goToCustomers(): Promise<void> {
    await this.visit("/staff/customers");
  }

  /**
   * Navigates to contracts.
   */
  async goToContracts(): Promise<void> {
    await this.visit("/staff/contracts");
  }

  /**
   * Navigates to invoices.
   */
  async goToInvoices(): Promise<void> {
    await this.visit("/staff/invoices");
  }

  /**
   * Navigates to profile.
   */
  async goToProfile(): Promise<void> {
    await this.visit("/staff/profile");
  }
}
