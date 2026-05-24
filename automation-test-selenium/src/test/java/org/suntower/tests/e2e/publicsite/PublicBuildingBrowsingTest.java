package org.suntower.tests.e2e.publicsite;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.NoAlertPresentException;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.data.scenarios.PublicBuildingScenario;
import org.suntower.fixtures.data.scenarios.PublicBuildingScenario.PublicBuildingRow;
import org.suntower.helpers.text.TextNormalizeHelper;
import org.suntower.pages.publicsite.PublicLandingPage;
import org.testng.annotations.BeforeMethod;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class PublicBuildingBrowsingTest extends BaseTest {
  private PublicLandingPage publicPage;

  @BeforeMethod(alwaysRun = true)
  public void openPublicPage() {
    publicPage = pageObjects.create(PublicLandingPage.class);
    publicPage.open();
    publicPage.waitForResultsLoaded();
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-001] should default filters and initial results display when landing page")
  public void shouldDefaultFiltersAndInitialResultsDisplay() {
    StepHelper.act(
        "perform landing page behavior",
        () -> {
          assertThat(driver.getCurrentUrl()).endsWith("/suntower");
          assertThat(publicPage.isFilterFormVisible()).isTrue();
          assertThat(publicPage.isSearchButtonVisible()).isTrue();
          publicPage.waitForHasResults();
          assertThat(publicPage.resultSummaryText()).containsPattern("tm th.y|tim thay|found");
        });

    StepHelper.assertStep("verify default filters and initial results display", () -> assertThat(publicPage.cardCount()).isPositive());
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-002] should building name prefill and auto search when query parameter")
  public void shouldBuildingNamePrefillAndAutoSearch() {
    String targetBuilding =
        StepHelper.arrange(
            "capture visible building name",
            () -> {
              publicPage.waitForHasResults();
              return publicPage.firstCardName();
            });

    StepHelper.act(
        "open with building name query parameter",
        () -> {
          publicPage.open("buildingName=" + publicPage.encodeQueryValue(targetBuilding));
          publicPage.waitForResultsLoaded();
        });

    StepHelper.assertStep(
        "building name is prefilled and result is shown",
        () -> {
          assertThat(publicPage.isBuildingNameInputValue(targetBuilding)).isTrue();
          assertThat(publicPage.cardByName(targetBuilding).isDisplayed()).isTrue();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-003] should dropdown metadata loading when filter metadata")
  public void shouldDropdownMetadataLoading() {
    StepHelper.assertStep(
        "metadata dropdowns contain loaded options",
        () -> {
          assertThat(publicPage.optionCount("districtId")).isGreaterThan(1);
          assertThat(publicPage.optionCount("ward")).isGreaterThan(1);
          assertThat(publicPage.optionCount("street")).isGreaterThan(1);
          assertThat(publicPage.optionCount("direction")).isGreaterThan(1);
          assertThat(publicPage.optionCount("level")).isGreaterThan(1);
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-004] should exact name search result when building name filter")
  public void shouldExactNameSearchResult() {
    PublicBuildingRow targetBuilding =
        StepHelper.arrange(
            "capture visible building from database",
            () -> requirePublicSeed(PublicBuildingScenario.getSingleVisiblePublicBuilding(), "At least one visible public building is required."));

    StepHelper.act("search by exact building name", () -> publicPage.searchByBuildingName(targetBuilding.name()));

    StepHelper.assertStep(
        "matching building card remains visible",
        () -> {
          assertThat(publicPage.cardByName(targetBuilding.name()).isDisplayed()).isTrue();
          assertThat(publicPage.cardCount()).isPositive();
          assertThat(PublicBuildingScenario.visiblePublicBuildingExists(targetBuilding.id(), targetBuilding.name())).isTrue();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-005] should district narrowing results when district filter")
  public void shouldDistrictNarrowingResults() {
    PublicBuildingRow targetBuilding =
        StepHelper.arrange(
            "capture building with district data",
            () ->
                requirePublicSeed(
                    PublicBuildingScenario.getSingleVisiblePublicBuilding("b.district_id IS NOT NULL"),
                    "At least one visible public building with district data is required."));

    StepHelper.act(
        "filter by name and district",
        () -> {
          publicPage.fillFilter("name", targetBuilding.name());
          publicPage.selectDistrict(String.valueOf(targetBuilding.districtId()));
          publicPage.search();
          publicPage.openBuildingDetailsByName(targetBuilding.name());
        });

    StepHelper.assertStep(
        "district appears in detail and matches seed",
        () -> {
          assertThat(publicPage.detailModalLooseText()).contains(TextNormalizeHelper.normalizeLooseText(targetBuilding.districtName()));
          assertThat(PublicBuildingScenario.visiblePublicBuildingDistrictMatches(targetBuilding.id(), targetBuilding.districtId())).isTrue();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-006] should restore default state when filter reset")
  public void shouldRestoreDefaultStateWhenFilterReset() {
    String targetBuilding =
        StepHelper.arrange(
            "capture filter values",
            () -> {
              publicPage.waitForHasResults();
              return publicPage.firstCardName();
            });
    String districtId = StepHelper.arrange("capture selectable district", () -> publicPage.firstNonEmptyOptionValue("districtId"));

    StepHelper.act(
        "fill filters and reset",
        () -> {
          publicPage.fillFilter("name", targetBuilding);
          publicPage.selectDistrict(districtId);
          publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "1", "9");
          publicPage.resetFilters();
        });

    StepHelper.assertStep(
        "filter inputs return to defaults",
        () -> {
          assertThat(publicPage.filterValue("name")).isEmpty();
          assertThat(publicPage.filterValue("numberOfFloorFrom")).isEmpty();
          assertThat(publicPage.filterValue("numberOfFloorTo")).isEmpty();
          assertThat(publicPage.selectedValue("districtId")).isEmpty();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-007] should empty state for unmatched search when search results")
  public void shouldEmptyStateForUnmatchedSearch() {
    StepHelper.act(
        "perform search results behavior",
        () -> {
          publicPage.searchByBuildingName(TestDataFactory.uniqueCode("zzz-e2e-no-match"));
          publicPage.waitForEmptyState();
        });

    StepHelper.assertStep("verify empty state for unmatched search", () -> assertThat(publicPage.cardCount()).isZero());
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-009] should modal display with key information and price when rental building details")
  public void shouldModalDisplayWithKeyInformationAndRentPrice() {
    PublicBuildingRow rentBuilding =
        StepHelper.arrange(
            "capture visible rental building",
            () ->
                requirePublicSeed(
                    PublicBuildingScenario.getSingleVisiblePublicBuilding("b.transaction_type = 'FOR_RENT'"),
                    "At least one visible FOR_RENT building is required."));

    StepHelper.act(
        "open rental building detail modal",
        () -> {
          publicPage.searchByBuildingName(rentBuilding.name());
          publicPage.openBuildingDetailsByName(rentBuilding.name());
        });

    StepHelper.assertStep(
        "rental detail modal shows key sections and pricing",
        () -> {
          String modalText = publicPage.detailModalLooseText();
          assertThat(modalText).contains(TextNormalizeHelper.normalizeLooseText(rentBuilding.name()));
          assertThat(modalText).containsPattern("thong tin chung|general information");
          assertThat(modalText).containsPattern("dac diem bat dong san|property features");
          assertThat(modalText).containsPattern("gia thue|rent price");
          assertThat(modalText).containsPattern("phi dich vu|service fee");
          assertThat(rentBuilding.rentPrice()).isNotNull();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-010] should sale price display without rental fields when sale building details")
  public void shouldSalePriceDisplayWithoutRentalFields() {
    PublicBuildingRow saleBuilding =
        StepHelper.arrange(
            "capture visible sale building",
            () ->
                requirePublicSeed(
                    PublicBuildingScenario.getSingleVisiblePublicBuilding("b.transaction_type = 'FOR_SALE'"),
                    "At least one visible FOR_SALE building is required."));

    StepHelper.act(
        "open sale building detail modal",
        () -> {
          publicPage.searchByBuildingName(saleBuilding.name());
          publicPage.openBuildingDetailsByName(saleBuilding.name());
        });

    StepHelper.assertStep(
        "sale detail modal shows sale price only",
        () -> {
          String modalText = publicPage.detailModalLooseText();
          assertThat(modalText).contains(TextNormalizeHelper.normalizeLooseText(saleBuilding.name()));
          assertThat(modalText).containsPattern("gia ban|sale price");
          assertThat(modalText).doesNotContainPattern("dien tich thue kha dung|rentable area");
          assertThat(saleBuilding.salePrice()).isNotNull();
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-011] should safe rendering without image when building card media")
  public void shouldSafeRenderingWithoutImage() {
    PublicBuildingRow buildingWithoutImage =
        StepHelper.arrange(
            "capture visible building without image",
            () ->
                requirePublicSeed(
                    PublicBuildingScenario.getSingleVisiblePublicBuilding("(b.image IS NULL OR TRIM(b.image) = '')"),
                    "At least one visible public building without an image is required."));

    StepHelper.act(
        "search for building without image",
        () -> {
          publicPage.searchByBuildingName(buildingWithoutImage.name());
          publicPage.waitForCardUsesBuildingIconFallback(buildingWithoutImage.name());
        });

    StepHelper.assertStep("fallback icon is displayed", () -> assertThat(publicPage.cardByName(buildingWithoutImage.name()).isDisplayed()).isTrue());
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-008] should multi-page navigation and active page update when pagination")
  public void shouldMultiPageNavigationAndActivePageUpdate() {
    StepHelper.arrange(
        "ensure pagination is available",
        () -> {
          if (!publicPage.isPaginationVisible() || publicPage.paginationCount() <= 1) {
            throw new SkipException("More than one public building result page is required for pagination coverage.");
          }
        });

    String firstPageNames = StepHelper.arrange("capture first page names", () -> String.join("|", publicPage.cardNames()));

    StepHelper.act("navigate to second page", () -> publicPage.clickPaginationPage(2));

    StepHelper.assertStep(
        "active page and result set update",
        () -> {
          assertThat(publicPage.paginationButton(2).isDisplayed()).isTrue();
          assertThat(publicPage.activePaginationText()).isEqualTo("2");
          String secondPageNames = String.join("|", publicPage.cardNames());
          assertThat(secondPageNames).isNotBlank();
          assertThat(secondPageNames).isNotEqualTo(firstPageNames);
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-012] should collapsed state persistence via local storage when filter panel state")
  public void shouldCollapsedStatePersistenceViaLocalStorage() {
    StepHelper.act(
        "toggle filter panel and reload page",
        () -> {
          assertThat(publicPage.isFilterCollapsed()).isFalse();
          publicPage.toggleFilterPanel();
          publicPage.waitForResultsSettled();
          publicPage.open();
          publicPage.waitForResultsLoaded();
        });

    StepHelper.assertStep(
        "collapsed state persists after reload",
        () -> {
          assertThat(publicPage.isFilterCollapsed()).isTrue();
          assertThat(publicPage.storedFilterCollapsedValue()).isEqualTo("true");
        });
  }

  @Test(
      groups = {"regression", "smoke"},
      description = "[E2E-PUB-BLD-013] should zero or reversed range stability when price range filter")
  public void shouldZeroOrReversedRangeStability() {
    StepHelper.act(
        "search with edge range values",
        () -> {
          publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "0", "0");
          publicPage.search();
          assertNoUnexpectedAlert();

          publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "10", "1");
          publicPage.search();
          assertNoUnexpectedAlert();
        });

    StepHelper.assertStep(
        "building list remains stable",
        () -> {
          publicPage.waitForResultsSettled();
          assertThat(publicPage.isFilterFormVisible()).isTrue();
        });
  }

  private void assertNoUnexpectedAlert() {
    try {
      driver.switchTo().alert();
      throw new AssertionError("Unexpected browser alert was shown.");
    } catch (NoAlertPresentException expected) {
      // No browser alert is the expected behavior.
    }
  }

  private PublicBuildingRow requirePublicSeed(PublicBuildingRow value, String requirement) {
    if (value == null) {
      throw new SkipException("[Public seed precondition] " + requirement);
    }
    return value;
  }
}
