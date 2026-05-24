package org.suntower.pages.publicsite;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.suntower.core.BasePage;

public class PublicLandingPage extends BasePage {
  private final By filterForm = anyCss("[data-testid='public-filter-form']", "#filterForm", "form");
  private final By filterBody = anyCss("[data-testid='public-filter-body']", "#filterBody");
  private final By toggleFilterButton = anyCss("[data-testid='public-filter-toggle']", ".btn-toggle-filter");
  private final By buildingNameInput = anyCss("[data-testid='public-building-name']", "[name='name']");
  private final By searchButton = anyCss("[data-testid='public-search']", "button[type='submit']");
  private final By resetButton = anyCss("[data-testid='public-reset']", ".btn-reset");
  private final By totalBuilding = anyCss("[data-testid='public-total-building']", "#totalBuilding");
  private final By buildingList = anyCss("[data-testid='public-building-list']", "#buildingList");
  private final By buildingCards = anyCss("[data-testid='building-card']", ".building-card");
  private final By emptyState = css(".empty-state");
  private final By paginationContainer = css("#paginationContainer");
  private final By paginationButtons = css("#paginationContainer button");
  private final By detailModal = css("#modalContainer .modal.show");
  private final By detailModalTitle = css("#modalContainer .modal.show .modal-title");
  private final By detailModalBody = css("#modalContainer .modal.show .modal-body");

