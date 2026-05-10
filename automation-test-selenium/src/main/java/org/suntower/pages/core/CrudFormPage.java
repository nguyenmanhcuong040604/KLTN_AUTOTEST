package org.suntower.pages.core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;
import org.suntower.helpers.browser.OptionalActionHelper;

public class CrudFormPage extends BasePage {
  protected String addPath;
  protected String editPath;
  protected final By form = css("form");
  protected final By submitButton =
      css("form button[type='submit'], form input[type='submit'], button[type='submit'][form], button.btn-submit, button.btn-save");
  protected final By cancelButton = css("button.btn-cancel, a.btn-cancel, button[type='button']");

  public CrudFormPage(WebDriver driver) {
    super(driver);
  }

  public void openAdd() {
    if (addPath == null || addPath.isBlank()) {
      throw new IllegalStateException("This form page does not define an add path.");
    }
    visit(addPath);
  }

  public void openEdit(int id) {
    if (editPath == null || editPath.isBlank()) {
      throw new IllegalStateException("This form page does not support edit navigation.");
    }
    visit(editPath + "/" + id);
  }

  public void fillTextField(String fieldName, String value) {
    fill(inputByName(fieldName), value);
  }

  public void fillNumberField(String fieldName, Number value) {
    fill(inputByName(fieldName), String.valueOf(value));
  }

  public void selectOption(String fieldName, String value) {
    selectByValue(inputByName(fieldName), value);
  }

  public void submit() {
    click(submitButton);
  }

  public boolean submitIfPresent() {
    return OptionalActionHelper.clickIfPresent(driver, submitButton);
  }

  public void cancel() {
    click(cancelButton);
  }
}
