package org.suntower.pages.core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class CrudDetailPage extends BasePage {
  protected String detailPath;
  protected final By pageHeader = css("h1, h2");

  public CrudDetailPage(WebDriver driver) {
    super(driver);
  }

  public void open(int id) {
    if (detailPath == null || detailPath.isBlank()) {
      throw new IllegalStateException("This detail page does not define a detail path.");
    }
    visit(detailPath + "/" + id);
  }

  public void waitForHeaderContains(String text) {
    waitForText(pageHeader, text);
  }
}
