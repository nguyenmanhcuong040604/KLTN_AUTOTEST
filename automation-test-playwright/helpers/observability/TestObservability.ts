import type { Page, TestInfo } from "@playwright/test";

const MAX_ENTRIES = 100;
const MAX_BODY_CHARS = 2_000;

type ObservabilityEntry = {
  timestamp: string;
  type: string;
  message: string;
};

function truncate(value: string, maxLength = MAX_BODY_CHARS): string {
  return value.length > maxLength ? `${value.slice(0, maxLength)}...<truncated>` : value;
}

function pushLimited(entries: ObservabilityEntry[], entry: ObservabilityEntry): void {
  entries.push(entry);
  if (entries.length > MAX_ENTRIES) {
    entries.shift();
  }
}

function now(): string {
  return new Date().toISOString();
}

export type TestObservability = {
  record: (type: string, message: string) => void;
  attachOnFailure: () => Promise<void>;
};

export function createTestObservability(page: Page, testInfo: TestInfo): TestObservability {
  const entries: ObservabilityEntry[] = [];

  const record = (type: string, message: string): void => {
    pushLimited(entries, {
      timestamp: now(),
      type,
      message: truncate(message)
    });
  };

  page.on("console", (message) => {
    record(`browser:${message.type()}`, message.text());
  });

  page.on("pageerror", (error) => {
    record("browser:pageerror", error.stack ?? error.message);
  });

  page.on("requestfailed", (request) => {
    record(
      "network:requestfailed",
      `${request.method()} ${request.url()} - ${request.failure()?.errorText ?? "unknown"}`
    );
  });

  page.on("response", (response) => {
    if (response.status() < 400) {
      return;
    }

    record("network:response", `${response.status()} ${response.request().method()} ${response.url()}`);

    const resourceType = response.request().resourceType();
    if (resourceType === "fetch" || resourceType === "xhr") {
      void response
        .text()
        .then((body) => record("network:response-body", `${response.status()} ${response.url()} - ${body}`))
        .catch((error: unknown) => {
          record("network:response-body-error", error instanceof Error ? error.message : String(error));
        });
    }
  });

  return {
    record,
    attachOnFailure: async () => {
      const failed = testInfo.status !== testInfo.expectedStatus;
      if (!failed || entries.length === 0) {
        return;
      }

      await testInfo.attach("observability-log", {
        body: JSON.stringify(entries, null, 2),
        contentType: "application/json"
      });
    }
  };
}
