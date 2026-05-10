package org.suntower.fixtures.auth;

import org.openqa.selenium.WebDriver;
import org.suntower.pages.auth.LoginPage;
import org.suntower.pages.core.NavigationPage;

public class RoleSession {
  private final WebDriver driver;
  private final String role;

  public RoleSession(WebDriver driver, String role) {
    this.driver = driver;
    this.role = role;
  }

  public void login() {
    AuthSessionHelper.loginAsRoleUiStrict(driver, role);
  }

  public void open() {
    open(defaultHomePath(role));
  }

  public void open(String path) {
    login();
    new NavigationPage(driver).open(path);
  }

  private static String defaultHomePath(String role) {
    return switch (role) {
      case "admin" -> "/admin/dashboard";
      case "staff" -> "/staff/dashboard";
      case "customer" -> "/customer/home";
      default -> throw new IllegalArgumentException("Unsupported role: " + role);
    };
  }
}
