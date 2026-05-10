import { AdminShellPage } from "../core/AdminShellPage";

export class AdminReportPage extends AdminShellPage {
  protected readonly path = "/admin/report";

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/report/);
    await this.waitForVisible(this.page.locator(".year-select, select[name='year']"));
    await this.waitForVisible(this.page.locator("#monthlyRevenueChart"));
  }

  async waitForOverviewVisible(): Promise<void> {
    await this.waitForLocatorCount(this.page.locator(".stat-card"), 3);
    await this.waitForVisible(this.page.locator("#monthlyRevenueChart"));
    await this.waitForVisible(this.page.locator("#rentGrowthChart"));
    await this.waitForVisible(this.page.locator("#yearlyRevenueChart"));
    await this.waitForVisible(this.page.locator("#propertyTypeChart"));
    await this.waitForVisible(this.page.locator("#topBuildingChart"));
  }

  async selectYear(value: string): Promise<void> {
    await this.firstVisible(this.page.locator(".year-select, select[name='year']")).selectOption(value);
  }

  async availableYears(): Promise<string[]> {
    return this.firstVisible(this.page.locator(".year-select, select[name='year']"))
      .locator("option")
      .evaluateAll((options) => options.map((option) => (option as HTMLOptionElement).value));
  }

  async submitYearFilter(): Promise<void> {
    const submitButton = this.firstVisible(this.page.locator("form.year-selector-form button, button[type='submit']"));
    if (await submitButton.count()) {
      await submitButton.click();
    }
  }

  async waitForYearSelected(value: string): Promise<void> {
    await this.waitForLocatorValue(this.firstVisible(this.page.locator(".year-select, select[name='year']")), value);
  }

  /**
   * Triggers the print action.
   */
  async triggerPrint(): Promise<void> {
    await this.page.locator('button[onclick*="window.print"]').click();
  }
}
