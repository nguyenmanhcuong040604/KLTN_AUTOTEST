package org.suntower.fixtures.auth;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.suntower.core.AppConfig;
import org.suntower.pages.auth.LoginPage;

public final class AuthSessionHelper {
  private AuthSessionHelper() {}

  public static void loginUi(WebDriver driver, String username, String password) {
    LoginPage loginPage = new LoginPage(driver);
    loginPage.open();
    loginPage.login(username, password);
  }

  public static void loginAsRoleUiStrict(WebDriver driver, String role) {
    String username = TestAccountResolver.rememberedOrDefault(role);
    loginUi(driver, username, AppConfig.get().defaultPassword());
    TestAccountResolver.remember(role, username);
    WebDriverWait wait = new WebDriverWait(driver, AppConfig.get().navigationTimeout());
    wait.until(ExpectedConditions.or(
        ExpectedConditions.urlContains("/" + role + "/"),
        ExpectedConditions.urlContains("/login-success")));
    if (driver.getCurrentUrl().contains("/login-success")) {
      driver.navigate().to(AppConfig.get().baseUrl() + defaultHomePath(role));
      wait.until(ExpectedConditions.urlContains("/" + role + "/"));
    }
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
