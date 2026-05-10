import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

export type PublicBuildingRow = {
  id: number;
  name: string;
  districtId: number | null;
  districtName: string | null;
  ward: string | null;
  street: string | null;
  direction: string | null;
  level: string | null;
  transactionType: "FOR_RENT" | "FOR_SALE";
  image: string | null;
  rentPrice: number | null;
  salePrice: number | null;
};

const PUBLIC_VISIBILITY_WHERE = `
(
  (b.transaction_type = 'FOR_SALE' AND NOT EXISTS (
    SELECT 1 FROM sale_contract sc WHERE sc.building_id = b.id
  ))
  OR
  (b.transaction_type = 'FOR_RENT' AND (
    EXISTS (SELECT 1 FROM rent_area ra WHERE ra.building_id = b.id)
    OR EXISTS (SELECT 1 FROM contract c WHERE c.building_id = b.id AND c.status = 'EXPIRED')
  ))
)
`;

const BASE_BUILDING_SELECT = `
  SELECT
    b.id,
    b.name,
    b.district_id AS districtId,
    d.name AS districtName,
    b.ward,
    b.street,
    b.direction,
    b.level,
    b.transaction_type AS transactionType,
    b.image,
    b.rent_price AS rentPrice,
    b.sale_price AS salePrice
  FROM building b
  LEFT JOIN district d ON d.id = b.district_id
`;

export async function getSingleVisiblePublicBuilding(
  extraWhere = "",
  params: Array<string | number> = []
): Promise<PublicBuildingRow | null> {
  const rows = await MySqlDbClient.query<PublicBuildingRow>(
    `
      ${BASE_BUILDING_SELECT}
      WHERE ${PUBLIC_VISIBILITY_WHERE}
      ${extraWhere ? `AND ${extraWhere}` : ""}
      ORDER BY b.id DESC
      LIMIT 1
    `,
    params
  );

  return rows[0] ?? null;
}

export async function getVisiblePublicBuildingCount(): Promise<number> {
  const rows = await MySqlDbClient.query<{ total: number }>(
    `
      SELECT COUNT(*) AS total
      FROM building b
      WHERE ${PUBLIC_VISIBILITY_WHERE}
    `
  );

  return Number(rows[0]?.total ?? 0);
}

export async function visiblePublicBuildingExists(buildingId: number, buildingName: string): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    `
      SELECT COUNT(*) AS count
      FROM building b
      WHERE ${PUBLIC_VISIBILITY_WHERE}
        AND b.id = ?
        AND b.name = ?
    `,
    [buildingId, buildingName]
  );

  return Number(rows[0]?.count ?? 0) === 1;
}

export async function visiblePublicBuildingDistrictMatches(buildingId: number, districtId: number): Promise<boolean> {
  const rows = await MySqlDbClient.query<{ count: number }>(
    `
      SELECT COUNT(*) AS count
      FROM building b
      WHERE ${PUBLIC_VISIBILITY_WHERE}
        AND b.id = ?
        AND b.district_id = ?
    `,
    [buildingId, districtId]
  );

  return Number(rows[0]?.count ?? 0) === 1;
}
