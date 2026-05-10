package org.suntower.pages.core;

import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class NavigationPage extends BasePage {
  public NavigationPage(WebDriver driver) {
    super(driver);
  }

  public void open(String path) {
    visit(path);
  }
}
