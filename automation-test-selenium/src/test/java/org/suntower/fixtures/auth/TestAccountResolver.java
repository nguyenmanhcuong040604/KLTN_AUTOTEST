package org.suntower.fixtures.auth;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.suntower.core.AppConfig;

public final class TestAccountResolver {
  private static final Map<String, String> REMEMBERED = new ConcurrentHashMap<>();

  private TestAccountResolver() {}

  public static List<String> usernameCandidates(String role) {
    AppConfig config = AppConfig.get();
    return switch (role) {
      case "admin" -> config.adminUsernames();
      case "staff" -> config.staffUsernames();
      case "customer" -> config.customerUsernames();
      default -> throw new IllegalArgumentException("Unsupported role: " + role);
    };
  }

  public static void remember(String role, String username) {
    REMEMBERED.put(role, username);
  }

  public static String rememberedOrDefault(String role) {
    return REMEMBERED.getOrDefault(role, usernameCandidates(role).get(0));
  }
}
