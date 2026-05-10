import { test as base, type Page, type TestInfo } from "@playwright/test";
import { AuthSessionHelper, type UserRole } from "@helpers-auth/AuthSessionHelper";
import { createTestObservability, type TestObservability } from "@helpers-observability/TestObservability";
import { NavigationPage } from "@pages/core/NavigationPage";
import { PublicLandingPage } from "@pages/public/PublicLandingPage";
import { createTestSteps, type TestSteps } from "./test-steps";
import { TestStateSession } from "@helpers-test-state/TestState";
import { attachTestStateHelpers, type TestStateFixture } from "@helpers-test-state/TestState";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

type PageObjectConstructor<TPageObject> = new (page: Page) => TPageObject;

type PageObjectFactory = {
  create: <TPageObject>(PageObject: PageObjectConstructor<TPageObject>) => TPageObject;
};

type RoleSessionFixture = {
  login: () => Promise<void>;
  open: (path?: string) => Promise<void>;
};

type AppFixtures = {
  testState: TestStateFixture;
  observability: TestObservability;
  steps: TestSteps;
  pageObjects: PageObjectFactory;
  navigationPage: NavigationPage;
  adminSession: RoleSessionFixture;
  staffSession: RoleSessionFixture;
  customerSession: RoleSessionFixture;
  publicPage: PublicLandingPage;
};

type WorkerFixtures = {
  testMetadata: void;
};

function instrumentTestState(testState: TestStateFixture, observability: TestObservability): TestStateFixture {
  return new Proxy(testState, {
    get(target, property, receiver) {
      const value = Reflect.get(target, property, receiver);
      if (typeof value !== "function" || typeof property !== "string") {
        return value;
      }

      return async (...args: unknown[]) => {
        observability.record("test-state:start", `${property}(${args.map((arg) => JSON.stringify(arg)).join(", ")})`);
        try {
          const result = await value(...args);
          observability.record("test-state:success", property);
          return result;
        } catch (error) {
          observability.record(
            "test-state:error",
            `${property}: ${error instanceof Error ? error.message : String(error)}`
          );
          throw error;
        }
      };
    }
  }) as TestStateFixture;
}

type TestMetadata = {
  testId?: string;
  layer: "E2E";
  actor?: string;
  feature?: string;
};

function parseTestMetadata(title: string): TestMetadata {
  const testId = title.match(/^\[([^\]]+)\]/)?.[1];
  const normalizedTitle = title.replace(/^\[[^\]]+\]\s*-\s*/, "");
  const segments = normalizedTitle
    .split(" - ")
    .map((item) => item.trim())
    .filter(Boolean);

  return {
    testId,
    layer: "E2E",
    actor: segments[0]?.replace(/^E2E\s+/i, ""),
    feature: segments[1]
  };
}

function annotateTestMetadata(testInfo: TestInfo): void {
  const metadata = parseTestMetadata(testInfo.title);
  const annotations = [
    ["testId", metadata.testId],
    ["layer", metadata.layer],
    ["actor", metadata.actor],
    ["feature", metadata.feature]
  ] as const;

  for (const [type, description] of annotations) {
    if (description) {
      testInfo.annotations.push({ type, description });
    }
  }
}

function defaultHomePath(role: UserRole): string {
  return AuthSessionHelper.roleHomePaths[role];
}

function createRoleSession(page: Page, navigationPage: NavigationPage, role: UserRole): RoleSessionFixture {
  return {
    login: () => AuthSessionHelper.loginAsRoleUiStrict(page, role),
    open: async (path = defaultHomePath(role)) => {
      await AuthSessionHelper.loginAsRoleUiStrict(page, role);
      await navigationPage.open(path);
    }
  };
}

export const test = base.extend<AppFixtures & WorkerFixtures>({
  testMetadata: [
    async ({ page: _page }, use, testInfo) => {
      annotateTestMetadata(testInfo);
      await use(undefined);
    },
    { auto: true }
  ],

  testState: async ({ playwright, observability }, use) => {
    const context = await TestStateSession.newContext(playwright, "admin");
    try {
      // Cleanup/setup operations are proxied so failed tests include the
      // scenario lifecycle in the Playwright attachment.
      await use(instrumentTestState(attachTestStateHelpers(context), observability));
    } finally {
      await context.dispose();
    }
  },

  observability: [
    async ({ page }, use, testInfo) => {
      const observability = createTestObservability(page, testInfo);
      await use(observability);
      await observability.attachOnFailure();
    },
    { auto: true }
  ],

  steps: async ({ page: _page }, use) => {
    await use(createTestSteps());
  },

  pageObjects: async ({ page }, use) => {
    // Centralizing page-object construction keeps dependency injection consistent
    // while still allowing each spec to request only the page class it needs.
    await use({
      create: <TPageObject>(PageObject: PageObjectConstructor<TPageObject>): TPageObject => new PageObject(page)
    });
  },

  navigationPage: async ({ pageObjects }, use) => {
    await use(pageObjects.create(NavigationPage));
  },

  adminSession: async ({ page, navigationPage }, use) => {
    await use(createRoleSession(page, navigationPage, "admin"));
  },

  staffSession: async ({ page, navigationPage }, use) => {
    await use(createRoleSession(page, navigationPage, "staff"));
  },

  customerSession: async ({ page, navigationPage }, use) => {
    await use(createRoleSession(page, navigationPage, "customer"));
  },

  publicPage: async ({ page }, use) => {
    await use(new PublicLandingPage(page));
  }
});

test.afterAll(async () => {
  await MySqlDbClient.close();
});

export { expect } from "@playwright/test";
