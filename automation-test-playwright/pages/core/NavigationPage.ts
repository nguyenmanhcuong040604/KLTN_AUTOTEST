import { BasePage } from "./BasePage";

export class NavigationPage extends BasePage {
  /**
   * Navigates to the given path.
   */
  async open(path: string): Promise<void> {
    await this.visit(path);
  }
}
