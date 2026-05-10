import type { FullConfig } from "@playwright/test";
import { runGlobalTestStateCleanup } from "@helpers-test-state/GlobalTeardownCleanup";

export default async function globalTeardown(_config: FullConfig): Promise<void> {
  await runGlobalTestStateCleanup();
}
