export type EnvironmentTestData = {
  districtId: number;
  buildingId: number;
  contractId: number;
  customerId: number;
  staffId: number;
  ward: string;
  street: string;
  latitude: number;
  longitude: number;
};

export type LocalAutomationDefaults = {
  baseUrl: string;
  adminUsernames: string[];
  staffUsernames: string[];
  customerUsernames: string[];
  defaultPassword: string;
  testSupportOtpToken: string;
  dbJdbcUrl: string;
  dbUsername: string;
  dbPassword: string;
};

const localSeed: EnvironmentTestData = {
  districtId: 1,
  buildingId: 1,
  contractId: 1,
  customerId: 1,
  staffId: 1,
  ward: "Xuan La",
  street: "Vo Chi Cong",
  latitude: 21.0686,
  longitude: 105.8033
};

export const localAutomationDefaults: LocalAutomationDefaults = {
  baseUrl: "http://localhost:8080",
  adminUsernames: ["admin123", "ntn162"],
  staffUsernames: ["tmq0102"],
  customerUsernames: ["abcVietNam"],
  defaultPassword: "12345678",
  testSupportOtpToken: "test-otp-token",
  dbJdbcUrl: "jdbc:mysql://localhost:3306/estate",
  dbUsername: "root",
  dbPassword: "123456"
};

// All environments currently use the same local seed data because the thesis
// project only deploys locally. When deploying to a real staging/test environment,
// each entry should be updated with environment-specific seed IDs.
export const environmentTestData: Record<"local" | "dev" | "test" | "staging", EnvironmentTestData> = {
  local: localSeed,
  dev: localSeed,
  test: localSeed,
  staging: localSeed
};
