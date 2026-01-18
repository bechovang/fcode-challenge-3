# Parallel Phase Summary: Stories 1.2, 1.3, 2.1

**For:** Tech Lead / Engineering Manager
**Phase:** Sprint 1 - Parallel Development Block
**Stories:** 1.2 (User Auth), 1.3 (Default Admin), 2.1 (Create Listing)

---

## 📊 Executive Summary

| Story | Feature | Complexity | Est. Hours | Developer |
|-------|---------|------------|------------|-----------|
| 1.2 | User Registration & Login | Medium | 4-6h | Dev A |
| 1.3 | Default Admin Account | Easy | 1-2h | Dev B |
| 2.1 | Create Listing Form | Medium | 3-4h | Dev C |

**Total Estimated Effort:** 8-12 hours (parallel: ~4-6 hours wall time)

**Parallel Feasibility:** ✅ **GREEN** - No blocking dependencies

---

## 🎯 Story Overviews

### Story 1.2: User Registration & Login
**Deliverables:**
- `RegistrationRequest`, `LoginRequest` DTOs
- `UserService` with BCrypt password hashing
- `AuthController` with form login endpoints
- `register.html`, `login.html` templates
- SecurityConfig updates for form authentication

**Key Acceptance:**
- BCrypt(10) password hashing
- 30-min session timeout
- Vietnamese error messages

---

### Story 1.3: Default Admin Account
**Deliverables:**
- `DataInitializer` (CommandLineRunner)
- Admin creation on first startup
- Idempotent (no duplicates on restart)

**Key Acceptance:**
- Creates admin/admin123 on empty DB
- Startup logging for visibility

---

### Story 2.1: Create Listing Form
**Deliverables:**
- `GameAccount` entity (with `account_rank` column)
- `ListingStatus` enum (PENDING → APPROVED → SOLD)
- `GameAccountRepository` with query methods
- `CreateListingRequest` DTO
- `ListingService` with validation
- `ListingController` with `@PreAuthorize`
- `create-listing.html` template

**Key Acceptance:**
- Price > 0 validation
- Status defaults to PENDING
- Authenticated access only

---

## 🔄 Parallel Feasibility Analysis

### Dependency Graph

```
┌─────────────────────────────────────────────────────────────┐
│                    Story 1.1 (DONE)                          │
│              Spring Boot Initialization                       │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ├─────────────────────────────────────┐
                            │                                     │
                    ┌───────▼────────┐                  ┌──────▼─────┐
                    │   Story 1.2     │                  │  Story 1.3  │
                    │  User Auth      │                  │ Default Admin│
                    │  (User entity)  │                  │ (User entity)│
                    └────────┬────────┘                  └──────┬──────┘
                             │                                  │
                             │                                  │
                    ┌────────▼──────────────────────────────────▼────────┐
                    │                 Story 2.1                          │
                    │            Create Listing Form                     │
                    │          (GameAccount entity)                      │
                    │         References User via FK)                    │
                    └────────────────────────────────────────────────────┘
```

**Analysis:**
- ✅ Stories 1.2 and 1.3: Can run 100% parallel (both extend User)
- ✅ Story 2.1: Can run parallel with 1.2/1.3 (different entity/controller)
- ⚠️ Story 2.1 references User entity, but only through FK (no shared code modification needed)

---

## 🏗️ Architecture Coordination Points

### Shared Components (Pre-existing from Story 1.1)
| Component | Status | Notes |
|-----------|--------|-------|
| `User` entity | ✅ Exists | All stories reference, no modification needed |
| `Role` enum | ✅ Exists | USER, ADMIN constants |
| `UserRepository` | ✅ Exists | May need `findByUsername` added (Story 1.2) |
| `SecurityConfig` | ✅ Exists | Stories 1.2, 2.1 modify (non-conflicting) |
| `application.yml` | ✅ Exists | All stories may add config |

### Non-Conflicting Modifications

**SecurityConfig.java:**
```java
// Story 1.2 adds: form login, logout
// Story 2.1 adds: @EnableMethodSecurity annotation
// NO CONFLICT: Different configuration sections
```

**application.yml:**
```yaml
# Story 1.2 adds: session timeout
# Story 1.3 adds: logging level
# Story 2.1 adds: (nothing new)
# NO CONFLICT: Different keys
```

---

## ⚠️ Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Merge conflict in SecurityConfig | Low | Low | Different config sections; easy resolution |
| Merge conflict in application.yml | Low | Low | Different keys; YAML is merge-friendly |
| User entity modification | Medium | Medium | Story 1.2 adds `findByUsername` to repo (not entity) |
| Database migration conflict | Very Low | Medium | Stories use different tables (users vs game_accounts) |
| BCrypt round mismatch | Low | Low | Story 1.3 uses same encoder bean from 1.2 |

