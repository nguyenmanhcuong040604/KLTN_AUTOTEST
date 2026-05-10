package org.suntower.fixtures.data;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.suntower.core.AppConfig;

public final class TestDataFactory {
  public static final int MISSING_ID = 999_999_999;
  public static final AuthPassword AUTH_PASSWORD = new AuthPassword();
  public static final AuthIdentity AUTH_IDENTITY = new AuthIdentity();

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

  public static String uniquePhoneNumber() {
    String suffix = String.valueOf(System.currentTimeMillis()).substring(4);
    return "0" + suffix;
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

  public record CustomerPayload(String username, String password, String fullName, String phone, String email) {}

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
}
