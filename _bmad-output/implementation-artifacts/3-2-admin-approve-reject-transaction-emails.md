# Story 3.2: Admin Approve/Reject Transaction & Send Emails

Status: todo

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an **admin**,
I want **to approve or reject payments and automatically send appropriate emails**,
So that **buyers receive their credentials or rejection reasons promptly**.

## Acceptance Criteria

**Given** A buyer has initiated a purchase
**When** I access the admin payment verification page
**Then** I see all transactions with status = "PENDING"
**And** each transaction displays:
  - Transaction ID
  - Listing info (Game Name, Rank, Image)
  - Buyer username and email
  - Seller username
  - Amount
  - Created Date
**And** I see an "Approve & Send Credentials" button for each transaction
**And** I see a "Reject" button with reason input for each transaction

**Given** I click "Approve & Send Credentials" for a transaction
**When** the approval completes
**Then** the transaction status is updated to "VERIFIED"
**And** the listing status is updated to "SOLD"
**And** sold_at timestamp is set
**And** the account credentials (from game_accounts table) are automatically sent to buyer's email
**And** the email contains:
  - Account username
  - Account password
  - Warning to change password immediately
  - Transaction details (Game Name, Rank, Amount)
**And** a success message "Đã xác nhận và gửi thông tin qua email" is displayed

**Given** I click "Reject" and enter a reason
**When** the rejection completes
**Then** the transaction status is updated to "REJECTED"
**And** the rejection reason is stored
**And** a rejection email is sent to the buyer
**And** the rejection email contains:
  - Transaction ID
  - Amount
  - Rejection reason
  - Contact info for support
**And** a success message "Đã từ chối giao dịch" is displayed

