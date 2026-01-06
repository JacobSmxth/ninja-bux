# Security Review Report

**Date:** 2026-01-03
**Branch:** main
**Review Focus:** Changes introduced in current PR

---

## Executive Summary

This security review identified **2 high-confidence vulnerabilities** newly introduced by the current changes. Both issues relate to authentication/authorization bypass and could allow unauthorized access to system functionality.

| # | Severity | Category | Description | File |
|---|----------|----------|-------------|------|
| 1 | HIGH | Authorization Bypass | Unauthenticated facility creation | `NinjaService.java:86-95` |
| 2 | HIGH | Hardcoded Credentials | Default super admin credentials | `DataInitializer.java:17-19` |

---

## Vulnerability Details

### Vuln 1: Unauthenticated Arbitrary Facility Creation

**File:** `backend/src/main/java/dev/jsmitty/bux/system/service/NinjaService.java`
**Lines:** 86-95
**Severity:** HIGH
**Category:** `authorization_bypass`
**Confidence:** 9/10

#### Description

The new code in `syncSingleNinjaLocal()` auto-creates facilities when they don't exist, using user-controlled input for the facility name. This endpoint is publicly accessible without authentication and does not perform authorization checks.

**Vulnerable Code:**
```java
// Auto-create facility if it doesn't exist
if (!facilityRepository.existsById(facilityId)) {
  String facilityName = payload.facilityName();
  if (facilityName == null || facilityName.isBlank()) {
    facilityName = "Facility " + facilityId.toString().substring(0, 8);
  }
  Facility newFacility = new Facility(facilityId, facilityName);
  facilityRepository.save(newFacility);
  log.info("Auto-created facility: {} ({})", facilityName, facilityId);
}
```

**Endpoint:** `POST /api/facilities/{facilityId}/ninjas/{studentId}/sync-local`

#### Attack Vector

1. The security configuration permits all requests to `/api/facilities/**` without authentication
2. The controller endpoint does not call `accessChecker.checkFacilityAccess()` (unlike other endpoints)
3. An attacker can create arbitrary facilities with any UUID and name

#### Exploit Scenario

```bash
# Unauthenticated request creates a new facility
curl -X POST "http://target/api/facilities/11111111-1111-1111-1111-111111111111/ninjas/attacker123/sync-local" \
  -H "Content-Type: application/json" \
  -d '{
    "facilityName": "Malicious Facility",
    "firstName": "Evil",
    "lastName": "Ninja",
    "beltRank": "WHITE_BELT"
  }'
```

**Impact:**
- Data pollution with attacker-controlled facilities
- Creation of fake ninja accounts with manipulated point balances
- Potential for downstream exploitation if facilities are used in billing or access control

#### Recommendation

1. Add authorization check to the controller endpoint:
   ```java
   @PostMapping("/{studentId}/sync-local")
   public ResponseEntity<SingleSyncResponse> syncSingleNinjaLocal(...) {
     accessChecker.checkFacilityAccess(facilityId);  // Add this line
     return ResponseEntity.ok(ninjaService.syncSingleNinjaLocal(facilityId, studentId, request));
   }
   ```

2. Remove or restrict auto-create facility feature to authenticated admin users only

3. Consider requiring facilities to exist before allowing ninja sync operations

---

### Vuln 2: Hardcoded Default Super Admin Credentials

**File:** `backend/src/main/java/dev/jsmitty/bux/system/config/DataInitializer.java`
**Lines:** 17-19
**Severity:** HIGH
**Category:** `hardcoded_credentials`
**Confidence:** 8/10

#### Description

The `DataInitializer` creates a super admin account with trivially guessable hardcoded credentials. The `@Profile("!test")` annotation only excludes the test profile, meaning this code executes in **all other environments including production**.

**Vulnerable Code:**
```java
@Bean
@Profile("!test")
CommandLineRunner initData(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
  return args -> {
    Admin admin = new Admin("admin", passwordEncoder.encode("password"), "admin@example.com");
    admin.setSuperAdmin(true);
    adminRepository.save(admin);

    System.out.println("Login with: admin / password");
  };
}
```

#### Attack Vector

1. Default credentials (`admin` / `password`) are publicly visible in source code
2. The `@Profile("!test")` annotation allows execution in production
3. No check exists to prevent recreation of the admin account
4. Credentials are printed to stdout, potentially appearing in production logs

#### Exploit Scenario

```bash
# Attacker logs in with default credentials
curl -X POST "http://target/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Response contains JWT token with super admin privileges
# Attacker now has full administrative access
```

**Impact:**
- Complete system compromise via super admin access
- Ability to create additional admin accounts
- Access to all facilities and user data
- Modification of point balances and transactions

#### Recommendation

1. **Restrict to development only** - Change the profile annotation:
   ```java
   @Profile("dev")  // or @Profile("local")
   ```

2. **Add existence check** - Prevent duplicate creation:
   ```java
   if (!adminRepository.existsByUsername("admin")) {
     // create admin
   }
   ```

3. **Use environment variables** - For any required initial setup:
   ```java
   @Value("${app.admin.password:#{null}}")
   private String initialAdminPassword;
   ```

4. **Remove console output** - Don't print credentials:
   ```java
   // Remove: System.out.println("Login with: admin / password");
   ```

5. **Require password change** - Force password reset on first login

---

## Excluded Findings

The following issues were identified but excluded from this report:

| Issue | Reason for Exclusion |
|-------|---------------------|
| `.anyRequest().permitAll()` in SecurityConfig | Pre-existing code, not introduced by this PR |
| H2 Console publicly accessible (`/h2-console`) | Pre-existing configuration |
| Permissive CORS (`*` with credentials) | Pre-existing configuration |
| Hardcoded JWT secret in `application.properties` | Confidence 6/10 - Spring allows runtime override via environment variables |

---

## Remediation Priority

| Priority | Vulnerability | Effort | Risk if Unaddressed |
|----------|--------------|--------|---------------------|
| 1 | Default admin credentials | Low | Critical - immediate system compromise |
| 2 | Unauthenticated facility creation | Low | High - data integrity compromise |

---

## Notes

- This review focused specifically on **new code introduced by the current PR**
- Pre-existing security concerns should be addressed in a separate security hardening effort
- The application appears to be in active development; these issues should be resolved before any production deployment
