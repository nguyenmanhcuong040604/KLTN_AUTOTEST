package org.suntower.core;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class AppConfig {
  private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
  private static final AppConfig INSTANCE = new AppConfig();

  private final String appEnv;
  private final String baseUrl;
  private final String browser;
  private final boolean headless;
  private final int workers;
  private final int retries;
  private final Duration expectTimeout;
  private final Duration actionTimeout;
  private final Duration navigationTimeout;
  private final String defaultPassword;
  private final List<String> adminUsernames;
  private final List<String> staffUsernames;
  private final List<String> customerUsernames;
  private final String testSupportOtpToken;
  private final String dbJdbcUrl;
  private final String dbUsername;
  private final String dbPassword;
  private final int dbPoolLimit;

  private AppConfig() {
    this.appEnv = value("APP_ENV", "local");
    this.baseUrl = requireUrl("BASE_URL", value(baseUrlKey(appEnv), "http://localhost:8080"));
    this.browser = value("BROWSER", "chrome").toLowerCase(Locale.ROOT);
    this.headless = bool("HEADLESS", true);
    this.workers = positiveInt(isCi() ? "CI_WORKERS" : "WORKERS", isCi() ? 2 : 1);
    this.retries = nonNegativeInt("E2E_RETRIES", isCi() ? 2 : 0);
    this.expectTimeout = Duration.ofMillis(positiveInt("EXPECT_TIMEOUT", 10_000));
    this.actionTimeout = Duration.ofMillis(positiveInt("ACTION_TIMEOUT", 15_000));
    this.navigationTimeout = Duration.ofMillis(positiveInt("NAVIGATION_TIMEOUT", 30_000));
    this.defaultPassword = value("DEFAULT_PASSWORD", "12345678");
    this.adminUsernames = candidates("ADMIN_USERNAMES", "ADMIN_USERNAME", "admin123");
    this.staffUsernames = candidates("STAFF_USERNAMES", "STAFF_USERNAME", "tmq0102");
    this.customerUsernames = candidates("CUSTOMER_USERNAMES", "CUSTOMER_USERNAME", "abcVietNam");
    this.testSupportOtpToken = value("TEST_SUPPORT_OTP_TOKEN", "test-otp-token");
    this.dbJdbcUrl = value("DB_JDBC_URL", value("SPRING_DATASOURCE_URL", "jdbc:mysql://localhost:3306/estate"));
    this.dbUsername = value("DB_USERNAME", value("SPRING_DATASOURCE_USERNAME", "root"));
    this.dbPassword = value("DB_PASSWORD", value("SPRING_DATASOURCE_PASSWORD", "123456"));
    this.dbPoolLimit = positiveInt("DB_POOL_LIMIT", 5);
  }

  public static AppConfig get() {
    return INSTANCE;
  }

  private static String baseUrlKey(String appEnv) {
    return switch (appEnv) {
      case "dev" -> "DEV_BASE_URL";
      case "test" -> "TEST_BASE_URL";
      case "staging" -> "STAGING_BASE_URL";
      default -> "LOCAL_BASE_URL";
    };
  }

  private static String value(String key, String fallback) {
    String systemProperty = System.getProperty(key);
    if (systemProperty != null && !systemProperty.isBlank()) {
      return systemProperty.trim();
    }
    String environment = System.getenv(key);
    if (environment != null && !environment.isBlank()) {
      return environment.trim();
    }
    String dotenv = DOTENV.get(key);
    if (dotenv != null && !dotenv.isBlank()) {
      return dotenv.trim();
    }
    return fallback;
  }

  private static boolean bool(String key, boolean fallback) {
    String raw = value(key, String.valueOf(fallback)).toLowerCase(Locale.ROOT);
    if (List.of("true", "1", "yes", "y").contains(raw)) {
      return true;
    }
    if (List.of("false", "0", "no", "n").contains(raw)) {
      return false;
    }
    throw new IllegalArgumentException("Invalid boolean environment variable " + key + ": " + raw);
  }

  private static int positiveInt(String key, int fallback) {
    int parsed = nonNegativeInt(key, fallback);
    if (parsed < 1) {
      throw new IllegalArgumentException("Invalid positive integer environment variable " + key + ": " + parsed);
    }
    return parsed;
  }

  private static int nonNegativeInt(String key, int fallback) {
    String raw = value(key, String.valueOf(fallback));
    try {
      int parsed = Integer.parseInt(raw);
      if (parsed < 0) {
        throw new IllegalArgumentException("Invalid non-negative integer environment variable " + key + ": " + raw);
      }
      return parsed;
    } catch (NumberFormatException error) {
      throw new IllegalArgumentException("Invalid numeric environment variable " + key + ": " + raw, error);
    }
  }

  private static String requireUrl(String key, String value) {
    try {
      URI.create(value).toURL();
      return value.replaceAll("/+$", "");
    } catch (IllegalArgumentException | MalformedURLException error) {
      throw new IllegalArgumentException("Invalid URL for " + key + ": " + value, error);
    }
  }

  private static List<String> candidates(String pluralKey, String singleKey, String fallback) {
    String raw = value(pluralKey, value(singleKey, fallback));
    return Arrays.stream(raw.split(",")).map(String::trim).filter(item -> !item.isEmpty()).toList();
  }

  public boolean isCi() {
    return "true".equalsIgnoreCase(value("CI", "false"));
  }

  public String appEnv() {
    return appEnv;
  }

  public String baseUrl() {
    return baseUrl;
  }

  public URL baseUrlAsUrl() {
    try {
      return URI.create(baseUrl).toURL();
    } catch (MalformedURLException error) {
      throw new IllegalStateException(error);
    }
  }

  public String browser() {
    return browser;
  }

  public boolean headless() {
    return headless;
  }

  public int workers() {
    return workers;
  }

  public int retries() {
    return retries;
  }

  public Duration expectTimeout() {
    return expectTimeout;
  }

  public Duration actionTimeout() {
    return actionTimeout;
  }

  public Duration navigationTimeout() {
    return navigationTimeout;
  }

  public String defaultPassword() {
    return defaultPassword;
  }

  public List<String> adminUsernames() {
    return adminUsernames;
  }

  public List<String> staffUsernames() {
    return staffUsernames;
  }

  public List<String> customerUsernames() {
    return customerUsernames;
  }

  public String testSupportOtpToken() {
    return testSupportOtpToken;
  }

  public String dbJdbcUrl() {
    return dbJdbcUrl;
  }

  public String dbUsername() {
    return dbUsername;
  }

  public String dbPassword() {
    return dbPassword;
  }

  public int dbPoolLimit() {
    return dbPoolLimit;
  }
}
