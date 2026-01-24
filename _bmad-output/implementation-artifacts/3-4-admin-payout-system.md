# Story 3.4: Admin Payout System

Status: done

## Story

As an **admin**,
I want **to manage monthly payouts to sellers with status tracking (NEEDS_PAYMENT → PAID → RECEIVED)**,
So that **sellers receive their earnings and both parties can track payout status**.

## Scope

**IMPORTANT:** This is a **monthly bulk payout system** with **3-way status tracking**.

**Payout Status Flow:**
```
NEEDS_PAYMENT → PAID → RECEIVED
     ↓              ↓         ↓
  Auto-create    Admin     Seller
  (1st of month)  clicks   clicks
                  button    button
```

**Workflow:**
1. **1st of month (auto):** System creates NEEDS_PAYMENT records for sellers with unpaid earnings
2. **Admin transfers:** Admin clicks "Mark as Paid" after bank transfer → PAID (seller gets email)
3. **Seller receives:** Seller clicks "Received Payment" → RECEIVED (admin gets email, profit reset)
4. **5-day window:** Informational reminder in email text, no enforcement

## Acceptance Criteria

### Admin Side

**Given** I am logged in as ADMIN
**When** I navigate to the admin payout page (GET /admin/payouts)
**Then** I see a list of ALL payouts grouped by status
**And** each payout entry displays:
  - Seller username and email
  - Amount (90% net earnings)
  - Status badge: "Chờ thanh toán" / "Đã chuyển" / "Đã nhận"
  - Action button based on status:
    - NEEDS_PAYMENT: "Mark as Paid" button
    - PAID: (waiting for seller)
    - RECEIVED: ✓ Completed

**Given** I click "Mark as Paid" for a NEEDS_PAYMENT payout
**When** the confirmation dialog appears
**Then** I see:
  - Seller username and email
  - Amount to transfer
  - Confirmation: "Have you completed the bank transfer?"
**And** I must confirm to proceed

**Given** I confirm the payout
**When** the payout is processed
**Then** the payout status changes from NEEDS_PAYMENT to PAID
**And** paid_at timestamp is set
**And** admin_id is saved
**And** an email is sent to seller with payout details
**And** a success message is displayed

**Given** there are no payouts in any status
**When** I navigate to the admin payout page
**Then** a message "Không có giao dịch thanh toán nào" is displayed

### Seller Side

**Given** I am logged in as a seller (ROLE_USER)
**When** I view the "My Listings" page
**Then** I see my profit section (from Story 3.3)
**And** if I have payouts with status PAID, I see a "Received Payment" button next to profit
**And** the button shows the amount waiting to be received

**Given** I click "Received Payment"
**When** the confirmation dialog appears
**Then** I see:
  - Amount I will confirm as received
  - Warning: "Only click if you have received the money in your bank account"
**And** I must confirm to proceed

**Given** I confirm receipt
**When** the confirmation is processed
**Then** the payout status changes from PAID to RECEIVED
**And** received_at timestamp is set
**And** my profit is recalculated (subtracting received amount)
**And** an email is sent to admin
**And** the "Received Payment" button disappears

**Given** I have no PAID payouts
**When** I view the "My Listings" page
**Then** I do NOT see any "Received Payment" button

### Email Notifications

**Given** Admin marks a payout as PAID
**When** the email is sent to seller
**Then** the email contains:
  - Subject: "💰 Thanh toán tiền bán tài khoản - Chờ nhận tiền"
  - Payout amount
  - Message: "Chúng tôi đã chuyển khoản..."
  - 5-day reminder: "Vui lòng kiểm tra trong vòng 5 ngày và nhấn 'Received Payment' trên website"
  - Contact email for issues

**Given** Seller marks payout as RECEIVED
**When** the email is sent to admin
**Then** the email contains:
  - Subject: "✓ Người bán đã nhận tiền - {Seller Username}"
  - Seller username
  - Payout amount
  - Confirmation that seller has received the money

### Automated Payout Creation

**Given** Today is the 1st of the month
**When** the scheduled task runs
**Then** NEEDS_PAYMENT payout records are created for all sellers with unpaid earnings
**And** duplicates are avoided (only create if no NEEDS_PAYMENT exists for seller this month)

## Tasks / Subtasks

### Phase 1: Database Layer

- [ ] Update PayoutStatus enum (AC: All)
  - [ ] NEEDS_PAYMENT, PAID, RECEIVED values
  - [ ] Update database table for new status

- [ ] Update Payout entity (AC: All)
  - [ ] Add received_at TIMESTAMP column
  - [ ] Keep existing fields: id, seller_id, amount, status, paid_at, created_at, admin_id
  - [ ] Add indexes on status and created_at

