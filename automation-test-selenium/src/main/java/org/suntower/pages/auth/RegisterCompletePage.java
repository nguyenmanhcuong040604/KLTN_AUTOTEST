package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class RegisterCompletePage extends BasePage {
  private final By fullNameInput = css("input[name='fullName']");
  private final By usernameInput = css("input[name='username']");
  private final By passwordInput = css("input[name='password']");
  private final By confirmPasswordInput = css("input[name='confirmPassword']");
  private final By completeButton =
      anyCss(
          "[data-testid='register-complete-submit']",
          "form[action='/auth/register/complete'] button[type='submit']",
          "button.submit",
          "button[type='submit']");
  private final By body = css("body");

  public RegisterCompletePage(WebDriver driver) {
    super(driver);
  }

  public void open(String ticket, String email) {
    visit("/register/complete?ticket=" + urlEncode(ticket) + "&email=" + urlEncode(email));
  }

  public void waitForLoaded() {
    waitForLoaded(null);
  }

  public void waitForLoaded(String email) {
    waitForUrlContains("/register/complete");
    waitForVisible(fullNameInput);
    waitForVisible(usernameInput);
    waitForVisible(completeButton);
    dismissSweetAlertIfPresent();
    if (email != null && !email.isBlank()) {
      waitForText(body, email);
    }
  }

  public void completeRegistration(String fullName, String username, String password) {
    completeRegistration(fullName, username, password, password);
  }

  public void completeRegistration(String fullName, String username, String password, String confirmPassword) {
    dismissSweetAlertIfPresent();
    fill(fullNameInput, fullName);
    fill(usernameInput, username);
    fill(passwordInput, password);
    fill(confirmPasswordInput, confirmPassword);
    click(completeButton);
  }

  public void waitForPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
