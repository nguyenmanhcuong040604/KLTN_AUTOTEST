import { type Locator, type Page } from "@playwright/test";
import { CrudFormPage } from "../core/CrudFormPage";

export class AdminContractFormPage extends CrudFormPage {
  protected readonly addPath = "/admin/contract/add";
  protected readonly editPath = "/admin/contract/edit";
  readonly form: Locator;
  readonly rentAreaSelect: Locator;
  readonly staffSelect: Locator;
  readonly quickAssignModal: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.form = this.page.locator("#contractForm");
    this.rentAreaSelect = this.page.locator("#rentAreaSelect, [name='rentArea']");
    this.staffSelect = this.page.locator("#staffSelect, [name='staffId']");
    this.quickAssignModal = this.page.locator("#quickAssignModal");
  }

  async waitForAddLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/contract\/add/);
    await this.waitForVisible(this.form);
  }

  async waitForEditLoaded(contractId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/contract/edit/${contractId}$`));
    await this.waitForVisible(this.form);
  }

  async selectBuilding(buildingId: number | string): Promise<void> {
    await this.firstVisible(this.page.locator("#buildingSelect, [name='buildingId']")).selectOption(String(buildingId));
  }

  async selectCustomer(customerId: number | string): Promise<void> {
    await this.firstVisible(this.page.locator("#customerSelect, [name='customerId']")).selectOption(String(customerId));
  }

  async selectRentArea(rentArea: number | string): Promise<void> {
    await this.firstVisible(this.rentAreaSelect).selectOption(String(rentArea));
  }

  async selectStaff(staffId: number | string): Promise<void> {
    await this.firstVisible(this.staffSelect).selectOption(String(staffId));
  }

  async fillRentPrice(value: number): Promise<void> {
    await this.fillNumberField("rentPrice", value);
  }

  async fillDates(startDate: string, endDate: string): Promise<void> {
    await this.fillTextField("startDate", startDate);
    await this.fillTextField("endDate", endDate);
  }

  async selectStatus(status: "ACTIVE" | "EXPIRED"): Promise<void> {
    await this.selectOption("status", status);
  }

  async waitForRentAreaOptions(): Promise<void> {
    const select = this.firstVisible(this.rentAreaSelect);
    await this.waitForEnabled(select);
    await this.waitForCondition(async () => {
      const count = await select.locator("option").count();
      return count > 1;
    }, "Rent area options were not loaded.");
  }

  async waitForStaffOptions(): Promise<void> {
    const select = this.firstVisible(this.staffSelect);
    await this.waitForCondition(async () => {
      const count = await select.locator("option").count();
      return count > 1;
    }, "Staff options were not loaded.");
  }

  async waitForNoCommonStaffOption(): Promise<void> {
    await this.waitForLocatorText(this.firstVisible(this.staffSelect), /không có nhân viên|khong co nhan vien/i);
  }

  async openQuickAssignModal(): Promise<void> {
    await this.page.locator("#btnQuickAssign").click();
    await this.waitForVisible(this.quickAssignModal);
  }

  async selectQuickAssignStaff(staffId: number | string): Promise<void> {
    await this.page.locator("#allStaffSelect").selectOption(String(staffId));
  }

  async submitQuickAssign(): Promise<void> {
    await this.page.locator("#submitQuickAssign").click();
  }

  async submitContract(): Promise<void> {
    await this.submit();
  }

  async waitForDateValidationContains(text: string | RegExp): Promise<void> {
    await this.waitForLocatorText(this.page.locator("#dateValidation"), text);
  }

  async waitForExpiredBanner(): Promise<void> {
    await this.waitForVisible(this.page.locator(".expired-banner"));
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