**Overall Risk Level:** 🟢 **LOW**

---

## 📋 Coordination Checklist

### Pre-Development (Tech Lead Actions)
- [ ] Verify Story 1.1 is complete and committed to main
- [ ] Create three feature branches: `feature/1.2-user-auth`, `feature/1.3-default-admin`, `feature/2.1-create-listing`
- [ ] Confirm GameAccount DB migration has `account_rank` (not `rank`) column
- [ ] Assign developers to stories

### During Development
- [ ] Daily sync: Check progress on each story
- [ ] Watch for: SecurityConfig merge needs
- [ ] Watch for: UserRepository modifications (Story 1.2)

### Integration & Merge Order (Recommended)
```
1. Merge Story 1.3 first (lowest risk, no conflicts)
   ↓
2. Merge Story 1.2 second (modifies SecurityConfig)
   ↓
3. Merge Story 2.1 last (depends on User being stable)
```

---

## 🔗 Integration Points

### Data Flow
```
Story 1.2/1.3                    Story 2.1
    │                                │
    ▼                                ▼
┌─────────┐                    ┌──────────────┐
│  users  │◄──────────────────│game_accounts │
│ table   │    seller_id FK    │    table     │
└─────────┘                    └──────────────┘
    │                                │
    │                                │
    ▼                                ▼
AuthController                  ListingController
(/login, /register)             (/listing/create)
```

### Endpoint Ownership
| Endpoint | Story | Controller | Access |
|----------|-------|------------|--------|
| `GET /login` | 1.2 | AuthController | Public |
| `POST /login` | 1.2 | Spring Security | Public |
| `GET /register` | 1.2 | AuthController | Public |
| `POST /register` | 1.2 | AuthController | Public |
| `POST /logout` | 1.2 | Spring Security | Authenticated |
| `GET /listing/create` | 2.1 | ListingController | Authenticated |
| `POST /listing/create` | 2.1 | ListingController | Authenticated |

---

## 🧪 Testing Strategy

### Unit Tests (Per Story)
| Story | Test Coverage |
|-------|---------------|
| 1.2 | UserService.register/login, password validation |
| 1.3 | DataInitializer idempotency, admin creation |
| 2.1 | ListingService.createListing, price validation |

### Integration Tests (Post-Merge)
1. **E2E Flow:** Register user → Login → Create listing
2. **Access Control:** Verify protected endpoints require auth
3. **Database Integrity:** Verify FK constraints (users → game_accounts)

---

## 📦 Deliverables Summary

### New Files Created
```
Story 1.2:
├── dto/RegistrationRequest.java
├── dto/LoginRequest.java
├── service/UserService.java
├── controller/AuthController.java
├── templates/register.html
└── templates/login.html

Story 1.3:
└── config/DataInitializer.java

Story 2.1:
├── entity/GameAccount.java
├── enums/ListingStatus.java
├── repository/GameAccountRepository.java
├── dto/CreateListingRequest.java
├── service/ListingService.java
├── controller/ListingController.java
└── templates/create-listing.html
```

### Modified Files
```
Shared modifications (non-conflicting):
├── config/SecurityConfig.java (Stories 1.2, 2.1)
├── repository/UserRepository.java (Story 1.2 - adds findByUsername)
└── resources/application.yml (Stories 1.2, 1.3 - different keys)
```

---

## ✅ Acceptance Criteria Matrix

| AC | Story 1.2 | Story 1.3 | Story 2.1 |
|----|-----------|-----------|-----------|
| BCrypt password hashing | ✅ 10 rounds | - | - |
| Vietnamese messages | ✅ | ✅ (log) | ✅ |
| Idempotent operation | - | ✅ | - |
| Access control | ✅ sessions | - | ✅ @PreAuthorize |
| Validation | ✅ form data | - | ✅ price > 0 |
| Default values | - | ✅ admin role | ✅ PENDING status |

---

## 📅 Timeline Recommendation

**Day 1 (Morning):** Branch setup + Story assignment
**Day 1 (Afternoon):** Development begins (all 3 stories)
**Day 2 (Morning):** Code review + testing
**Day 2 (Afternoon):** Merge in recommended order + integration testing

**Total Calendar Time:** 2 days (with 3 developers in parallel)

---

## 🎯 Success Criteria

- [ ] All acceptance criteria met per story
- [ ] No merge conflicts blocking deployment
- [ ] Integration tests pass (register → login → create listing)
- [ ] Database constraints verified
- [ ] No regression in existing functionality

---

**Prepared by:** Dev Agent
**Date:** 2025-01-17
**Status:** ✅ Ready for parallel development kickoff
