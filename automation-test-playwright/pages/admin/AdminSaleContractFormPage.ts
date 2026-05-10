import { type Locator, type Page } from "@playwright/test";
import { CrudFormPage } from "../core/CrudFormPage";

export class AdminSaleContractFormPage extends CrudFormPage {
  protected readonly addPath = "/admin/sale-contract/add";
  protected readonly editPath = "/admin/sale-contract/edit";
  readonly form: Locator;
  readonly staffSelect: Locator;
  readonly quickAssignModal: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.form = this.page.locator("#saleContractForm, #editForm");
    this.staffSelect = this.page.locator("#staffSelect, [name='staffId']");
    this.quickAssignModal = this.page.locator("#quickAssignModal");
  }

  async waitForAddLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/sale-contract\/add/);
    await this.waitForVisible(this.form);
  }

  async waitForEditLoaded(contractId: number): Promise<void> {
    await this.waitForPageUrl(new RegExp(`/admin/sale-contract/edit/${contractId}$`));
    await this.waitForVisible(this.form);
  }

  async selectBuilding(buildingId: number | string): Promise<void> {
    await this.firstVisible(this.page.locator("#buildingId, [name='buildingId']")).selectOption(String(buildingId));
  }

  async selectCustomer(customerId: number | string): Promise<void> {
    await this.firstVisible(this.page.locator("#customerSelect, [name='customerId']")).selectOption(String(customerId));
  }

  async selectStaff(staffId: number | string): Promise<void> {
    await this.firstVisible(this.staffSelect).selectOption(String(staffId));
  }

  async fillSalePrice(value: number): Promise<void> {
    await this.fillNumberField("salePrice", value);
  }

  async fillTransferDate(date: string): Promise<void> {
    await this.fillTextField("transferDate", date);
  }

  async fillNote(note: string): Promise<void> {
    await this.fillTextField("note", note);
  }

  async waitForStaffOptions(): Promise<void> {
    const select = this.firstVisible(this.staffSelect);
    await this.waitForCondition(async () => {
      const count = await select.locator("option").count();
      return count > 1;
    }, "Sale contract staff options were not loaded.");
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

  async submitSaleContract(): Promise<void> {
    await this.submit();
  }

  async waitForTransferDateHintContains(text: string | RegExp): Promise<void> {
    await this.waitForLocatorText(this.page.locator("#transferDateHint"), text);
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
