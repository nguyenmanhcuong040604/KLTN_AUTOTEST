package org.suntower.pages.staff;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.core.CrudListPage;

public class StaffBuildingListPage extends CrudListPage {
  private final By cardsContainer = css("#buildingCardsContainer");
  private final By detailModal = css("#modalContainer .modal.show");

  public StaffBuildingListPage(WebDriver driver) {
    super(driver);
    this.path = "/staff/buildings";
  }

  public void waitForLoaded() {
    waitForUrlContains("/staff/buildings");
    waitForVisible(cardsContainer);
  }

  public void waitForBuildingData() {
    waitForCondition(
        () -> !driver.findElements(css("#buildingCardsContainer .building-card")).isEmpty() || isVisible(css("#buildingCardsContainer .empty-state")),
        "Staff building list did not load cards or empty state.");
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
            driver.findElements(css("#buildingCardsContainer .building-card")).stream()
                .anyMatch(
                    card -> {
                      try {
                        return card.isDisplayed() && card.getText().contains(name);
                      } catch (RuntimeException ignored) {
                        return false;
                      }
                    }),
        "No visible building card contained text: " + name);
    return driver.findElements(css("#buildingCardsContainer .building-card")).stream()
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
            WebElement button = cardByBuildingName(name).findElement(By.cssSelector(".btn-view-detail"));
            click(button);
            return true;
          } catch (RuntimeException ignored) {
            return false;
          }
        },
        "Could not open building detail for: " + name);
  }

  public void waitForDetailModalContains(String name) {
    waitForVisible(detailModal);
    waitForText(detailModal, name);
  }
}
