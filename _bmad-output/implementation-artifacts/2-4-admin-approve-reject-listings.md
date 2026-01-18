# Story 2.4: Admin Approve/Reject Listings

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an **admin user**,
I want **to approve or reject pending listings**,
So that **the marketplace shows only quality listings**.

## Acceptance Criteria

**Given** I am logged in as ADMIN
**When** I navigate to the admin review page (/admin/review)
**Then** I see all listings with status = "PENDING"
**And** listings are ordered by created_at ASC (oldest first - FIFO)
**And** each pending listing displays as a card:
  - Game Name: "Liên Minh Huyền Thoại" (LOL-only MVP)
  - Rank: e.g., "Gold III", "Diamond II"
  - Price: formatted as "500,000 VNĐ"
  - Seller username (from users table)
  - Description: full text
  - Created date/time: e.g., "18/01/2026 14:30"
**And** I see an "Approve" button for each listing
**And** I see a "Reject" button with reason text input for each listing

**Given** I am NOT logged in as ADMIN
**When** I try to access /admin/review
**Then** I receive HTTP 403 Forbidden
**And** I am redirected to home page with error message

**Given** I click the "Approve" button for a listing
**When** the approval completes
**Then** the listing status is updated to "APPROVED"
**And** the approved listing is removed from the review queue
**And** a success message "Đã duyệt tài khoản" is displayed
**And** the page refreshes to show remaining pending listings

**Given** I enter a rejection reason and click "Reject" button
**When** the rejection completes
**Then** the listing status is updated to "REJECTED"
**And** the rejection reason is stored in rejection_reason column
**And** the rejected listing is removed from the review queue
**And** a success message "Đã từ chối tài khoản" is displayed
**And** the page refreshes to show remaining pending listings

**Given** there are no pending listings
**When** I navigate to the admin review page
**Then** a message "Không có listing nào chờ duyệt" is displayed

**Technical Notes:**
- Separate admin page: /admin/review
- ADMIN role only (strict - 403 if not ADMIN)
- Approval: UPDATE game_accounts SET status = 'APPROVED' WHERE id = :id
- Rejection: UPDATE game_accounts SET status = 'REJECTED', rejection_reason = :reason WHERE id = :id
- Order: created_at ASC (oldest first - FIFO)
- Reason input: text input field next to reject button
- Post-action: Stay on page, refresh list

## Tasks / Subtasks

