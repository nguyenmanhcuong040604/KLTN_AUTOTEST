package org.suntower.fixtures.data.scenarios;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.suntower.fixtures.state.MySqlDbClient;

public final class PublicBuildingScenario {
  private static final String PUBLIC_VISIBILITY_WHERE =
      """
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
      AND b.name NOT LIKE 'PW Building%'
      """;

  private static final String BASE_BUILDING_SELECT =
      """
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
      """;

  private PublicBuildingScenario() {}

  public static PublicBuildingRow getSingleVisiblePublicBuilding() {
    return getSingleVisiblePublicBuilding("", new Object[0]);
  }

  public static PublicBuildingRow getSingleVisiblePublicBuilding(String extraWhere, Object... params) {
    String extraClause = extraWhere == null || extraWhere.isBlank() ? "" : " AND " + extraWhere;
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            BASE_BUILDING_SELECT
                + " WHERE "
                + PUBLIC_VISIBILITY_WHERE
                + extraClause
                + " ORDER BY b.id DESC LIMIT 1",
            params);
    return rows.isEmpty() ? null : PublicBuildingRow.from(rows.get(0));
  }

  public static long getVisiblePublicBuildingCount() {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS total FROM building b WHERE " + PUBLIC_VISIBILITY_WHERE);
    return rows.isEmpty() ? 0 : toLong(rows.get(0).get("total"));
  }

  public static boolean visiblePublicBuildingExists(long buildingId, String buildingName) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM building b WHERE "
                + PUBLIC_VISIBILITY_WHERE
                + " AND b.id = ? AND b.name = ?",
            buildingId,
            buildingName);
    return !rows.isEmpty() && toLong(rows.get(0).get("count")) == 1;
  }

  public static boolean visiblePublicBuildingDistrictMatches(long buildingId, long districtId) {
    List<Map<String, Object>> rows =
        MySqlDbClient.query(
            "SELECT COUNT(*) AS count FROM building b WHERE "
                + PUBLIC_VISIBILITY_WHERE
                + " AND b.id = ? AND b.district_id = ?",
            buildingId,
            districtId);
    return !rows.isEmpty() && toLong(rows.get(0).get("count")) == 1;
  }

  private static long toLong(Object value) {
    if (value == null) {
      return 0;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }

  private static BigDecimal toBigDecimal(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    if (value instanceof BigInteger integer) {
      return new BigDecimal(integer);
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    return new BigDecimal(value.toString());
  }

  private static Long toNullableLong(Object value) {
    return value == null ? null : toLong(value);
  }

  private static String toNullableString(Object value) {
    return value == null ? null : value.toString();
  }

  public record PublicBuildingRow(
      long id,
      String name,
      Long districtId,
      String districtName,
      String ward,
      String street,
      String direction,
      String level,
      String transactionType,
      String image,
      BigDecimal rentPrice,
      BigDecimal salePrice) {
    static PublicBuildingRow from(Map<String, Object> row) {
      return new PublicBuildingRow(
          toLong(row.get("id")),
          toNullableString(row.get("name")),
          toNullableLong(row.get("districtId")),
          toNullableString(row.get("districtName")),
          toNullableString(row.get("ward")),
          toNullableString(row.get("street")),
          toNullableString(row.get("direction")),
          toNullableString(row.get("level")),
          toNullableString(row.get("transactionType")),
          toNullableString(row.get("image")),
          toBigDecimal(row.get("rentPrice")),
          toBigDecimal(row.get("salePrice")));
    }
  }
}
