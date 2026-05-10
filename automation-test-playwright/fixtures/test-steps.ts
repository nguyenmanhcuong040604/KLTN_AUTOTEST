import { test as base } from "@playwright/test";

type StepBody<T> = () => T | Promise<T>;

export type TestSteps = {
  arrange: <T>(title: string, body: StepBody<T>) => Promise<T>;
  act: <T>(title: string, body: StepBody<T>) => Promise<T>;
  assert: <T>(title: string, body: StepBody<T>) => Promise<T>;
};

function runStep<T>(phase: "Arrange" | "Act" | "Assert", title: string, body: StepBody<T>): Promise<T> {
  return base.step(`${phase}: ${title}`, body);
}

export function createTestSteps(): TestSteps {
  return {
    arrange: (title, body) => runStep("Arrange", title, body),
    act: (title, body) => runStep("Act", title, body),
    assert: (title, body) => runStep("Assert", title, body)
  };
}
