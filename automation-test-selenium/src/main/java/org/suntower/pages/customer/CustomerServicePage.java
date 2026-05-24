package org.suntower.pages.customer;

import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;

public class CustomerServicePage extends BasePage {
  private final By categories = css(".category-section");
  private final By cards = css(".service-card");

  public CustomerServicePage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/service");
    waitForCondition(() -> driver.findElements(categories).size() >= 2, "Customer service categories did not load.");
  }

  public void waitForCardVisible(String title) {
    cardByTitle(title);
  }

  public void waitForRequestDisabled(String title) {
    WebElement card = cardByTitle(title);
    waitForCondition(
        () -> card.findElements(By.cssSelector(".btn-request")).stream().anyMatch(button -> !button.isEnabled() || button.getAttribute("disabled") != null),
        "Request button was not disabled for service: " + title);
  }

  private WebElement cardByTitle(String title) {
    List<String> words = serviceTitleWords(title);
    waitForCondition(
        () ->
            driver.findElements(cards).stream()
                .anyMatch(card -> card.isDisplayed() && words.stream().allMatch(word -> normalizeLooseText(card.getText()).contains(word))),
        "No service card contained title words: " + title);
    return driver.findElements(cards).stream()
        .filter(card -> card.isDisplayed() && words.stream().allMatch(word -> normalizeLooseText(card.getText()).contains(word)))
        .findFirst()
        .orElseThrow();
  }

  private List<String> serviceTitleWords(String title) {
    String normalized = normalizeLooseText(title);
    if (normalized.contains("xe")) return List.of("xe");
    if (normalized.contains("internet")) return List.of("internet");
    if (normalized.contains("gym")) return List.of("gym");
    if (normalized.contains("an ninh")) return List.of("an", "ninh");
    return Arrays.asList(normalized.split(" "));
  }
}
