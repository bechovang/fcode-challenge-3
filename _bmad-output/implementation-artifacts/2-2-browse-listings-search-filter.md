# Story 2.2: Browse Listings with Search/Filter

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **guest or buyer**,
I want **to browse and search listings on the home page**,
So that **I can find LoL accounts I want to buy**.

## Acceptance Criteria

**Given** I am on the home page (/)
**When** the page loads
**Then** I see all APPROVED listings in a 3-column grid
**And** each listing card displays:
  - Game Name: "Liên Minh Huyền Thoại" (always the same - LoL-only MVP)
  - Rank/Level (e.g., "Gold III", "Diamond II")
  - Price formatted: "500,000 VNĐ"
  - Seller username (need to join with users table)
  - Creation date: "18/01/2026" (DD/MM/YYYY format)
  - Description preview: first 100 characters
**And** listings are ordered by created_at DESC (newest first)

**Given** I want to search for listings
**When** I type in the search box and click "Tìm kiếm"
**Then** the form submits and page reloads with filtered results
**And** search uses SQL LIKE: `WHERE game_name LIKE '%search%'`

**Given** I want to filter by rank
**When** I select a rank from the dropdown
**Then** I see only listings with that rank

**Given** there are no approved listings matching my criteria
**When** the results are empty
**Then** a message "Không tìm thấy tài khoản nào" is displayed

**Given** I want to access the listings page
**When** I click "Danh sách" in the navbar
**Then** I am redirected to the home page which shows listings

**Technical Notes:**
- Home page (/) shows listings directly (no separate /listings page)
- Search: Form submission via GET with ?search= and ?rank= parameters
- Filter: `AND account_rank = :selectedRank`
- Both search and filter can work together
- 3-column grid layout: responsive to 1 column on mobile
- Date format: DD/MM/YYYY (e.g., 18/01/2026)

## Tasks / Subtasks

