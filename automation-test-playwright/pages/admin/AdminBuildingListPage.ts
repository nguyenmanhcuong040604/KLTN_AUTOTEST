import { type Locator, type Page } from "@playwright/test";
import { TableComponent } from "../components/TableComponent";
import { CrudListPage } from "../core/CrudListPage";

export class AdminBuildingListPage extends CrudListPage {
  protected readonly path = "/admin/building/list";
  readonly addButton: Locator;
  readonly tableBody: Locator;
  private readonly table: TableComponent;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.addButton = this.page.locator(".btn-add");
    this.tableBody = this.page.locator("#buildingTableBody");
    this.table = new TableComponent(page, "#buildingTableBody");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/building\/(list|search)/);
    await this.waitForVisible(this.tableBody);
  }

  async waitForTableData(): Promise<void> {
    await this.table.waitForDataOrEmpty();
  }

  async openAddForm(): Promise<void> {
    await this.addButton.click();
  }

  /**
   * Applies the name filter.
   */
  async filterByName(name: string): Promise<void> {
    await this.fillFilter("name", name);
  }

  /**
   * Applies the property type filter.
   */
  async filterByPropertyType(propertyType: string): Promise<void> {
    await this.selectFilter("propertyType", propertyType);
  }

  /**
   * Applies the transaction type filter.
   */
  async filterByTransactionType(transactionType: string): Promise<void> {
    await this.selectFilter("transactionType", transactionType);
  }

  /**
   * Applies the district filter.
   */
  async filterByDistrict(districtId: string): Promise<void> {
    await this.selectFilter("districtId", districtId);
  }

  async search(): Promise<void> {
    const searchForm = this.page.locator("#searchForm");
    await Promise.all([
      this.page.waitForResponse(
        (response) =>
          response.request().method() === "GET" &&
          response.url().includes("/api/v1/admin/buildings") &&
          response.status() === 200
      ),
      searchForm.evaluate((form) => (form as HTMLFormElement).requestSubmit())
    ]);
  }

  rowByBuildingName(name: string): Locator {
    return this.table.rowByText(name);
  }

  async openDetail(name: string): Promise<void> {
    await this.clickRowLink(name, "/admin/building/");
  }

  async openEdit(name: string): Promise<void> {
    await this.clickRowLink(name, "/admin/building/edit/");
  }

  async openAdditionalInformation(name: string): Promise<void> {
    await this.clickRowLink(name, "/admin/building-additional-information/");
  }

  /**
   * Deletes building through the UI.
   */
  async deleteBuilding(name: string): Promise<void> {
    await this.actionButton(this.rowByBuildingName(name), "delete").click();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }

  async confirmSweetAlert(): Promise<void> {
    await super.confirmSweetAlert();
  }
}
