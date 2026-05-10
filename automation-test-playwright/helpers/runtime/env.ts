import dotenv from "dotenv";
import { environmentTestData, localAutomationDefaults } from "../../fixtures/test-data/environments";

dotenv.config();

const toNumber = (name: string, value: string | undefined, fallback: number): number => {
  if (value === undefined || value.trim() === "") {
    return fallback;
  }

  const parsed = Number(value);
  if (!Number.isFinite(parsed)) {
    // Fail fast because a silent fallback can make CI execute against the wrong timeout,
    // worker count, database, or target environment without anyone noticing.
    throw new Error(`Invalid numeric environment variable ${name}: "${value}".`);
  }

  return parsed;
};

const toPositiveInteger = (name: string, value: string | undefined, fallback: number): number => {
  const parsed = toNumber(name, value, fallback);
  if (!Number.isInteger(parsed) || parsed < 1) {
    throw new Error(`Invalid positive integer environment variable ${name}: "${value ?? parsed}".`);
  }

  return parsed;
};

const toBoolean = (name: string, value: string | undefined, fallback: boolean): boolean => {
  if (value === undefined || value.trim() === "") {
    return fallback;
  }

  const normalized = value.trim().toLowerCase();
  if (["true", "1", "yes", "y"].includes(normalized)) {
    return true;
  }

  if (["false", "0", "no", "n"].includes(normalized)) {
    return false;
  }

  throw new Error(`Invalid boolean environment variable ${name}: "${value}".`);
};

const parseCandidates = (value: string | undefined, fallback: string[]): string[] => {
  if (!value) {
    return fallback;
  }

  const items = value
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);

  return items.length ? items : fallback;
};

type EnvironmentName = "local" | "dev" | "test" | "staging";
const supportedEnvironments = ["local", "dev", "test", "staging"] as const satisfies readonly EnvironmentName[];
const toEnvironmentName = (value: string | undefined): EnvironmentName => {
  if (!value) {
    return "local";
  }

  if (!supportedEnvironments.includes(value as EnvironmentName)) {
    throw new Error(`Unsupported APP_ENV "${value}". Supported values: ${supportedEnvironments.join(", ")}.`);
  }

  return value as EnvironmentName;
};

const isCi = process.env.CI === "true";
const environmentName = toEnvironmentName(process.env.APP_ENV);
const strictEnvironmentConfig = isCi || environmentName !== "local";

const requireEnv = (name: string, fallback: string): string => {
  const value = process.env[name];
  if (value !== undefined && value.trim() !== "") {
    return value;
  }

  if (strictEnvironmentConfig) {
    throw new Error(`Missing required environment variable ${name} for ${environmentName} test run.`);
  }

  return fallback;
};

const optionalDefault = (name: string, fallback: string): string => {
  return strictEnvironmentConfig ? requireEnv(name, fallback) : (process.env[name] ?? fallback);
};

const baseUrlByEnvironment: Record<EnvironmentName, string> = {
  local: process.env.LOCAL_BASE_URL ?? localAutomationDefaults.baseUrl,
  dev:
    process.env.DEV_BASE_URL ??
    process.env.BASE_URL ??
    (strictEnvironmentConfig ? "" : localAutomationDefaults.baseUrl),
  test:
    process.env.TEST_BASE_URL ??
    process.env.BASE_URL ??
    (strictEnvironmentConfig ? "" : localAutomationDefaults.baseUrl),
  staging:
    process.env.STAGING_BASE_URL ??
    process.env.BASE_URL ??
    (strictEnvironmentConfig ? "" : localAutomationDefaults.baseUrl)
};

const retryPolicy = {
  e2e: toNumber("E2E_RETRIES", process.env.E2E_RETRIES, isCi ? 2 : 0)
};

const adminUsernames = parseCandidates(
  process.env.ADMIN_USERNAMES ?? process.env.ADMIN_USERNAME,
  strictEnvironmentConfig ? [] : localAutomationDefaults.adminUsernames
);
const staffUsernames = parseCandidates(
  process.env.STAFF_USERNAMES ?? process.env.STAFF_USERNAME,
  strictEnvironmentConfig ? [] : localAutomationDefaults.staffUsernames
);
const customerUsernames = parseCandidates(
  process.env.CUSTOMER_USERNAMES ?? process.env.CUSTOMER_USERNAME,
  strictEnvironmentConfig ? [] : localAutomationDefaults.customerUsernames
);
const selectedEnvironmentTestData = environmentTestData[environmentName];

const isLocalEnvironment = environmentName === "local";

const assertValidUrl = (name: string, value: string): void => {
  try {
    new URL(value);
  } catch {
    throw new Error(`Invalid ${name}: "${value}".`);
  }
};

const assertNonLocalSecret = (name: string, value: string, forbiddenValues: string[]): void => {
  if (isLocalEnvironment) {
    return;
  }

  if (forbiddenValues.includes(value)) {
    throw new Error(
      `${name} uses a local-only default value in ${environmentName}. Configure a real value via environment variables.`
    );
  }
};

const assertNonLocalCandidates = (name: string, values: string[], forbiddenValues: string[]): void => {
  if (isLocalEnvironment) {
    return;
  }

  const forbidden = values.filter((value) => forbiddenValues.includes(value));
  if (forbidden.length > 0) {
    throw new Error(`${name} contains local-only default account(s) in ${environmentName}: ${forbidden.join(", ")}.`);
  }
};

const assertNonLocalBaseUrl = (value: string): void => {
  if (isLocalEnvironment) {
    return;
  }

  const parsedUrl = new URL(value);
  if (["localhost", "127.0.0.1", "0.0.0.0"].includes(parsedUrl.hostname)) {
    throw new Error(
      `BASE_URL points to a local host in ${environmentName}. Configure the target environment URL explicitly.`
    );
  }
};