- [ ] Update HomeController with search/filter parameters (AC: #1, #3)
  - [ ] GET / - show all APPROVED listings with optional search/rank
  - [ ] Add @RequestParam Optional<String> search
  - [ ] Add @RequestParam Optional<String> rank
  - [ ] Order by created_at DESC (newest first)
  - [ ] Pass listings, search, rank to template

- [ ] Add search functionality in repository and service (AC: #3, #4)
  - [ ] Add findByGameNameContainingAndStatus query method
  - [ ] Add findByStatusAndAccountRank query method
  - [ ] Add combined search+filter query method
  - [ ] Implement logic in GameAccountService.findApprovedListings()

- [ ] Add filter by rank functionality (AC: #5, #6)
  - [ ] Add rank dropdown with LoL ranks (Iron through Challenger)
  - [ ] Support "Tất cả rank" (All ranks) option
  - [ ] Support combined search + filter

- [ ] Handle empty results (AC: #7)
  - [ ] Check if listings list is empty
  - [ ] Display "Không tìm thấy tài khoản nào" message
  - [ ] Keep search/filter UI visible for retry
  - [ ] Add "Xem tất cả tài khoản" reset link

- [ ] Create/update home.html template with listing grid (AC: #1, #2)
  - [ ] 3-column grid layout: `grid-template-columns: repeat(3, 1fr)`
  - [ ] Responsive: 1 column on mobile (@media max-width: 768px)
  - [ ] Search/filter form at top of page
  - [ ] Display listing cards with all required fields
  - [ ] Format price with comma separator: #numbers.formatInteger
  - [ ] Format date as DD/MM/YYYY: #temporals.format
  - [ ] Truncate description to 100 chars

- [ ] Add seller username to listing cards (AC: #2)
  - [ ] Create DTO with seller username OR
  - [ ] Add @Query to join with users table in repository
  - [ ] Pass seller username to template

- [ ] Update navigation (AC: #8)
  - [ ] Add "Danh sách" link to navbar
  - [ ] Link points to "/" (home page)
  - [ ] Make listings accessible to guest users (no auth required)

---

## Review Follow-ups (AI-Generated Code Review)

**Date:** 2026-01-18
**Issues Found:** 2 High, 3 Medium, 3 Low

### HIGH Priority Issues

- [ ] [AI-Review][HIGH] Fix combined search + filter loses ORDER BY created_at DESC - AC #5 requires newest first ordering [GameAccountService.java:78-83]
  - Current: `findByStatusAndAccountRank()` has no ORDER BY, then in-memory filter loses ordering
  - Fix: Add `@Query("SELECT g FROM GameAccount g WHERE g.accountRank = :rank AND g.status = :status ORDER BY g.createdAt DESC")`

- [ ] [AI-Review][HIGH] Fix inconsistent case sensitivity in search - search behavior changes based on whether rank filter is selected [GameAccountRepository.java:29, GameAccountService.java:82,88]
  - Current: Repository `LIKE` is case-sensitive, but combined search+rank adds `.toLowerCase()` manually
  - Fix: Use `LOWER(g.gameName) LIKE CONCAT('%', LOWER(:search), '%')` in repository for consistent case-insensitive search

### MEDIUM Priority Issues

- [ ] [AI-Review][MEDIUM] Add test coverage for Story 2.2 - no tests for HomeController, search/filter service methods, or new repository queries
  - Create `HomeControllerTest.java` with tests for search/rank parameters
  - Add tests to `GameAccountServiceTest` for `findApprovedListings(String, String)` edge cases

- [ ] [AI-Review][MEDIUM] Replace deprecated `#strings.abbreviate()` in template [home.html:211]
  - Current: Uses deprecated Thymeleaf method
  - Fix: Use `#strings.substring()` with explicit truncation logic

- [ ] [AI-Review][MEDIUM] Fix V2 seed data passwords - plain text instead of BCrypt hashed [V2__Insert_Seed_Data.sql:15-21]
  - Current: Plain text `'123456'` but comment claims "BCrypt hashed"
  - Fix: Use actual BCrypt hashes like `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

### LOW Priority Issues

- [ ] [AI-Review][LOW] Remove redundant null check for gameName [GameAccountService.java:82]
  - Schema has DEFAULT value, so null is impossible

- [ ] [AI-Review][LOW] Consider pagination for listing queries (acceptable for MVP)
  - Current: Loads all approved listings into memory

- [ ] [AI-Review][LOW] Remove duplicate navbar links when logged in [header.html:107-108]
  - Both "Danh sách" and "Trang chủ" link to "/"

## Dev Notes

### User Requirements Summary (From Interview)

**Page Design:**
- Location: Home page (/) - listings shown directly on home
- Layout: 3-column grid, responsive to 1 column on mobile
- Search position: Top of page
- Search method: Form submission (no AJAX)

**Listing Card Fields:**
- Game Name: "Liên Minh Huyền Thoại" (always - LoL-only MVP)
- Rank: e.g., "Gold III", "Diamond II"
- Price: formatted as "500,000 VNĐ"
- Seller Username: from users table
- Creation Date: DD/MM/YYYY format (e.g., "18/01/2026")
- Description Preview: first 100 characters

**Access Points:**
- Navbar: "Danh sách" link → home page
- Direct access: / shows listings

**Empty State:**
- Message: "Không tìm thấy tài khoản nào"
- Reset link: "Xem tất cả tài khoản"

### Previous Story Intelligence (Story 2.1)

**Key Learnings from Story 2.1 (Create Listing):**

1. **Database Schema:**
   - Table `game_accounts` has columns: `game_name` (DEFAULT "Liên Minh Huyền Thoại"), `account_rank`, `price`, `description`, `status`, `seller_id`, `created_at`
   - Status enum: PENDING, APPROVED, REJECTED, SOLD
   - Only APPROVED listings visible to buyers
   - Price is BIGINT (not DECIMAL)

2. **Code Patterns:**
   - Entity: `GameAccount` with `@Column(name = "account_rank")`
   - Repository: `GameAccountRepository` extends `JpaRepository`
   - Service: `GameAccountService` with `@Slf4j` logging
   - Template: Thymeleaf fragment pattern `layout/header :: navbar`

3. **Critical Bug from Story 2.1:**
   - **ClassCastException**: `User user = (User) authentication.getPrincipal();` was WRONG
   - **Correct**: Use `authentication.getName()` + `UserRepository.findByUsername()`
   - This story doesn't need auth (guest access), but remember pattern for future

4. **Project Structure:**
   - Package: `com.gameaccountshop`
   - Templates: `src/main/resources/templates/`

### Database Schema for This Story

**Table: game_accounts** (already exists from Story 2.1)

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | Primary key |
| `seller_id` | BIGINT | FK to users.id |
| `game_name` | VARCHAR(100) | DEFAULT "Liên Minh Huyền Thoại" |
| `account_rank` | VARCHAR(50) | e.g., "Gold III", "Diamond II" |
| `price` | BIGINT | NOT NULL, CHECK > 2000 |
| `description` | TEXT | Optional, truncate to 100 chars |
| `status` | ENUM | PENDING, APPROVED, REJECTED, SOLD |
| `created_at` | TIMESTAMP | Auto-set, format as DD/MM/YYYY |

**Table: users** (for seller username lookup)

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | Primary key |
| `username` | VARCHAR(50) | Seller username |

**Key Query Patterns:**
```sql
-- All approved listings (newest first)
SELECT ga.*, u.username as seller_username
FROM game_accounts ga
LEFT JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;

-- Search by game name
SELECT ga.*, u.username as seller_username
FROM game_accounts ga
LEFT JOIN users u ON ga.seller_id = u.id
WHERE ga.game_name LIKE CONCAT('%', ?search, '%')
  AND ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;

-- Filter by rank
SELECT ga.*, u.username as seller_username
FROM game_accounts ga
LEFT JOIN users u ON ga.seller_id = u.id
WHERE ga.account_rank = ?rank
  AND ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;

-- Combined search + filter
SELECT ga.*, u.username as seller_username
FROM game_accounts ga
LEFT JOIN users u ON ga.seller_id = u.id
WHERE ga.game_name LIKE CONCAT('%', ?search, '%')
  AND ga.account_rank = ?rank
  AND ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;
```

### Repository Query Methods

**Approach 1: Create a DTO with seller username (Recommended)**

```java
// DTO for listing display
public class ListingDisplayDto {
    private Long id;
    private String gameName;
    private String accountRank;
    private Long price;
    private String description;
    private LocalDateTime createdAt;
    private String sellerUsername; // Joined from users table

    // Getters and setters
}
```

**Repository with @Query:**

```java
@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // NEW: All approved with seller username
    @Query("SELECT new com.gameaccountshop.dto.ListingDisplayDto(" +
           "g.id, g.gameName, g.accountRank, g.price, g.description, g.createdAt, u.username) " +
           "FROM GameAccount g " +
           "LEFT JOIN User u ON g.sellerId = u.id " +
           "WHERE g.status = :status " +
           "ORDER BY g.createdAt DESC")
    List<ListingDisplayDto> findApprovedWithSeller(@Param("status") ListingStatus status);

    // NEW: Search by game name
    @Query("SELECT new com.gameaccountshop.dto.ListingDisplayDto(" +
           "g.id, g.gameName, g.accountRank, g.price, g.description, g.createdAt, u.username) " +
           "FROM GameAccount g " +
           "LEFT JOIN User u ON g.sellerId = u.id " +
           "WHERE g.gameName LIKE CONCAT('%', :search, '%') AND g.status = :status " +
           "ORDER BY g.createdAt DESC")
    List<ListingDisplayDto> findByGameNameContainingWithSeller(
        @Param("search") String search,
        @Param("status") ListingStatus status
    );

    // NEW: Filter by rank
    @Query("SELECT new com.gameaccountshop.dto.ListingDisplayDto(" +
           "g.id, g.gameName, g.accountRank, g.price, g.description, g.createdAt, u.username) " +
           "FROM GameAccount g " +
           "LEFT JOIN User u ON g.sellerId = u.id " +
           "WHERE g.accountRank = :rank AND g.status = :status " +
           "ORDER BY g.createdAt DESC")
    List<ListingDisplayDto> findByRankWithSeller(
        @Param("rank") String rank,
        @Param("status") ListingStatus status
    );

    // NEW: Combined search + filter
    @Query("SELECT new com.gameaccountshop.dto.ListingDisplayDto(" +
           "g.id, g.gameName, g.accountRank, g.price, g.description, g.createdAt, u.username) " +
           "FROM GameAccount g " +
           "LEFT JOIN User u ON g.sellerId = u.id " +
           "WHERE g.gameName LIKE CONCAT('%', :search, '%') " +
           "  AND g.accountRank = :rank " +
           "  AND g.status = :status " +
           "ORDER BY g.createdAt DESC")
    List<ListingDisplayDto> findBySearchAndRankWithSeller(
        @Param("search") String search,
        @Param("rank") String rank,
        @Param("status") ListingStatus status
    );
}
```

**Alternative Approach: Use native query**

```java
@Query(value = "SELECT ga.*, u.username as seller_username " +
               "FROM game_accounts ga " +
               "LEFT JOIN users u ON ga.seller_id = u.id " +
               "WHERE ga.status = :status " +
               "ORDER BY ga.created_at DESC",
       nativeQuery = true)
List<Object[]> findApprovedWithSellerNative(@Param("status") String status);
```

### Service Layer

**Update `GameAccountService.java`:**

```java
@Service
@Slf4j
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;

    /**
     * Find all approved listings, optionally filtered by search and/or rank
     * Returns DTO with seller username for display
     */
    public List<ListingDisplayDto> findApprovedListings(String search, String rank) {
        log.info("Finding approved listings: search={}, rank={}", search, rank);

        if (search != null && !search.isBlank() && rank != null && !rank.isBlank()) {
            // Combined search + filter
            return gameAccountRepository.findBySearchAndRankWithSeller(
                search, rank, ListingStatus.APPROVED
            );
        } else if (search != null && !search.isBlank()) {
            // Search only
            return gameAccountRepository.findByGameNameContainingWithSeller(
                search, ListingStatus.APPROVED
            );
        } else if (rank != null && !rank.isBlank()) {
            // Filter by rank only
            return gameAccountRepository.findByRankWithSeller(
                rank, ListingStatus.APPROVED
            );
        } else {
            // No filters - return all approved
            return gameAccountRepository.findApprovedWithSeller(ListingStatus.APPROVED);
        }
    }
}
```

### Controller

**Update `HomeController.java`:**

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.dto.ListingDisplayDto;
import com.gameaccountshop.service.GameAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final GameAccountService gameAccountService;

    public HomeController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Home page - browse listings with optional search and filter
     * GET /?search=...&rank=...
     */
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rank,
            Model model) {

        List<ListingDisplayDto> listings = gameAccountService.findApprovedListings(search, rank);
        model.addAttribute("listings", listings);
        model.addAttribute("search", search);
        model.addAttribute("rank", rank);

        return "home";
    }
}
```

### Thymeleaf Template

**Update `home.html`:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Game Account Shop - Danh sách tài khoản</title>
  <style>
    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 40px 20px;
    }

    .search-filter {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 30px;
    }

    .search-filter input,
    .search-filter select {
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      margin-right: 10px;
    }

    .search-filter button {
      padding: 10px 20px;
      background: #3498db;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .search-filter button:hover {
      background: #2980b9;
    }

    /* 3-column grid layout */
    .listings-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
    }

    /* Responsive: 1 column on mobile */
    @media (max-width: 768px) {
      .listings-grid {
        grid-template-columns: 1fr;
      }
    }

    .listing-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      background: white;
      transition: box-shadow 0.3s;
    }

    .listing-card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .listing-card .game-name {
      color: #2c3e50;
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .listing-card .rank {
      color: #7f8c8d;
      margin: 5px 0;
    }

    .listing-card .price {
      color: #27ae60;
      font-size: 24px;
      font-weight: bold;
      margin: 10px 0;
    }

    .listing-card .seller {
      color: #3498db;
      margin: 5px 0;
    }

    .listing-card .date {
      color: #95a5a6;
      font-size: 12px;
      margin: 5px 0;
    }

    .listing-card .description {
      color: #7f8c8d;
      font-size: 14px;
      margin: 10px 0;
      line-height: 1.4;
    }

    .empty-message {
      text-align: center;
      padding: 60px 20px;
      color: #7f8c8d;
      font-size: 18px;
    }
  </style>
</head>

<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="content">
    <!-- Page Title -->
    <h1 style="color: #2c3e50; margin-bottom: 20px;">Danh sách tài khoản</h1>

    <!-- Search & Filter Form at top of page -->
    <div class="search-filter">
      <form th:action="@{/}" method="get">
        <input type="text"
               name="search"
               placeholder="Tìm theo tên game..."
               th:value="${search}"
               style="width: 200px;">
        <select name="rank" style="width: 150px;">
          <option value="">Tất cả rank</option>
          <option value="Iron" th:selected="${rank == 'Iron'}">Iron</option>
          <option value="Bronze" th:selected="${rank == 'Bronze'}">Bronze</option>
          <option value="Silver" th:selected="${rank == 'Silver'}">Silver</option>
          <option value="Gold" th:selected="${rank == 'Gold'}">Gold</option>
          <option value="Platinum" th:selected="${rank == 'Platinum'}">Platinum</option>
          <option value="Emerald" th:selected="${rank == 'Emerald'}">Emerald</option>
          <option value="Diamond" th:selected="${rank == 'Diamond'}">Diamond</option>
          <option value="Master" th:selected="${rank == 'Master'}">Master</option>
          <option value="Grandmaster" th:selected="${rank == 'Grandmaster'}">Grandmaster</option>
          <option value="Challenger" th:selected="${rank == 'Challenger'}">Challenger</option>
        </select>
        <button type="submit">Tìm kiếm</button>
        <a th:href="@{/}" style="margin-left: 10px; color: #3498db;">Reset</a>
      </form>
    </div>

    <!-- Listings Grid: 3 columns, responsive to 1 on mobile -->
    <div th:if="${listings != null and !listings.empty}" class="listings-grid">
      <div th:each="listing : ${listings}" class="listing-card">
        <!-- Game Name: Always "Liên Minh Huyền Thoại" for LoL-only MVP -->
        <div class="game-name" th:text="${listing.gameName}">Liên Minh Huyền Thoại</div>

        <!-- Rank -->
        <div class="rank">
          Rank: <strong th:text="${listing.accountRank}">Gold III</strong>
        </div>

        <!-- Price: Formatted with comma separator -->
        <div class="price">
          <span th:text="${#numbers.formatInteger(listing.price, 3, 'POINT')}">500,000</span> VNĐ
        </div>

        <!-- Seller Username -->
        <div class="seller">
          Người bán: <span th:text="${listing.sellerUsername}">seller123</span>
        </div>

        <!-- Creation Date: DD/MM/YYYY format -->
        <div class="date">
          Đăng ngày: <span th:text="${#temporals.format(listing.createdAt, 'dd/MM/yyyy')}">18/01/2026</span>
        </div>

        <!-- Description Preview: First 100 characters -->
        <div class="description" th:if="${listing.description}">
          <span th:text="${#strings.substring(listing.description, 0, T(Math).min(100, #strings.length(listing.description)))}">Description preview...</span>
          <span th:if="${#strings.length(listing.description) > 100}">...</span>
        </div>

        <!-- Detail Page Link (for Story 2.3) -->
        <a th:href="@{/listings/{id}(id=${listing.id})}"
           style="display: inline-block; margin-top: 15px; padding: 10px 20px; background: #27ae60; color: white; text-decoration: none; border-radius: 4px;">
          Xem chi tiết
        </a>
      </div>
    </div>

    <!-- Empty State -->
    <div th:if="${listings == null or listings.empty}" class="empty-message">
      <h2>Không tìm thấy tài khoản nào</h2>
      <p>Thử thay đổi điều kiện tìm kiếm hoặc lọc nhé!</p>
      <a th:href="@{/}" style="color: #3498db; text-decoration: none; font-weight: bold;">Xem tất cả tài khoản</a>
    </div>
  </div>

</body>
</html>
```

### Update Navigation

**Update `layout/header.html`:**

Add "Danh sách" link to navbar:

```html
<!-- Add to navbar links section -->
<a class="nav-link" th:href="@{/}">Danh sách</a>
```

### Common Ranks for LoL (Filter Dropdown)

The full list of LoL ranks for the dropdown:

| Tier | Divisions |
|------|-----------|
| Iron | IV, III, II, I |
| Bronze | IV, III, II, I |
| Silver | IV, III, II, I |
| Gold | IV, III, II, I |
| Platinum | IV, III, II, I |
| Emerald | IV, III, II, I |
| Diamond | IV, III, II, I |
| Master | (no divisions) |
| Grandmaster | (no divisions) |
| Challenger | (no divisions) |

For MVP simplicity, filter by tier only (e.g., "Gold" matches all Gold divisions).

### Thymeleaf Utility Reference

**Price Formatting:**
```html
<!-- Formats 500000 as "500.000" (using POINT as thousands separator) -->
<th:text="${#numbers.formatInteger(listing.price, 3, 'POINT')}">
```

**Date Formatting:**
```html
<!-- Formats LocalDateTime as "18/01/2026" -->
<th:text="${#temporals.format(listing.createdAt, 'dd/MM/YYYY')}">
```

**String Truncation:**
```html
<!-- First 100 characters, add "..." if longer -->
<span th:text="${#strings.substring(listing.description, 0, T(Math).min(100, #strings.length(listing.description)))}"></span>
<span th:if="${#strings.length(listing.description) > 100}">...</span>
```

### Project Structure Notes

**Files to Modify:**
1. `HomeController.java` - Add search/filter parameters
2. `GameAccountService.java` - Add search/filter logic
3. `GameAccountRepository.java` - Add query methods with @Query
4. `home.html` - Update with listing grid and search form
5. `layout/header.html` - Add "Danh sách" link

**Files to Create:**
1. `ListingDisplayDto.java` - DTO with seller username

### References

- [Source: planning-artifacts/epics.md#Story 2.2: Browse Listings with Search/Filter]
- [Source: planning-artifacts/architecture.md#Database Schema Design]
- [Source: implementation-artifacts/2-1-create-listing-simplified.md] (previous story patterns)

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Debug Log References

### Completion Notes List

- Story requirements clarified via user interview
- User confirmed: Home page (/) with 3-column grid layout
- User confirmed: Form submission (no AJAX)
- User confirmed: Display seller username, date (DD/MM/YYYY), description preview
- User confirmed: Both navbar and home page access points
- Previous story (2.1) patterns analyzed for consistency
- ClassCastException bug from Story 2.1 noted for avoidance

### User Interview Summary

**Questions Asked & Answers:**
1. Page location: **Home page** (not separate /listings)
2. Search method: **Form submission** (simpler, no AJAX)
3. Card info: **Seller username, creation date, description preview, rank, price**
4. Access point: **Both navbar and home page**
5. Card layout: **Grid view (3 columns)**
6. Search position: **Top of page**
7. Date format: **Absolute (DD/MM/YYYY)**

### File List

### Files to Create

| Path | Description |
|------|-------------|
| `src/main/java/com/gameaccountshop/dto/ListingDisplayDto.java` | DTO with seller username for listing display |

### Files to Modify

| Path | Changes |
|------|---------|
| `src/main/java/com/gameaccountshop/controller/HomeController.java` | Add search/rank request params |
| `src/main/java/com/gameaccountshop/service/GameAccountService.java` | Return List<ListingDisplayDto> with seller |
| `src/main/java/com/gameaccountshop/repository/GameAccountRepository.java` | Add @Query methods with User join |
| `src/main/resources/templates/home.html` | Replace with listing grid + search form |
| `src/main/resources/templates/layout/header.html` | Add "Danh sách" navbar link |

### Files to Create (Tests)

| Path | Description |
|------|-------------|
| `src/test/java/com/gameaccountshop/controller/HomeControllerTest.java` | Test search/filter parameters |
| `src/test/java/com/gameaccountshop/service/GameAccountServiceSearchTest.java` | Test search/filter logic |
| `src/test/java/com/gameaccountshop/repository/GameAccountRepositorySearchTest.java` | Test query methods |
