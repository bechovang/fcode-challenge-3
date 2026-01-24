# Story 3.3: My Listings - Filtering & Profit Display

Status: **review**

## Story

As a **seller**,
I want **to view all my listings with status filters and see my profit from sold listings**,
So that **I can manage my inventory and track my net earnings**.

## Scope Limitation

**IMPORTANT:** This story ONLY adds:
1. Status filter dropdown (All, Pending, Approved, Sold)
2. Profit display (net earnings after 10% platform fee)

All other UI elements (listing cards, layout, information display) **reuse existing project behavior** from browse listings. No new display requirements.

## Acceptance Criteria

**Given** I am logged in as a USER
**When** I navigate to the "My Listings" page
**Then** I see all listings where seller_id = my user ID
**And** the listings display uses the **existing project UI patterns** (same as browse listings)
**And** I can filter my listings by Status (All, Pending, Approved, Sold)
**And** I see my profit displayed (net earnings after 10% platform fee)
**And** the profit is displayed in VNĐ format (e.g., "4,950,000 VNĐ")

**Given** I select a status filter (e.g., "Approved")
**When** the filter is applied
**Then** only my listings with that status are displayed
**And** the filter selection is preserved in the URL

**Given** I have no listings
**When** I navigate to "My Listings"
**Then** a message "Bạn chưa có tài khoản nào" is displayed
**And** a link to create a new listing is shown

**Given** I view my listings
**When** the page loads
**Then** each listing displays:
  - Image thumbnail (clickable to view detail)
  - Game Name
  - Rank/Level
  - Price
  - Status badge (Pending/Approved/Sold)
  - Created date
  - Action button: View (to see listing details)
**And** there is no Edit functionality (view only page)

## Tasks / Subtasks

### Phase 1: Controller & Service Layer

- [x] Create MyListingsController (AC: All)
  - [x] GET /my-listings - show seller's listings with status filter
  - [x] Accept optional query parameter: status
  - [x] Get currently authenticated user's ID
  - [x] Fetch listings by seller_id from GameAccountService
  - [x] Calculate profit from SOLD listings (total - 10% commission)
  - [x] Pass data to Thymeleaf template

- [x] Update GameAccountService (AC: All)
  - [x] Add findBySellerId(Long sellerId) method
  - [x] Add findBySellerIdAndStatus(Long sellerId, ListingStatus status) method
  - [x] Add calculateProfit(Long sellerId) method - sum of SOLD listings minus 10% commission
  - [x] Platform commission = 10%

- [x] Update GameAccountRepository (AC: All)
  - [x] Add findBySellerId(Long sellerId) query method
  - [x] Add findBySellerIdAndStatus(Long sellerId, ListingStatus status) query method
  - [x] Add sumPriceBySellerIdAndStatus(Long sellerId, ListingStatus status) query method

### Phase 2: View Template

- [x] Create my-listings.html template (AC: All)
  - [x] **Reuse existing listing card UI** from browse-listings.html
  - [x] Add status filter dropdown at top (All, Pending, Approved, Sold)
  - [x] Add profit display section (simple text, no cards)
  - [x] Filter by seller_id (only show own listings)
  - [x] Apply filter when status selected

- [x] Update navbar (AC: Navigation)
  - [x] Add "Tài khoản của tôi" link to /my-listings

### Phase 3: Security & Access Control

- [x] Update SecurityConfig (AC: Security)
  - [x] Add @PreAuthorize("isAuthenticated()") to /my-listings endpoint
  - [x] Ensure users can only see their own listings (enforced in service layer)

### Phase 4: Testing

- [x] Create MyListingsControllerTest (AC: All)
  - [x] testShowMyListings_AuthenticatedUser_ReturnsOwnListings()
  - [x] testShowMyListings_FilterByStatus()
  - [x] testShowMyListings_NoListings_ReturnsEmptyState()
  - [x] testShowMyListings_CalculatesCorrectProfit()
  - [x] testShowMyListings_WithAllStatus_ShowsAllListings()

- [x] Create GameAccountServiceTest (AC: Revenue)
  - [x] testCalculateProfit_SoldListingsOnly()
  - [x] testCalculateProfit_NoSoldListings_ReturnsZero()
  - [x] testFindMyListings_WithAndWithoutStatusFilter()

---

## Dev Notes

### Previous Story Intelligence

