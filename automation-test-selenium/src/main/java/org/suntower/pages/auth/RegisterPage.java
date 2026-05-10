package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class RegisterPage extends BasePage {
  private final By emailInput = anyCss("[data-testid='register-email']", "input[name='email']", "input[type='email']");
  private final By submitButton = anyCss("[data-testid='register-submit']", "button[type='submit']");

  public RegisterPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    visit("/register");
  }

  public void waitForLoaded() {
    waitForUrlContains("/register");
    waitForVisible(emailInput);
    waitForVisible(submitButton);
  }

  public void requestRegistrationCode(String email) {
    fill(emailInput, email);
    click(submitButton);
  }

  public String emailValue() {
    return value(emailInput);
  }
}
