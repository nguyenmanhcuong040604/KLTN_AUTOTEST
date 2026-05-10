package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class ForgotPasswordPage extends BasePage {
  private final By emailInput = anyCss("[data-testid='forgot-password-email']", "input[name='email']", "input[type='email']");
  private final By submitButton = anyCss("[data-testid='forgot-password-submit']", "button[type='submit']");

  public ForgotPasswordPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    visit("/forgot-password");
  }

  public void waitForLoaded() {
    waitForUrlContains("/forgot-password");
    waitForVisible(emailInput);
    waitForVisible(submitButton);
  }

  public void submitEmail(String email) {
    fill(emailInput, email);
    click(submitButton);
  }

  public void waitForPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
