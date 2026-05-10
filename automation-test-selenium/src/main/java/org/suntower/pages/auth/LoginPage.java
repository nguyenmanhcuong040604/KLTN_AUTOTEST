package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class LoginPage extends BasePage {
  private final By usernameInput = anyCss("[data-testid='login-username']", "#username", "[name='username']");
  private final By passwordInput = anyCss("[data-testid='login-password']", "#password", "[name='password']");
  private final By submitButton = anyCss("[data-testid='login-submit']", ".login-button", "button[type='submit']");
  private final By forgotPasswordLink = anyCss("[data-testid='forgot-password-link']", "a[href='/forgot-password']");
  private final By registerLink = anyCss("[data-testid='register-link']", "a[href='/register']");

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    visit("/login");
  }

  public void login(String username, String password) {
    dismissSweetAlertIfPresent();
    fill(usernameInput, username);
    fill(passwordInput, password);
    click(submitButton);
  }

  public void clickForgotPassword() {
    click(forgotPasswordLink);
  }

  public void clickRegister() {
    click(registerLink);
  }

  public void assertLoaded() {
    waitForUrlContains("/login");
    waitForVisible(usernameInput);
    waitForVisible(passwordInput);
    waitForVisible(submitButton);
    dismissSweetAlertIfPresent();
  }

  public void waitForPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void waitForRegistrationRoute() {
    waitForUrlContains("/register");
  }

  public void waitForForgotPasswordRoute() {
    waitForUrlContains("/forgot-password");
  }

  public void waitForLoginErrorRoute() {
    waitForUrlContains("/login?errorMessage=");
  }
}
