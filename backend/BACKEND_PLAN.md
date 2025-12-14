# NinjaBux Backend Plan - Bare Minimum

## Overview
Spring Boot REST API for managing ninja points system across multiple facilities. Integrates with Code Ninjas API for student data.

---

## Data Models

### 1. Facility
```java
- id: UUID (matches facilityId from API headers)
- name: String
- createdAt: LocalDateTime
```

### 2. Admin
```java
- id: Long
- username: String (unique)
- passwordHash: String
- email: String
- facilities: Set<Facility> (many-to-many)
- createdAt: LocalDateTime
```

### 3. Ninja
```java
- id: Long
- studentId: String (from API, unique per facility)
- facilityId: UUID (FK to Facility)
- firstName: String (nullable)
- lastName: String (nullable)
- courseName: String ("Yellow Belt")
- levelName: String ("Level 4")
- activityName: String (last completed)
- activityType: String ("code-adventure", "solve", etc.)
- currentBalance: Integer (cached)
- lastSyncedAt: LocalDateTime
- createdAt: LocalDateTime
```

### 4. LedgerTxn
```java
- id: Long
- facilityId: UUID
- studentId: String
- amount: Integer (positive = earn, negative = spend)
- type: Enum (INITIAL_BALANCE, ACTIVITY_REWARD, PURCHASE, ADJUSTMENT)
- description: String
- relatedEntityId: Long (nullable, references Purchase or Adjustment)
- createdAt: LocalDateTime
```

### 5. Purchase
```java
- id: Long
- facilityId: UUID
- studentId: String
- shopItemId: Long
- itemName: String (snapshot)
- price: Integer (snapshot)
- status: Enum (PENDING, FULFILLED, CANCELLED)
- purchasedAt: LocalDateTime
- fulfilledAt: LocalDateTime (nullable)
```

### 6. Adjustment
```java
- id: Long
- facilityId: UUID
- studentId: String
- adminId: Long
- amount: Integer (can be positive or negative)
- reason: String
- createdAt: LocalDateTime
```

### 7. ShopItem
```java
- id: Long
- facilityId: UUID
- name: String
- description: String
- price: Integer
- isAvailable: Boolean
- createdAt: LocalDateTime
```

---

## API Endpoints

### Authentication

#### `POST /api/auth/login`
**Request:**
```json
{
  "username": "admin",
  "password": "password"
}
```
**Response:**
```json
{
  "token": "jwt-token",
  "adminId": 1,
  "username": "admin",
  "facilities": [
    {"id": "fcd4728c-afff-4a3c-8a39-05d2cd9d87ac", "name": "Alpharetta"}
  ]
}
```

---

### Ninjas (Read-only for most, synced from API)

#### `GET /api/facilities/{facilityId}/ninjas`
Get all ninjas for a facility (paginated).
**Response:**
```json
{
  "ninjas": [
    {
      "id": 1,
      "studentId": "53389e2e-c4f5-4e06-96bb-27e3a9427754",
      "firstName": "John",
      "lastName": "Doe",
      "courseName": "Yellow Belt",
      "levelName": "Level 4",
      "currentBalance": 112,
      "lastSyncedAt": "2025-12-13T10:30:00"
    }
  ],
  "totalCount": 45
}
```

#### `GET /api/facilities/{facilityId}/ninjas/{studentId}`
Get single ninja details.
**Response:**
```json
{
  "id": 1,
  "studentId": "53389e2e-c4f5-4e06-96bb-27e3a9427754",
  "firstName": "John",
  "lastName": "Doe",
  "courseName": "Yellow Belt",
  "levelName": "Level 4",
  "activityName": "Creating with Repeat and For Element loops!",
  "activityType": "code-adventure",
  "currentBalance": 112,
  "lastSyncedAt": "2025-12-13T10:30:00"
}
```

#### `POST /api/facilities/{facilityId}/ninjas/sync`
Manually trigger sync from Code Ninjas API for all ninjas in facility.
**Response:**
```json
{
  "syncedCount": 45,
  "newNinjas": 3,
  "updatedNinjas": 42,
  "errors": []
}
```

#### `POST /api/facilities/{facilityId}/ninjas/{studentId}/sync`
Sync single ninja from API.
**Response:**
```json
{
  "studentId": "53389e2e-c4f5-4e06-96bb-27e3a9427754",
  "updated": true,
  "changes": {
    "activityName": "New activity completed",
    "pointsAwarded": 2
  }
}
```

