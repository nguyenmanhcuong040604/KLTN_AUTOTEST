import type { TestStateFixture } from "@helpers-test-state/TestState";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

export type BuildingAdditionalInfoType = "legal" | "amenity" | "planning" | "supplier";

export async function deleteAdditionalInfoRecord(
  testState: TestStateFixture,
  type: BuildingAdditionalInfoType,
  id?: number
): Promise<void> {
  await testState.deleteAdditionalInfoRecord(type, id);
}

export async function findLegalAuthorityId(buildingId: number, authorityName: string): Promise<number | undefined> {
  const rows = await MySqlDbClient.query<{ id: number }>(
    "SELECT id FROM legal_authority WHERE building_id = ? AND authority_name = ? ORDER BY id DESC LIMIT 1",
    [buildingId, authorityName]
  );
  return rows[0]?.id;
}

export async function readLegalAuthority(
  legalAuthorityId: number
): Promise<{ authority_name: string; authority_type: string } | undefined> {
  const rows = await MySqlDbClient.query<{ authority_name: string; authority_type: string }>(
    "SELECT authority_name, authority_type FROM legal_authority WHERE id = ?",
    [legalAuthorityId]
  );
  return rows[0];
}

export async function findAmenityId(buildingId: number, amenityName: string): Promise<number | undefined> {
  const rows = await MySqlDbClient.query<{ id: number }>(
    "SELECT id FROM nearby_amenity WHERE building_id = ? AND name = ? ORDER BY id DESC LIMIT 1",
    [buildingId, amenityName]
  );
  return rows[0]?.id;
}

export async function readAmenity(
  amenityId: number
): Promise<{ name: string; amenity_type: string; distance_meter: number } | undefined> {
  const rows = await MySqlDbClient.query<{ name: string; amenity_type: string; distance_meter: number }>(
    "SELECT name, amenity_type, distance_meter FROM nearby_amenity WHERE id = ?",
    [amenityId]
  );
  return rows[0];
}

export async function findSupplierId(buildingId: number, supplierName: string): Promise<number | undefined> {
  const rows = await MySqlDbClient.query<{ id: number }>(
    "SELECT id FROM supplier WHERE building_id = ? AND name = ? ORDER BY id DESC LIMIT 1",
    [buildingId, supplierName]
  );
  return rows[0]?.id;
}

export async function readSupplier(
  supplierId: number
): Promise<{ name: string; service_type: string; email: string } | undefined> {
  const rows = await MySqlDbClient.query<{ name: string; service_type: string; email: string }>(
    "SELECT name, service_type, email FROM supplier WHERE id = ?",
    [supplierId]
  );
  return rows[0];
}

export async function findPlanningMapId(buildingId: number, mapType: string): Promise<number | undefined> {
  const rows = await MySqlDbClient.query<{ id: number }>(
    "SELECT id FROM planning_map WHERE building_id = ? AND map_type = ? ORDER BY id DESC LIMIT 1",
    [buildingId, mapType]
  );
  return rows[0]?.id;
}

export async function readPlanningMap(
  planningMapId: number
): Promise<{ map_type: string; issued_by: string; image_url: string } | undefined> {
  const rows = await MySqlDbClient.query<{ map_type: string; issued_by: string; image_url: string }>(
    "SELECT map_type, issued_by, image_url FROM planning_map WHERE id = ?",
    [planningMapId]
  );
  return rows[0];
}

export async function planningMapExists(planningMapId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ id: number }>("SELECT id FROM planning_map WHERE id = ?", [planningMapId]);
  return rows.length > 0;
}

export async function planningMapCount(buildingId: number): Promise<number> {
  const rows = await MySqlDbClient.query<{ total: number }>(
    "SELECT COUNT(*) AS total FROM planning_map WHERE building_id = ?",
    [buildingId]
  );
  return Number(rows[0]?.total ?? 0);
}
