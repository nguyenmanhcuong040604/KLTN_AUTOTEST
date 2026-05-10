package org.suntower.pages.publicsite;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;

public class PublicLandingPage extends BasePage {
  private final By filterForm = anyCss("[data-testid='public-filter-form']", "#filterForm", "form");
  private final By filterBody = anyCss("[data-testid='public-filter-body']", "#filterBody");
  private final By buildingNameInput = anyCss("[data-testid='public-building-name']", "[name='name']");
  private final By searchButton = anyCss("[data-testid='public-search']", "button[type='submit']");
  private final By resetButton = anyCss("[data-testid='public-reset']", ".btn-reset");
  private final By totalBuilding = anyCss("[data-testid='public-total-building']", "#totalBuilding");
  private final By buildingList = anyCss("[data-testid='public-building-list']", "#buildingList");
  private final By buildingCards = anyCss("[data-testid='building-card']", ".building-card");
  private final By emptyState = css(".empty-state");
  private final By paginationContainer = css("#paginationContainer");
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

  public void assertLoaded() {
    waitForResultsLoaded();
  }

  public void waitForResultsLoaded() {
    waitForVisible(filterForm);
    waitForResultsSettled();
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
    return visible(By.name(fieldName)).findElements(By.tagName("option")).size();
  }

  public String selectedValue(String fieldName) {
    return visible(By.name(fieldName)).getAttribute("value");
  }

  public String filterValue(String fieldName) {
    return visible(By.name(fieldName)).getAttribute("value");
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
    return visible(filterBody).getAttribute("class").contains("collapsed");
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
