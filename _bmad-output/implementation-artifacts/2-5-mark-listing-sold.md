# Story 2.5: Mark Listing as Sold

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an **admin or system**,
I want **to mark a listing as sold after purchase**,
So that **buyers know it's no longer available**.

## Acceptance Criteria

**Given** A buyer has purchased a listing
**When** the transaction is completed
**Then** the listing status is updated to "SOLD"
**And** the sold_at timestamp is set
**And** the listing no longer appears in browse results
**And** the detail page shows "Đã bán" badge

**Given** I am viewing the listing detail page
**When** the listing status is "SOLD"
**Then** the "Mua ngay" button is disabled
**And** a visible "ĐÃ BÁN" badge is displayed
**And** the seller username is still visible

**Technical Notes:**
- Simple status update: `UPDATE game_accounts SET status = 'SOLD', sold_at = NOW() WHERE id = :id`
- Add CSS badge with red/orange background for visual emphasis
- SOLD listings should already be excluded from browse (Story 2.2 only shows APPROVED)
- Detail page already has SOLD badge UI from Story 2.3

## Tasks / Subtasks

- [x] Add markAsSold service method (AC: #1, #2)
  - [x] Create markAsSold(Long id) in GameAccountService
  - [x] Validate listing exists and is APPROVED before marking SOLD
  - [x] Set status to SOLD and sold_at to current timestamp
  - [x] Add @Slf4j logging for the action

- [x] Add admin endpoint to mark listing as sold (AC: #1, #2)
  - [x] POST /admin/listings/{id}/mark-sold
  - [x] Add @PreAuthorize("hasRole('ADMIN')")
  - [x] Call service.markAsSold(id)
  - [x] Redirect to listing detail page with success message
  - [x] Handle errors (listing not found, not APPROVED)

- [x] Verify SOLD listings excluded from browse (AC: #3)
  - [x] Confirm findApprovedListings() only returns APPROVED status
  - [x] SOLD status listings should not appear in /browse results

- [x] Verify detail page SOLD badge displays (AC: #4, #5, #6)
  - [x] Confirm listing.isSold() returns true for SOLD status
  - [x] Confirm "ĐÃ BÁN" badge displays correctly
  - [x] Confirm "Mua ngay" button is disabled
  - [x] Confirm seller information still visible

- [x] Add tests for markAsSold functionality
  - [x] Test markAsSold updates status to SOLD
  - [x] Test markAsSold sets sold_at timestamp
  - [x] Test markAsSold throws error for non-APPROVED listings
  - [x] Test markAsSold throws error for non-existent listings
  - [x] Test admin endpoint for successful mark-as-sold
  - [x] Test admin endpoint error handling

## Dev Notes

### User Requirements Summary

**Trigger:**
- Admin manually marks listing as sold after purchase completion
- This is a manual admin action (not automatic payment trigger - that's Story 3.x)

**Business Logic:**
- Only APPROVED listings can be marked as SOLD
- Once SOLD, status cannot be changed back (one-way transition)
- sold_at timestamp records when the sale occurred

**UI Impact:**
- Browse listings (Story 2.2): Already excludes SOLD (only shows APPROVED)
- Detail page (Story 2.3): Already has SOLD badge UI implemented
- Admin interface: Need to add "Mark as Sold" button/action

### Previous Story Intelligence (Story 2.4: Admin Approve/Reject)

**Database Schema from Story 2.1:**

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | Primary key |
| `status` | ENUM | PENDING, APPROVED, REJECTED, SOLD |
| `sold_at` | TIMESTAMP | NULL until sold |
| `rejection_reason` | VARCHAR(500) | For rejected listings |

**Status Flow:**
```
PENDING (new listing)
    ↓ (admin approves - Story 2.4)
APPROVED (visible in browse)
    ↓ (purchase complete - Story 2.5)
SOLD (no longer available)
```

**Code Patterns from Story 2.4:**
- Service methods use @Transactional for status updates
- @Slf4j logging for admin actions
- ResourceNotFoundException for missing resources
- IllegalArgumentException for business logic violations
- RedirectAttributes for flash messages
- @PreAuthorize("hasRole('ADMIN')") for admin endpoints

**AdminController Pattern:**
```java
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final GameAccountService gameAccountService;

    @PostMapping("/review/{id}/approve")
    public String approveListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            gameAccountService.approveListing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt tài khoản");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/review";
    }
}
```

### Previous Story Intelligence (Story 2.3: Listing Details)

**Detail Page UI Already Implemented:**
- ListingDetailDto has `isSold()` method (returns status == SOLD)
- Status badge displays "ĐANG BÁN" for APPROVED, "ĐÃ BÁN" for SOLD
- "Mua ngay" button disabled when listing.isSold() == true
- Sold message: "⚠️ Tài khoản này đã được bán"

**ListingDetailDto Structure:**
```java
public record ListingDetailDto(
    Long id,
    String gameName,
    String accountRank,
    Long price,
    String description,
    ListingStatus status,  // PENDING, APPROVED, REJECTED, SOLD
    LocalDateTime createdAt,
    LocalDateTime soldAt,
    Long sellerId,
    String sellerUsername,
    String sellerEmail
) {
    public boolean isSold() {
        return status == ListingStatus.SOLD;
    }

    public boolean isApproved() {
        return status == ListingStatus.APPROVED;
    }
}
```

### Previous Story Intelligence (Story 2.2: Browse Listings)

**Browse Already Excludes SOLD:**
- `findApprovedListings()` only returns APPROVED status
- SOLD listings automatically excluded from browse page
- No changes needed to browse logic

### Service Layer Implementation

**Add to `GameAccountService.java`:**

```java
/**
 * Mark a listing as sold
 * Story 2.5: Mark Listing as Sold
 *
 * @param id Listing ID
 * @throws ResourceNotFoundException if listing not found
 * @throws IllegalArgumentException if listing is not APPROVED
 */
@Transactional
public void markAsSold(Long id) {
    log.info("Admin marking listing as sold: id={}", id);

    GameAccount listing = gameAccountRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Listing not found for mark-as-sold: id={}", id);
                return new ResourceNotFoundException("Không tìm thấy tài khoản này");
            });

    if (listing.getStatus() != ListingStatus.APPROVED) {
        log.warn("Cannot mark listing with status {} as SOLD: id={}", listing.getStatus(), id);
        throw new IllegalArgumentException("Chỉ có thể đánh dấu bán cho tài khoản đang đăng bán (APPROVED)");
    }

    listing.setStatus(ListingStatus.SOLD);
    listing.setSoldAt(LocalDateTime.now());
    gameAccountRepository.save(listing);

    log.info("Admin marked listing as sold: id={}, soldAt={}", id, listing.getSoldAt());
}
```

### Controller Implementation

**Update `AdminController.java`:**

```java
/**
 * Mark a listing as sold
 * POST /admin/listings/{id}/mark-sold
 * Updates listing status to SOLD and redirects to detail page
 */
@PostMapping("/listings/{id}/mark-sold")
public String markAsSold(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {
    log.info("Admin marking listing as sold: id={}", id);
    try {
        gameAccountService.markAsSold(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu tài khoản đã bán");
    } catch (ResourceNotFoundException e) {
        log.warn("Listing not found for mark-as-sold: id={}", id);
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (IllegalArgumentException e) {
        log.warn("Invalid mark-as-sold attempt: id={}, error={}", id, e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
        log.error("Error marking listing as sold: id={}", id, e);
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đánh dấu đã bán");
    }
    return "redirect:/listings/" + id;
}
```

### Admin UI Option (Optional)

**Option A: Add button to admin review page**
- Add "Đã bán" button to pending listings review page
- But this doesn't make sense - only APPROVED listings should be marked sold

**Option B: Add button to listing detail page (for ADMIN only)**
- Add "Mark as Sold" button on detail page if user is ADMIN and listing is APPROVED
- This allows admin to mark listings sold directly from the detail view

**Option C: Separate admin sold listings page**
- /admin/sold - view all sold listings
- /admin/approved - view all approved listings with "Mark as Sold" action

**Recommendation:** Option B - Add ADMIN-only button to detail page
```html
<!-- In listing-detail.html, inside the detail-card -->
<div th:if="${#authorization.expression('hasRole(''ADMIN'')') and listing.isApproved()}"
     style="margin-top: 20px; padding: 15px; background: #fff3cd; border-radius: 6px;">
    <form th:action="@{/admin/listings/{id}/mark-sold(id=${listing.id})}" method="post">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <button type="submit" style="padding: 10px 20px; background: #e67e22; color: white; border: none; border-radius: 4px; cursor: pointer;">
            🏷️ Đánh dấu đã bán (Admin)
        </button>
    </form>
</div>
```

### Database Query

**Mark as Sold:**
```sql
UPDATE game_accounts
SET status = 'SOLD',
    sold_at = NOW()
WHERE id = :id
  AND status = 'APPROVED';
```

**Browse Listings (already excludes SOLD):**
```sql
-- From Story 2.2 - only returns APPROVED
SELECT * FROM game_accounts
WHERE status = 'APPROVED'
ORDER BY created_at DESC;
```

### Testing Notes

**Test Cases:**
1. markAsSold updates status from APPROVED to SOLD
2. markAsSold sets sold_at timestamp to current time
3. markAsSold throws error for PENDING listings
4. markAsSold throws error for REJECTED listings
5. markAsSold throws error for already SOLD listings
6. markAsSold throws error for non-existent listings
7. Admin endpoint successfully marks listing as sold and redirects
8. Admin endpoint shows error message for invalid operations
9. SOLD listings do not appear in browse results
10. Detail page shows "ĐÃ BÁN" badge for SOLD listings
11. Detail page disables "Mua ngay" button for SOLD listings

**Service Test Example:**
```java
@Test
void markAsSold_ValidApprovedListing_UpdatesStatusToSold() {
    // Given
    Long listingId = 1L;
    GameAccount approvedListing = new GameAccount();
    approvedListing.setId(listingId);
    approvedListing.setStatus(ListingStatus.APPROVED);

    when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(approvedListing));
    when(gameAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    gameAccountService.markAsSold(listingId);

    // Then
    assertEquals(ListingStatus.SOLD, approvedListing.getStatus());
    assertNotNull(approvedListing.getSoldAt());
    verify(gameAccountRepository).save(approvedListing);
}

@Test
void markAsSold_PendingListing_ThrowsIllegalArgumentException() {
    // Given
    Long listingId = 1L;
    GameAccount pendingListing = new GameAccount();
    pendingListing.setId(listingId);
    pendingListing.setStatus(ListingStatus.PENDING);

    when(gameAccountRepository.findById(listingId)).thenReturn(Optional.of(pendingListing));

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
        gameAccountService.markAsSold(listingId);
    });
    assertEquals(ListingStatus.PENDING, pendingListing.getStatus()); // Status unchanged
    assertNull(pendingListing.getSoldAt());
}
```

### Security Configuration

**Current SecurityConfig (from Story 2.4):**
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

**New endpoint will be protected:**
- POST /admin/listings/{id}/mark-sold
- Covered by existing `/admin/**` rule
- No changes needed to SecurityConfig

### Project Structure Notes

**Files to Create:**
None - using existing files

**Files to Modify:**
1. `GameAccountService.java` - Add markAsSold() method
2. `AdminController.java` - Add markAs-sold endpoint
3. `listing-detail.html` - Add ADMIN-only "Mark as Sold" button (optional)
4. `GameAccountServiceTest.java` - Add markAsSold tests
5. `AdminControllerTest.java` - Add mark-as-sold endpoint tests

**Package Structure:**
```
com.gameaccountshop/
├── controller/
│   ├── AdminController.java      (MODIFY - add markAsSold endpoint)
│   └── ListingController.java
├── service/
│   └── GameAccountService.java    (MODIFY - add markAsSold method)
└── repository/
    └── GameAccountRepository.java (no changes needed)
```

### References

- [Source: planning-artifacts/epics.md#Story 2.5: Mark Listing as Sold]
- [Source: planning-artifacts/architecture.md#Security Requirements]
- [Source: implementation-artifacts/2-4-admin-approve-reject-listings.md] (admin patterns)
- [Source: implementation-artifacts/2-3-listing-details-rating.md] (SOLD badge UI)
- [Source: implementation-artifacts/2-2-browse-listings-search-filter.md] (browse exclusions)

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Debug Log References

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created
- Story 2.5 focuses on simple status update (APPROVED → SOLD)
- UI for SOLD badge already implemented in Story 2.3
- Browse exclusion already handled in Story 2.2
- Primary work: add service method and admin endpoint

**Implementation Summary (2026-01-19):**
- Added markAsSold(Long id) method to GameAccountService
  - Validates listing exists and is APPROVED before marking SOLD
  - Sets status to SOLD and sold_at to current timestamp
  - Uses @Transactional for data consistency
  - @Slf4j logging for admin actions
- Added POST /admin/listings/{id}/mark-sold endpoint to AdminController
  - @PreAuthorize("hasRole('ADMIN')") at class level
  - Redirects to listing detail page with success/error messages
  - Handles ResourceNotFoundException and IllegalArgumentException
- Verified SOLD listings excluded from browse (findApprovedListings only returns APPROVED status)
- Verified detail page SOLD badge displays correctly (ListingDetailDto.isSold() method)
- Added ADMIN-only "Mark as Sold" button to listing-detail.html
  - Only visible to ADMIN users when listing is APPROVED
  - Includes CSRF token for security
  - Orange styling to distinguish from buy button
- All 88 tests passing (5 new markAsSold service tests, 3 new admin endpoint tests)

### File List

### Files to Create

| Path | Description |
|------|-------------|
| (None - using existing files) | |

### Files to Modify

| Path | Changes |
|------|---------|
| `src/main/java/com/gameaccountshop/service/GameAccountService.java` | Add markAsSold(Long id) method |
| `src/main/java/com/gameaccountshop/controller/AdminController.java` | Add POST /admin/listings/{id}/mark-sold endpoint |
| `src/main/resources/templates/listing-detail.html` | Add optional ADMIN-only "Mark as Sold" button |
| `src/test/java/com/gameaccountshop/service/GameAccountServiceTest.java` | Add markAsSold tests |
| `src/test/java/com/gameaccountshop/controller/AdminControllerTest.java` | Add mark-as-sold endpoint tests |

### Dependencies

**Prerequisite Stories:**
- Story 2.1 (Create Listing) - Creates APPROVED listings
- Story 2.2 (Browse Listings) - Already excludes SOLD from browse
- Story 2.3 (Listing Details) - Already has SOLD badge UI
- Story 2.4 (Admin Approve/Reject) - Creates admin operation patterns

**Enables Stories:**
- Story 3.1 (Buyer Click Buy) - Will trigger mark-as-sold after payment
- Story 3.3 (Admin Verify) - Manual admin mark-as-sold action