export const env = {
  isCi,
  appEnv: environmentName,
  baseUrl:
    (process.env.BASE_URL ?? baseUrlByEnvironment[environmentName]) ||
    optionalDefault("BASE_URL", localAutomationDefaults.baseUrl),
  adminUsernames,
  staffUsernames,
  customerUsernames,
  adminUsername:
    process.env.ADMIN_USERNAME ??
    adminUsernames[0] ??
    optionalDefault("ADMIN_USERNAME", localAutomationDefaults.adminUsernames[0]),
  staffUsername:
    process.env.STAFF_USERNAME ??
    staffUsernames[0] ??
    optionalDefault("STAFF_USERNAME", localAutomationDefaults.staffUsernames[0]),
  customerUsername:
    process.env.CUSTOMER_USERNAME ??
    customerUsernames[0] ??
    optionalDefault("CUSTOMER_USERNAME", localAutomationDefaults.customerUsernames[0]),
  defaultPassword: optionalDefault("DEFAULT_PASSWORD", localAutomationDefaults.defaultPassword),
  expectTimeout: toPositiveInteger("EXPECT_TIMEOUT", process.env.EXPECT_TIMEOUT, 10_000),
  actionTimeout: toPositiveInteger("ACTION_TIMEOUT", process.env.ACTION_TIMEOUT, 15_000),
  navigationTimeout: toPositiveInteger("NAVIGATION_TIMEOUT", process.env.NAVIGATION_TIMEOUT, 30_000),
  configWorkers: isCi
    ? toPositiveInteger("CI_WORKERS", process.env.CI_WORKERS, 2)
    : toPositiveInteger("WORKERS", process.env.WORKERS, 1),
  fullyParallel: toBoolean("FULLY_PARALLEL", process.env.FULLY_PARALLEL, false),
  forbidOnly: isCi,
  retryPolicy,
  testSupportOtpToken: optionalDefault("TEST_SUPPORT_OTP_TOKEN", localAutomationDefaults.testSupportOtpToken),
  testDataSeed: {
    districtId: toNumber("TEST_DISTRICT_ID", process.env.TEST_DISTRICT_ID, selectedEnvironmentTestData.districtId),
    buildingId: toNumber("TEST_BUILDING_ID", process.env.TEST_BUILDING_ID, selectedEnvironmentTestData.buildingId),
    contractId: toNumber("TEST_CONTRACT_ID", process.env.TEST_CONTRACT_ID, selectedEnvironmentTestData.contractId),
    customerId: toNumber("TEST_CUSTOMER_ID", process.env.TEST_CUSTOMER_ID, selectedEnvironmentTestData.customerId),
    staffId: toNumber("TEST_STAFF_ID", process.env.TEST_STAFF_ID, selectedEnvironmentTestData.staffId),
    ward: process.env.TEST_BUILDING_WARD ?? selectedEnvironmentTestData.ward,
    street: process.env.TEST_BUILDING_STREET ?? selectedEnvironmentTestData.street,
    latitude: toNumber(
      "TEST_BUILDING_LATITUDE",
      process.env.TEST_BUILDING_LATITUDE,
      selectedEnvironmentTestData.latitude
    ),
    longitude: toNumber(
      "TEST_BUILDING_LONGITUDE",
      process.env.TEST_BUILDING_LONGITUDE,
      selectedEnvironmentTestData.longitude
    )
  },
  dbJdbcUrl:
    process.env.DB_JDBC_URL ??
    process.env.SPRING_DATASOURCE_URL ??
    optionalDefault("DB_JDBC_URL", localAutomationDefaults.dbJdbcUrl),
  dbUsername:
    process.env.DB_USERNAME ??
    process.env.SPRING_DATASOURCE_USERNAME ??
    optionalDefault("DB_USERNAME", localAutomationDefaults.dbUsername),
  dbPassword:
    process.env.DB_PASSWORD ??
    process.env.SPRING_DATASOURCE_PASSWORD ??
    optionalDefault("DB_PASSWORD", localAutomationDefaults.dbPassword),
  dbPoolLimit: toPositiveInteger("DB_POOL_LIMIT", process.env.DB_POOL_LIMIT, 5)
};

assertValidUrl("BASE_URL", env.baseUrl);
assertNonLocalBaseUrl(env.baseUrl);
assertNonLocalCandidates("ADMIN_USERNAMES", env.adminUsernames, localAutomationDefaults.adminUsernames);
assertNonLocalCandidates("STAFF_USERNAMES", env.staffUsernames, localAutomationDefaults.staffUsernames);
assertNonLocalCandidates("CUSTOMER_USERNAMES", env.customerUsernames, localAutomationDefaults.customerUsernames);
assertNonLocalSecret("ADMIN_USERNAME", env.adminUsername, localAutomationDefaults.adminUsernames);
assertNonLocalSecret("STAFF_USERNAME", env.staffUsername, localAutomationDefaults.staffUsernames);
assertNonLocalSecret("CUSTOMER_USERNAME", env.customerUsername, localAutomationDefaults.customerUsernames);
assertNonLocalSecret("DEFAULT_PASSWORD", env.defaultPassword, [localAutomationDefaults.defaultPassword]);
assertNonLocalSecret("TEST_SUPPORT_OTP_TOKEN", env.testSupportOtpToken, [localAutomationDefaults.testSupportOtpToken]);
assertNonLocalSecret("DB_USERNAME", env.dbUsername, [localAutomationDefaults.dbUsername]);
assertNonLocalSecret("DB_PASSWORD", env.dbPassword, [localAutomationDefaults.dbPassword]);
