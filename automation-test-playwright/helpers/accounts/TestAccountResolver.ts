import { env } from "@helpers-runtime/env";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

export type TestUserRole = "admin" | "staff" | "customer";

export class TestAccountResolver {
  private static readonly resolvedUsernames = new Map<TestUserRole, string>();

  static usernameCandidates(role: TestUserRole): string[] {
    const unique = new Set<string>();
    const resolved = this.resolvedUsernames.get(role);
    if (resolved) {
      unique.add(resolved);
    }

    const configured =
      role === "admin" ? env.adminUsernames : role === "staff" ? env.staffUsernames : env.customerUsernames;
    configured.forEach((username) => unique.add(username));

    const fallback = role === "admin" ? env.adminUsername : role === "staff" ? env.staffUsername : env.customerUsername;
    unique.add(fallback);

    return [...unique].filter(Boolean);
  }

  static remember(role: TestUserRole, username: string): void {
    this.resolvedUsernames.set(role, username);
  }

  static async usernameMatchesRole(role: TestUserRole, username: string): Promise<boolean> {
    if (role === "customer") {
      const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM customer WHERE username = ? LIMIT 1", [
        username
      ]);
      return rows.length > 0;
    }

    const expectedRole = role === "admin" ? "ADMIN" : "STAFF";
    const rows = await MySqlDbClient.query<{ id: number }>(
      "SELECT id FROM staff WHERE username = ? AND role = ? LIMIT 1",
      [username, expectedRole]
    );
    return rows.length > 0;
  }
}
