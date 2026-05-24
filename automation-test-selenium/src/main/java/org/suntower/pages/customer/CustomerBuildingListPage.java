package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.core.CrudListPage;

public class CustomerBuildingListPage extends CrudListPage {
  private final By list = css("#buildingList");
  private final By detailModal = css("#modalContainer .modal.show, #modalContainer .modal");

  public CustomerBuildingListPage(WebDriver driver) {
    super(driver);
    this.path = "/customer/building/list";
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/building/list");
    waitForVisible(list);
  }

  public void waitForBuildingData() {
    waitForCondition(
        () -> !driver.findElements(css("#buildingList .building-card")).isEmpty() || isVisible(css("#buildingList .empty-state")),
        "Customer building list did not load cards or empty state.");
  }

  public void filterByName(String name) {
    fill(inputByName("name"), name);
  }

  public void submitFilters() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#filterForm")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    waitForBuildingData();
  }

  public WebElement cardByBuildingName(String name) {
    waitForCondition(
        () ->
            driver.findElements(css("#buildingList .building-card")).stream()
                .anyMatch(
                    card -> {
                      try {
                        return card.isDisplayed() && card.getText().contains(name);
                      } catch (RuntimeException ignored) {
                        return false;
                      }
                    }),
        "No visible customer building card contained text: " + name);
    return driver.findElements(css("#buildingList .building-card")).stream()
        .filter(
            card -> {
              try {
                return card.isDisplayed() && card.getText().contains(name);
              } catch (RuntimeException ignored) {
                return false;
              }
            })
        .findFirst()
        .orElseThrow();
  }

  public void openBuildingDetail(String name) {
    waitForCondition(
        () -> {
          try {
            click(cardByBuildingName(name));
            return true;
          } catch (RuntimeException ignored) {
            return false;
          }
        },
        "Could not open customer building detail for: " + name);
  }

  public void waitForDetailModalContains(String name) {
    waitForText(detailModal, name);
  }

  public void waitForEmptyState() {
    waitForVisible(css("#buildingList .empty-state"));
  }
}