---

### Ledger

#### `GET /api/facilities/{facilityId}/ninjas/{studentId}/ledger`
Get transaction history for a ninja.
**Query params:** `?limit=50&offset=0`
**Response:**
```json
{
  "transactions": [
    {
      "id": 123,
      "amount": 2,
      "type": "ACTIVITY_REWARD",
      "description": "Completed: Creating with Repeat",
      "createdAt": "2025-12-13T10:30:00"
    },
    {
      "id": 122,
      "amount": -50,
      "type": "PURCHASE",
      "description": "Purchased: Pizza Party Ticket",
      "createdAt": "2025-12-12T14:20:00"
    }
  ],
  "currentBalance": 112
}
```

---

### Adjustments (Admin only)

#### `POST /api/facilities/{facilityId}/ninjas/{studentId}/adjustments`
Admin adds or deducts points.
**Request:**
```json
{
  "amount": 50,
  "reason": "Bonus for helping other students"
}
```
**Response:**
```json
{
  "adjustmentId": 45,
  "newBalance": 162,
  "ledgerTxnId": 124
}
```

#### `GET /api/facilities/{facilityId}/adjustments`
Get all adjustments for a facility (audit log).
**Query params:** `?limit=50&offset=0`
**Response:**
```json
{
  "adjustments": [
    {
      "id": 45,
      "studentId": "53389e2e...",
      "ninjaName": "John Doe",
      "adminUsername": "admin",
      "amount": 50,
      "reason": "Bonus for helping",
      "createdAt": "2025-12-13T10:30:00"
    }
  ]
}
```

---

### Shop

#### `GET /api/facilities/{facilityId}/shop`
Get all shop items for a facility.
**Response:**
```json
{
  "items": [
    {
      "id": 1,
      "name": "Pizza Party Ticket",
      "description": "Entry to monthly pizza party",
      "price": 50,
      "isAvailable": true
    },
    {
      "id": 2,
      "name": "Extra Lab Time",
      "description": "30 minutes extra coding time",
      "price": 25,
      "isAvailable": true
    }
  ]
}
```

#### `POST /api/facilities/{facilityId}/shop`
Admin creates a shop item.
**Request:**
```json
{
  "name": "Candy Bar",
  "description": "Choice of candy",
  "price": 10,
  "isAvailable": true
}
```
**Response:**
```json
{
  "id": 3,
  "name": "Candy Bar",
  "price": 10
}
```

#### `PUT /api/facilities/{facilityId}/shop/{itemId}`
Admin updates shop item.
**Request:**
```json
{
  "name": "Candy Bar",
  "description": "Choice of candy",
  "price": 15,
  "isAvailable": false
}
```

#### `DELETE /api/facilities/{facilityId}/shop/{itemId}`
Admin deletes shop item.

---

### Purchases

#### `POST /api/facilities/{facilityId}/ninjas/{studentId}/purchases`
Ninja makes a purchase.
**Request:**
```json
{
  "shopItemId": 1
}
```
**Response:**
```json
{
  "purchaseId": 78,
  "itemName": "Pizza Party Ticket",
  "price": 50,
  "newBalance": 62,
  "status": "PENDING"
}
```

#### `GET /api/facilities/{facilityId}/purchases`
Get all purchases for a facility (admin view).
**Query params:** `?status=PENDING&limit=50&offset=0`
**Response:**
```json
{
  "purchases": [
    {
      "id": 78,
      "studentId": "53389e2e...",
      "ninjaName": "John Doe",
      "itemName": "Pizza Party Ticket",
      "price": 50,
      "status": "PENDING",
      "purchasedAt": "2025-12-13T10:30:00"
    }
  ]
}
```

#### `PUT /api/facilities/{facilityId}/purchases/{purchaseId}/fulfill`
Admin marks purchase as fulfilled.
**Response:**
```json
{
  "purchaseId": 78,
  "status": "FULFILLED",
  "fulfilledAt": "2025-12-13T11:00:00"
}
```

#### `PUT /api/facilities/{facilityId}/purchases/{purchaseId}/cancel`
Admin cancels purchase (refunds points).
**Response:**
```json
{
  "purchaseId": 78,
  "status": "CANCELLED",
  "refunded": true,
  "newBalance": 112
}
```

