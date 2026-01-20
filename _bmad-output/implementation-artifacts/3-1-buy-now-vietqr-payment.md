# Story 3.1: Buy Now & Show VietQR Payment

Status: todo

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **logged-in buyer**,
I want **to click "Buy Now" and see a QR code with exact payment amount and description**,
So that **I can easily pay using my banking app**.

## Acceptance Criteria

**Given** I am logged in as a USER
**When** I view an APPROVED listing detail page
**And** I click the "Mua ngay" button
**Then** a new Transaction record is created
**And** the transaction status is set to "PENDING"
**And** buyer_id is set to my user ID
**And** listing_id and seller_id are captured
**And** amount is set to the listing price
**And** commission (10%) is calculated
**And** I am redirected to the payment page

**Given** I reach the payment page
**When** the page loads
**Then** I see the payment details:
  - Listing price (e.g., 500,000 VNĐ)
  - Platform fee 10% (e.g., 50,000 VNĐ)
  - Total amount (e.g., 550,000 VNĐ)
  - Transaction ID

**And** I see a QR code image generated from VietQR.net
**And** the QR code contains:
  - Bank account (from application.yml config)
  - Exact total amount
  - Transfer description (transaction ID)

**And** I see payment instructions:
  - "Mở ứng dụng ngân hàng của bạn"
  - "Quét mã QR bên dưới"
  - "Hoặc chuyển khoản đến: [bank account]"
  - "Số tiền: [total amount] VNĐ"
  - "Nội dung: [transaction ID]"

**And** I see a message "Sau khi thanh toán, admin sẽ xác nhận và gửi thông tin tài khoản qua email"

**Given** the listing status is not "APPROVED"
**When** I view the listing
**Then** the "Mua ngay" button is not available

**Given** I am the seller of this listing
**When** I view my own listing
**Then** I cannot buy my own listing (button hidden)

**Technical Notes:**
- Transaction table: id, listing_id, buyer_id, seller_id, amount, commission, status, created_at
- Commission = amount * 0.10
- Status flow: PENDING → VERIFIED → REJECTED
- VietQR.net URL format: `https://img.vietqr.io/image/{bankId}-{accountNo}-compact.png?amount={amount}&addInfo={description}`
- URL encode the description parameter
- Store transaction ID in session for reference

**application.yml configuration:**
```yaml
vietqr:
  bank-id: 970415
  account-no: 113366668888
  account-name: Your Name
```

**Example QR URL:**
```
https://img.vietqr.io/image/970415-113366668888-compact.png?amount=550000&addInfo=TXN123456
```

## Tasks / Subtasks

