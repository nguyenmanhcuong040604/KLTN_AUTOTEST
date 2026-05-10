import { type Page } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { LoginPage } from "@pages/auth/LoginPage";
import { TestAccountResolver, type TestUserRole } from "@helpers-accounts/TestAccountResolver";

export type { TestUserRole as UserRole };

export class AuthSessionHelper {
  private static usernameCandidatesFor(role: TestUserRole): string[] {
    return TestAccountResolver.usernameCandidates(role);
  }

  private static roleLandingPattern(role: TestUserRole): RegExp {
    switch (role) {
      case "admin":
        return /\/admin\//;
      case "staff":
        return /\/staff\//;
      case "customer":
        return /\/customer\//;
    }
  }

  private static authenticatedUrlPattern(role: TestUserRole): RegExp {
    return new RegExp(`${this.roleLandingPattern(role).source}|/login-success/`);
  }

  /** Role-to-landing-path mapping shared with base.fixture.ts. */
  static readonly roleHomePaths: Record<TestUserRole, string> = {
    admin: "/admin/dashboard",
    staff: "/staff/dashboard",
    customer: "/customer/home"
  };

  private static defaultRoleHomePath(role: TestUserRole): string {
    return this.roleHomePaths[role];
  }

  private static matchesAuthenticatedUrl(role: TestUserRole, rawUrl: string): boolean {
    return this.authenticatedUrlPattern(role).test(rawUrl);
  }

  private static matchesStrictRoleLandingUrl(role: TestUserRole, rawUrl: string): boolean {
    return this.roleLandingPattern(role).test(rawUrl);
  }

  private static async usernameMatchesRole(role: TestUserRole, username: string): Promise<boolean> {
    return TestAccountResolver.usernameMatchesRole(role, username);
  }

  private static isSuccessfulUiLogin(page: Page, role: TestUserRole): boolean {
    const url = page.url();
    return this.matchesAuthenticatedUrl(role, url) && !/\/login(?:\?|$)/.test(url);
  }

  private static async waitForStableRoleLanding(page: Page, role: TestUserRole): Promise<void> {
    const currentUrl = page.url();
    if (!/\/login-success/.test(currentUrl)) {
      return;
    }

    await page.waitForURL(
      (url) => this.matchesStrictRoleLandingUrl(role, url.toString()) && !/\/login-success/.test(url.toString()),
      { timeout: 5_000 }
    );

    await page.waitForLoadState("domcontentloaded");
  }

  private static async ensureStrictRoleLanding(page: Page, role: TestUserRole): Promise<void> {
    await this.waitForStableRoleLanding(page, role);

    if (this.matchesStrictRoleLandingUrl(role, page.url())) {
      return;
    }

    await page.goto(this.defaultRoleHomePath(role), { waitUntil: "domcontentloaded" });
    await page.waitForURL(this.roleLandingPattern(role));
  }

  private static async tryLoginUi(page: Page, role: TestUserRole, username: string, password: string): Promise<boolean> {
    const loginPage = new LoginPage(page);
    await loginPage.open();
    await loginPage.login(username, password);

    await Promise.race([
      page.waitForURL(
        (url) => this.matchesAuthenticatedUrl(role, url.toString()) && !/\/login(?:\?|$)/.test(url.toString()),
        { timeout: 5_000 }
      ),
      page.waitForURL((url) => /\/login(?:\?|$)/.test(url.toString()), { timeout: 5_000 })
    ]);

    await page.waitForLoadState("domcontentloaded");
    return this.isSuccessfulUiLogin(page, role);
  }

  static async loginUi(page: Page, username: string, password = env.defaultPassword): Promise<void> {
    const loginPage = new LoginPage(page);
    await loginPage.open();
    await loginPage.login(username, password);
  }

  static async logoutUi(page: Page): Promise<void> {
    try {
      await page.goto("/logout", { waitUntil: "commit" });
    } catch (error) {
      if (!(error instanceof Error) || !error.message.includes("ERR_ABORTED")) {
        throw error;
      }
    }

    try {
      await page.waitForURL((url) => /\/login(?:\?|$)/.test(url.toString()) || /\/$/.test(url.toString()), {
        timeout: 5_000
      });
    } catch {
      // Neu he thong redirect khac, ta van xoa cookie va dua ve trang login de dong bo session.
    }

    await page.context().clearCookies();
    await page.goto("/login", { waitUntil: "domcontentloaded" });
  }

  static async resolveWorkingUsername(page: Page, role: TestUserRole, password = env.defaultPassword): Promise<string> {
    const candidates = this.usernameCandidatesFor(role);

    for (const username of candidates) {
      if (!(await this.usernameMatchesRole(role, username))) {
        continue;
      }

      if (await this.tryLoginUi(page, role, username, password)) {
        TestAccountResolver.remember(role, username);
        return username;
      }
    }

    throw new Error(
      `Khong tim thay tai khoan ${role} hop le. Da thu: ${candidates.join(", ")}. Hay cap nhat bien moi truong ${role.toUpperCase()}_USERNAME hoac ${role.toUpperCase()}_USERNAMES.`
    );
  }

  static async loginAsRoleUi(page: Page, role: TestUserRole): Promise<void> {
    const resolved = await this.resolveWorkingUsername(page, role);
    TestAccountResolver.remember(role, resolved);
  }

  static async loginAsRoleUiStrict(page: Page, role: TestUserRole): Promise<void> {
    await this.loginAsRoleUi(page, role);
    await this.waitForStableRoleLanding(page, role);
    await this.ensureStrictRoleLanding(page, role);
  }
}
