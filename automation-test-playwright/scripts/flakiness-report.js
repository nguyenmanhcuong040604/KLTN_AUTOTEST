const fs = require("node:fs");
const path = require("node:path");

const runtimeRoot = ".runtime";
const jsonReportFile = path.join(runtimeRoot, "reports", "json", "results.json");
const junitReportFile = path.join(runtimeRoot, "reports", "junit", "results.xml");
const flakinessDir = path.join(runtimeRoot, "reports", "flakiness");
const historyFile = path.join(flakinessDir, "history.json");
const markdownReportFile = path.join(flakinessDir, "flakiness-report.md");

function readJson(filePath, fallback) {
  if (!fs.existsSync(filePath)) {
    return fallback;
  }

  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function decodeXml(value) {
  return value
    .replaceAll("&quot;", '"')
    .replaceAll("&apos;", "'")
    .replaceAll("&lt;", "<")
    .replaceAll("&gt;", ">")
    .replaceAll("&amp;", "&");
}

function attributesOf(tag) {
  const attributes = {};
  for (const match of tag.matchAll(/\s([a-zA-Z_:.-]+)="([^"]*)"/g)) {
    attributes[match[1]] = decodeXml(match[2]);
  }

  return attributes;
}

function collectSpecs(suite, parentTitles = []) {
  const entries = [];
  const currentTitles = suite.title ? [...parentTitles, suite.title] : parentTitles;

  for (const spec of suite.specs ?? []) {
    for (const test of spec.tests ?? []) {
      const titlePath = [...currentTitles, spec.title, test.title].filter(Boolean).join(" > ");
      const statuses = (test.results ?? []).map((result) => result.status);
      const failedAttempts = statuses.filter((status) => status !== "passed" && status !== "skipped").length;
      const finalStatus = statuses.at(-1) ?? test.outcome ?? "unknown";
      const retryCount = Math.max(statuses.length - 1, 0);
      const flaky = test.outcome === "flaky" || (finalStatus === "passed" && failedAttempts > 0);

      entries.push({
        id: `${test.projectName ?? "default"}:${titlePath}`,
        titlePath,
        projectName: test.projectName ?? "default",
        outcome: test.outcome ?? finalStatus,
        finalStatus,
        retryCount,
        failedAttempts,
        flaky
      });
    }
  }

  for (const childSuite of suite.suites ?? []) {
    entries.push(...collectSpecs(childSuite, currentTitles));
  }

  return entries;
}

function collectJUnitTests(xml) {
  const tests = [];
  for (const match of xml.matchAll(/<testcase\b([^>]*)>([\s\S]*?)<\/testcase>|<testcase\b([^>]*)\/>/g)) {
    const attributes = attributesOf(match[1] ?? match[3] ?? "");
    const body = match[2] ?? "";
    const titlePath = [attributes.classname, attributes.name].filter(Boolean).join(" > ");
    const failed = /<(failure|error)\b/.test(body);
    const skipped = /<skipped\b/.test(body);
    const finalStatus = skipped ? "skipped" : failed ? "failed" : "passed";

    tests.push({
      id: `junit:${titlePath}`,
      titlePath,
      projectName: "junit",
      outcome: finalStatus,
      finalStatus,
      retryCount: 0,
      failedAttempts: failed ? 1 : 0,
      flaky: false
    });
  }

  return tests;
}

function summarizeRunFromJson(report) {
  const tests = [];
  for (const suite of report.suites ?? []) {
    tests.push(...collectSpecs(suite));
  }

  return {
    runId: process.env.PW_RUN_ID ?? new Date().toISOString(),
    generatedAt: new Date().toISOString(),
    source: "json",
    total: tests.length,
    passed: tests.filter((test) => test.finalStatus === "passed").length,
    failed: tests.filter((test) => test.finalStatus === "failed" || test.finalStatus === "timedOut").length,
    skipped: tests.filter((test) => test.finalStatus === "skipped").length,
    flaky: tests.filter((test) => test.flaky).length,
    tests
  };
}

function summarizeRunFromJUnit(xml) {
  const tests = collectJUnitTests(xml);

  return {
    runId: process.env.PW_RUN_ID ?? new Date().toISOString(),
    generatedAt: new Date().toISOString(),
    source: "junit",
    total: tests.length,
    passed: tests.filter((test) => test.finalStatus === "passed").length,
    failed: tests.filter((test) => test.finalStatus === "failed" || test.finalStatus === "timedOut").length,
    skipped: tests.filter((test) => test.finalStatus === "skipped").length,
    flaky: tests.filter((test) => test.flaky).length,
    tests
  };
}

function calculateHistory(history) {
  const aggregate = new Map();

  for (const run of history.runs) {
    for (const test of run.tests ?? []) {
      const record = aggregate.get(test.id) ?? {
        id: test.id,
        titlePath: test.titlePath,
        runs: 0,
        failedRuns: 0,
        flakyRuns: 0,
        retries: 0
      };

      record.runs += 1;
      record.failedRuns += test.finalStatus === "failed" || test.finalStatus === "timedOut" ? 1 : 0;
      record.flakyRuns += test.flaky ? 1 : 0;
      record.retries += test.retryCount;
      aggregate.set(test.id, record);
    }
  }

  return [...aggregate.values()]
    .map((record) => ({
      ...record,
      flakinessRate: record.runs === 0 ? 0 : Number(((record.flakyRuns + record.failedRuns) / record.runs).toFixed(4))
    }))
    .sort((left, right) => right.flakinessRate - left.flakinessRate || right.retries - left.retries);
}

function writeMarkdownReport(history, rankedTests) {
  const latest = history.runs.at(-1);
  const lines = [
    "# Flakiness Report",
    "",
    `Generated: ${new Date().toISOString()}`,
    latest
      ? `Latest run: ${latest.runId} | source=${latest.source ?? "unknown"} total=${latest.total} passed=${latest.passed} failed=${latest.failed} flaky=${latest.flaky}`
      : "Latest run: none",
    "",
    "## Highest Risk Tests",
    "",
    "| Test | Runs | Failed | Flaky | Retries | Risk |",
    "| --- | ---: | ---: | ---: | ---: | ---: |"
  ];

  for (const test of rankedTests.slice(0, 20)) {
    lines.push(
      `| ${test.titlePath.replaceAll("|", "\\|")} | ${test.runs} | ${test.failedRuns} | ${test.flakyRuns} | ${test.retries} | ${Math.round(
        test.flakinessRate * 100
      )}% |`
    );
  }

  fs.writeFileSync(markdownReportFile, `${lines.join("\n")}\n`);
}

function main() {
  ensureDir(flakinessDir);

  if (!fs.existsSync(jsonReportFile) && !fs.existsSync(junitReportFile)) {
    console.info(
      `No Playwright JSON or JUnit report found at ${jsonReportFile} / ${junitReportFile}. Run tests before generating flakiness history.`
    );
    return;
  }

  const run = fs.existsSync(jsonReportFile)
    ? summarizeRunFromJson(readJson(jsonReportFile, {}))
    : summarizeRunFromJUnit(fs.readFileSync(junitReportFile, "utf8"));
  const history = readJson(historyFile, { runs: [] });
  history.runs.push(run);
  history.runs = history.runs.slice(-30);

  const rankedTests = calculateHistory(history);
  fs.writeFileSync(historyFile, `${JSON.stringify(history, null, 2)}\n`);
  writeMarkdownReport(history, rankedTests);

  console.info(`Flakiness report written to ${markdownReportFile}`);
}

main();