- [ ] Create Transaction entity (AC: #1)
  - [ ] Add fields: id, listingId, buyerId, sellerId, amount, commission, status, createdAt
  - [ ] Add JPA annotations and table mapping
  - [ ] Add @PrePersist for created_at timestamp

- [ ] Create TransactionStatus enum (AC: #1)
  - [ ] PENDING, VERIFIED, REJECTED

- [ ] Create TransactionRepository interface (AC: #1)
  - [ ] Extend JpaRepository
  - [ ] Add method to find by buyer_id
  - [ ] Add method to find by status

- [ ] Create TransactionDto for form binding (AC: #1)
  - [ ] Add validation annotations

- [ ] Create TransactionService (AC: #1)
  - [ ] createTransaction method
  - [ ] Set PENDING status on creation
  - [ ] Calculate commission (10%)
  - [ ] Store buyer_id from authenticated user
  - [ ] Generate unique transaction ID

- [ ] Create TransactionController (AC: #1, #2, #3)
  - [ ] POST /listings/{id}/buy - create transaction and redirect to payment
  - [ ] GET /payment/{transactionId} - show payment page with QR
  - [ ] Add authentication check (must be logged in)
  - [ ] Validate listing is APPROVED
  - [ ] Prevent buying own listing

- [ ] Create Thymeleaf template for payment page (AC: #2, #3)
  - [ ] Create payment.html template
  - [ ] Display payment details (price, commission, total)
  - [ ] Display QR code from VietQR.net
  - [ ] Show payment instructions
  - [ ] Add transaction info display
  - [ ] Add custom CSS styling

- [ ] Add "Buy Now" button to listing detail page (AC: #1)
  - [ ] Update listing-detail.html
  - [ ] Link to /listings/{id}/buy
  - [ ] Show only for APPROVED listings
  - [ ] Hide for own listings

- [ ] Implement validation logic (AC: #4, #5)
  - [ ] Validate listing status before allowing purchase
  - [ ] Prevent buying own listing
  - [ ] Check user is authenticated
  - [ ] Handle errors gracefully

- [ ] Create database migration (AC: #1)
  - [ ] Create transactions table
  - [ ] Add foreign keys to game_accounts and users

---

## Dev Notes

### Previous Story Intelligence

**From Story 2.3 (Listing Details):**
- Listing detail page exists at `/listings/{id}`
- ListingDetailDto contains all listing info
- Listing status can be APPROVED or SOLD
- "Mua ngay" button placeholder exists

**From Story 2.1 (Create Listing):**
- GameAccount entity has: id, seller_id, price, status
- GameAccountService has getListingDetail() method

### Database Schema: transactions Table

**V4__Create_Transactions_Table.sql:**
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    commission BIGINT NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Entity: Transaction

**Create `Transaction.java`:**

```java
package com.gameaccountshop.entity;

import com.gameaccountshop.enums.TransactionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "commission", nullable = false)
    private Long commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(sellerId) { this.sellerId = sellerId; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public Long getCommission() { return commission; }
    public void setCommission(Long commission) { this.commission = commission; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper method to get total amount
    public Long getTotalAmount() {
        return amount + commission;
    }

    // Helper method to get transaction ID (TXN prefix)
    public String getTransactionId() {
        return "TXN" + id;
    }
}
```

### Enum: TransactionStatus

**Create `TransactionStatus.java`:**

```java
package com.gameaccountshop.enums;

public enum TransactionStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
```

### Service Layer: TransactionService

**Create `TransactionService.java`:**

```java
package com.gameaccountshop.service;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.ListingStatus;
import com.gameaccountshop.enums.TransactionStatus;
import com.gameaccountshop.exception.ResourceNotFoundException;
import com.gameaccountshop.repository.GameAccountRepository;
import com.gameaccountshop.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GameAccountRepository gameAccountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              GameAccountRepository gameAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.gameAccountRepository = gameAccountRepository;
    }

    /**
     * Create a new transaction for buying a listing
     * @param listingId The listing to purchase
     * @param buyerId The buyer's user ID
     * @return Created transaction
     */
    @Transactional
    public Transaction createTransaction(Long listingId, Long buyerId) {
        log.info("Creating transaction for listing: {} by buyer: {}", listingId, buyerId);

        // Get listing
        GameAccount listing = gameAccountRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Validate listing status
        if (listing.getStatus() != ListingStatus.APPROVED) {
            throw new IllegalStateException("Listing is not available for purchase");
        }

        // Prevent buying own listing
        if (listing.getSellerId().equals(buyerId)) {
            throw new IllegalStateException("You cannot buy your own listing");
        }

        // Calculate commission (10%)
        Long commission = (long) (listing.getPrice() * 0.1);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setListingId(listingId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(listing.getSellerId());
        transaction.setAmount(listing.getPrice());
        transaction.setCommission(commission);
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    /**
     * Get all pending transactions
     */
    public java.util.List<Transaction> getPendingTransactions() {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING);
    }
}
```

### Controller: TransactionController

**Create `TransactionController.java`:**

```java
package com.gameaccountshop.controller;

import com.gameaccountshop.entity.GameAccount;
import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.entity.User;
import com.gameaccountshop.service.GameAccountService;
import com.gameaccountshop.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final GameAccountService gameAccountService;

    public TransactionController(TransactionService transactionService,
                                GameAccountService gameAccountService) {
        this.transactionService = transactionService;
        this.gameAccountService = gameAccountService;
    }

    /**
     * Create transaction and redirect to payment page
     * POST /listings/{id}/buy
     */
    @PostMapping("/listings/{id}/buy")
    public String buyListing(
            @PathVariable Long id,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = (User) authentication.getPrincipal();

            // Create transaction
            Transaction transaction = transactionService.createTransaction(id, user.getId());

            // Store transaction ID in session
            session.setAttribute("lastTransactionId", transaction.getId());

            log.info("User {} initiated purchase for listing {}", user.getId(), id);

            return "redirect:/payment/" + transaction.getId();

        } catch (IllegalStateException e) {
            log.warn("Purchase failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/" + id;
        }
    }

    /**
     * Show payment page with QR code
     * GET /payment/{transactionId}
     */
    @GetMapping("/payment/{transactionId}")
    public String showPaymentPage(
            @PathVariable Long transactionId,
            Model model) {

        Transaction transaction = transactionService.getTransaction(transactionId);
        GameAccount listing = gameAccountService.getListingDetail(transaction.getListingId());

        model.addAttribute("transaction", transaction);
        model.addAttribute("listing", listing);

        // Calculate totals
        Long totalAmount = transaction.getTotalAmount();
        model.addAttribute("totalAmount", totalAmount);

        log.info("Payment page viewed for transaction: {}", transactionId);

        return "payment";
    }
}
```

### Template: payment.html

**Create `payment.html`:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Thanh toán - Game Account Shop</title>
  <style>
    .content {
      max-width: 800px;
      margin: 0 auto;
      padding: 40px 20px;
    }

    .payment-card {
      background: white;
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    }

    .payment-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .payment-details {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin: 20px 0;
    }

    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #dee2e6;
    }

    .detail-row:last-child {
      border-bottom: none;
    }

    .detail-label {
      color: #6c757d;
    }

    .detail-value {
      font-weight: bold;
      color: #2c3e50;
    }

    .total-amount {
      font-size: 32px;
      color: #27ae60;
      font-weight: bold;
      text-align: center;
      margin: 20px 0;
    }

    .qr-container {
      text-align: center;
      margin: 30px 0;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .qr-code {
      max-width: 300px;
      height: auto;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .instructions {
      background: #e7f3ff;
      padding: 20px;
      border-radius: 8px;
      border-left: 4px solid #3498db;
      margin: 20px 0;
    }

    .instructions h4 {
      color: #2980b9;
      margin-top: 0;
    }

    .info-box {
      background: #fff3cd;
      padding: 15px;
      border-radius: 8px;
      border-left: 4px solid #ffc107;
      margin: 20px 0;
      text-align: center;
    }
  </style>
</head>

<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="content">
    <div class="payment-card">
      <div class="payment-header">
        <h1>💳 Thanh toán</h1>
        <p>Hoàn tất thanh toán để nhận thông tin tài khoản</p>
      </div>

      <!-- Payment Details -->
      <div class="payment-details">
        <div class="detail-row">
          <span class="detail-label">Mã giao dịch:</span>
          <span class="detail-value" th:text="${transaction.transactionId}">TXN123</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Game:</span>
          <span class="detail-value" th:text="${listing.gameName}">Liên Minh Huyền Thoại</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Rank:</span>
          <span class="detail-value" th:text="${listing.accountRank}">Gold III</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Giá listing:</span>
          <span class="detail-value" th:text="${#numbers.formatInteger(transaction.amount, 3, 'POINT')} + ' VNĐ'">500,000 VNĐ</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Phí platform (10%):</span>
          <span class="detail-value" th:text="${#numbers.formatInteger(transaction.commission, 3, 'POINT')} + ' VNĐ'">50,000 VNĐ</span>
        </div>
      </div>

      <!-- Total Amount -->
      <div class="total-amount">
        Tổng: <span th:text="${#numbers.formatInteger(totalAmount, 3, 'POINT')}">550,000</span> VNĐ
      </div>

      <!-- QR Code from VietQR -->
      <div class="qr-container">
        <h3>Quét mã QR để thanh toán</h3>
        <img th:src="@{'https://img.vietqr.io/image/${@environment.getProperty('vietqr.bank-id')}-${@environment.getProperty('vietqr.account-no')}-compact.png?amount=' + ${totalAmount} + '&addInfo=' + ${transaction.transactionId}}"
             alt="VietQR Payment Code"
             class="qr-code" />
      </div>

      <!-- Payment Instructions -->
      <div class="instructions">
        <h4>Hướng dẫn thanh toán:</h4>
        <ol>
          <li>Mở ứng dụng ngân hàng của bạn</li>
          <li>Quét mã QR bên trên hoặc chuyển khoản</li>
          <li>Nhập chính xác số tiền: <strong th:text="${#numbers.formatInteger(totalAmount, 3, 'POINT')}">550,000</strong> VNĐ</li>
          <li>Nội dung chuyển khoản: <strong th:text="${transaction.transactionId}">TXN123</strong></li>
        </ol>
      </div>

      <!-- Info Message -->
      <div class="info-box">
        <p>📧 Sau khi thanh toán, admin sẽ xác nhận và gửi thông tin tài khoản qua email</p>
      </div>

      <!-- Back Button -->
      <div style="text-align: center; margin-top: 20px;">
        <a th:href="@{/listings/{id}(id=${listing.id})}"
           style="color: #3498db; text-decoration: none;">
          ← Quay lại chi tiết tài khoản
        </a>
      </div>
    </div>
  </div>

</body>
</html>
```

### Update listing-detail.html

**Add "Buy Now" button (replacing placeholder):**

```html
<!-- Buy Button (now functional) -->
<div th:if="${listing.status == 'APPROVED'}">
  <form th:action="@{/listings/{id}/buy(id=${listing.id})}" method="post">
    <button type="submit" class="btn-buy">Mua ngay</button>
  </form>
</div>
```

### Configuration: application.yml

**Add VietQR configuration:**

```yaml
# VietQR Payment Configuration
vietqr:
  bank-id: 970415
  account-no: 113366668888
  account-name: Your Name
```

### Update GameAccount entity

**Add `sellerId` getter if not present:**

```java
public Long getSellerId() {
    return sellerId;
}
```

### Repository: TransactionRepository

**Create `TransactionRepository.java`:**

```java
package com.gameaccountshop.repository;

import com.gameaccountshop.entity.Transaction;
import com.gameaccountshop.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyerId(Long buyerId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    List<Transaction> findBySellerId(Long sellerId);
}
```

### Testing Checklist

- [ ] Clicking "Buy Now" creates a transaction
- [ ] Transaction has correct buyer_id, seller_id, listing_id
- [ ] Commission is calculated correctly (10%)
- [ ] Status is set to PENDING
- [ ] User is redirected to payment page
- [ ] Payment page shows correct details
- [ ] QR code is generated with correct amount and transaction ID
- [ ] Cannot buy own listing
- [ ] Cannot buy SOLD or PENDING listings
- [ ] Guest users cannot access buy endpoint

### Error Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| Listing not found | Error: "Listing not found" |
| Listing not APPROVED | Error: "Listing is not available for purchase" |
| Buying own listing | Error: "You cannot buy your own listing" |
| Not logged in | Redirect to login page |
| Transaction not found | Error: "Transaction not found" |

### Security Considerations

- User must be authenticated to buy
- CSRF protection on POST requests
- Validate listing ownership before purchase
- Prevent race conditions (multiple purchases)
- Rate limiting on buy button

### References

- [Source: planning-artifacts/epics.md#Story 3.1: Buy Now & Show VietQR Payment]
- [VietQR.net API](https://vietqr.net)
- [Story 2.3: Listing Details - buy button placeholder]

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Completion Notes List

- **Story Status:** New story - not yet implemented
- **Dependencies:** Stories 2.1, 2.3 must be completed first
- **Configuration Required:** VietQR bank info in application.yml
- **Migration Required:** V4__Create_Transactions_Table.sql

### File List

**Files to Create:**
- `src/main/java/com/gameaccountshop/entity/Transaction.java`
- `src/main/java/com/gameaccountshop/enums/TransactionStatus.java`
- `src/main/java/com/gameaccountshop/repository/TransactionRepository.java`
- `src/main/java/com/gameaccountshop/service/TransactionService.java`
- `src/main/java/com/gameaccountshop/controller/TransactionController.java`
- `src/main/resources/templates/payment.html`
- `src/main/resources/db/migration/V4__Create_Transactions_Table.sql`

**Files to Modify:**
- `src/main/java/com/gameaccountshop/entity/GameAccount.java` - Ensure getSellerId() exists
- `src/main/resources/templates/listing-detail.html` - Update buy button
- `src/main/resources/application.yml` - Add VietQR configuration