- [ ] Create PayoutRepository (AC: All)
  - [ ] extends JpaRepository<Payout, Long>
  - [ ] findBySellerIdAndStatus(Long sellerId, PayoutStatus status)
  - [ ] findByStatusOrderByCreatedAtDesc(PayoutStatus status)
  - [ ] findBySellerId(Long sellerId)
  - [ ] sumAmountBySellerIdAndStatus(Long sellerId, PayoutStatus status)
  - [ ] existsBySellerIdAndStatusAndMonth(Long sellerId, PayoutStatus status, int month, int year)

### Phase 2: Service Layer

- [ ] Create PayoutService (AC: All)
  - [ ] createMonthlyPayouts() - scheduled task, creates NEEDS_PAYMENT records
  - [ ] getPendingPayouts() - returns NEEDS_PAYMENT payouts for admin
  - [ ] getPaidPayouts() - returns PAID payouts waiting for seller confirmation
  - [ ] getReceivedPayouts() - returns RECEIVED payouts
  - [ ] calculateUnpaidEarnings(Long sellerId) - sum of 90% minus RECEIVED amounts
  - [ ] markAsPaid(Long payoutId, Long adminId) - NEEDS_PAYMENT → PAID, sends email
  - [ ] markAsReceived(Long payoutId, Long sellerId) - PAID → RECEIVED, sends email
  - [ ] getSellersPendingPayment() - sellers with NEEDS_PAYMENT status
  - [ ] getPendingPayoutsForSeller(Long sellerId) - PAID payouts for seller's button

- [ ] Update GameAccountService (AC: Profit Calculation)
  - [ ] Modify calculateProfit() to exclude RECEIVED (not PAID) payouts

- [ ] Update EmailService (AC: Email Notifications)
  - [ ] sendPayoutPaidEmail(String email, BigDecimal amount, String payoutId)
  - [ ] sendPayoutReceivedEmail(String adminEmail, String sellerUsername, BigDecimal amount)

- [ ] Create DTOs (AC: Architecture)
  - [ ] AdminPayoutDto - id, sellerId, username, email, amount, status, createdAt, paidAt
  - [ ] SellerPayoutSummaryDto - totalPendingAmount, pendingCount

### Phase 3: Controller Layer

- [ ] Update AdminPayoutController (AC: All)
  - [ ] GET /admin/payouts - show all payouts grouped by status
  - [ ] POST /admin/payouts/{payoutId}/mark-paid - mark as PAID
  - [ ] @PreAuthorize("hasAuthority('ROLE_ADMIN')")

- [ ] Update MyListingsController (AC: Seller Button)
  - [ ] GET /my-listings - add PAID payouts data to model
  - [ ] POST /my-listings/payouts/{payoutId}/confirm-received - seller confirms receipt
  - [ ] @PreAuthorize("isAuthenticated()")

### Phase 4: Scheduled Tasks

- [ ] Create PayoutScheduler (AC: Auto-create on 1st)
  - [ ] @Scheduled(cron = "0 0 0 1 * ?") - runs at midnight on 1st of each month
  - [ ] createMonthlyPayouts() method
  - [ ] Logs created payouts

### Phase 5: View Templates

- [ ] Update admin-payouts.html (AC: All)
  - [ ] Tabs or sections for each status (NEEDS_PAYMENT, PAID, RECEIVED)
  - [ ] "Mark as Paid" button for NEEDS_PAYMENT
  - [ ] Show "Waiting for seller" for PAID
  - [ ] Show "✓ Completed" for RECEIVED
  - [ ] Vietnamese localization

- [ ] Update my-listings.html (AC: Seller Button)
  - [ ] Add "Received Payment" button next to profit section
  - [ ] Only show when pending payouts exist (PAID status)
  - [ ] Show amount to be received
  - [ ] Confirmation dialog before submitting

- [ ] Update admin navbar (AC: Navigation)
  - [ ] Add "Thanh toán" link to /admin/payouts

### Phase 6: Testing

- [ ] Create PayoutServiceTest (AC: All)
  - [ ] testCreateMonthlyPayouts_CreatesNeedsPaymentRecords()
  - [ ] testMarkAsPaid_UpdatesStatusAndSendsEmail()
  - [ ] testMarkAsReceived_UpdatesStatusAndSendsEmail()
  - [ ] testCalculateUnpaidEarnings_ExcludesReceivedOnly()
  - [ ] testGetPendingPayoutsForSeller()

