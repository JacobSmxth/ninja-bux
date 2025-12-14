export interface Facility {
  id: string;
  name: string;
}

export interface LoginResponse {
  token: string;
  adminId: number;
  username: string;
  facilities: Facility[];
}

export interface Ninja {
  id: number;
  studentId: string;
  firstName: string;
  lastName: string;
  courseName: string;
  levelName: string;
  activityName: string | null;
  activityType: string | null;
  currentBalance: number;
  lastSyncedAt: string | null;
}

export interface NinjaListResponse {
  ninjas: Ninja[];
  totalCount: number;
}

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

export type PurchaseStatus = 'PENDING' | 'FULFILLED' | 'CANCELLED';

export interface Purchase {
  id: number;
  studentId: string;
  ninjaName: string | null;
  itemName: string;
  price: number;
  status: PurchaseStatus;
  purchasedAt: string;
  fulfilledAt: string | null;
}

export interface PurchaseListResponse {
  purchases: Purchase[];
}

export interface LedgerTxn {
  id: number;
  amount: number;
  type: 'INITIAL_BALANCE' | 'ACTIVITY_REWARD' | 'PURCHASE' | 'ADJUSTMENT';
  description: string;
  createdAt: string;
}

export interface LedgerResponse {
  transactions: LedgerTxn[];
  currentBalance: number;
}

export interface AdjustmentResponse {
  adjustmentId: number;
  newBalance: number;
  ledgerTxnId: number;
}

export interface LeaderboardEntry {
  rank: number;
  studentId: string;
  ninjaName: string | null;
  pointsEarned: number | null;
  pointsSpent: number | null;
  currentBalance: number;
}

export interface LeaderboardResponse {
  period: string;
  leaderboard: LeaderboardEntry[];
}
