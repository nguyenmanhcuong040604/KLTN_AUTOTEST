package org.suntower.fixtures.data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.suntower.core.AppConfig;

public final class TestDataFactory {
  public static final int MISSING_ID = 999_999_999;
  public static final AuthPassword AUTH_PASSWORD = new AuthPassword();
  public static final AuthIdentity AUTH_IDENTITY = new AuthIdentity();
  public static final BuildingForm BUILDING_FORM = new BuildingForm();

  private static final AtomicInteger UNIQUE_COUNTER = new AtomicInteger();

  private TestDataFactory() {}

  public static String runToken() {
    String seed = System.getProperty("TEST_RUN_ID", System.getenv().getOrDefault("TEST_RUN_ID", "selenium-local"));
    String normalized = seed.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    if (normalized.length() >= 8) {
      return normalized.substring(normalized.length() - 8);
    }
    return String.format("%8s", normalized).replace(' ', '0');
  }

  public static String uniqueCode(String prefix) {
    return "%s-%s-%s-%04d".formatted(
        prefix,
        runToken(),
        Long.toString(System.currentTimeMillis(), 36),
        UNIQUE_COUNTER.getAndIncrement() % 10_000);
  }

  public static String uniqueIdentifier(String prefix) {
    return uniqueCode(prefix).replaceAll("[^a-zA-Z0-9]", "");
  }

  public static String uniqueUsername(String prefix) {
    String value = uniqueIdentifier(prefix);
    return value.length() > 30 ? value.substring(0, 30) : value;
  }

  public static String uniqueEmail(String prefix) {
    return uniqueCode(prefix) + "@example.com";
  }

  public static String uniqueBuildingName(String prefix) {
    return prefix + " " + uniqueCode("building");
  }

  public static String uniquePhoneNumber() {
    long value = Math.abs(System.nanoTime() + UNIQUE_COUNTER.getAndIncrement());
    return "09" + String.format("%08d", value % 100_000_000);
  }

  public static CustomerPayload buildCustomerPayload() {
    String suffix = uniqueCode("customer");
    return new CustomerPayload(
        uniqueUsername("selcust"),
        AppConfig.get().defaultPassword(),
        "SEL Customer " + suffix,
        uniquePhoneNumber(),
        uniqueEmail("sel-customer"));
  }

  public static StaffPayload buildStaffPayload(String role) {
    String normalizedRole = role == null || role.isBlank() ? "STAFF" : role;
    String suffix = uniqueCode(normalizedRole.toLowerCase(Locale.ROOT));
    return new StaffPayload(
        uniqueUsername("selstf"),
        AppConfig.get().defaultPassword(),
        "SEL " + normalizedRole + " " + suffix,
        uniquePhoneNumber(),
        uniqueEmail("sel-staff"),
        normalizedRole);
  }

  public static Map<String, Object> buildBuildingPayload(String transactionType) {
    String normalizedTransactionType = transactionType == null || transactionType.isBlank() ? "FOR_RENT" : transactionType;
    String suffix = uniqueCode("building");
    boolean forRent = "FOR_RENT".equals(normalizedTransactionType);
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("districtId", 1);
    payload.put("numberOfFloor", 10);
    payload.put("numberOfBasement", 1);
    payload.put("floorArea", 200);
    payload.put("rentPrice", forRent ? 1_000_000 : null);
    payload.put("deposit", forRent ? 2_000_000 : null);
    payload.put("serviceFee", forRent ? 100_000 : null);
    payload.put("carFee", forRent ? 50_000 : null);
    payload.put("motorbikeFee", forRent ? 20_000 : null);
    payload.put("waterFee", forRent ? 15_000 : null);
    payload.put("electricityFee", forRent ? 3_500 : null);
    payload.put("salePrice", forRent ? null : 3_000_000_000L);
    payload.put("name", "SEL Building " + suffix);
    payload.put("ward", "Xuan La");
    payload.put("street", "Vo Chi Cong");
    payload.put("propertyType", "OFFICE");
    payload.put("transactionType", normalizedTransactionType);
    payload.put("direction", "DONG");
    payload.put("level", "A");
    payload.put("taxCode", uniqueNumberCode("SEL", 10));
    payload.put("linkOfBuilding", "https://example.com");
    payload.put("image", null);
    payload.put("rentAreaValues", forRent ? "50,100" : "");
    payload.put("latitude", 21.0686);
    payload.put("longitude", 105.8033);
    payload.put("staffIds", java.util.List.of());
    return payload;
  }

  public static String uniqueNumberCode(String prefix, int digits) {
    String numeric = String.valueOf(System.currentTimeMillis()) + String.format("%06d", UNIQUE_COUNTER.getAndIncrement() % 1_000_000);
    String suffix = numeric.substring(Math.max(0, numeric.length() - digits));
    return prefix + suffix;
  }

  public record CustomerPayload(String username, String password, String fullName, String phone, String email) {}

  public record StaffPayload(String username, String password, String fullName, String phone, String email, String role) {}

  public static final class AuthPassword {
    public final String registrationDefault = "Password@123";
    public final String resetNewPassword = "Password@456";
    public final String shortOtp = "123456";
    public final String mismatchConfirmation = "Mismatch@456";
    public final String invalidLoginPassword = "wrong-password";
    public final String invalidRegisteredPassword = "WrongPassword!123";
  }

  public static final class AuthIdentity {
    public final String unknownUsername = "unknown-user";
  }

  public static final class BuildingForm {
    public final String taxCodePrefix = "TAX";
    public final String validLink = "https://example.com/building";
    public final String defaultDistrictId = "1";
    public final String defaultWard = "Xuan La";
    public final String defaultStreet = "Vo Chi Cong";
    public final String defaultLevel = "A";
    public final String defaultDirection = "DONG";
    public final Coordinates defaultCoordinates = new Coordinates(21.0686, 105.8033);
    public final RentalFields rentalCreate =
        new RentalFields(12, 2, 450, 1_200_000, 2_400_000, 100_000, 50_000, 20_000, 15_000, 3_500, "50,100");
    public final RentalFields rentalUpdate =
        new RentalFields(15, null, 999, 1_300_000, 2_500_000, 110_000, 60_000, 30_000, 18_000, 4_000, "70,140");
  }

  public record Coordinates(Number latitude, Number longitude) {}

  public record RentalFields(
      Number numberOfFloor,
      Number numberOfBasement,
      Number floorArea,
      Number rentPrice,
      Number deposit,
      Number serviceFee,
      Number carFee,
      Number motorbikeFee,
      Number waterFee,
      Number electricityFee,
      String rentAreaValues) {}
}
