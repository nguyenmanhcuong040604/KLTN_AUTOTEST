import { env } from "@helpers-runtime/env";

type WaitOptions = {
  timeout?: number;
  interval?: number;
  message?: string;
};

const DEFAULT_INTERVAL_MS = 100;

export class WaitHelper {
  /**
   * Repeats a predicate until it returns true or the timeout is reached.
   */
  static async until(predicate: () => Promise<boolean> | boolean, options: WaitOptions = {}): Promise<void> {
    const timeout = options.timeout ?? env.expectTimeout;
    const interval = options.interval ?? DEFAULT_INTERVAL_MS;
    const deadline = Date.now() + timeout;
    let lastError: unknown;

    while (Date.now() <= deadline) {
      try {
        if (await predicate()) {
          return;
        }
      } catch (error) {
        lastError = error;
      }

      await new Promise((resolve) => setTimeout(resolve, interval));
    }

    const cause = lastError instanceof Error ? ` Last error: ${lastError.message}` : "";
    throw new Error(`${options.message ?? "Condition was not met before timeout."}${cause}`);
  }
}
