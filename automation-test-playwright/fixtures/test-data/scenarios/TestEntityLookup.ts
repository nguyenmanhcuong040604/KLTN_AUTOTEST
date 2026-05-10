import { type APIResponse } from "@playwright/test";
import { Invariant } from "@helpers-validation/Invariant";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";
import type { PaginatedList } from "./TestEntityTypes";

/**
 * Small lookup utilities for scenario setup.
 * It keeps API response parsing and DB lookup details out of test files and page objects.
 */
export class TestEntityLookup {
  static async json<T>(response: APIResponse): Promise<T> {
    Invariant.ensure(response.ok(), `API tra ve status ${response.status()} thay vi 2xx`);
    return response.json() as Promise<T>;
  }

  static listContent<T>(data: PaginatedList<T> | T[]): T[] {
    if (Array.isArray(data)) {
      return data;
    }

    return Array.isArray(data.content) ? data.content : [];
  }

  static async emailForId(table: "staff" | "customer", id: number): Promise<string | undefined> {
    const rows = await MySqlDbClient.query<{ email: string | null }>(
      `SELECT email FROM ${table} WHERE id = ? LIMIT 1`,
      [id]
    );

    const email = rows[0]?.email?.trim();
    return email ? email : undefined;
  }

  static async customerIdByUsername(username: string): Promise<number> {
    const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM customer WHERE username = ? LIMIT 1", [
      username
    ]);

    Invariant.ensure(rows.length > 0, "Khong tim thay entity trong DB");
    return rows[0]!.id;
  }

  static async saleContractIdByParties(buildingId: number, customerId: number, staffId: number): Promise<number> {
    const rows = await MySqlDbClient.query<{ id: number }>(
      `
        SELECT id
        FROM sale_contract
        WHERE building_id = ? AND customer_id = ? AND staff_id = ?
        ORDER BY id DESC
        LIMIT 1
      `,
      [buildingId, customerId, staffId]
    );

    const saleContract = rows[0];
    Invariant.ensure(Boolean(saleContract?.id), "Khong tim thay id cua sale contract vua tao");
    return Number(saleContract!.id);
  }
}
