# Ninja Bux Backend

Spring Boot backend for tracking and rewarding "bux" for Code Ninjas student progress. The system
syncs activity from the Code Ninjas API, calculates rewards, tracks balances via a ledger, and
supports a shop + purchase flow. Admins manage facilities and can make manual balance adjustments.

## Quick Map

- `src/main/java/dev/jsmitty/bux/system`
  - `config` - Spring beans and seed data
  - `controller` - REST endpoints (API surface)
  - `domain` - JPA entities (database tables)
  - `dto` - API request/response payloads
  - `external` - Code Ninjas API client and DTOs
  - `repository` - Spring Data JPA repositories
  - `security` - JWT auth + Spring Security plumbing
  - `service` - business logic and transactional flows

## Core Request Flows

- Auth: `AuthController` -> `AuthService` -> `AdminRepository` -> `JwtUtil`
- Sync (remote): `NinjaController` -> `NinjaService` -> `CodeNinjasApiClient` -> `NinjaRepository`
  -> `LedgerService` -> `LedgerTxnRepository`
- Sync (local): `NinjaController` -> `NinjaService` -> `NinjaRepository` -> `LedgerService`
- Ledger: `NinjaController` -> `LedgerService` -> `LedgerTxnRepository` (+ `NinjaRepository` to
  refresh cached balance)
- Shop + Purchases: `ShopController`/`PurchaseController` -> `ShopService`/`PurchaseService`
  -> `ShopItemRepository`/`PurchaseRepository` -> `LedgerService`
- Adjustments: `AdjustmentController` -> `AdjustmentService` -> `AdjustmentRepository`
  -> `LedgerService`
- Leaderboard: `LeaderboardController` -> `LeaderboardService` -> `LedgerTxnRepository`

## Database Schema (JPA)

Note: H2 is used in dev/test. JPA is configured with `ddl-auto=update`, so tables are inferred
from entities. Foreign keys are modeled logically; most are stored as ids without explicit
constraints.

### facilities
- `id` (UUID, PK)
- `name` (string)
- `created_at` (timestamp)

Used by: `Admin` (many-to-many), `Ninja`, `ShopItem`, `Purchase`, `Adjustment`, `LedgerTxn`.

### admins
- `id` (PK)
- `username` (unique)
- `password_hash`
- `email`
- `super_admin` (boolean)
- `created_at`

### admin_facilities (join table)
- `admin_id` -> `admins.id`
- `facility_id` -> `facilities.id`

### ninjas
- `id` (PK)
- `facility_id` (UUID)
- `student_id` (string)
- `first_name`, `last_name`
- `course_name`, `level_name`, `level_id`, `level_sequence`
- `activity_id`, `activity_sequence`
- `group_id`, `sub_group_id`
- `completed_steps`, `total_steps`
- `last_activity_id`, `last_activity_sequence`, `last_completed_steps`, `last_activity_updated_at`
- `current_balance` (cached sum of ledger)
- `last_synced_at`
- `created_at`

Constraints/indexes:
- Unique `(student_id, facility_id)`
- Index `(facility_id, student_id)`

### ledger_txns
- `id` (PK)
- `facility_id`, `student_id`
- `amount` (signed integer; positive earns, negative spends)
- `type` (enum: INITIAL_BALANCE, ACTIVITY_REWARD, PURCHASE, ADJUSTMENT)
- `description`
- `related_entity_id` (purchase/adjustment id, not enforced)
- `created_at`

Index: `(facility_id, student_id, created_at DESC)`

### shop_items
- `id` (PK)
- `facility_id`
- `name`, `description`
- `price`
- `is_available`
- `created_at`

### purchases
- `id` (PK)
- `facility_id`, `student_id`
- `shop_item_id`
- `item_name` (denormalized snapshot)
- `price`
- `status` (PENDING, FULFILLED, CANCELLED)
- `purchased_at`, `fulfilled_at`

### adjustments
- `id` (PK)
- `facility_id`, `student_id`
- `admin_id`
- `amount`
- `reason`
- `created_at`

## Ledger and Balance Rules

- `LedgerTxn` is the source of truth; `Ninja.currentBalance` is cached for quick reads.
- Balance is recomputed from the ledger on each transaction creation.
- Purchases create negative ledger entries; cancellations create a refund adjustment.

## Sync Logic Summary

- Remote sync uses Code Ninjas login + current activity + level status (when available).
- Local sync accepts client-provided activity data for offline or test scenarios.
- Sync is done under a row-level lock (SELECT ... FOR UPDATE) to prevent double-awards.

## External Integration (Code Ninjas)

`CodeNinjasApiClient` calls:
- `POST /center/api/login`
- `GET /center/api/common/activity/current`
- `GET /center/api/level/statusinfo`
- `GET /center/api/common/groups/currentGroup`
- `GET /center/api/common/ninjaInfo`

Responses are normalized into internal DTOs for the sync workflow.

## API Endpoints (High Level)

Auth
- `POST /api/auth/login`

Facilities / Ninjas
- `GET /api/facilities/{facilityId}/ninjas`
- `GET /api/facilities/{facilityId}/ninjas/{studentId}`
- `POST /api/facilities/{facilityId}/ninjas/{studentId}/sync`
- `POST /api/facilities/{facilityId}/ninjas/{studentId}/sync-local`
- `GET /api/facilities/{facilityId}/ninjas/{studentId}/ledger`

Shop / Purchases
- `GET /api/facilities/{facilityId}/shop`
- `POST /api/facilities/{facilityId}/shop`
- `PUT /api/facilities/{facilityId}/shop/{itemId}`
- `DELETE /api/facilities/{facilityId}/shop/{itemId}`
- `POST /api/facilities/{facilityId}/ninjas/{studentId}/purchases`
- `GET /api/facilities/{facilityId}/purchases`
- `PUT /api/facilities/{facilityId}/purchases/{purchaseId}/fulfill`
- `PUT /api/facilities/{facilityId}/purchases/{purchaseId}/cancel`

Adjustments
- `POST /api/facilities/{facilityId}/ninjas/{studentId}/adjustments`
- `GET /api/facilities/{facilityId}/adjustments`

Leaderboard
- `GET /api/facilities/{facilityId}/leaderboard/earned`
- `GET /api/facilities/{facilityId}/leaderboard/spent`

Admin
- `GET /api/admin/admins`
- `GET /api/admin/admins/{id}`
- `POST /api/admin/admins`
- `PUT /api/admin/admins/{id}`
- `DELETE /api/admin/admins/{id}`
- `GET /api/admin/facilities`
- `GET /api/admin/me`

Code Ninjas Proxy/Helpers
- `POST /api/cn/login`
- `GET /api/cn/activity/current`
- `GET /api/cn/level/statusinfo`
- `GET /api/cn/groups/current`

## Configuration Notes

- H2 console at `/h2-console` (enabled in `application.properties`).
- Default dev admin user is created in `DataInitializer` (non-test profiles).
- JWT secret and expiration are configured in `application.properties`.
