package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class ResetPasswordPage extends BasePage {
  private final By otpInput = anyCss("[data-testid='reset-password-otp']", "input[name='otp']");
  private final By newPasswordInput = anyCss("[data-testid='reset-password-new']", "input[name='newPassword']");
  private final By confirmPasswordInput = anyCss("[data-testid='reset-password-confirm']", "input[name='confirmPassword']");
  private final By submitButton = anyCss("[data-testid='reset-password-submit']", "button[type='submit']");
  private final By emailDisplay = css("#emailDisplay");

  public ResetPasswordPage(WebDriver driver) {
    super(driver);
  }

  public void open(String email) {
    visit("/auth/reset-password?email=" + urlEncode(email));
  }

  public void waitForLoaded() {
    waitForLoaded(null);
  }

  public void waitForLoaded(String email) {
    waitForUrlContains("/auth/reset-password");
    waitForVisible(otpInput);
    waitForVisible(newPasswordInput);
    waitForVisible(confirmPasswordInput);
    waitForVisible(submitButton);
    if (email != null && !email.isBlank() && isVisible(emailDisplay)) {
      waitForValue(emailDisplay, email);
    }
  }

  public void resetPassword(String otp, String password) {
    resetPassword(otp, password, password);
  }

  public void resetPassword(String otp, String password, String confirmPassword) {
    fill(otpInput, otp);
    fill(newPasswordInput, password);
    fill(confirmPasswordInput, confirmPassword);
    click(submitButton);
  }

  public void waitForPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