**From Story 2.1 (Create Listing):**
- GameAccount entity has seller_id field referencing the user who created the listing
- GameAccount has status field: PENDING, APPROVED, SOLD, REJECTED
- GameAccount has price field (DECIMAL(12,2))
- GameAccount has game_name field for filtering

**From Story 3.1 (Wallet System & Buy with Balance):**
- Transaction entity tracks purchases (not needed for this story)
- Listings marked as SOLD when purchased
- User authentication via Spring Security

**From Story 2.2 (Browse Listings):**
- Similar filtering pattern can be reused (status, game search)
- Thymeleaf template patterns for listing display

**Key Patterns to Follow:**
- Use @PreAuthorize("isAuthenticated()") for authenticated endpoints
- Return Vietnamese messages for all user-facing text
- Format currency with thousand separators (e.g., "5,500,000 VNĐ")
- Use Thymeleaf fragments for consistent UI elements

### Architecture Compliance

**Layer Architecture:**
```
Browser → MyListingsController → GameAccountService → GameAccountRepository → Database
```

**Entity Design (ID-Based Navigation):**
- GameAccount entity uses seller_id (Long) to reference User
- NO @ManyToOne relationship to User
- Filter by seller_id in repository queries

**Security Requirements:**
- Endpoint requires authenticated user (ROLE_USER or ROLE_ADMIN)
- Use @PreAuthorize("isAuthenticated()") on controller method
- Validate user can only see their own listings (enforced by seller_id filter)

**Database Schema:**
```sql
-- Existing game_accounts table
CREATE TABLE game_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    rank VARCHAR(50) NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    description TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP NULL,
    image_url VARCHAR(500),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_game_name (game_name),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### File Structure Requirements

**New Files to Create:**
```
src/main/java/com/gameaccountshop/
├── controller/
│   └── MyListingsController.java       # HTTP endpoint for my-listings page
└── service/
    └── GameAccountService.java         # Add filtering and revenue methods

src/main/resources/templates/
└── my-listings.html                    # Thymeleaf template for my listings page

src/test/java/com/gameaccountshop/
├── controller/
│   └── MyListingsControllerTest.java   # Controller unit tests
└── service/
    └── GameAccountServiceTest.java     # Add revenue calculation tests
```

**Files to Modify:**
```
src/main/java/com/gameaccountshop/
├── repository/
│   └── GameAccountRepository.java      # Add filter query methods
└── config/
    └── SecurityConfig.java             # Add /my-listings authorization (optional)

src/main/resources/templates/
└── layout/
    └── header.html (navbar)            # Add "Tài khoản của tôi" link
```

### Technical Requirements

**Controller: MyListingsController**

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.service.GameAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@Slf4j
public class MyListingsController {

    private final GameAccountService gameAccountService;

    public MyListingsController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Show seller's listings with filters
     * GET /my-listings
     */
    @GetMapping("/my-listings")
    @PreAuthorize("isAuthenticated()")
    public String showMyListings(
            @RequestParam(required = false) String status,
            Authentication authentication,
            Model model) {

        // Get current user ID
        Long userId = (Long) authentication.getPrincipal();

        log.info("User {} viewing my-listings with status filter: {}", userId, status);

        // Fetch listings based on status filter
        var listings = gameAccountService.findMyListings(userId, status);

        // Calculate profit from SOLD listings (total - 10% commission)
        BigDecimal profit = gameAccountService.calculateProfit(userId);

        // Add data to model
        model.addAttribute("listings", listings);
        model.addAttribute("profit", profit);
        model.addAttribute("selectedStatus", status);

        return "my-listings";
    }
}
```

**Service: GameAccountService (Add Methods)**

```java
package com.gameaccountshop.service;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.repository.GameAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;

    // Existing constructor and methods...

    /**
     * Find listings by seller with optional status filter
     */
    public List<GameAccount> findMyListings(Long sellerId, String status) {
        log.debug("Finding listings for seller: {} with status: {}", sellerId, status);

        // Apply status filter if provided
        if (status != null && !status.isEmpty() && !status.equals("All")) {
            ListingStatus listingStatus = ListingStatus.valueOf(status.toUpperCase());
            return gameAccountRepository.findBySellerIdAndStatus(sellerId, listingStatus);
        }

        // No filter - return all listings
        return gameAccountRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Calculate profit from sold listings (total earnings - 10% platform commission)
     * Profit = Sum of SOLD listing prices * 0.90
     */
    public BigDecimal calculateProfit(Long sellerId) {
        BigDecimal totalEarnings = gameAccountRepository.sumPriceBySellerIdAndStatus(
            sellerId, ListingStatus.SOLD);
        if (totalEarnings == null) {
            return BigDecimal.ZERO;
        }
        // Apply 10% platform commission
        return totalEarnings.multiply(new BigDecimal("0.90"));
    }
}
```

