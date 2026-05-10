const fs = require("node:fs");
const path = require("node:path");
const ts = require("typescript");

const rootDir = path.resolve(__dirname, "..");
const testRoot = path.join(rootDir, "tests", "e2e");
const outputDir = path.join(rootDir, ".runtime", "reports", "test-catalog");
const markdownFile = path.join(outputDir, "test-case-catalog.md");
const csvFile = path.join(outputDir, "test-case-catalog.csv");

function walk(dir, files = []) {
  if (!fs.existsSync(dir)) {
    return files;
  }

  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(fullPath, files);
    } else if (entry.isFile() && fullPath.endsWith(".spec.ts")) {
      files.push(fullPath);
    }
  }

  return files;
}

function relative(filePath) {
  return path.relative(rootDir, filePath).replace(/\\/g, "/");
}

function parseTitle(title) {
  const testId = title.match(/^\[([^\]]+)\]/)?.[1] ?? "";
  const name = title.replace(/^\[[^\]]+\]\s*/, "");
  const tags = title.match(/@[a-z]+/g) ?? [];
  return { testId, name, tags };
}

function collectTests(filePath) {
  const source = fs.readFileSync(filePath, "utf8");
  const sourceFile = ts.createSourceFile(filePath, source, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);
  const entries = [];
  const suiteStack = [];

  function isDescribeCall(node) {
    return (
      ts.isCallExpression(node) &&
      node.arguments.length >= 2 &&
      ts.isPropertyAccessExpression(node.expression) &&
      node.expression.name.text === "describe" &&
      ts.isStringLiteral(node.arguments[0])
    );
  }

  function isTestCall(node) {
    return (
      ts.isCallExpression(node) &&
      node.arguments.length >= 2 &&
      ts.isIdentifier(node.expression) &&
      node.expression.text === "test" &&
      ts.isStringLiteral(node.arguments[0])
    );
  }

  function visit(node) {
    if (isDescribeCall(node)) {
      suiteStack.push(node.arguments[0].text);
      ts.forEachChild(node, visit);
      suiteStack.pop();
      return;
    }

    if (isTestCall(node)) {
      const title = node.arguments[0].text;
      const parsed = parseTitle(title);
      const suiteTitle = suiteStack.at(-1) ?? "";
      const suiteTags = suiteTitle.match(/@[a-z]+/g) ?? [];
      entries.push({
        file: relative(filePath),
        suite: suiteTitle.replace(/\s+@[a-z]+/g, ""),
        testId: parsed.testId,
        name: parsed.name,
        tags: [...new Set([...suiteTags, ...parsed.tags])].join(" ")
      });
    }

    ts.forEachChild(node, visit);
  }

  visit(sourceFile);
  return entries;
}

function csvEscape(value) {
  const text = String(value);
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text;
}

function writeMarkdown(entries) {
  const lines = [
    "# Test Case Catalog",
    "",
    `Generated: ${new Date().toISOString()}`,
    "",
    "| Test ID | Suite | Scenario | Tags | File |",
    "| --- | --- | --- | --- | --- |"
  ];

  for (const entry of entries) {
    lines.push(`| ${entry.testId} | ${entry.suite} | ${entry.name} | ${entry.tags} | ${entry.file} |`);
  }

  fs.writeFileSync(markdownFile, `${lines.join("\n")}\n`);
}

function writeCsv(entries) {
  const header = ["testId", "suite", "scenario", "tags", "file"];
  const rows = entries.map((entry) => [entry.testId, entry.suite, entry.name, entry.tags, entry.file]);
  fs.writeFileSync(csvFile, `${[header, ...rows].map((row) => row.map(csvEscape).join(",")).join("\n")}\n`);
}

function main() {
  const entries = walk(testRoot)
    .flatMap(collectTests)
    .sort((left, right) => left.testId.localeCompare(right.testId));

  fs.mkdirSync(outputDir, { recursive: true });
  writeMarkdown(entries);
  writeCsv(entries);

  console.info(`Exported ${entries.length} test case(s) to ${relative(markdownFile)} and ${relative(csvFile)}.`);
}

main();
