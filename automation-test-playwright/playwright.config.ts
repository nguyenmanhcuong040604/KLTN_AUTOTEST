import path from "node:path";
import { defineConfig, devices } from "@playwright/test";
import { env } from "./helpers/runtime/env";
import { playwrightOutputDir, runtimePaths } from "./helpers/runtime/paths";

export default defineConfig({
  testDir: "./tests",
  globalSetup: "./helpers/runtime/global-setup.ts",
  globalTeardown: "./helpers/runtime/global-teardown.ts",
  fullyParallel: env.fullyParallel,
  forbidOnly: env.forbidOnly,
  retries: env.retryPolicy.e2e,
  workers: env.configWorkers,
  timeout: 60_000,
  expect: {
    timeout: env.expectTimeout
  },
  reporter: [
    ["html", { open: "never", outputFolder: runtimePaths.htmlReportDir }],
    ["list"],
    ["junit", { outputFile: runtimePaths.junitReportFile }],
    ["json", { outputFile: runtimePaths.jsonReportFile }]
  ],
  outputDir: playwrightOutputDir,
  use: {
    baseURL: env.baseUrl,
    headless: true,
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    actionTimeout: env.actionTimeout,
    navigationTimeout: env.navigationTimeout
  },
  projects: [
    {
      name: "e2e",
      testMatch: /tests\/e2e\/.*\.spec\.ts/,
      retries: env.retryPolicy.e2e,
      use: {
        ...devices["Desktop Chrome"]
      }
    }
  ],
  metadata: {
    project: "SunTower Playwright Automation",
    baseUrl: env.baseUrl,
    framework: "Playwright + TypeScript",
    environment: env.appEnv,
    runId: runtimePaths.runId,
    outputDir: path.normalize(playwrightOutputDir)
  }
});
