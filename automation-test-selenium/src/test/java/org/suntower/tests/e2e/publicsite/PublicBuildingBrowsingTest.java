package org.suntower.tests.e2e.publicsite;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.pages.publicsite.PublicLandingPage;
import org.testng.annotations.BeforeMethod;
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
}
