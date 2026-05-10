import { type Page } from "@playwright/test";
import { ProfilePageBase } from "@pages/core/ProfilePageBase";

export class AdminProfilePage extends ProfilePageBase {
  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page, "/admin/profile", /thong tin ca nhan|profile/i, ".info-box");
  }
}

export class StaffProfilePage extends ProfilePageBase {
  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page, "/staff/profile", /tai khoan nhan vien|profile/i, ".info-item");
  }
}

export class CustomerProfilePage extends ProfilePageBase {
  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page, "/customer/profile", /tai khoan|profile/i, ".info-item");
  }

  async openGoogleLink(): Promise<void> {
    await this.page.getByRole("link", { name: /lien ket google|google/i }).click();
  }
}
