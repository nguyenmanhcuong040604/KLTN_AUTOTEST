import { type Locator } from "@playwright/test";
import { CustomerShellPage } from "../core/CustomerShellPage";

export class CustomerPropertyRequestListPage extends CustomerShellPage {
  private readonly requestList = this.page.locator("#requestList");

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/customer\/property-request\/list/);
    await this.waitForVisible(this.requestList);
  }

  cardByRequestId(id: number): Locator {
    return this.firstVisible(
      this.requestList.locator(".request-card").filter({
        has: this.page.locator(".request-id", { hasText: `#${id}` })
      })
    );
  }

  async waitForRequestVisible(id: number): Promise<void> {
    await this.waitForVisible(this.cardByRequestId(id));
  }

  async waitForRequestContains(id: number, text: string | RegExp): Promise<void> {
    const card = this.cardByRequestId(id);
    await this.waitForVisible(card);
    await this.waitForCondition(
      async () => {
        const rawText = ((await card.textContent()) ?? "").trim();
        const normalizedText = this.normalizeLooseText(rawText);
        if (typeof text === "string") {
          return rawText.includes(text) || normalizedText.includes(this.normalizeLooseText(text));
        }

        const normalizedPattern = new RegExp(this.normalizeLooseText(text.source), text.flags.replace("g", ""));
        return text.test(rawText) || normalizedPattern.test(normalizedText);
      },
      `Request card ${id} did not contain expected text: ${String(text)}`
    );
  }

  async cancelRequest(id: number): Promise<void> {
    await this.cardByRequestId(id).locator(".btn-cancel").click();
  }

  async waitForCancelButtonVisible(id: number): Promise<void> {
    await this.waitForVisible(this.cardByRequestId(id).locator(".btn-cancel"));
  }

  async waitForCancelButtonHidden(id: number): Promise<void> {
    const cancelButton = this.cardByRequestId(id).locator(".btn-cancel");
    const count = await cancelButton.count();
    if (count === 0) {
      return;
    }

    await this.waitForHidden(cancelButton.first());
  }

  async waitForEmptyState(): Promise<void> {
    await this.waitForCondition(
      async () => /chua co yeu cau nao/i.test(await this.locatorLooseText(this.requestList)),
      "Request list empty state was not displayed."
    );
  }
}
