import { type Locator, type Page } from "@playwright/test";
import { CrudDetailPage } from "../core/CrudDetailPage";

export class AdminStaffDetailPage extends CrudDetailPage {
  protected readonly detailPath = "/admin/staff";
  readonly deleteButton: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.deleteButton = this.page.locator(".btn-hd-delete");
  }

  async waitForLoaded(staffId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/staff/${staffId}$`));
    await this.waitForVisible(this.firstVisible(this.page.locator(".role-badge")));
  }

  /**
   * Deletes staff through the UI.
   */
  async deleteStaff(): Promise<void> {
    await this.deleteButton.click();
  }

  async openBuildingAssignments(): Promise<void> {
    await this.page.locator("#btnEditBuildings").click();
    await this.waitForVisible(this.page.locator("#modalBuildings"));
  }

  async openCustomerAssignments(): Promise<void> {
    await this.page.locator("#btnEditCustomers").click();
    await this.waitForVisible(this.page.locator("#modalCustomers"));
  }

  /**
   * Sets building assignment in the UI.
   */
  async setBuildingAssignment(buildingId: number, checked: boolean): Promise<void> {
    const checkbox = this.page.locator(`#buildingCheckList input[name="buildingIds"][value="${buildingId}"]`);
    if (checked) {
      await checkbox.check();
    } else {
      await checkbox.uncheck();
    }
  }

  /**
   * Sets customer assignment in the UI.
   */
  async setCustomerAssignment(customerId: number, checked: boolean): Promise<void> {
    const checkbox = this.page.locator(`#customerCheckList input[name="customerIds"][value="${customerId}"]`);
    if (checked) {
      await checkbox.check();
    } else {
      await checkbox.uncheck();
    }
  }

  /**
   * Saves building assignments changes.
   */
  async saveBuildingAssignments(): Promise<void> {
    await this.page.locator("#saveBuildingsBtn").click();
  }

  /**
   * Saves customer assignments changes.
   */
  async saveCustomerAssignments(): Promise<void> {
    await this.page.locator("#saveCustomersBtn").click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
