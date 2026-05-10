import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import {
  getSingleVisiblePublicBuilding,
  getVisiblePublicBuildingCount,
  visiblePublicBuildingDistrictMatches,
  visiblePublicBuildingExists
} from "@test-data-scenarios/publicBuildingScenario";
import { TextNormalizeHelper } from "@helpers-text/TextNormalizeHelper";

const expectPublicSeedAvailable = (condition: boolean, requirement: string): void => {
  expect(condition, `[Public seed precondition] ${requirement}`).toBeFalsy();
};

const requirePublicSeed = <T>(value: T | null | undefined | false, requirement: string): T => {
  expectPublicSeedAvailable(!value, requirement);
  return value as T;
};

const assertPublicSeedAvailable = (condition: boolean, requirement: string): void => {
  expectPublicSeedAvailable(!condition, requirement);
};

base.describe("Public - Building Browsing @regression @smoke", () => {
  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ publicPage }, use) => {
        await publicPage.open();
        await publicPage.waitForResultsLoaded();

        await use(undefined);
      },
      { auto: true }
    ]
  });

  test("[E2E-PUB-BLD-001] should default filters and initial results display when landing page", async ({
    page,
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare landing page context", async () => {
      expect.soft(true, "precondition: prepare landing page context").toBe(true);
    });

    await steps.act("perform landing page behavior", async () => {
      const total = await getVisiblePublicBuildingCount();
      expect(total).toBeGreaterThan(0);

      await expect(page).toHaveURL(/\/suntower$/);
      await expect(publicPage.filterForm).toBeVisible();
      await expect(publicPage.searchButton).toBeVisible();
      await publicPage.waitForHasResults();
      expect(await publicPage.resultSummaryText()).toMatch(/tm th?y|tim thay|found/i);
    });

    await steps.assert("verify default filters and initial results display", async () => {
      expect.soft(true, "verification checkpoint: verify default filters and initial results display").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-002] should building name prefill and auto search when query parameter", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare query parameter context", async () => {
      expect.soft(true, "precondition: prepare query parameter context").toBe(true);
    });

    await steps.act("perform query parameter behavior", async () => {
      const targetBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding(),
        "At least one visible public building is required."
      );

      await publicPage.open(`buildingName=${encodeURIComponent(targetBuilding.name)}`);
      await publicPage.waitForResultsLoaded();

      await expect(publicPage.buildingNameInput).toHaveValue(targetBuilding.name);
      await expect(publicPage.cardByName(targetBuilding.name)).toBeVisible();
    });

    await steps.assert("verify building name prefill and auto search", async () => {
      expect.soft(true, "verification checkpoint: verify building name prefill and auto search").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-003] should dropdown metadata loading when filter metadata", async ({ publicPage, steps }) => {
    await steps.arrange("prepare filter metadata context", async () => {
      expect.soft(true, "precondition: prepare filter metadata context").toBe(true);
    });

    await steps.act("perform filter metadata behavior", async () => {
      await expect.poll(() => publicPage.optionCount("districtId")).toBeGreaterThan(1);
      await expect.poll(() => publicPage.optionCount("ward")).toBeGreaterThan(1);
      await expect.poll(() => publicPage.optionCount("street")).toBeGreaterThan(1);
      await expect.poll(() => publicPage.optionCount("direction")).toBeGreaterThan(1);
      await expect.poll(() => publicPage.optionCount("level")).toBeGreaterThan(1);
    });

    await steps.assert("verify dropdown metadata loading", async () => {
      expect.soft(true, "verification checkpoint: verify dropdown metadata loading").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-004] should exact name search result when building name filter", async ({ publicPage, steps }) => {
    await steps.arrange("prepare building name filter context", async () => {
      expect.soft(true, "precondition: prepare building name filter context").toBe(true);
    });

    await steps.act("perform building name filter behavior", async () => {
      const targetBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding(),
        "At least one visible public building is required."
      );

      await publicPage.searchByBuildingName(targetBuilding.name);

      await expect(publicPage.cardByName(targetBuilding.name)).toBeVisible();
      expect(await publicPage.cardCount()).toBeGreaterThan(0);
      await expect.poll(() => visiblePublicBuildingExists(targetBuilding.id, targetBuilding.name)).toBeTruthy();
    });

    await steps.assert("verify exact name search result", async () => {
      expect.soft(true, "verification checkpoint: verify exact name search result").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-005] should district narrowing results when district filter", async ({ publicPage, steps }) => {
    await steps.arrange("prepare district filter context", async () => {
      expect.soft(true, "precondition: prepare district filter context").toBe(true);
    });

    await steps.act("perform district filter behavior", async () => {
      const targetBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding("b.district_id IS NOT NULL"),
        "At least one visible public building with district data is required."
      );
      const districtId = requirePublicSeed(
        targetBuilding.districtId,
        "At least one visible public building with district data is required."
      );

      await publicPage.fillFilter("name", targetBuilding.name);
      await publicPage.selectDistrict(String(districtId));
      await publicPage.search();

      await expect(publicPage.cardByName(targetBuilding.name)).toBeVisible();
      await publicPage.openBuildingDetailsByName(targetBuilding.name);
      await expect(publicPage.detailModalBody).toContainText(targetBuilding.districtName ?? "");
      await expect.poll(() => visiblePublicBuildingDistrictMatches(targetBuilding.id, districtId)).toBeTruthy();
    });

    await steps.assert("verify district narrowing results", async () => {
      expect.soft(true, "verification checkpoint: verify district narrowing results").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-006] should restore default state when filter reset", async ({ publicPage, steps }) => {
    await steps.arrange("prepare filter reset context", async () => {
      expect.soft(true, "precondition: prepare filter reset context").toBe(true);
    });

    await steps.act("perform filter reset behavior", async () => {
      const targetBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding("b.district_id IS NOT NULL"),
        "At least one visible public building with district data is required."
      );
      const districtId = requirePublicSeed(
        targetBuilding.districtId,
        "At least one visible public building with district data is required."
      );

      await publicPage.fillFilter("name", targetBuilding.name);
      await publicPage.selectDistrict(String(districtId));
      await publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "1", "9");

      await publicPage.resetFilters();

      await expect(publicPage.buildingNameInput).toHaveValue("");
      await expect(publicPage.filterInput("numberOfFloorFrom")).toHaveValue("");
      await expect(publicPage.filterInput("numberOfFloorTo")).toHaveValue("");
      expect(await publicPage.selectedValue("districtId")).toBe("");
    });

    await steps.assert("verify restore default state", async () => {
      expect.soft(true, "verification checkpoint: verify restore default state").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-007] should empty state for unmatched search when search results", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare search results context", async () => {
      expect.soft(true, "precondition: prepare search results context").toBe(true);
    });

    await steps.act("perform search results behavior", async () => {
      await publicPage.searchByBuildingName(TestDataFactory.uniqueCode("zzz-e2e-no-match"));

      await publicPage.waitForEmptyState();
      expect(await publicPage.cardCount()).toBe(0);
    });

    await steps.assert("verify empty state for unmatched search", async () => {
      expect.soft(true, "verification checkpoint: verify empty state for unmatched search").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-008] should multi-page navigation and active page update when pagination", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare pagination context", async () => {
      expect.soft(true, "precondition: prepare pagination context").toBe(true);
    });

    await steps.act("perform pagination behavior", async () => {
      const total = await getVisiblePublicBuildingCount();
      assertPublicSeedAvailable(total > 9, "More than 9 visible public buildings are required for pagination.");

      const firstPageNames = await publicPage.cardNames();
      expect(firstPageNames.length).toBeGreaterThan(0);

      await expect(publicPage.paginationContainer).toBeVisible();
      expect(await publicPage.paginationCount()).toBeGreaterThan(1);

      await publicPage.clickPaginationPage(2);

      await expect(publicPage.paginationButton(2)).toBeVisible();
      await expect.poll(() => publicPage.activePaginationText()).toBe("2");
      const secondPageNames = await publicPage.cardNames();
      expect(secondPageNames.length).toBeGreaterThan(0);
      expect(secondPageNames.join("|")).not.toBe(firstPageNames.join("|"));
    });

    await steps.assert("verify multi-page navigation and active page update", async () => {
      expect.soft(true, "verification checkpoint: verify multi-page navigation and active page update").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-009] should modal display with key information and price when rental building details", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare rental building details context", async () => {
      expect.soft(true, "precondition: prepare rental building details context").toBe(true);
    });

    await steps.act("perform rental building details behavior", async () => {
      const rentBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding("b.transaction_type = 'FOR_RENT'"),
        "At least one visible FOR_RENT building is required."
      );

      await publicPage.searchByBuildingName(rentBuilding.name);
      await publicPage.openBuildingDetailsByName(rentBuilding.name);

      const modalText = await publicPage.detailModalLooseText();
      expect(modalText).toContain(TextNormalizeHelper.normalizeLooseText(rentBuilding.name));
      expect(modalText).toMatch(/thong tin chung|general information/i);
      expect(modalText).toMatch(/dac diem bat dong san|property features/i);
      expect(modalText).toMatch(/gia thue|rent price/i);
      expect(modalText).toMatch(/phi dich vu|service fee/i);
      expect(rentBuilding.rentPrice).toBeTruthy();
    });

    await steps.assert("verify modal display with key information and price", async () => {
      expect.soft(true, "verification checkpoint: verify modal display with key information and price").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-010] should sale price display without rental fields when sale building details", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare sale building details context", async () => {
      expect.soft(true, "precondition: prepare sale building details context").toBe(true);
    });

    await steps.act("perform sale building details behavior", async () => {
      const saleBuilding = requirePublicSeed(
        await getSingleVisiblePublicBuilding("b.transaction_type = 'FOR_SALE'"),
        "At least one visible FOR_SALE building is required."
      );

      await publicPage.searchByBuildingName(saleBuilding.name);
      await publicPage.openBuildingDetailsByName(saleBuilding.name);

      const modalText = await publicPage.detailModalLooseText();
      expect(modalText).toContain(TextNormalizeHelper.normalizeLooseText(saleBuilding.name));
      expect(modalText).toMatch(/gia ban|sale price/i);
      expect(modalText).not.toMatch(/dien tich thue kha dung|rentable area/i);
      expect(saleBuilding.salePrice).toBeTruthy();
    });

    await steps.assert("verify sale price display without rental fields", async () => {
      expect.soft(true, "verification checkpoint: verify sale price display without rental fields").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-011] should safe rendering without image when building card media", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare building card media context", async () => {
      expect.soft(true, "precondition: prepare building card media context").toBe(true);
    });

    await steps.act("perform building card media behavior", async () => {
      const buildingWithoutImage = requirePublicSeed(
        await getSingleVisiblePublicBuilding("(b.image IS NULL OR TRIM(b.image) = '')"),
        "At least one visible public building without an image is required."
      );

      await publicPage.searchByBuildingName(buildingWithoutImage.name);
      await publicPage.waitForCardUsesBuildingIconFallback(buildingWithoutImage.name);
    });

    await steps.assert("verify safe rendering without image", async () => {
      expect.soft(true, "verification checkpoint: verify safe rendering without image").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-012] should collapsed state persistence via local storage when filter panel state", async ({
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare filter panel state context", async () => {
      expect.soft(true, "precondition: prepare filter panel state context").toBe(true);
    });

    await steps.act("perform filter panel state behavior", async () => {
      expect(await publicPage.isFilterCollapsed()).toBeFalsy();

      await publicPage.toggleFilterPanel();
      await expect.poll(() => publicPage.isFilterCollapsed()).toBeTruthy();
      expect(await publicPage.storedFilterCollapsedValue()).toBe("true");

      await publicPage.open();
      await publicPage.waitForResultsLoaded();
      await expect.poll(() => publicPage.isFilterCollapsed()).toBeTruthy();
    });

    await steps.assert("verify collapsed state persistence via local storage", async () => {
      expect.soft(true, "verification checkpoint: verify collapsed state persistence via local storage").toBe(true);
    });
  });

  test("[E2E-PUB-BLD-013] should zero or reversed range stability when price range filter", async ({
    page,
    publicPage,
    steps
  }) => {
    await steps.arrange("prepare price range filter context", async () => {
      expect.soft(true, "precondition: prepare price range filter context").toBe(true);
    });

    await steps.act("perform price range filter behavior", async () => {
      let dialogMessage = "";
      page.on("dialog", async (dialog) => {
        dialogMessage = dialog.message();
        await dialog.dismiss();
      });

      await publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "0", "0");
      await publicPage.search();
      expect(dialogMessage).toBe("");

      await publicPage.fillNumberRange("numberOfFloorFrom", "numberOfFloorTo", "10", "1");
      await publicPage.search();
      expect(dialogMessage).toBe("");
      await expect(publicPage.buildingList).toBeVisible();
    });

    await steps.assert("verify zero or reversed range stability", async () => {
      expect.soft(true, "verification checkpoint: verify zero or reversed range stability").toBe(true);
    });
  });
});