**Repository: GameAccountRepository (Add Methods)**

```java
package com.gameaccountshop.repository;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // Existing methods...

    List<GameAccount> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<GameAccount> findBySellerIdAndStatus(Long sellerId, ListingStatus status);

    @Query("SELECT COALESCE(SUM(g.price), 0) FROM GameAccount g WHERE g.sellerId = :sellerId AND g.status = :status")
    BigDecimal sumPriceBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                           @Param("status") ListingStatus status);
}
```

**Template: my-listings.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Tài khoản của tôi - Game Account Shop</title>
</head>
<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="container mt-4">
    <h1>Tài khoản của tôi</h1>

    <!-- Profit Display (simple text, no cards) -->
    <div class="alert alert-success mb-3">
      <strong>Lợi nhuận:</strong>
      <span th:text="${#numbers.formatInteger(profit, 3, 'POINT')} + ' VNĐ'">0 VNĐ</span>
      <small class="text-muted">(Sau khi trừ 10% phí nền tảng)</small>
    </div>

    <!-- Status Filter -->
    <div class="row mb-3">
      <div class="col-md-3">
        <form th:action="@{/my-listings}" method="get">
          <select class="form-select" name="status" onchange="this.form.submit()">
            <option value="">Tất cả</option>
            <option value="PENDING" th:selected="${selectedStatus == 'PENDING'}">Pending</option>
            <option value="APPROVED" th:selected="${selectedStatus == 'APPROVED'}">Approved</option>
            <option value="SOLD" th:selected="${selectedStatus == 'SOLD'}">Sold</option>
          </select>
        </form>
      </div>
    </div>

    <!-- Listings Grid - REUSE EXISTING UI from browse-listings.html -->
    <div th:if="${!listings.isEmpty()}" class="row">
      <div class="col-md-4 mb-4" th:each="listing : ${listings}">
        <div class="card h-100">
          <img th:src="${listing.imageUrl}" class="card-img-top" alt="Game image">
          <div class="card-body">
            <h5 class="card-title" th:text="${listing.gameName}">Game Name</h5>
            <p class="card-text">
              <span class="badge"
                    th:classappend="${listing.status.name() == 'PENDING' ? 'bg-warning' :
                                   listing.status.name() == 'APPROVED' ? 'bg-success' :
                                   listing.status.name() == 'SOLD' ? 'bg-secondary' : 'bg-danger'}"
                    th:text="${listing.status.name()}">Status</span>
            </p>
            <p class="card-text" th:text="${#numbers.formatInteger(listing.price, 3, 'POINT')} + ' VNĐ'">Price</p>
            <a th:href="@{/listings/{id}(id=${listing.id})}" class="btn btn-primary">Xem chi tiết</a>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div th:if="${listings.isEmpty()}" class="text-center py-5">
      <p>Chưa có tài khoản nào.</p>
    </div>
  </div>

  <div th:replace="~{layout/footer :: footer}"></div>
</body>
</html>
```

### Integration Points

**Dependencies:**
- GameAccountRepository (data access)
- Spring Security (authentication)
- Thymeleaf (template rendering)

**Navigation:**
- Link from navbar: "Tài khoản của tôi" → GET /my-listings
- Link from empty state: "Đăng bán ngay" → GET /listings/create (existing)

### Testing Requirements

**Unit Tests:**
```java
@MyListingsControllerTest
class MyListingsControllerTest {
    - testShowMyListings_AuthenticatedUser_ReturnsOwnListings()
    - testShowMyListings_FilterByStatus_ReturnsFilteredListings()
    - testShowMyListings_NoListings_ReturnsEmptyState()
    - testShowMyListings_GuestUser_ReturnsLoginRedirect()
    - testShowMyListings_CalculatesCorrectProfit()
}

