const fs = require("node:fs");
const path = require("node:path");
const ts = require("typescript");

const rootDir = path.resolve(__dirname, "..");
const scanDirs = ["fixtures", "helpers", "pages", "tests"].map((dir) => path.join(rootDir, dir));
const failures = [];
const allowedTags = new Set(["@regression", "@smoke", "@critical"]);
let smokeCount = 0;
let criticalCount = 0;

function walk(dir, files = []) {
  if (!fs.existsSync(dir)) {
    return files;
  }

  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(fullPath, files);
    } else if (entry.isFile() && fullPath.endsWith(".ts")) {
      files.push(fullPath);
    }
  }

  return files;
}

function relative(filePath) {
  return path.relative(rootDir, filePath).replace(/\\/g, "/");
}

function addFailure(filePath, lineNumber, message, line) {
  failures.push(`${relative(filePath)}:${lineNumber} - ${message}\n  ${line.trim()}`);
}

function lineNumberAt(source, position) {
  return source.slice(0, position).split(/\r?\n/).length;
}

function strippedStepBody(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "")
    .trim();
}

function isE2ETestCall(node) {
  if (!ts.isCallExpression(node) || node.arguments.length < 2) {
    return false;
  }

  return ts.isIdentifier(node.expression) && node.expression.text === "test" && ts.isStringLiteral(node.arguments[0]);
}

function assertAaaSteps(filePath, source) {
  const sourceFile = ts.createSourceFile(filePath, source, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);

  function visit(node) {
    if (isE2ETestCall(node)) {
      const title = node.arguments[0].text;
      if (!/^\[E2E-[A-Z0-9-]+\]/.test(title)) {
        addFailure(
          filePath,
          lineNumberAt(source, node.getStart(sourceFile)),
          "E2E test title must start with a Test ID like [E2E-MODULE-FEATURE-001].",
          title
        );
      }

      const callback = node.arguments.find((argument) => ts.isArrowFunction(argument));
      if (callback && ts.isArrowFunction(callback) && ts.isBlock(callback.body)) {
        const body = source.slice(callback.body.pos, callback.body.end);
        const missing = ["arrange", "act", "assert"].filter(
          (phase) => !new RegExp(`steps\\.${phase}\\s*\\(`).test(body)
        );
        if (missing.length > 0) {
          addFailure(
            filePath,
            lineNumberAt(source, node.getStart(sourceFile)),
            `E2E test must use AAA steps: missing ${missing.map((phase) => `steps.${phase}`).join(", ")}.`,
            source.slice(node.getStart(sourceFile), Math.min(node.getStart(sourceFile) + 140, source.length))
          );
        }

        function assertNonEmptyStep(stepCall) {
          if (!ts.isCallExpression(stepCall)) {
            return;
          }

          const stepBody = stepCall.arguments.find((argument) => ts.isArrowFunction(argument));
          if (!stepBody || !ts.isArrowFunction(stepBody) || !ts.isBlock(stepBody.body)) {
            return;
          }

          const content = strippedStepBody(source.slice(stepBody.body.pos + 1, stepBody.body.end - 1));
          if (!content) {
            addFailure(
              filePath,
              lineNumberAt(source, stepCall.getStart(sourceFile)),
              "AAA step body must contain a real precondition, action, assertion, or explicit checkpoint.",
              source.slice(stepCall.getStart(sourceFile), Math.min(stepCall.getStart(sourceFile) + 160, source.length))
            );
          }
        }

        function visitStepCalls(stepNode) {
          if (
            ts.isCallExpression(stepNode) &&
            ts.isPropertyAccessExpression(stepNode.expression) &&
            ts.isIdentifier(stepNode.expression.expression) &&
            stepNode.expression.expression.text === "steps" &&
            ["arrange", "act", "assert"].includes(stepNode.expression.name.text)
          ) {
            assertNonEmptyStep(stepNode);
          }

          ts.forEachChild(stepNode, visitStepCalls);
        }

        visitStepCalls(callback.body);
      }
    }

    ts.forEachChild(node, visit);
  }

  visit(sourceFile);
}

