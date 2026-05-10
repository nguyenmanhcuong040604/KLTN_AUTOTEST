import { type Locator } from "@playwright/test";
import { AdminShellPage } from "../core/AdminShellPage";

export class AdminDashboardPage extends AdminShellPage {
  protected readonly path = "/admin/dashboard";

  private statCardByStatId(statId: string): Locator {
    return this.page.locator(`#${statId}`).locator("xpath=ancestor::div[contains(@class,'stat-card')]");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/dashboard/);
    await this.waitForVisible(this.page.locator("#totalBuildingsStat"));
  }

  async waitForOverviewVisible(): Promise<void> {
    await this.waitForVisible(this.page.locator("#totalBuildingsStat"));
    await this.waitForVisible(this.page.locator("#totalCustomersStat"));
    await this.waitForVisible(this.page.locator("#totalStaffsStat"));
    await this.waitForVisible(this.page.locator("#totalContractsStat"));
    await this.waitForVisible(this.page.locator("#revenueChartCombined"));
    await this.waitForVisible(this.page.locator("#contractsByBuildingChart"));
    await this.waitForVisible(this.page.locator("#districtChart"));
    await this.waitForVisible(this.page.locator("#saleRateChart"));
    await this.waitForVisible(this.page.locator("#potentialCustomersBody"));
    await this.waitForVisible(this.page.locator("#topStaffsBody"));
    await this.waitForVisible(this.page.locator("#recentBuildingsContainer"));
  }

  async openBuildingsFromStatCard(): Promise<void> {
    await this.statCardByStatId("totalBuildingsStat").click();
  }

  async openCustomersFromStatCard(): Promise<void> {
    await this.statCardByStatId("totalCustomersStat").click();
  }

  async openStaffsFromStatCard(): Promise<void> {
    await this.statCardByStatId("totalStaffsStat").click();
  }

  async openContractsFromStatCard(): Promise<void> {
    await this.statCardByStatId("totalContractsStat").click();
  }

  async waitForRecentBuildingVisible(buildingName: string): Promise<void> {
    await this.waitForVisible(
      this.firstVisible(this.page.locator("#recentBuildingsContainer .recent-item").filter({ hasText: buildingName }))
    );
  }

  async openRecentBuilding(buildingName: string): Promise<void> {
    await this.firstVisible(
      this.page.locator("#recentBuildingsContainer .recent-item").filter({ hasText: buildingName })
    ).click();
  }
}
