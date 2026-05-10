import { type Locator, type Page } from "@playwright/test";
import { CrudFormPage } from "../core/CrudFormPage";

export class AdminStaffFormPage extends CrudFormPage {
  protected readonly addPath = "/admin/staff/add";
  readonly form: Locator;

  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
    this.form = this.page.locator("#staffForm");
  }

  async waitForLoaded(): Promise<void> {
    await this.page.waitForURL(/\/admin\/staff\/add/);
    await this.waitForVisible(this.form);
  }

  async fillStaffBasics(data: {
    fullName?: string;
    email?: string;
    phone?: string;
    username?: string;
    password?: string;
  }): Promise<void> {
    if (data.fullName) await this.fillTextField("fullName", data.fullName);
    if (data.email) await this.fillTextField("email", data.email);
    if (data.phone) await this.fillTextField("phone", data.phone);
    if (data.username) await this.fillTextField("username", data.username);
    if (data.password) await this.fillTextField("password", data.password);
  }

  async selectRole(role: "STAFF" | "ADMIN"): Promise<void> {
    const selector = role === "ADMIN" ? "#roleAdmin" : "#roleStaff";
    await this.page.locator(selector).check();
  }

  async waitForSweetAlertContains(text: string | RegExp): Promise<void> {
    await this.waitForSweetAlertContainsText(text);
  }
}