for (const filePath of scanDirs.flatMap((dir) => walk(dir))) {
  const rel = relative(filePath);
  const source = fs.readFileSync(filePath, "utf8");
  const lines = source.split(/\r?\n/);

  if (rel.startsWith("tests/e2e/") && rel.endsWith(".spec.ts")) {
    assertAaaSteps(filePath, source);

    const describeTitles = [
      ...source.matchAll(/(?:base\.)?test\.describe\("([^"]+)"/g),
      ...source.matchAll(/base\.describe\("([^"]+)"/g)
    ];
    if (describeTitles.length === 0) {
      failures.push(`${rel}:1 - E2E spec must declare a describe title with tags.`);
    }

    for (const match of describeTitles) {
      const title = match[1];
      const tags = title.match(/@[a-z]+/g) ?? [];
      if (!tags.includes("@regression")) {
        failures.push(`${rel}:1 - E2E describe title must include @regression.\n  ${title}`);
      }

      for (const tag of tags) {
        if (!allowedTags.has(tag)) {
          failures.push(`${rel}:1 - Unsupported tag ${tag}. Allowed tags: ${[...allowedTags].join(", ")}.\n  ${title}`);
        }
      }

      if (tags.includes("@smoke")) {
        smokeCount += 1;
      }
      if (tags.includes("@critical")) {
        criticalCount += 1;
      }
    }
  }

  lines.forEach((line, index) => {
    const lineNumber = index + 1;

    if (/\b(test|describe)\.only\s*\(/.test(line)) {
      addFailure(filePath, lineNumber, "Focused test is not allowed.", line);
    }

    if (/waitForTimeout\s*\(/.test(line)) {
      addFailure(filePath, lineNumber, "Fixed sleeps are not allowed. Use locator/web assertions instead.", line);
    }

    if (
      rel.startsWith("tests/e2e/") &&
      /Scenario setup is handled by fixtures|Assertions are executed through expect calls/.test(line)
    ) {
      addFailure(filePath, lineNumber, "Placeholder AAA comments are not allowed; use a meaningful step body.", line);
    }

    if (rel.startsWith("tests/e2e/") && /from\s+["']@helpers-test-state\//.test(line)) {
      addFailure(
        filePath,
        lineNumber,
        "E2E specs must use testState or scenario facades instead of importing helpers/test-state directly.",
        line
      );
    }

    if (/console\.(log|warn|error|info|debug)\s*\(/.test(line) && rel !== "helpers/runtime/Logger.ts") {
      addFailure(filePath, lineNumber, "Use Logger instead of console.*.", line);
    }

    if (rel.startsWith("tests/") && /new [A-Z][A-Za-z0-9]+Page\(page\)/.test(line)) {
      addFailure(
        filePath,
        lineNumber,
        "Use pageObjects.create(PageClass) instead of direct Page Object construction.",
        line
      );
    }

    if (rel.startsWith("tests/") && /NavigationPage/.test(line)) {
      addFailure(
        filePath,
        lineNumber,
        "Use the navigationPage fixture instead of importing or constructing NavigationPage in specs.",
        line
      );
    }

    if (rel.startsWith("tests/e2e/") && /\bpage\.(locator|getBy[A-Z][A-Za-z]*)\s*\(/.test(line)) {
      addFailure(filePath, lineNumber, "Use Page Object methods instead of direct selectors in specs.", line);
    }

    if (
      rel.startsWith("tests/e2e/") &&
      /steps\.(arrange|act|assert)\("(?:prepare test context|execute test behavior|verify expected outcomes)"/.test(
        line
      )
    ) {
      addFailure(filePath, lineNumber, "Use semantic AAA step titles derived from the tested behavior.", line);
    }

    if (
      rel.startsWith("tests/") &&
      /"Password@456"|"Mismatch@456"|"123456"|"Password@123"|"WrongPassword!123"|"unknown-user"|"wrong-password"|"Xuan La"|"Vo Chi Cong"|"DONG"|"https:\/\/example\.com\/building"|21\.0686|105\.8033/.test(
        line
      )
    ) {
      addFailure(filePath, lineNumber, "Move shared test data literals into TestDataFactory constants.", line);
    }
  });
}

if (smokeCount === 0) {
  failures.push("tests/e2e:1 - At least one suite must be tagged @smoke.");
}

if (criticalCount === 0) {
  failures.push("tests/e2e:1 - At least one suite must be tagged @critical.");
}

if (failures.length > 0) {
  console.error(`Quality gate failed with ${failures.length} issue(s):`);
  console.error(failures.join("\n"));
  process.exit(1);
}

console.info("Quality gate passed.");