**Given** the email fails to send
**When** the admin approves or rejects a transaction
**Then** an error message is displayed on the admin dashboard
**And** the admin is notified that the email was not delivered
**And** the transaction status is still updated (email failure doesn't block the action)

**Given** there are no pending transactions
**When** I navigate to the admin verification page
**Then** a message "Không có giao dịch nào chờ xác nhận" is displayed

**Technical Notes:**
- Credentials are retrieved from game_accounts.account_username and game_accounts.account_password
- Use JavaMail API with Gmail SMTP
- Email config in application.yml:
  ```yaml
  spring.mail.host: smtp.gmail.com
  spring.mail.port: 587
  spring.mail.username: your-email@gmail.com
  spring.mail.password: xxxx-xxxx-xxxx-xxxx
  spring.mail.properties.mail.smtp.auth: true
  spring.mail.properties.mail.smtp.starttls.enable: true
  ```
- Send emails asynchronously to avoid blocking the approval/rejection action
- Handle email exceptions gracefully (log error, show dashboard notification)
- HTML email templates for better formatting

**Email Template - Payment Approved:**
```
Subject: 🎉 Thanh toán thành công! Đây là thông tin tài khoản

Cảm ơn bạn đã mua hàng!

Thông tin tài khoản game:
- Username: [Account Username]
- Password: [Account Password]

⚠️ QUAN TRỌNG: Đổi mật khẩu ngay sau khi đăng nhập!

Chi tiết giao dịch:
- Game: [Game Name]
- Rank: [Rank]
- Mã giao dịch: [Transaction ID]
- Số tiền: [Amount]
```

**Email Template - Payment Rejected:**
```
Subject: ❌ Giao dịch của bạn đã bị từ chối

Rất tiếc, giao dịch của bạn đã bị từ chối:

- Mã giao dịch: [Transaction ID]
- Số tiền: [Amount]

Lý do: [Rejection Reason]

Nếu bạn nghĩ đây là sự nhầm lẫn, vui lòng liên hệ admin kèm ảnh chụp thanh toán.
```

## Tasks / Subtasks

- [ ] Create TransactionDisplayDto (AC: #1)
  - [ ] Add fields: transactionId, gameName, accountRank, imageUrl, buyerEmail, buyerUsername, sellerUsername, amount, commission, createdAt
  - [ ] Add getters and setters

- [ ] Add payment verification methods to repository (AC: #1)
  - [ ] Create @Query to join transactions with game_accounts and users
  - [ ] Return TransactionDisplayDto with all info
  - [ ] Filter by PENDING status
  - [ ] Order by created_at DESC

- [ ] Add payment verification methods to service (AC: #2, #3, #4)
  - [ ] getPendingTransactionsForReview() - returns DTOs with all info
  - [ ] approvePayment() - updates statuses, sends email
  - [ ] rejectPayment() - updates status, sends rejection email
  - [ ] Handle email failures gracefully

- [ ] Add email methods to EmailService (AC: #2, #3)
  - [ ] sendPaymentApprovedEmail() - with credentials
  - [ ] sendPaymentRejectedEmail() - with reason
  - [ ] Use HTML templates with Vietnamese language

- [ ] Create AdminController for payment verification (AC: #1, #2, #3)
  - [ ] GET /admin/payments - show pending transactions
  - [ ] POST /admin/payments/{id}/approve - approve payment
  - [ ] POST /admin/payments/{id}/reject - reject payment with reason
  - [ ] Add @PreAuthorize("hasRole('ADMIN')")
  - [ ] Show success/error messages

- [ ] Create Thymeleaf template for admin payment page (AC: #1)
  - [ ] Create admin/payments.html template
  - [ ] Display all pending transactions
  - [ ] Show transaction details in cards
  - [ ] Add Approve button
  - [ ] Add Reject form with reason input
  - [ ] Handle empty state
  - [ ] Add custom CSS styling

- [ ] Update GameAccount entity (AC: #2)
  - [ ] Ensure soldAt timestamp is set when marked as SOLD
  - [ ] Add updateStatus method

- [ ] Testing (AC: #1, #2, #3, #4, #5)
  - [ ] Test approve payment flow
  - [ ] Test reject payment flow
  - [ ] Test email delivery for approval
  - [ ] Test email delivery for rejection
  - [ ] Test email failure handling
  - [ ] Test access control (admin only)

---

## Dev Notes

### Previous Story Intelligence

**From Story 3.1 (Buy Now + VietQR Payment):**
- Transaction entity exists with: id, listingId, buyerId, sellerId, amount, commission, status, createdAt
- TransactionStatus enum: PENDING, VERIFIED, REJECTED
- TransactionRepository exists
- TransactionService with createTransaction() and getPendingTransactions()

**From Story 2.1 (Create Listing):**
- GameAccount entity has: accountUsername, accountPassword fields
- These credentials were stored when seller created listing

**From Story 2.7 (Listing Email Notifications):**
- EmailService exists with JavaMail API
- Gmail SMTP is configured
- @Async email sending is set up

### DTO: TransactionDisplayDto

**Create `TransactionDisplayDto.java`:**

```java
package com.gameaccountshop.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin payment verification page
 * Contains transaction + listing + buyer + seller info
 */
public class TransactionDisplayDto {
    // Transaction info
    private Long id;
    private String transactionId;
    private Long amount;
    private Long commission;
    private LocalDateTime createdAt;

    // Listing info
    private Long listingId;
    private String gameName;
    private String accountRank;
    private String imageUrl;

    // Buyer info
    private String buyerUsername;
    private String buyerEmail;

    // Seller info
    private String sellerUsername;

    // Constructor for JPA @Query projection
    public TransactionDisplayDto(Long id, String transactionId, Long amount, Long commission,
                                  LocalDateTime createdAt, Long listingId, String gameName,
                                  String accountRank, String imageUrl, String buyerUsername,
                                  String buyerEmail, String sellerUsername) {
        this.id = id;
        this.transactionId = transactionId;
        this.amount = amount;
        this.commission = commission;
        this.createdAt = createdAt;
        this.listingId = listingId;
        this.gameName = gameName;
        this.accountRank = accountRank;
        this.imageUrl = imageUrl;
        this.buyerUsername = buyerUsername;
        this.buyerEmail = buyerEmail;
        this.sellerUsername = sellerUsername;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public Long getCommission() { return commission; }
    public void setCommission(Long commission) { this.commission = commission; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBuyerUsername() { return buyerUsername; }
    public void setBuyerUsername(String buyerUsername) { this.buyerUsername = buyerUsername; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    // Helper method to get total amount
    public Long getTotalAmount() {
        return amount + commission;
    }

    // Helper method to get formatted transaction ID
    public String getFormattedTransactionId() {
        return "TXN" + id;
    }
}
```

### Repository: TransactionRepository

**Add payment verification query:**

```java
/**
 * Find all pending transactions with full details for admin review
 * @return List of TransactionDisplayDto with transaction, listing, buyer, seller info
 */
@Query("SELECT new com.gameaccountshop.dto.TransactionDisplayDto(" +
       "t.id, CONCAT('TXN', t.id), t.amount, t.commission, t.createdAt, " +
       "g.id, g.gameName, g.accountRank, g.imageUrl, " +
       "ub.username, ub.email, us.username) " +
       "FROM Transaction t " +
       "LEFT JOIN GameAccount g ON t.listingId = g.id " +
       "LEFT JOIN User ub ON t.buyerId = ub.id " +
       "LEFT JOIN User us ON t.sellerId = us.id " +
       "WHERE t.status = :status " +
       "ORDER BY t.createdAt DESC")
List<TransactionDisplayDto> findPendingForReview(@Param("status") TransactionStatus status);
```

### Service: TransactionService

**Add payment verification methods:**

```java
/**
 * Get all pending transactions for admin review
 */
public List<TransactionDisplayDto> getPendingTransactionsForReview() {
    log.info("Fetching pending transactions for review");
    return transactionRepository.findPendingForReview(TransactionStatus.PENDING);
}

/**
 * Approve payment and send credentials to buyer
 * @param transactionId Transaction to approve
 */
@Transactional
public void approvePayment(Long transactionId) {
    log.info("Approving payment for transaction: {}", transactionId);

    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    if (transaction.getStatus() != TransactionStatus.PENDING) {
        throw new IllegalStateException("Transaction is not pending");
    }

    // Get listing to retrieve credentials
    GameAccount listing = gameAccountRepository.findById(transaction.getListingId())
        .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

    // Update transaction status
    transaction.setStatus(TransactionStatus.VERIFIED);
    transactionRepository.save(transaction);

    // Update listing status to SOLD
    listing.setStatus(ListingStatus.SOLD);
    listing.setSoldAt(LocalDateTime.now());
    gameAccountRepository.save(listing);

    // Send email with credentials
    try {
        User buyer = userRepository.findById(transaction.getBuyerId())
            .orElseThrow(() -> new RuntimeException("Buyer not found"));

        emailService.sendPaymentApprovedEmail(
            buyer.getEmail(),
            listing.getGameName(),
            listing.getAccountRank(),
            listing.getAccountUsername(),
            listing.getAccountPassword(),
            transaction.getTotalAmount(),
            transaction.getFormattedTransactionId()
        );
        log.info("Payment approval email sent to: {}", buyer.getEmail());
    } catch (Exception e) {
        log.error("Failed to send payment approval email for transaction: {}", transactionId, e);
        // Don't throw - transaction is already approved
    }
}

/**
 * Reject payment and send rejection email
 * @param transactionId Transaction to reject
 * @param reason Rejection reason
 */
@Transactional
public void rejectPayment(Long transactionId, String reason) {
    log.info("Rejecting payment for transaction: {} with reason: {}", transactionId, reason);

    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    // Update transaction status
    transaction.setStatus(TransactionStatus.REJECTED);
    transaction.setRejectionReason(reason);
    transactionRepository.save(transaction);

    // Send rejection email
    try {
        User buyer = userRepository.findById(transaction.getBuyerId())
            .orElseThrow(() -> new RuntimeException("Buyer not found"));

        emailService.sendPaymentRejectedEmail(
            buyer.getEmail(),
            transaction.getFormattedTransactionId(),
            transaction.getTotalAmount(),
            reason
        );
        log.info("Payment rejection email sent to: {}", buyer.getEmail());
    } catch (Exception e) {
        log.error("Failed to send payment rejection email for transaction: {}", transactionId, e);
        // Don't throw - transaction is already rejected
    }
}
```

### EmailService Updates

**Add payment email methods:**

```java
/**
 * Send payment approval email with credentials
 */
@Async
public void sendPaymentApprovedEmail(String toEmail, String gameName, String accountRank,
                                      String accountUsername, String accountPassword,
                                      Long totalAmount, String transactionId) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("🎉 Thanh toán thành công! Đây là thông tin tài khoản");

        String htmlContent = buildPaymentApprovalEmail(gameName, accountRank,
            accountUsername, accountPassword, totalAmount, transactionId);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Payment approval email sent to: {} for transaction: {}", toEmail, transactionId);

    } catch (MessagingException e) {
        log.error("Failed to send payment approval email to: {}", toEmail, e);
        throw new RuntimeException("Failed to send payment approval email", e);
    }
}

/**
 * Send payment rejection email
 */
@Async
public void sendPaymentRejectedEmail(String toEmail, String transactionId,
                                      Long amount, String reason) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("❌ Giao dịch của bạn đã bị từ chối");

        String htmlContent = buildPaymentRejectionEmail(transactionId, amount, reason);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Payment rejection email sent to: {} for transaction: {}", toEmail, transactionId);

    } catch (MessagingException e) {
        log.error("Failed to send payment rejection email to: {}", toEmail, e);
        throw new RuntimeException("Failed to send payment rejection email", e);
    }
}

private String buildPaymentApprovalEmail(String gameName, String accountRank,
                                          String accountUsername, String accountPassword,
                                          Long amount, String transactionId) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
                .credentials { background: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin: 20px 0; }
                .credentials code { background: #f8f9fa; padding: 10px; display: block; margin: 5px 0; border-radius: 4px; }
                .warning { background: #f8d7da; padding: 15px; border-left: 4px solid #dc3545; margin: 20px 0; }
                .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🎉 Thanh toán thành công!</h1>
                </div>
                <div class="content">
                    <p>Cảm ơn bạn đã mua hàng!</p>

                    <h3>Thông tin tài khoản game:</h3>
                    <div class="credentials">
                        <p><strong>Username:</strong></p>
                        <code>%s</code>
                        <p><strong>Password:</strong></p>
                        <code>%s</code>
                    </div>

                    <div class="warning">
                        <p>⚠️ <strong>QUAN TRỌNG:</strong> Đổi mật khẩu ngay sau khi đăng nhập!</p>
                    </div>

                    <h3>Chi tiết giao dịch:</h3>
                    <p><strong>Game:</strong> %s</p>
                    <p><strong>Rank:</strong> %s</p>
                    <p><strong>Mã giao dịch:</strong> %s</p>
                    <p><strong>Số tiền:</strong> %s VNĐ</p>
                </div>
                <div class="footer">
                    <p>Email này được gửi tự động từ Game Account Shop.</p>
                </div>
            </div>
        </body>
        </html>
        """, accountUsername, accountPassword, gameName, accountRank, transactionId, String.format("%,d", amount));
}

private String buildPaymentRejectionEmail(String transactionId, Long amount, String reason) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }
                .reason { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0; }
                .footer { text-align: center; margin-top: 20px; color: #7f8c8d; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>❌ Giao dịch của bạn đã bị từ chối</h1>
                </div>
                <div class="content">
                    <p>Rất tiếc, giao dịch của bạn đã bị từ chối:</p>

                    <p><strong>Mã giao dịch:</strong> %s</p>
                    <p><strong>Số tiền:</strong> %s VNĐ</p>

                    <div class="reason">
                        <h3>Lý do:</h3>
                        <p>%s</p>
                    </div>

                    <p>Nếu bạn nghĩ đây là sự nhầm lẫn, vui lòng liên hệ admin kèm ảnh chụp thanh toán.</p>
                </div>
                <div class="footer">
                    <p>Email này được gửi tự động từ Game Account Shop.</p>
                </div>
            </div>
        </body>
        </html>
        """, transactionId, String.format("%,d", amount), reason);
}
```

### Controller: AdminController

**Add payment verification endpoints:**

```java
/**
 * Admin payment verification page
 * GET /admin/payments
 */
@GetMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public String paymentVerification(Model model) {
    List<TransactionDisplayDto> pendingTransactions =
        transactionService.getPendingTransactionsForReview();

    model.addAttribute("pendingTransactions", pendingTransactions);

    log.info("Admin viewed payment verification page. Pending transactions: {}",
        pendingTransactions.size());

    return "admin/payments";
}

/**
 * Approve payment
 * POST /admin/payments/{id}/approve
 */
@PostMapping("/admin/payments/{id}/approve")
@PreAuthorize("hasRole('ADMIN')")
public String approvePayment(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
    try {
        transactionService.approvePayment(id);
        redirectAttributes.addFlashAttribute("successMessage",
            "Đã xác nhận và gửi thông tin qua email");
        log.info("Admin approved payment for transaction: {}", id);
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Lỗi khi xác nhận thanh toán: " + e.getMessage());
        log.error("Error approving payment for transaction: {}", id, e);
    }

    return "redirect:/admin/payments";
}

/**
 * Reject payment
 * POST /admin/payments/{id}/reject
 */
@PostMapping("/admin/payments/{id}/reject")
@PreAuthorize("hasRole('ADMIN')")
public String rejectPayment(@PathVariable Long id,
                            @RequestParam String reason,
                            RedirectAttributes redirectAttributes) {
    try {
        transactionService.rejectPayment(id, reason);
        redirectAttributes.addFlashAttribute("successMessage",
            "Đã từ chối giao dịch");
        log.info("Admin rejected payment for transaction: {} with reason: {}", id, reason);
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Lỗi khi từ chối thanh toán: " + e.getMessage());
        log.error("Error rejecting payment for transaction: {}", id, e);
    }

    return "redirect:/admin/payments";
}
```

### Template: admin/payments.html

**Create `admin/payments.html`:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <title>Xác nhận thanh toán - Admin</title>
  <style>
    .content { max-width: 1200px; margin: 0 auto; padding: 40px 20px; }
    .header { margin-bottom: 30px; }
    .transaction-card {
      background: white;
      border: 1px solid #dee2e6;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    .transaction-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      padding-bottom: 15px;
      border-bottom: 1px solid #dee2e6;
    }
    .transaction-id {
      font-size: 18px;
      font-weight: bold;
      color: #2c3e50;
    }
    .transaction-date {
      color: #6c757d;
      font-size: 14px;
    }
    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin: 15px 0;
    }
    .info-item {
      padding: 10px;
      background: #f8f9fa;
      border-radius: 4px;
    }
    .info-label {
      font-size: 12px;
      color: #6c757d;
      margin-bottom: 5px;
    }
    .info-value {
      font-weight: 500;
      color: #2c3e50;
    }
    .listing-thumb {
      width: 100px;
      height: 100px;
      object-fit: cover;
      border-radius: 4px;
    }
    .actions {
      display: flex;
      gap: 10px;
      margin-top: 15px;
      padding-top: 15px;
      border-top: 1px solid #dee2e6;
    }
    .btn-approve {
      padding: 10px 20px;
      background: #27ae60;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .btn-reject {
      padding: 10px 20px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #6c757d;
    }
    .total-amount {
      color: #27ae60;
      font-weight: bold;
    }
  </style>
</head>

<body>
  <div th:replace="~{layout/header :: navbar}"></div>

  <div class="content">
    <div class="header">
      <h1>💰 Xác nhận thanh toán</h1>
      <p>Duyệt hoặc từ chối các giao dịch thanh toán</p>
    </div>

    <!-- Success/Error Messages -->
    <div th:if="${successMessage}" class="alert alert-success" style="padding: 15px; background: #d4edda; border-radius: 4px; margin-bottom: 20px;">
      <span th:text="${successMessage}"></span>
    </div>

    <div th:if="${errorMessage}" class="alert alert-danger" style="padding: 15px; background: #f8d7da; border-radius: 4px; margin-bottom: 20px;">
      <span th:text="${errorMessage}"></span>
    </div>

    <!-- Pending Transactions -->
    <div th:if="${pendingTransactions != null and !pendingTransactions.empty}">
      <div th:each="txn : ${pendingTransactions}" class="transaction-card">
        <div class="transaction-header">
          <div class="transaction-id" th:text="${txn.transactionId}">TXN123</div>
          <div class="transaction-date" th:text="${#temporals.format(txn.createdAt, 'dd/MM/yyyy HH:mm')}">18/01/2026 14:30</div>
        </div>

        <div style="display: flex; gap: 20px;">
          <!-- Listing Image -->
          <div th:if="${txn.imageUrl}">
            <img th:src="${txn.imageUrl}" alt="Listing" class="listing-thumb" />
          </div>

          <!-- Info -->
          <div style="flex: 1;">
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">Game</div>
                <div class="info-value" th:text="${txn.gameName}">Liên Minh Huyền Thoại</div>
              </div>
              <div class="info-item">
                <div class="info-label">Rank</div>
                <div class="info-value" th:text="${txn.accountRank}">Gold III</div>
              </div>
              <div class="info-item">
                <div class="info-label">Người mua</div>
                <div class="info-value">
                  <span th:text="${txn.buyerUsername}">buyer123</span>
                  <small style="color: #6c757d;" th:text="'(' + ${txn.buyerEmail} + ')'">(buyer@example.com)</small>
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">Người bán</div>
                <div class="info-value" th:text="${txn.sellerUsername}">seller456</div>
              </div>
              <div class="info-item">
                <div class="info-label">Số tiền listing</div>
                <div class="info-value" th:text="${#numbers.formatInteger(txn.amount, 3, 'POINT')} + ' VNĐ'">500,000 VNĐ</div>
              </div>
              <div class="info-item">
                <div class="info-label">Phí platform (10%)</div>
                <div class="info-value" th:text="${#numbers.formatInteger(txn.commission, 3, 'POINT')} + ' VNĐ'">50,000 VNĐ</div>
              </div>
              <div class="info-item">
                <div class="info-label">Tổng cộng</div>
                <div class="info-value total-amount" th:text="${#numbers.formatInteger(txn.totalAmount, 3, 'POINT')} + ' VNĐ'">550,000 VNĐ</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="actions">
          <form th:action="@{/admin/payments/{id}/approve(id=${txn.id})}" method="post" style="display: inline;">
            <button type="submit" class="btn-approve">✓ Duyệt & Gửi tài khoản</button>
          </form>

          <button onclick="showRejectForm(${txn.id})" class="btn-reject">✗ Từ chối</button>

          <!-- Reject Form (hidden by default) -->
          <div th:id="'reject-form-' + ${txn.id}" style="display: none; margin-top: 10px;">
            <form th:action="@{/admin/payments/{id}/reject(id=${txn.id})}" method="post">
              <input type="text"
                     name="reason"
                     placeholder="Lý do từ chối..."
                     required
                     style="padding: 8px; width: 300px; border: 1px solid #ced4da; border-radius: 4px;" />
              <button type="submit" class="btn-reject" style="padding: 8px 15px;">Xác nhận từ chối</button>
              <button type="button" onclick="hideRejectForm(${txn.id})" style="padding: 8px 15px; background: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer;">Hủy</button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div th:if="${pendingTransactions == null or pendingTransactions.empty}" class="empty-state">
      <h2>Không có giao dịch nào chờ xác nhận</h2>
      <p>Tất cả thanh toán đã được xử lý!</p>
    </div>
  </div>

  <script>
    function showRejectForm(id) {
      document.getElementById('reject-form-' + id).style.display = 'block';
    }

    function hideRejectForm(id) {
      document.getElementById('reject-form-' + id).style.display = 'none';
    }
  </script>
</body>
</html>
```

### Testing Checklist

- [ ] Admin can access /admin/payments
- [ ] Page shows all pending transactions
- [ ] Each transaction displays complete information
- [ ] Approve button works correctly
- [ ] Approve updates transaction to VERIFIED
- [ ] Approve updates listing to SOLD
- [ ] Approve sends email with credentials
- [ ] Reject button works correctly
- [ ] Reject requires reason input
- [ ] Reject updates transaction to REJECTED
- [ ] Reject sends rejection email
- [ ] Email failures don't block status updates
- [ ] Success/error messages display correctly
- [ ] Empty state shows when no pending transactions
- [ ] Non-admin users cannot access page

### Error Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| Transaction not found | Error: "Transaction not found" |
| Transaction not pending | Error: "Transaction is not pending" |
| Email send fails | Warning logged, status still updated |
| Buyer email not found | Error logged, status still updated |
| Listing not found | Error: "Listing not found" |

### Security Considerations

- Only ADMIN role can access payment verification
- CSRF protection on all POST requests
- Credentials are sensitive - handle securely
- Log all approval/rejection actions
- Audit trail for all payment verifications

### References

- [Source: planning-artifacts/epics.md#Story 3.2: Admin Approve/Reject Transaction & Send Emails]
- [Story 2.7: Listing Email Notifications - EmailService pattern]
- [Story 3.1: Buy Now + VietQR Payment - Transaction entity]
- [Story 2.1: Create Listing - Credential fields]

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (glm-4.6)

### Completion Notes List

- **Story Status:** New story - not yet implemented
- **Dependencies:** Stories 2.1, 2.7, 3.1 must be completed first
- **Configuration Required:** Gmail SMTP (already configured in Story 2.7)
- **Critical Feature:** This completes the payment flow

### File List

**Files to Create:**
- `src/main/java/com/gameaccountshop/dto/TransactionDisplayDto.java`
- `src/main/resources/templates/admin/payments.html`

**Files to Modify:**
- `src/main/java/com/gameaccountshop/repository/TransactionRepository.java` - Add findPendingForReview query
- `src/main/java/com/gameaccountshop/service/TransactionService.java` - Add approve/reject methods
- `src/main/java/com/gameaccountshop/service/EmailService.java` - Add payment email methods
- `src/main/java/com/gameaccountshop/controller/AdminController.java` - Add payment endpoints
- `src/main/java/com/gameaccountshop/entity/GameAccount.java` - Ensure soldAt is set correctly
