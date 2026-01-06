// Transaction types
export type TxnType =
  | "INITIAL_BALANCE"
  | "ACTIVITY_REWARD"
  | "PURCHASE"
  | "ADJUSTMENT";

export type PurchaseStatus = "PENDING" | "FULFILLED" | "CANCELLED";

export type LeaderboardPeriod = "weekly" | "monthly" | "allTime";

// Ninja
export interface Ninja {
  id: number;
  studentId: string;
  firstName: string;
  lastName: string;
  courseName: string;
  levelName: string | null;
  levelSequence?: number | null;
  activityId?: string | null;
  activitySequence?: number | null;
  groupId?: string | null;
  subGroupId?: string | null;
  completedSteps?: number | null;
  totalSteps?: number | null;
  currentBalance: number;
  lastSyncedAt: string | null;
}

export interface NinjaListResponse {
  ninjas: Ninja[];
  totalCount: number;
}

// Ledger
export interface LedgerTxn {
  id: number;
  amount: number;
  type: TxnType;
  description: string;
  createdAt: string;
}

export interface LedgerResponse {
  transactions: LedgerTxn[];
  currentBalance: number;
}

// Shop
export interface ShopItem {
  id: number;
  name: string;
  description: string;
  price: number;
  isAvailable: boolean;
}

export interface ShopListResponse {
  items: ShopItem[];
}

// Purchase
export interface PurchaseRequest {
  shopItemId: number;
}

export interface PurchaseResponse {
  purchaseId: number;
  itemName: string;
  price: number;
  newBalance: number;
  status: PurchaseStatus;
  fulfilledAt: string | null;
}

// Leaderboard
export interface LeaderboardEntry {
  rank: number;
  studentId: string;
  ninjaName: string;
  pointsEarned: number | null;
  pointsSpent: number | null;
  currentBalance: number;
}

export interface LeaderboardResponse {
  period: string;
  leaderboard: LeaderboardEntry[];
}

// API Response wrapper
export interface ApiResponse<T> {
  data?: T;
  error?: string;
}

// App State
export interface AppState {
  facilityId: string;
  studentId: string | null;
  currentNinja: Ninja | null;
}