---

### Leaderboards

#### `GET /api/facilities/{facilityId}/leaderboard/earned`
Most points earned in a time period.
**Query params:** `?period=weekly|monthly|yearly&limit=10`
**Response:**
```json
{
  "period": "weekly",
  "leaderboard": [
    {
      "rank": 1,
      "studentId": "53389e2e...",
      "ninjaName": "John Doe",
      "pointsEarned": 150,
      "currentBalance": 112
    }
  ]
}
```

#### `GET /api/facilities/{facilityId}/leaderboard/spent`
Most points spent in a time period.
**Query params:** `?period=weekly|monthly|yearly&limit=10`
**Response:**
```json
{
  "period": "monthly",
  "leaderboard": [
    {
      "rank": 1,
      "studentId": "53389e2e...",
      "ninjaName": "Jane Smith",
      "pointsSpent": 200,
      "currentBalance": 50
    }
  ]
}
```

---

## Background Services

### API Sync Service
- **Scheduled job** (runs hourly or daily)
- Fetches all students for each facility from Code Ninjas API
- Creates new Ninja records for students not in DB
- Updates existing Ninjas:
  - Compares `activityName` - if changed, award points based on `activityType` and `courseName`
  - Updates `courseName`, `levelName`, `activityType`
  - Sets `lastSyncedAt`

### Point Calculation Logic

#### Initial Balance (when ninja first syncs):
```
Belt bases and per-level points:
- White Belt: base=0, per_level=10
- Yellow Belt: base=80, per_level=8
- Orange Belt: base=144, per_level=6
- Green Belt: base=192, per_level=4
- Blue Belt: base=224, per_level=2
(etc.)

Formula: base + (level_number * per_level)
Example: Yellow Belt Level 4 = 80 + (4 * 8) = 112 points
```

#### Activity Rewards (ongoing):
```
Activity types:
- "solve": 1 point (flat, no multiplier)
- "build", "code-adventure", "quest": 1 * belt_multiplier

Belt multipliers:
- White Belt: 1x
- Yellow Belt: 2x
- Orange Belt: 3x
- Green Belt: 4x
- Blue Belt: 5x
(etc.)

Example: Yellow Belt completes "code-adventure" = 1 * 2 = 2 points
Example: Orange Belt completes "solve" = 1 point
```

---

## Security

- JWT authentication for admins
- Facility-scoped data (admins can only access their assigned facilities)
- Request header `X-Facility-Id` or similar to scope operations
- Admin actions logged in Adjustment table

---

## External API Integration

### Code Ninjas API
- **Headers required:**
  - `facilityId: fcd4728c-afff-4a3c-8a39-05d2cd9d87ac`
  - (Authentication headers - TBD)

- **Endpoints to call:**
  - Get student data (including relationships with studentId, programId, courseId, levelId, activityId, courseName, levelName, activityName, activityType)
  - Fetch roster for facility

- **Caching strategy:**
  - Store student data locally in Ninja table
  - TTL: Refresh if `lastSyncedAt` > 6-24 hours old
  - On-demand refresh via sync endpoints

---

## Database

- PostgreSQL
- Tables: `facilities`, `admins`, `admin_facilities` (join table), `ninjas`, `ledger_txns`, `purchases`, `adjustments`, `shop_items`

---

## MVP Feature Checklist

### Must Have (Week 1-2):
- [ ] Admin authentication
- [ ] Facility management
- [ ] Ninja sync from API (manual trigger)
- [ ] Initial balance calculation
- [ ] Activity reward calculation
- [ ] Admin adjustments (add/deduct points)
- [ ] Shop item CRUD
- [ ] Purchase flow
- [ ] Ledger transaction history

### Nice to Have (Week 2-3):
- [ ] Leaderboards (earned/spent)
- [ ] Background sync job
- [ ] Purchase fulfillment workflow
- [ ] Admin audit log view

### Not in MVP:
- Achievements
- Notifications/Webhooks
- Analytics dashboard
- Ninja-facing authentication (just lookup by studentId)

---

## Notes

- Keep it simple - no unnecessary abstractions
- Facility-scoped everything (no cross-facility data sharing)
- Admin can belong to multiple facilities (use facility switcher in frontend)
- Ninjas generated from API, not manual entry
- All point transactions go through LedgerTxn for audit trail