  public PublicLandingPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    open("");
  }

  public void open(String query) {
    String normalizedQuery = query == null || query.isBlank() ? "" : (query.startsWith("?") ? query : "?" + query);
    visit("/suntower" + normalizedQuery);
  }

  public String encodeQueryValue(String value) {
    return urlEncode(value);
  }

  public void assertLoaded() {
    waitForResultsLoaded();
  }

  public void waitForResultsLoaded() {
    waitForFilterMetadataLoaded();
    waitForVisible(filterForm);
    waitForResultsSettled();
  }

  public void waitForFilterMetadataLoaded() {
    waitForCondition(() -> optionCount("districtId") > 1, "District metadata was not loaded.");
    waitForCondition(() -> optionCount("ward") > 1, "Ward metadata was not loaded.");
    waitForCondition(() -> optionCount("street") > 1, "Street metadata was not loaded.");
    waitForCondition(() -> optionCount("direction") > 1, "Direction metadata was not loaded.");
    waitForCondition(() -> optionCount("level") > 1, "Level metadata was not loaded.");
  }

  public void waitForResultsSettled() {
    waitForVisible(buildingList);
    waitForCondition(
        () -> cardCount() > 0 || isVisible(emptyState) || normalizeLooseText(text(totalBuilding)).contains("tim thay"),
        "Public building results did not settle.");
  }

  public void waitForHasResults() {
    waitForCondition(() -> cardCount() > 0, "Public building cards did not appear.");
  }

  public void searchByBuildingName(String name) {
    fill(buildingNameInput, name);
    search();
  }

  public void search() {
    click(firstVisible(searchButton));
    waitForResultsSettled();
  }

  public void resetFilters() {
    click(firstVisible(resetButton));
    waitForValue(buildingNameInput, "");
  }

  public void toggleFilterPanel() {
    click(firstVisible(toggleFilterButton));
  }

  public void fillFilter(String fieldName, String value) {
    fill(By.name(fieldName), value);
  }

  public void fillNumberRange(String fromName, String toName, String fromValue, String toValue) {
    fillFilter(fromName, fromValue);
    fillFilter(toName, toValue);
  }

  public void selectDistrict(String value) {
    selectByValue(By.name("districtId"), value);
  }

  public int optionCount(String fieldName) {
    WebElement select = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(fieldName)));
    return select.findElements(By.tagName("option")).size();
  }

  public String selectedValue(String fieldName) {
    return visible(By.name(fieldName)).getAttribute("value");
  }

  public String filterValue(String fieldName) {
    return visible(By.name(fieldName)).getAttribute("value");
  }

  public String firstNonEmptyOptionValue(String fieldName) {
    return new Select(visible(By.name(fieldName))).getOptions().stream()
        .map(option -> option.getAttribute("value"))
        .filter(value -> value != null && !value.isBlank())
        .findFirst()
        .orElseThrow(() -> new AssertionError("No non-empty option available for " + fieldName));
  }

  public int cardCount() {
    return visibles(buildingCards).size();
  }

  public List<String> cardNames() {
    return visibles(buildingCards).stream()
        .map(card -> card.findElements(By.cssSelector(".building-name")).stream().findFirst().map(WebElement::getText).orElse(card.getText()))
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .toList();
  }

  public String firstCardName() {
    waitForHasResults();
    return cardNames().stream().findFirst().orElseThrow(() -> new AssertionError("No public building card names were displayed."));
  }

  public String resultSummaryText() {
    return looseText(totalBuilding);
  }

  public void waitForEmptyState() {
    waitForVisible(emptyState);
  }

  public void openFirstBuildingDetails() {
    click(firstVisible(buildingCards));
    waitForVisible(detailModal);
  }

  public void openBuildingDetailsByName(String name) {
    click(cardByName(name));
    waitForDetailModalVisible(name);
  }

  public void waitForCardUsesBuildingIconFallback(String name) {
    WebElement card = cardByName(name);
    waitForCondition(
        () -> card.findElements(By.cssSelector(".building-image img")).stream().noneMatch(WebElement::isDisplayed),
        "Building card still displayed an image: " + name);
    waitForCondition(
        () -> card.findElements(By.cssSelector(".building-image .bi-building")).stream().anyMatch(WebElement::isDisplayed),
        "Building icon fallback was not displayed: " + name);
  }

  public boolean isPaginationVisible() {
    return isVisible(paginationContainer);
  }

  public int paginationCount() {
    return visibles(paginationButtons).size();
  }

  public void clickPaginationPage(int pageNumber) {
    String label = String.valueOf(pageNumber);
    ((org.openqa.selenium.JavascriptExecutor) driver)
        .executeScript(
            """
            const label = arguments[0];
            const zeroBasedPage = Number(label) - 1;
            if (typeof window.loadBuildings === 'function') {
              window.loadBuildings(zeroBasedPage);
              return;
            }
            const button = [...document.querySelectorAll('#paginationContainer button')]
              .find(item => item.textContent.trim() === label);
            if (button) button.click();
            """,
            label);
    waitForResultsSettled();
    waitForCondition(() -> label.equals(activePaginationText()), "Pagination active page did not update to: " + pageNumber);
  }

  public WebElement paginationButton(int pageNumber) {
    String label = String.valueOf(pageNumber);
    By button = By.xpath("//*[@id='paginationContainer']//button[normalize-space(.)='" + label + "']");
    waitForVisible(button);
    return firstVisible(button);
  }

  public String activePaginationText() {
    Object activeText =
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript(
                """
                const buttons = [...document.querySelectorAll('#paginationContainer button')];
                const active = buttons.find(button => (button.getAttribute('style') || '').replaceAll(' ', '').includes('font-weight:700'));
                return active ? active.textContent.trim() : '';
                """);
    return activeText == null ? "" : String.valueOf(activeText);
  }

  public WebElement cardByName(String name) {
    waitForCondition(
        () -> visibles(buildingCards).stream().anyMatch(card -> card.getText().contains(name)),
        "No building card found by name: " + name);
    return visibles(buildingCards).stream().filter(card -> card.getText().contains(name)).findFirst().orElseThrow();
  }

  public void waitForDetailModalVisible(String buildingName) {
    waitForVisible(detailModal);
    waitForCondition(
        () -> normalizeLooseText(text(detailModalTitle)).contains("thong tin") || normalizeLooseText(text(detailModalTitle)).contains("information"),
        "Building detail modal title was not displayed.");
    if (buildingName != null && !buildingName.isBlank()) {
      waitForText(detailModalBody, buildingName);
    }
  }

  public String detailModalLooseText() {
    return looseText(detailModalBody);
  }

  public boolean isFilterCollapsed() {
    return wait.until(ExpectedConditions.presenceOfElementLocated(filterBody)).getAttribute("class").contains("collapsed");
  }

  public String storedFilterCollapsedValue() {
    Object value = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('filterCollapsed')");
    return value == null ? null : value.toString();
  }

  public boolean isFilterFormVisible() {
    return isVisible(filterForm);
  }

  public boolean isSearchButtonVisible() {
    return isVisible(searchButton);
  }

  public boolean isBuildingNameInputValue(String expected) {
    return expected.equals(value(buildingNameInput));
  }
}