- [ ] Create AdminController with review endpoint (AC: #1, #2)
  - [ ] @GetMapping /admin/review - show pending listings
  - [ ] Add @PreAuthorize("hasRole('ADMIN')") for security
  - [ ] Order by created_at ASC (oldest first)
  - [ ] Pass listings to template

- [ ] Create approval endpoint (AC: #3, #4, #5, #6)
  - [ ] @PostMapping /admin/review/{id}/approve
  - [ ] Update status to APPROVED
  - [ ] Add success message "Đã duyệt tài khoản"
  - [ ] Redirect back to /admin/review

- [ ] Create rejection endpoint (AC: #7, #8, #9, #10)
  - [ ] @PostMapping /admin/review/{id}/reject
  - [ ] Accept @RequestParam String reason
  - [ ] Update status to REJECTED
  - [ ] Store rejection_reason
  - [ ] Add success message "Đã từ chối tài khoản"
  - [ ] Redirect back to /admin/review

- [ ] Add repository query methods (AC: #1)
  - [ ] findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING)
  - [ ] Or: findByStatusOrderByCreatedAtAsc(String status)

- [ ] Add service methods (AC: #5, #9)
  - [ ] approveListing(Long id) - update status to APPROVED
  - [ ] rejectListing(Long id, String reason) - update status to REJECTED with reason
  - [ ] Add @Slf4j logging for admin actions

- [ ] Create admin review Thymeleaf template (AC: #1, #2)
  - [ ] Create templates/admin/review.html
  - [ ] Display listing cards with all required fields
  - [ ] Add Approve button (green)
  - [ ] Add Reject button (red) + reason text input
  - [ ] Use card layout for each listing

- [ ] Add access control (AC: #3, #4)
  - [ ] Use @PreAuthorize("hasRole('ADMIN')") on controller methods
  - [ ] OR configure SecurityConfig for /admin/** paths
  - [ ] Return 403 or redirect if not ADMIN

- [ ] Handle empty state (AC: #11)
  - [ ] Check if listings list is empty
  - [ ] Display "Không có listing nào chờ duyệt"
  - [ ] Keep page layout consistent

## Dev Notes

### User Requirements Summary (From Interview)

**Access Control:**
- ADMIN role only (strict - 403 if not ADMIN)
- Non-ADMIN users get forbidden

**Page Design:**
- Location: Separate admin page (/admin/review)
- Not part of dashboard (standalone page)

**Listing Display:**
- Game Name: "Liên Minh Huyền Thoại" (LOL-only MVP)
- Rank: e.g., "Gold III"
- Price: formatted as "500,000 VNĐ"
- Seller username (from users table join)
- Description: full text
- Creation date/time

**Approval Method:**
- Approve button for each listing
- Reject button + reason text input on same page
- Separate buttons, not dropdown

**List Order:**
- Oldest PENDING listings first (FIFO - created_at ASC)

**Post-Action:**
- Stay on review page, refresh list
- Removed processed listings from view

**Empty State:**
- Vietnamese message: "Không có listing nào chờ duyệt"

**Success Messages:**
- Approve: "Đã duyệt tài khoản"
- Reject: "Đã từ chối tài khoản"

### Previous Story Intelligence (Story 2.1)

**Database Schema from Story 2.1:**

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | Primary key |
| `seller_id` | BIGINT | FK to users.id |
| `game_name` | VARCHAR(100) | DEFAULT "Liên Minh Huyền Thoại" |
| `account_rank` | VARCHAR(50) | Maps to entity field `accountRank` |
| `price` | BIGINT | NOT NULL, CHECK > 2000 |
| `description` | TEXT | Full description |
| `status` | ENUM | PENDING, APPROVED, REJECTED, SOLD |
| `rejection_reason` | VARCHAR(500) | For rejected listings |
| `created_at` | TIMESTAMP | Auto-set |
| `sold_at` | TIMESTAMP | NULL until sold |

**Status Flow:**
```
PENDING (new listing from Story 2.1)
    ↓ (admin approves)
APPROVED (visible in Story 2.2)
    ↓ (purchased in Story 3.1)
SOLD (Story 2.5)

OR

PENDING
    ↓ (admin rejects)
REJECTED (with reason stored)
```

**Code Patterns from Story 2.1:**
- Entity: `GameAccount` with `@Column(name = "account_rank")` mapping
- Repository: `GameAccountRepository` extends `JpaRepository`
- Service: `GameAccountService` with `@Slf4j` logging
- Template: Thymeleaf fragment pattern `layout/header :: navbar`

**Critical Bug from Story 2.1:**
- ClassCastException: `User user = (User) authentication.getPrincipal();` was WRONG
- Correct: Use `authentication.getName()` + `UserRepository.findByUsername()`
- This story NEEDS auth (ADMIN only), so use the correct pattern

### Security Configuration

**Update SecurityConfig.java:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/").permitAll()
                .requestMatchers("/listing/create").hasRole("USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")  // NEW: Admin-only
                .anyRequest().authenticated()
            )
            // ... rest of config
    }
}
```

**Or use @PreAuthorize on controller:**

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/review")
public String reviewListings(Model model) {
    // ...
}
```

### Repository Query Methods

**Add to `GameAccountRepository.java`:**

```java
@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    // Already exists from Story 2.1
    List<GameAccount> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    // NEW: Find pending listings (oldest first for FIFO)
    List<GameAccount> findByStatusOrderByCreatedAtAsc(ListingStatus status);

    // Alternative: Query method
    @Query("SELECT g FROM GameAccount g WHERE g.status = :status ORDER BY g.createdAt ASC")
    List<GameAccount> findPendingListings(@Param("status") ListingStatus status);
}
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
     * Find all pending listings (oldest first - FIFO)
     */
    public List<GameAccount> findPendingListings() {
        log.info("Finding pending listings for admin review");
        return gameAccountRepository.findByStatusOrderByCreatedAtAsc(ListingStatus.PENDING);
    }

    /**
     * Approve a listing
     */
    @Transactional
    public void approveListing(Long id) {
        GameAccount listing = gameAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.PENDING) {
            throw new BusinessException("Only PENDING listings can be approved");
        }

        listing.setStatus(ListingStatus.APPROVED);
        gameAccountRepository.save(listing);

        log.info("Admin approved listing: id={}", id);
    }

    /**
     * Reject a listing with reason
     */
    @Transactional
    public void rejectListing(Long id, String reason) {
        GameAccount listing = gameAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.PENDING) {
            throw new BusinessException("Only PENDING listings can be rejected");
        }

        listing.setStatus(ListingStatus.REJECTED);
        listing.setRejectionReason(reason);
        gameAccountRepository.save(listing);

        log.info("Admin rejected listing: id={}, reason={}", id, reason);
    }
}
```

### Controller

**Create `AdminController.java`:**

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.service.GameAccountService;
import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GameAccountService gameAccountService;

    public AdminController(GameAccountService gameAccountService) {
        this.gameAccountService = gameAccountService;
    }

    /**
     * Admin review page - show all pending listings
     * GET /admin/review
     */
    @GetMapping("/review")
    public String reviewListings(Model model) {
        List<GameAccount> pendingListings = gameAccountService.findPendingListings();
        model.addAttribute("listings", pendingListings);
        return "admin/review";
    }

    /**
     * Approve a listing
     * POST /admin/review/{id}/approve
     */
    @PostMapping("/review/{id}/approve")
    public String approveListing(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            gameAccountService.approveListing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt tài khoản");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi duyệt tài khoản");
        }
        return "redirect:/admin/review";
    }

    /**
     * Reject a listing
     * POST /admin/review/{id}/reject
     */
    @PostMapping("/review/{id}/reject")
    public String rejectListing(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            gameAccountService.rejectListing(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối tài khoản");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi từ chối tài khoản");
        }
        return "redirect:/admin/review";
    }
}
```

### Thymeleaf Template

**Create `templates/admin/review.html`:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Admin Review - Game Account Shop</title>
  <style>
    .content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 40px 20px;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .page-header h1 {
      color: #2c3e50;
      margin: 0;
    }

    .pending-count {
      background: #3498db;
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
    }

    .listings-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .listing-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      background: white;
      border-left: 4px solid #f39c12; /* Orange for pending */
    }

    .listing-card .header {
      display: flex;
      justify-content: space-between;
      align-items: start;
      margin-bottom: 15px;
    }

    .listing-card .game-name {
      font-size: 18px;
      font-weight: bold;
      color: #2c3e50;
    }

    .listing-card .info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 10px;
      margin-bottom: 15px;
    }

    .listing-card .info-item {
      color: #7f8c8d;
    }

    .listing-card .info-item strong {
      color: #2c3e50;
    }

    .listing-card .description {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      margin: 15px 0;
      color: #555;
      white-space: pre-wrap;
    }

    .listing-card .actions {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .listing-card .actions input[type="text"] {
      flex: 1;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .btn-approve {
      padding: 10px 20px;
      background: #27ae60;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-approve:hover {
      background: #229954;
    }

    .btn-reject {
      padding: 10px 20px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-reject:hover {
      background: #c0392b;
    }

    .empty-message {
      text-align: center;
      padding: 60px 20px;
      color: #7f8c8d;
      font-size: 18px;
    }

    .success {
      background: #d4edda;
      color: #155724;
      padding: 15px 20px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .error {
      background: #f8d7da;
      color: #721c24;
      padding: 15px 20px;
      border-radius: 8px;
      margin-bottom: 20px;
    }
  </style>
</head>

<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="content">
    <!-- Flash Messages -->
    <div th:if="${successMessage}" class="success">
      <span th:text="${successMessage}">Success message</span>
    </div>

    <div th:if="${errorMessage}" class="error">
      <span th:text="${errorMessage}">Error message</span>
    </div>

    <!-- Page Header -->
    <div class="page-header">
      <h1>Duyệt tài khoản</h1>
      <div class="pending-count">
        <span th:text="${listings != null ? listings.size() : 0}">0</span> chờ duyệt
      </div>
    </div>

    <!-- Pending Listings -->
    <div th:if="${listings != null and !listings.empty}" class="listings-list">
      <div th:each="listing : ${listings}" class="listing-card">
        <div class="header">
          <div class="game-name" th:text="${listing.gameName}">Liên Minh Huyền Thoại</div>
          <div style="color: #f39c12; font-size: 12px;">⏳ CHỜ DUYỆT</div>
        </div>

        <div class="info">
          <div class="info-item">
            Rank: <strong th:text="${listing.accountRank}">Gold III</strong>
          </div>
          <div class="info-item">
            Giá: <strong th:text="${#numbers.formatInteger(listing.price, 3, 'POINT')} + ' VNĐ'">500,000 VNĐ</strong>
          </div>
          <div class="info-item">
            Người bán: <strong th:text="${listing.sellerId}">seller123</strong>
            <!-- TODO: Add seller username lookup -->
          </div>
          <div class="info-item">
            Ngày đăng: <strong th:text="${#temporals.format(listing.createdAt, 'dd/MM/yyyy HH:mm')}">18/01/2026 14:30</strong>
          </div>
        </div>

        <div class="description" th:if="${listing.description}">
          <strong>Mô tả:</strong>
          <span th:text="${listing.description}">Full description here...</span>
        </div>

        <form th:action="@{/admin/review/{id}/reject(id=${listing.id})}" method="post" class="actions">
          <input type="text"
                 name="reason"
                 placeholder="Lý do từ chối..."
                 required>
          <button type="submit" class="btn-reject">Từ chối</button>
        </form>

        <form th:action="@{/admin/review/{id}/approve(id=${listing.id})}" method="post" class="actions" style="margin-top: 10px;">
          <button type="submit" class="btn-approve">Duyệt</button>
        </form>
      </div>
    </div>

    <!-- Empty State -->
    <div th:if="${listings == null or listings.empty}" class="empty-message">
      <h2>Không có listing nào chờ duyệt</h2>
      <p>Tất cả các tài khoản đã được xử lý!</p>
    </div>
  </div>

</body>
</html>
```

### Access Control Implementation

**Option 1: @PreAuthorize annotation (Recommended)**

Add to controller methods:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/review")
public String reviewListings(Model model) {
    // ...
}
```

Enable in SecurityConfig:
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // ...
}
```

**Option 2: URL-based security**

In SecurityConfig:
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    // ...
);
```

### Project Structure Notes

**Files to Create:**
1. `AdminController.java` - New controller for admin operations
2. `templates/admin/review.html` - Admin review page

**Files to Modify:**
1. `GameAccountService.java` - Add approve/reject methods
2. `GameAccountRepository.java` - Add findByStatusOrderByCreatedAtAsc
3. `SecurityConfig.java` - Add /admin/** security rules

**Package Structure:**
```
com.gameaccountshop/
├── controller/
│   ├── AdminController.java     (NEW)
│   ├── HomeController.java
│   └── ListingController.java
├── service/
│   └── GameAccountService.java   (MODIFY)
└── repository/
    └── GameAccountRepository.java (MODIFY)
```

### Database Queries

**Approve:**
```sql
UPDATE game_accounts
SET status = 'APPROVED'
WHERE id = :id AND status = 'PENDING';
```

**Reject:**
```sql
UPDATE game_accounts
SET status = 'REJECTED',
    rejection_reason = :reason
WHERE id = :id AND status = 'PENDING';
```

**Find Pending (Oldest First):**
```sql
SELECT * FROM game_accounts
WHERE status = 'PENDING'
ORDER BY created_at ASC;
```

### Testing Notes

**Test Cases:**
1. ADMIN can access /admin/review
2. USER gets 403 when accessing /admin/review
3. Pending listings show in oldest-first order
4. Approve button updates status to APPROVED
5. Reject button updates status to REJECTED with reason
6. Approved/rejected listings removed from queue
7. Success messages display correctly
8. Empty state shows when no pending listings

### References

- [Source: planning-artifacts/epics.md#Story 2.4: Admin Approve/Reject Listings]
- [Source: planning-artifacts/architecture.md#Security Requirements]
- [Source: implementation-artifacts/2-1-create-listing-simplified.md] (previous story patterns)

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Debug Log References

### Completion Notes List

- Story 2.4 requirements clarified via user interview
- User confirmed: ADMIN only access (strict 403)
- User confirmed: Separate admin page (/admin/review)
- User confirmed: Approve + Reject buttons with reason input on page
- User confirmed: Show seller, description, date, rank, price (LOL)
- User confirmed: Oldest first (FIFO) ordering
- User confirmed: Stay on page after action
- User confirmed: Vietnamese messages
- This story MUST be completed before Story 2.2 (Browse Listings)

### User Interview Summary

**Questions Asked & Answers:**
1. Admin access: **ADMIN only (strict 403)**
2. Page location: **Separate admin page**
3. Approval method: **Approve + Reject buttons**
4. Rejection flow: **Reason input on page**
5. Listing display: **Seller, description, date, rank, price (LOL)**
6. Post-action: **Stay, refresh list**
7. List order: **Oldest first (FIFO)**
8. Empty state: **Vietnamese message**
9. Success messages: **Vietnamese**

### File List

### Files to Create

| Path | Description |
|------|-------------|
| `src/main/java/com/gameaccountshop/controller/AdminController.java` | Admin controller with review endpoints |
| `src/main/resources/templates/admin/review.html` | Admin review page template |
| `src/test/java/com/gameaccountshop/controller/AdminControllerTest.java` | Admin controller tests |
| `src/test/java/com/gameaccountshop/service/GameAccountServiceAdminTest.java` | Admin service tests |

### Files to Modify

| Path | Changes |
|------|---------|
| `src/main/java/com/gameaccountshop/service/GameAccountService.java` | Add approve/reject methods |
| `src/main/java/com/gameaccountshop/repository/GameAccountRepository.java` | Add findByStatusOrderByCreatedAtAsc |
| `src/main/java/com/gameaccountshop/config/SecurityConfig.java` | Add /admin/** security rules |
| `src/main/resources/templates/layout/header.html` | Add "Admin Review" link for ADMIN users |

### Dependencies

**Prerequisite Stories:**
- Story 2.1 (Create Listing) - Creates PENDING listings
- Story 1.3 (Default Admin) - Creates admin account

**Enables Stories:**
- Story 2.2 (Browse Listings) - Needs APPROVED listings to display
- Story 2.3 (Listing Details) - Shows approved listings
- Story 2.5 (Mark Sold) - Operates on APPROVED listings