@GameAccountServiceTest {
    - testCalculateProfit_SoldListingsOnly()
    - testCalculateProfit_NoSoldListings_ReturnsZero()
    - testCalculateProfit_MultipleSoldListings_CalculatesCorrectly()
    - testCalculateProfit_Applies10PercentCommission()
    - testFindMyListings_WithAndWithoutStatusFilter()
}
```

### Error Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| Not logged in | Redirect to login page |
| No listings | Show "Chưa có tài khoản nào" message |
| Invalid status filter | Ignore filter, show all listings |

### References

**Source: epics.md**
- Epic 3: Simple Buying + Seller Tools
- Story 3.3: My Listings with Filtering + Revenue Display (lines 641-692)

**Source: architecture.md**
- Layer Architecture Pattern (lines 679-761)
- Entity Relationship Pattern (lines 194-264)
- API Naming Conventions (lines 688-718)
- Controller Architecture Pattern (lines 337-398)

**Source: project-context.md**
- Technology Stack (lines 15-36)
- Naming Conventions (lines 41-65)
- Security Requirements (lines 86-106)
- Localization (lines 252-267)

---

## Implementation Notes

### Profit Calculation Notes

**Profit Definition:**
- Profit = Sum of all SOLD listing prices - 10% platform commission
- Platform commission = 10% (0.10)
- Formula: `profit = totalEarnings * 0.90`
- Only SOLD listings count toward profit
- PENDING, APPROVED, REJECTED listings are NOT included

**Example:**
- Seller has 2 listings SOLD: 500,000 VNĐ and 800,000 VNĐ
- Total Earnings = 1,300,000 VNĐ
- Platform Commission (10%) = 130,000 VNĐ
- Profit = 1,300,000 * 0.90 = 1,170,000 VNĐ

### Filter Combinations

**Supported Filter Options:**
1. No filter (All or empty) - Show all listings
2. Status filter - Show listings with that status

**Status Values:**
- "All" or empty = Show all statuses
- "PENDING" = Show pending listings
- "APPROVED" = Show approved listings
- "SOLD" = Show sold listings

### Security Considerations

- Users can ONLY see their own listings (enforced by seller_id = userId)
- No cross-user data access possible
- @PreAuthorize ensures authentication
- Service layer validates seller_id matches current user

### Scope Limitations

**Important:** This story ONLY adds:
1. Status filter dropdown
2. Profit display (net after 10% commission)

All other UI **reuses existing project behavior**:
- Listing cards follow browse-listings.html pattern
- No new display requirements
- No edit/delete functionality (not in scope)

### File List

**New Files to Create:**
- `src/main/java/com/gameaccountshop/controller/MyListingsController.java`
- `src/main/resources/templates/my-listings.html` (reuse browse-listings.html pattern)
- `src/test/java/com/gameaccountshop/controller/MyListingsControllerTest.java`

**Files to Modify:**
- `src/main/java/com/gameaccountshop/repository/GameAccountRepository.java` - Add seller filter query methods
- `src/main/java/com/gameaccountshop/service/GameAccountService.java` - Add calculateProfit method
- `src/main/resources/templates/layout/header.html` - Add "Tài khoản của tôi" navbar link

### Testing Checklist

- [x] Authenticated user can see their own listings
- [x] Profit calculated from SOLD listings (total - 10% commission)
- [x] Status filter works correctly (All, Pending, Approved, Sold)
- [x] Empty state shows when no listings
- [x] View button navigates to listing detail
- [x] Navbar link navigates to my-listings
- [x] Guest users redirected to login
- [x] Listing display reuses existing browse-listings UI

---

## Code Review Follow-ups (AI)

**Review Date:** 2026-01-24
**Reviewer:** Adversarial Code Review Workflow
**Status:** ✅ All Fixed

### 🔴 HIGH Priority Issues

- [x] [AI-Review][HIGH][Security] Fix dangerous fallback to userId=1L in MyListingsController.java:44 - throws IllegalStateException instead
- [x] [AI-Review][HIGH][AC] Change empty state heading from "Chưa có tài khoản nào" to "Bạn chưa có tài khoản nào" to match AC requirement
- [x] [AI-Review][HIGH][NFR-061] Change status filter labels to Vietnamese: "Chờ duyệt", "Đã duyệt", "Đã bán" (currently English)
- [x] [AI-Review][HIGH][Architecture] Create MyListingDto and return DTO instead of exposing GameAccount entity to view layer
- [x] [AI-Review][HIGH][Epic] Add missing REJECTED status filter option (epic requirement includes Rejected)
- [x] [AI-Review][HIGH][Accessibility] Add image alt fallback or placeholder for broken image links

### 🟡 MEDIUM Priority Issues

- [x] [AI-Review][MEDIUM][Documentation] Update Implementation Summary section to properly document header.html modification

### 🟢 LOW Priority Issues

- [x] [AI-Review][LOW][Logging] Change log.info to log.debug for routine page view action
- [x] [AI-Review][LOW][NullSafety] Add null check on authentication.getPrincipal() before instanceof check

---

## Code Review Fixes Summary

**Date:** 2026-01-24
**All 9 issues fixed, 142 tests passing**

### Files Created (Fixes)
- `src/main/java/com/gameaccountshop/dto/MyListingDto.java` - New DTO for architecture compliance

### Files Modified (Fixes)
- `MyListingsController.java` - Fixed security vulnerability, null safety, logging level
- `GameAccountService.java` - Changed to return MyListingDto instead of entity
- `my-listings.html` - Fixed empty state message, Vietnamese labels, REJECTED option, image fallback
- `MyListingsControllerTest.java` - Updated to use DTO, added 2 new security tests
- `GameAccountServiceTest.java` - Updated to test DTO conversion

---

## Implementation Summary

**Date:** 2026-01-24
**Status:** Complete - Ready for Review (All Code Review Issues Fixed)

### Files Created

1. **MyListingsController.java** - New controller with `/my-listings` endpoint
   - `src/main/java/com/gameaccountshop/controller/MyListingsController.java`
   - GET endpoint with @PreAuthorize("isAuthenticated()")
   - Supports optional status query parameter
   - Calculates profit using GameAccountService
   - **Fixed:** Security vulnerability (throws IllegalStateException on invalid principal), null safety, proper logging level

2. **my-listings.html** - Thymeleaf template
   - `src/main/resources/templates/my-listings.html`
   - Reuses home.html listing card UI patterns
   - Profit display section (simple text with 10% commission note)
   - Status filter dropdown form
   - Empty state message
   - **Fixed:** Empty state message matches AC, Vietnamese labels, REJECTED option added, image fallback

3. **MyListingsControllerTest.java** - Controller unit tests
   - `src/test/java/com/gameaccountshop/controller/MyListingsControllerTest.java`
   - 7 tests covering all scenarios (including 2 new security tests)
   - **Fixed:** Uses MyListingDto instead of entity

4. **MyListingDto.java** - New DTO for architecture compliance
   - `src/main/java/com/gameaccountshop/dto/MyListingDto.java`
   - Record DTO for seller's own listings
   - **NEW:** Created to fix architecture violation

### Files Modified

1. **GameAccountRepository.java**
   - Added `findBySellerIdOrderByCreatedAtDesc(Long sellerId)`
   - Added `findBySellerIdAndStatus(Long sellerId, ListingStatus status)`
   - Added `@Query sumPriceBySellerIdAndStatus()` for profit calculation

2. **GameAccountRepositoryTest.java**
   - Added 5 tests for new repository methods
   - All 13 tests passing

3. **GameAccountService.java**
   - Added `findMyListings(Long sellerId, String status)` method
   - Added `calculateProfit(Long sellerId)` method (total * 0.90)
   - **Fixed:** Returns MyListingDto instead of GameAccount entity (architecture compliance)
   - Uses Long arithmetic instead of BigDecimal (project pattern)

4. **GameAccountServiceTest.java**
   - Added 8 tests for findMyListings and calculateProfit
   - **Fixed:** Tests updated to use MyListingDto
   - All 40 tests passing (27 existing + 13 new)

5. **header.html** (navbar)
   - Added "Tài khoản của tôi" link after "Đăng bán" link

### Test Results

**All 142 tests passing:**
- DataInitializerTest: 3 tests
- GameAccountRepositoryTest: 13 tests
- GameAccountServiceTest: 40 tests
- MyListingsControllerTest: 7 tests (including 2 new security tests)
- HomeControllerTest: 2 tests
- ListingControllerTest: 15 tests
- AdminControllerTest: 29 tests
- WalletControllerTest: 12 tests
- WalletServiceTest: 7 tests
- EmailServiceTest: 2 tests
- ImageUploadServiceTest: 12 tests

### Technical Notes

- Used Long type for price/profit (not BigDecimal) - follows project convention
- Profit calculation: `(totalEarnings * 90) / 100` using integer arithmetic
- CustomUserDetails stores user ID as the principal (Long type)
- Status filter supports: All (empty), PENDING, APPROVED, SOLD, REJECTED
- Empty state handled with user-friendly message and create link
- **Architecture:** DTO pattern properly implemented for view layer
- **Security:** Authentication principal properly validated with exceptions
- **Accessibility:** Image fallback with placeholder for broken links