- [ ] Create PayoutSchedulerTest (AC: Scheduled Task)
  - [ ] testMonthlyPayoutCreation_OnFirstOfMonth()

- [ ] Update AdminPayoutControllerTest (AC: All)
  - [ ] testShowPayouts_GroupedByStatus()
  - [ ] testMarkAsPaid_CreatesPayoutRecord()

- [ ] Update MyListingsControllerTest (AC: Seller Button)
  - [ ] testShowMyListings_WithPendingPayouts_ShowsButton()
  - [ ] testConfirmReceived_MarksAsReceived()

---

## Dev Notes

### Payout Status Flow

```
┌──────────────────┐    Admin clicks     ┌──────────┐    Seller clicks    ┌───────────┐
│ NEEDS_PAYMENT    │ ──────────────────> │   PAID   │ ──────────────────> │  RECEIVED │
│ (Auto-created)   │   "Mark as Paid"    │          │   "Received"       │           │
└──────────────────┘                     └──────────┘                     └───────────┘
       ↓                                        ↓                                  ↓
   1st of month                           paid_at set                        received_at set
   (scheduled)                           Email to seller                    Profit reset
                                          (5-day reminder)                  Email to admin
```

### Previous Story Intelligence

**From Story 3.3 (My Listings):**
- Profit calculation = total SOLD listings * 0.90 (after 10% commission)
- calculateProfit() returns ALL sold listings profit
- My Listings page exists at GET /my-listings

**From Story 3.1 (Wallet System):**
- Admin ID from CustomUserDetails.getId()
- @PreAuthorize("hasAuthority('ROLE_ADMIN')") pattern
- EmailService for notifications

**From Story 3.2 (Transaction Approval):**
- Email failure handling pattern (log error, don't block action)

### Architecture Compliance

**Layer Architecture:**
```
Browser → AdminPayoutController → PayoutService → PayoutRepository → Database
                                  → EmailService
                                  → GameAccountService

Browser → MyListingsController → PayoutService → PayoutRepository → Database
                                → EmailService
```

**Database Schema:**
```sql
CREATE TABLE payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status ENUM('NEEDS_PAYMENT', 'PAID', 'RECEIVED') NOT NULL DEFAULT 'NEEDS_PAYMENT',
    paid_at TIMESTAMP NULL,
    received_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_id BIGINT NULL COMMENT 'Admin who marked as paid',
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_seller_status_month (seller_id, status, created_at),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Profit Calculation Logic:**
```java
// Story 3.3: ALL sold listings
profit = SUM(listing_price) * 0.90 for all SOLD listings

// Story 3.4: EXCLUDE RECEIVED payouts (not PAID!)
unpaidProfit = SUM(listing_price) * 0.90 for SOLD listings - RECEIVED payouts
// PAID payouts DO NOT reset profit yet!
```

### File Structure Requirements

**New Files to Create:**
```
src/main/java/com/gameaccountshop/
├── entity/
│   └── Payout.java                        # Payout entity
├── enums/
│   └── PayoutStatus.java                  # NEEDS_PAYMENT, PAID, RECEIVED
├── dto/
│   ├── AdminPayoutDto.java                # Admin page DTO
│   └── SellerPayoutSummaryDto.java        # Seller pending summary
├── repository/
│   └── PayoutRepository.java              # Payout data access
├── service/
│   ├── PayoutService.java                 # Payout business logic
│   └── PayoutScheduler.java               # Monthly scheduled task
├── controller/
│   └── AdminPayoutController.java         # Admin payout endpoints
└── scheduled/                             # (or root package)
    └── PayoutScheduler.java               # @Scheduled component

src/main/resources/templates/
└── admin-payouts.html                     # Payout management UI

src/test/java/com/gameaccountshop/
├── service/
│   ├── PayoutServiceTest.java
│   └── PayoutSchedulerTest.java
└── controller/
    └── AdminPayoutControllerTest.java
```

**Files to Modify:**
```
src/main/java/com/gameaccountshop/
├── controller/
│   └── MyListingsController.java          # Add received button logic
├── service/
│   ├── GameAccountService.java            # Update calculateProfit()
│   └── EmailService.java                  # Add payout emails
└── repository/
    └── GameAccountRepository.java         # Add findDistinctSellerIdsByStatus()

src/main/resources/templates/
├── admin-payouts.html                     # Create new
├── my-listings.html                       # Add received button
└── layout/
    └── header.html (admin navbar)         # Add payout link
```

### Technical Requirements

**Enum: PayoutStatus**

```java
package com.gameaccountshop.enums;

public enum PayoutStatus {
    /**
     * Payout created automatically on 1st of month
     * Waiting for admin to process
     */
    NEEDS_PAYMENT,

    /**
     * Admin has transferred money and clicked "Mark as Paid"
     * Waiting for seller to confirm receipt
     */
    PAID,

    /**
     * Seller has confirmed receipt
     * Payout is complete
     */
    RECEIVED
}
```

**Entity: Payout**

```java
package com.gameaccountshop.entity;

import com.gameaccountshop.enums.PayoutStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
public class Payout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayoutStatus status = PayoutStatus.NEEDS_PAYMENT;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "admin_id")
    private Long adminId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PayoutStatus.NEEDS_PAYMENT;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    /**
     * Get Vietnamese display name for status
     */
    public String getStatusDisplayName() {
        switch (status) {
            case NEEDS_PAYMENT: return "Chờ thanh toán";
            case PAID: return "Đã chuyển";
            case RECEIVED: return "Đã nhận";
            default: return "Không xác định";
        }
    }

    /**
     * Get CSS class for status badge
     */
    public String getStatusClass() {
        switch (status) {
            case NEEDS_PAYMENT: return "bg-warning text-dark";
            case PAID: return "bg-info text-dark";
            case RECEIVED: return "bg-success";
            default: return "bg-secondary";
        }
    }
}
```

**Repository: PayoutRepository**

```java
package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Payout> findByStatusOrderByCreatedAtDesc(PayoutStatus status);

    List<Payout> findBySellerIdAndStatus(Long sellerId, PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payout p WHERE p.sellerId = :sellerId AND p.status = :status")
    BigDecimal sumAmountBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                           @Param("status") PayoutStatus status);

    @Query("SELECT COUNT(p) > 0 FROM Payout p WHERE p.sellerId = :sellerId AND p.status = :status AND MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    boolean existsBySellerIdAndStatusAndMonth(@Param("sellerId") Long sellerId,
                                              @Param("status") PayoutStatus status,
                                              @Param("month") int month,
                                              @Param("year") int year);
}
```

**Service: PayoutService (Key Methods)**

```java
package com.gameaccountshop.service;

