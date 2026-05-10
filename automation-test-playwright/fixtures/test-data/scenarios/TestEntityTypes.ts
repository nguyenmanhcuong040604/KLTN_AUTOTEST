/**
 * Shared shapes for test-state records created by scenario helpers.
 * These types describe records that are persisted through the app/API during E2E setup and later cleaned up.
 */
export type PaginatedList<T> = {
  content?: T[];
};

export type EntityRecord = {
  id?: number;
  name?: string;
  fullName?: string;
  username?: string;
  customer?: string;
  building?: string;
  month?: number;
  year?: number;
  role?: string;
};

export type StaffState = { id: number; username: string; fullName: string };
export type CustomerState = { id: number; username: string; fullName: string; staffId: number };
export type BuildingState = { id: number; name: string; transactionType: "FOR_RENT" | "FOR_SALE" };

export type ContractState = {
  id: number;
  staff: StaffState;
  customer: CustomerState;
  building: BuildingState;
};

export type SaleContractState = {
  id: number;
  staff: StaffState;
  customer: CustomerState;
  building: BuildingState;
};

export type PropertyRequestState = {
  id: number;
  buildingId: number;
  customerId: number;
  requestType: "RENT" | "BUY";
};