import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.repository.PayoutRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Constructor...

    /**
     * Scheduled task: Create NEEDS_PAYMENT payouts on 1st of month
     * Runs at midnight on the 1st of each month
     */
    @Transactional
    public void createMonthlyPayouts() {
        LocalDate now = LocalDate.now();
        log.info("Running monthly payout creation for: {}", now);

        // Get all sellers with sold listings
        List<Long> sellerIds = gameAccountRepository.findDistinctSellerIdsByStatus(
            ListingStatus.SOLD);

        int created = 0;
        for (Long sellerId : sellerIds) {
            // Check if already has NEEDS_PAYMENT for this month
            if (payoutRepository.existsBySellerIdAndStatusAndMonth(
                    sellerId, PayoutStatus.NEEDS_PAYMENT, now.getMonthValue(), now.getYear())) {
                continue;
            }

            // Calculate unpaid amount (total sold - received)
            BigDecimal unpaidAmount = calculateUnpaidEarnings(sellerId);

            if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payout payout = new Payout();
                payout.setSellerId(sellerId);
                payout.setAmount(unpaidAmount);
                payout.setStatus(PayoutStatus.NEEDS_PAYMENT);
                payoutRepository.save(payout);
                created++;
                log.info("Created NEEDS_PAYMENT payout for seller: {}, amount: {}", sellerId, unpaidAmount);
            }
        }

        log.info("Monthly payout creation completed. Created {} payouts.", created);
    }

    /**
     * Calculate unpaid earnings
     * = Total sold (90%) - RECEIVED payouts
     * NOTE: PAID payouts do NOT reduce unpaid earnings
     */
    public BigDecimal calculateUnpaidEarnings(Long sellerId) {
        BigDecimal totalSold = gameAccountRepository.sumPriceBySellerIdAndStatus(
            sellerId, ListingStatus.SOLD);

        if (totalSold == null) {
            totalSold = BigDecimal.ZERO;
        }

        BigDecimal netEarnings = totalSold.multiply(new BigDecimal("0.90"));

        // Only subtract RECEIVED payouts, NOT PAID payouts
        BigDecimal receivedAmount = payoutRepository.sumAmountBySellerIdAndStatus(
            sellerId, PayoutStatus.RECEIVED);

        if (receivedAmount == null) {
            receivedAmount = BigDecimal.ZERO;
        }

        return netEarnings.subtract(receivedAmount);
    }

    /**
     * Mark payout as PAID (admin action)
     * Sends email to seller
     */
    @Transactional
    public void markAsPaid(Long payoutId, Long adminId) {
        Payout payout = payoutRepository.findById(payoutId)
            .orElseThrow(() -> new IllegalArgumentException("Payout not found"));

        if (payout.getStatus() != PayoutStatus.NEEDS_PAYMENT) {
            throw new IllegalStateException("Payout is not in NEEDS_PAYMENT status");
        }

        payout.setStatus(PayoutStatus.PAID);
        payout.setPaidAt(LocalDateTime.now());
        payout.setAdminId(adminId);
        payoutRepository.save(payout);

        // Send email to seller
        try {
            var seller = userRepository.findById(payout.getSellerId()).orElse(null);
            if (seller != null) {
                emailService.sendPayoutPaidEmail(
                    seller.getEmail(),
                    payout.getAmount(),
                    "PAYOUT" + payout.getId()
                );
                log.info("Sent payout paid email to seller: {}", seller.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to send payout paid email for payout: {}", payoutId, e);
            // Don't throw - payout status is already updated
        }
    }

    /**
     * Mark payout as RECEIVED (seller action)
     * Sends email to admin
     */
    @Transactional
    public void markAsReceived(Long payoutId, Long sellerId) {
        Payout payout = payoutRepository.findById(payoutId)
            .orElseThrow(() -> new IllegalArgumentException("Payout not found"));

        if (payout.getStatus() != PayoutStatus.PAID) {
            throw new IllegalStateException("Payout is not in PAID status");
        }

        if (!payout.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("This payout does not belong to you");
        }

        payout.setStatus(PayoutStatus.RECEIVED);
        payout.setReceivedAt(LocalDateTime.now());
        payoutRepository.save(payout);

        // Send email to admin
        try {
            var seller = userRepository.findById(sellerId).orElse(null);
            var admin = userRepository.findByUsername("admin").orElse(null);
            if (seller != null && admin != null) {
                emailService.sendPayoutReceivedEmail(
                    admin.getEmail(),
                    seller.getUsername(),
                    payout.getAmount()
                );
                log.info("Sent payout received email to admin");
            }
        } catch (Exception e) {
            log.error("Failed to send payout received email for payout: {}", payoutId, e);
            // Don't throw - payout status is already updated
        }

        log.info("Payout {} marked as RECEIVED by seller {}", payoutId, sellerId);
    }

    /**
     * Get PAID payouts for seller (for "Received" button)
     */
    public List<Payout> getPendingPayoutsForSeller(Long sellerId) {
        return payoutRepository.findBySellerIdAndStatus(sellerId, PayoutStatus.PAID);
    }
}
```

**Scheduler: PayoutScheduler**

```java
package com.gameaccountshop.scheduled;

import com.gameaccountshop.service.PayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for payout system
 */
@Component
@Slf4j
public class PayoutScheduler {

    private final PayoutService payoutService;

    public PayoutScheduler(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * Create monthly NEEDS_PAYMENT payouts
     * Runs at midnight (00:00) on the 1st of every month
     * Cron: 0 0 0 1 * ?
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void createMonthlyPayouts() {
        log.info("Scheduled task: Creating monthly payouts");
        try {
            payoutService.createMonthlyPayouts();
        } catch (Exception e) {
            log.error("Error creating monthly payouts", e);
        }
    }
}
```

**Controller: AdminPayoutController**

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.entity.Payout;
import com.gameaccountshop.enums.PayoutStatus;
import com.gameaccountshop.security.CustomUserDetails;
import com.gameaccountshop.service.PayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
public class AdminPayoutController {

    private final PayoutService payoutService;

    public AdminPayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    /**
     * Show admin payout management page
     * GET /admin/payouts
     */
    @GetMapping("/admin/payouts")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String showPayouts(Model model) {
        log.info("Admin viewing payout page");

        List<Payout> needsPayment = payoutService.getPayoutsByStatus(PayoutStatus.NEEDS_PAYMENT);
        List<Payout> paid = payoutService.getPayoutsByStatus(PayoutStatus.PAID);
        List<Payout> received = payoutService.getPayoutsByStatus(PayoutStatus.RECEIVED);

        model.addAttribute("needsPayment", needsPayment);
        model.addAttribute("paid", paid);
        model.addAttribute("received", received);

        return "admin-payouts";
    }

    /**
     * Mark payout as PAID
     * POST /admin/payouts/{payoutId}/mark-paid
     */
    @PostMapping("/admin/payouts/{payoutId}/mark-paid")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String markAsPaid(
            @PathVariable Long payoutId,
            @AuthenticationPrincipal CustomUserDetails adminDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long adminId = adminDetails.getId();
            payoutService.markAsPaid(payoutId, adminId);

            redirectAttributes.addFlashAttribute("successMessage",
                "Đã đánh dấu thanh toán. Email đã được gửi cho người bán.");

            return "redirect:/admin/payouts";

        } catch (Exception e) {
            log.error("Error marking payout as paid: {}", payoutId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Lỗi khi xác nhận thanh toán. Vui lòng thử lại.");
            return "redirect:/admin/payouts";
        }
    }
}
```

**Controller: MyListingsController (Updates)**

```java
// Add to existing MyListingsController:

/**
 * Show seller's listings with status filter and pending payouts
 */
@GetMapping("/my-listings")
@PreAuthorize("isAuthenticated()")
public String showMyListings(
        @RequestParam(required = false) String status,
        Authentication authentication,
        Model model) {

    // ... existing code ...

    // Add pending payouts for "Received" button
    List<Payout> pendingPayouts = payoutService.getPendingPayoutsForSeller(userId);
    BigDecimal totalPendingAmount = pendingPayouts.stream()
        .map(Payout::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("pendingPayouts", pendingPayouts);
    model.addAttribute("totalPendingAmount", totalPendingAmount);

    return "my-listings";
}

/**
 * Confirm payout receipt (seller action)
 * POST /my-listings/payouts/{payoutId}/confirm-received
 */
@PostMapping("/my-listings/payouts/{payoutId}/confirm-received")
@PreAuthorize("isAuthenticated()")
public String confirmReceived(
        @PathVariable Long payoutId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        RedirectAttributes redirectAttributes) {

    try {
        Long sellerId = userDetails.getId();
        payoutService.markAsReceived(payoutId, sellerId);

        redirectAttributes.addFlashAttribute("successMessage",
            "Đã xác nhận nhận tiền. Cảm ơn bạn!");

        return "redirect:/my-listings";

    } catch (Exception e) {
        log.error("Error confirming payout receipt: {}", payoutId, e);
        redirectAttributes.addFlashAttribute("errorMessage",
            "Lỗi khi xác nhận. Vui lòng thử lại.");
        return "redirect:/my-listings";
    }
}
```

**Template: admin-payouts.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Quản lý thanh toán - Admin</title>
</head>
<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="container mt-4">
    <h1>Quản lý thanh toán</h1>

    <div class="alert alert-info mb-3">
      <strong>Quy trình:</strong> Chờ thanh toán → Đã chuyển → Đã nhận
      <br><small>Tự động tạo vào ngày 1 hàng tháng</small>
    </div>

    <!-- Tabs -->
    <ul class="nav nav-tabs mb-3">
      <li class="nav-item">
        <a class="nav-link" th:classappend="${activeTab == 'needs' ? 'active' : ''}"
           th:href="@{/admin/payouts(tab=needs)}">
          Chờ thanh toán (<span th:text="${#lists.size(needsPayment)}">0</span>)
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" th:classappend="${activeTab == 'paid' ? 'active' : ''}"
           th:href="@{/admin/payouts(tab=paid)}">
          Đã chuyển (<span th:text="${#lists.size(paid)}">0</span>)
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link" th:classappend="${activeTab == 'received' ? 'active' : ''}"
           th:href="@{/admin/payouts(tab=received)}">
          Đã nhận (<span th:text="${#lists.size(received)}">0</span>)
        </a>
      </li>
    </ul>

    <!-- NEEDS_PAYMENT Table -->
    <div th:if="${activeTab == null or activeTab == 'needs'}">
      <h4>Chờ thanh toán</h4>
      <div th:if="${!needsPayment.isEmpty()}">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Seller</th>
              <th>Email</th>
              <th>Số tiền</th>
              <th>Ngày tạo</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="payout : ${needsPayment}">
              <td th:text="${payout.sellerUsername}">username</td>
              <td th:text="${payout.sellerEmail}">email@example.com</td>
              <td>
                <strong th:text="${#numbers.formatInteger(payout.amount, 3, 'POINT')} + ' VNĐ'">0 VNĐ</strong>
              </td>
              <td th:text="${#temporals.format(payout.createdAt, 'dd/MM/yyyy')}">01/01/2026</td>
              <td>
                <form th:action="@{/admin/payouts/{payoutId}/mark-paid(payoutId=${payout.id})}"
                      method="post"
                      onsubmit="return confirm('Bạn đã chuyển khoản cho người bán này chưa?')">
                  <button type="submit" class="btn btn-success btn-sm">
                    ✓ Đã thanh toán
                  </button>
                </form>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div th:if="${needsPayment.isEmpty()}" class="text-center py-3">
        <p class="text-muted">Không có khoản thanh toán nào chờ xử lý</p>
      </div>
    </div>

    <!-- PAID Table -->
    <div th:if="${activeTab == 'paid'}">
      <h4>Đã chuyển (chờ người bán xác nhận)</h4>
      <div th:if="${!paid.isEmpty()}">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Seller</th>
              <th>Email</th>
              <th>Số tiền</th>
              <th>Ngày chuyển</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="payout : ${paid}">
              <td th:text="${payout.sellerUsername}">username</td>
              <td th:text="${payout.sellerEmail}">email@example.com</td>
              <td th:text="${#numbers.formatInteger(payout.amount, 3, 'POINT')} + ' VNĐ'">0 VNĐ</td>
              <td th:text="${#temporals.format(payout.paidAt, 'dd/MM/yyyy')}">01/01/2026</td>
              <td>
                <span class="badge bg-info">⏳ Chờ người bán xác nhận</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div th:if="${paid.isEmpty()}" class="text-center py-3">
        <p class="text-muted">Không có khoản nào đang chờ xác nhận</p>
      </div>
    </div>

    <!-- RECEIVED Table -->
    <div th:if="${activeTab == 'received'}">
      <h4>Đã nhận (hoàn thành)</h4>
      <div th:if="${!received.isEmpty()}">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Seller</th>
              <th>Số tiền</th>
              <th>Ngày chuyển</th>
              <th>Ngày nhận</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="payout : ${received}">
              <td th:text="${payout.sellerUsername}">username</td>
              <td th:text="${#numbers.formatInteger(payout.amount, 3, 'POINT')} + ' VNĐ'">0 VNĐ</td>
              <td th:text="${#temporals.format(payout.paidAt, 'dd/MM/yyyy')}">01/01/2026</td>
              <td th:text="${#temporals.format(payout.receivedAt, 'dd/MM/yyyy')}">05/01/2026</td>
              <td>
                <span class="badge bg-success">✓ Hoàn thành</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div th:if="${received.isEmpty()}" class="text-center py-3">
        <p class="text-muted">Chưa có khoản nào hoàn thành</p>
      </div>
    </div>
  </div>
</body>
</html>
```

**Template: my-listings.html (Add button next to profit)**

```html
<!-- Add this section after the profit display -->

<div class="alert alert-info" th:if="${!pendingPayouts.isEmpty()}">
  <strong>Bạn có khoản thanh toán đang chờ xác nhận!</strong>
  <div>Số tiền: <span th:text="${#numbers.formatInteger(totalPendingAmount, 3, 'POINT')} + ' VNĐ'">0 VNĐ</span></div>
  <div class="mt-2">
    <form th:each="payout : ${pendingPayouts}"
          th:action="@{/my-listings/payouts/{payoutId}/confirm-received(payoutId=${payout.id})}"
          method="post"
          onsubmit="return confirm('Bạn đã nhận được số tiền ' + [[${payout.amount}]] + ' VNĐ chưa?')"
          style="display: inline;">
      <button type="submit" class="btn btn-success btn-sm">
        ✓ Xác nhận đã nhận tiền
      </button>
    </form>
  </div>
  <small class="text-muted">Chỉ nhấn nút này khi bạn đã nhận được tiền trong tài khoản ngân hàng</small>
</div>
```

**Email Templates (add to EmailService)**

```java
/**
 * Send email to seller when admin marks payout as PAID
 */
public void sendPayoutPaidEmail(String toEmail, BigDecimal amount, String payoutId) {
    String subject = "💰 Thanh toán tiền bán tài khoản - Chờ nhận tiền";
    String content = """
        <h2>Chúng tôi đã chuyển khoản!</h2>
        <p>Chào bạn,</p>
        <p>Chúng tôi đã chuyển khoản thanh toán tiền bán tài khoản:</p>
        <ul>
            <li>Số tiền: <strong>%s VNĐ</strong></li>
            <li>Mã thanh toán: %s</li>
        </ul>
        <p>Vui lòng kiểm tra tài khoản ngân hàng của bạn trong vòng <strong>5 ngày</strong>.</p>
        <p>Sau khi nhận được tiền, hãy đăng nhập vào website và nhấn nút <strong>"Xác nhận đã nhận tiền"</strong> trên trang Tài khoản của tôi.</p>
        <p>Nếu sau 5 ngày bạn仍未 nhận được tiền, vui lòng phản hồi email này.</p>
        <p>Cảm ơn bạn đã tham gia cùng Game Account Shop!</p>
        """.formatted(
            String.format("%,.0f", amount),
            payoutId
        );

    sendEmail(toEmail, subject, content);
}

/**
 * Send email to admin when seller confirms receipt
 */
public void sendPayoutReceivedEmail(String adminEmail, String sellerUsername, BigDecimal amount) {
    String subject = "✓ Người bán đã nhận tiền - " + sellerUsername;
    String content = """
        <h2>Người bán đã xác nhận nhận tiền</h2>
        <p>Người bán <strong>%s</strong> đã xác nhận nhận được khoản thanh toán.</p>
        <ul>
            <li>Số tiền: <strong>%s VNĐ</strong></li>
            <li>Thời gian: <strong>%s</strong></li>
        </ul>
        <p>Giao dịch thanh toán đã hoàn thành.</p>
        """.formatted(
            sellerUsername,
            String.format("%,.0f", amount),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );

    sendEmail(adminEmail, subject, content);
}
```

### Testing Requirements

**Unit Tests:**
```java
@PayoutServiceTest
class PayoutServiceTest {
    - testCreateMonthlyPayouts_CreatesNeedsPaymentRecords()
    - testCreateMonthlyPayouts_AvoidsDuplicates()
    - testMarkAsPaid_UpdatesStatusAndSendsEmail()
    - testMarkAsReceived_UpdatesStatusAndSendsEmail()
    - testCalculateUnpaidEarnings_ExcludesReceivedOnly()
    - testGetPendingPayoutsForSeller()
}

@PayoutSchedulerTest
class PayoutSchedulerTest {
    - testScheduledTask_RunsMonthly()
}

@AdminPayoutControllerTest {
    - testShowPayouts_GroupedByStatus()
    - testMarkAsPaid_CreatesPayoutRecord()
    - testMarkAsPaid_SendsEmail()
}

@MyListingsControllerTest {
    - testShowMyListings_WithPendingPayouts_ShowsButton()
    - testConfirmReceived_MarksAsReceived()
    - testConfirmReceived_SendsEmailToAdmin()
}
```

### Error Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| Not logged in (admin) | Redirect to login page |
| Not admin | 403 Forbidden |
| Mark as paid on wrong status | Error: "Payout is not in NEEDS_PAYMENT status" |
| Confirm received on wrong status | Error: "Payout is not in PAID status" |
| Seller confirms another's payout | Error: "This payout does not belong to you" |
| Email send failure | Log error, don't block status update |

### References

**Source: epics.md**
- Epic 3: Simple Buying + Seller Tools
- Story 3.4: Admin Payout System (lines 694-779)

**User Requirements (from conversation):**
- Auto-create NEEDS_PAYMENT on 1st of month
- Admin "Mark as Paid" → PAID → seller gets email
- Seller "Received Payment" button → RECEIVED → admin gets email, profit reset
- 3 statuses: NEEDS_PAYMENT → PAID → RECEIVED
- 5-day window: informational reminder only
- Email notifications required both ways
- Profit reset only on RECEIVED (not PAID)

---

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101

### Debug Log References

None

### Completion Notes List

### File List

**Expected Files to Create:**
- `src/main/java/com/gameaccountshop/entity/Payout.java`
- `src/main/java/com/gameaccountshop/enums/PayoutStatus.java` (updated)
- `src/main/java/com/gameaccountshop/dto/AdminPayoutDto.java`
- `src/main/java/com/gameaccountshop/dto/SellerPayoutSummaryDto.java`
- `src/main/java/com/gameaccountshop/repository/PayoutRepository.java`
- `src/main/java/com/gameaccountshop/service/PayoutService.java`
- `src/main/java/com/gameaccountshop/scheduled/PayoutScheduler.java`
- `src/main/java/com/gameaccountshop/controller/AdminPayoutController.java`
- `src/main/resources/templates/admin-payouts.html`
- `src/test/java/com/gameaccountshop/service/PayoutServiceTest.java`
- `src/test/java/com/gameaccountshop/scheduled/PayoutSchedulerTest.java`
- `src/test/java/com/gameaccountshop/controller/AdminPayoutControllerTest.java`

**Expected Files to Modify:**
- `src/main/java/com/gameaccountshop/controller/MyListingsController.java` - Add received button
- `src/main/java/com/gameaccountshop/service/GameAccountService.java` - Update calculateProfit()
- `src/main/java/com/gameaccountshop/service/EmailService.java` - Add payout emails
- `src/main/java/com/gameaccountshop/repository/GameAccountRepository.java` - Add seller query methods
- `src/main/resources/templates/my-listings.html` - Add received button UI
- `src/main/resources/templates/layout/header.html` - Add admin payout link
