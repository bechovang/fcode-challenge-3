# Story 3.2: Admin Approve/Reject Transaction & Send Emails

Status: ready-for-dev

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

## Tasks / Subtasks

- [ ] Create admin transaction verification page (AC: #1, #7)
  - [ ] Create admin-transactions.html Thymeleaf template
  - [ ] Fetch all PENDING transactions
  - [ ] Display transaction list with listing info, buyer, seller, amount
  - [ ] Add "Approve & Send Credentials" button for each transaction
  - [ ] Add "Reject" button with reason input for each transaction
  - [ ] Show "no pending transactions" message when list is empty
  - [ ] Add route in AdminController

- [ ] Update TransactionService (AC: #2, #3, #4, #5, #6)
  - [ ] Create approveTransaction(transactionId) method
  - [ ] Create rejectTransaction(transactionId, reason) method
  - [ ] When approving: update transaction to VERIFIED
  - [ ] When approving: update listing status to SOLD, set sold_at
  - [ ] When approving: retrieve credentials from game_accounts table
  - [ ] When approving: send email with credentials to buyer
  - [ ] When rejecting: update transaction to REJECTED, store reason
  - [ ] When rejecting: send rejection email to buyer
  - [ ] Handle email failures gracefully without blocking status updates

- [ ] Create email templates (AC: #2, #4)
  - [ ] Create payment approved email template (HTML)
  - [ ] Include account username and password
  - [ ] Include warning to change password immediately
  - [ ] Include transaction details (Game Name, Rank, Amount)
  - [ ] Create payment rejected email template (HTML)
  - [ ] Include transaction ID, amount, rejection reason
  - [ ] Include contact info for support

- [ ] Update EmailService (AC: #2, #4)
  - [ ] Create sendPaymentApprovedEmail method
  - [ ] Create sendPaymentRejectedEmail method
  - [ ] Use HTML formatted emails
  - [ ] Handle exceptions gracefully

- [ ] Update AdminController (AC: #2, #3, #4, #5, #6)
  - [ ] POST /admin/transactions/approve/{id} endpoint
  - [ ] POST /admin/transactions/reject/{id} endpoint
  - [ ] Show success/error messages for email delivery
  - [ ] Log email failures for admin visibility
  - [ ] Ensure transaction status updates even if email fails

- [ ] Add transaction status REJECTED (AC: #3, #4)
  - [ ] Update TransactionStatus enum to include REJECTED
  - [ ] Update database schema to add REJECTED status
  - [ ] Update migration script

- [ ] Testing (AC: All)
  - [ ] Test transaction approval with email delivery
  - [ ] Test transaction rejection with email delivery
  - [ ] Test email failure handling
  - [ ] Test listing status update to SOLD
  - [ ] Test sold_at timestamp setting
  - [ ] Test credentials retrieval and email content

---

## Dev Notes

### Previous Story Intelligence

**From Story 2.4 (Admin Approve/Reject Listings):**
- AdminController has approveListing() and rejectListing() methods
- Pattern: GET /admin/review-queue for pending items
- Pattern: POST /admin/listings/approve/{id} and POST /admin/listings/reject/{id}
- Success messages: "Đã duyệt tài khoản", "Đã từ chối tài khoản"

**From Story 2.5 (Mark Listing as Sold):**
- GameAccount has status field with SOLD state
- sold_at timestamp is set when listing is sold
- Status update: listing.setStatus(AccountStatus.SOLD); listing.setSoldAt(LocalDateTime.now())

**From Story 2.7 (Listing Email Notifications):**
- EmailService is configured with Spring Boot Mail
- EmailService uses JavaMailSender with Gmail SMTP
- EmailService has @Async methods for non-blocking email sending
- application.yml has spring.mail configuration
- AsyncConfiguration enables @Async support
- Email failure handling: log error, return without throwing

**From Story 3.1 (Buyer Click Buy):**
- Transaction entity created with PENDING status
- Transaction has listing_id, buyer_id, seller_id, amount, commission
- Transaction will have account_username, account_password fields (credentials from game_accounts)
- VietQR payment page created

**Key Patterns to Follow:**
- Use TransactionService for business logic
- Use EmailService for email sending
- Use @Async for non-blocking email sending
- Return Vietnamese success messages
- Handle email failures gracefully without blocking transaction updates

### Architecture Compliance

**Layer Architecture:**
```
Browser → AdminController → TransactionService → TransactionRepository → Database
                                         → EmailService → Buyer's Email
```

**Entity Design (ID-Based Navigation):**
- Transaction uses ID references: listing_id, buyer_id, seller_id
- To get credentials: fetch GameAccount by listing_id, read account_username, account_password

**Security Requirements:**
- Admin-only endpoints (ROLE_ADMIN required)
- Use @PreAuthorize("hasRole('ADMIN')") on controller methods
- Ensure only admins can approve/reject transactions

**Database Schema Updates:**
```sql
-- Add REJECTED status to TransactionStatus enum
ALTER TABLE transactions MODIFY COLUMN status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING';

-- Add rejection_reason column if not exists
ALTER TABLE transactions ADD COLUMN rejection_reason VARCHAR(500) AFTER account_notes;

-- Add verified_at column if not exists
ALTER TABLE transactions ADD COLUMN verified_at TIMESTAMP NULL AFTER created_at;
```

**Transaction Status Flow:**
```
PENDING (buyer clicked "Buy Now")
    ↓ (admin approves)
VERIFIED (credentials sent to buyer's email)

OR

PENDING
    ↓ (admin rejects)
REJECTED (rejection reason sent to buyer)
```

### File Structure Requirements

**Files to Create:**
```
src/main/java/com/gameaccountshop/
├── service/
│   └── EmailService.java              # Add payment email methods

src/main/resources/
├── templates/
│   └── admin-transactions.html        # Admin verification page
├── templates/email/
│   ├── payment-approved.html          # Payment approved email template
│   └── payment-rejected.html          # Payment rejected email template

src/main/resources/db/migration/
└── V3__Add_Transaction_Rejected_Status.sql  # Update TransactionStatus enum
```

**Files to Modify:**
```
src/main/java/com/gameaccountshop/
├── controller/
│   └── AdminController.java           # Add transaction approval endpoints
├── service/
│   └── TransactionService.java        # Add approve/reject methods
├── enums/
│   └── TransactionStatus.java         # Add REJECTED status
```

### Technical Requirements

**Email Templates (Vietnamese):**

**Payment Approved Email:**
```html
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

**Payment Rejected Email:**
```html
Subject: ❌ Giao dịch của bạn đã bị từ chối

Rất tiếc, giao dịch của bạn đã bị từ chối:

- Mã giao dịch: [Transaction ID]
- Số tiền: [Amount]

Lý do: [Rejection Reason]

Nếu bạn nghĩ đây là sự nhầm lẫn, vui lòng liên hệ admin kèm ảnh chụp thanh toán.
```

**Implementation Notes:**

**Approve Transaction Flow:**
```java
@Transactional
public void approveTransaction(Long transactionId) {
    // 1. Fetch transaction
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Giao dịch không tồn tại"));

    // 2. Update transaction status
    transaction.setStatus(TransactionStatus.VERIFIED);
    transaction.setVerifiedAt(LocalDateTime.now());
    transactionRepository.save(transaction);

    // 3. Update listing status to SOLD
    GameAccount listing = gameAccountRepository.findById(transaction.getListingId())
        .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
    listing.setStatus(AccountStatus.SOLD);
    listing.setSoldAt(LocalDateTime.now());
    gameAccountRepository.save(listing);

    // 4. Get buyer email
    User buyer = userRepository.findById(transaction.getBuyerId())
        .orElseThrow(() -> new ResourceNotFoundException("Người mua không tồn tại"));

    // 5. Send email with credentials
    emailService.sendPaymentApprovedEmail(
        buyer.getEmail(),
        listing.getGameName(),
        listing.getRank(),
        transaction.getAmount(),
        transaction.getId(),
        listing.getAccountUsername(),
        listing.getAccountPassword()
    );
}
```

**Reject Transaction Flow:**
```java
@Transactional
public void rejectTransaction(Long transactionId, String reason) {
    // 1. Fetch transaction
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Giao dịch không tồn tại"));

    // 2. Update transaction status
    transaction.setStatus(TransactionStatus.REJECTED);
    transaction.setRejectionReason(reason);
    transactionRepository.save(transaction);

    // 3. Get buyer email
    User buyer = userRepository.findById(transaction.getBuyerId())
        .orElseThrow(() -> new ResourceNotFoundException("Người mua không tồn tại"));

    // 4. Send rejection email
    emailService.sendPaymentRejectedEmail(
        buyer.getEmail(),
        transaction.getId(),
        transaction.getAmount(),
        reason
    );
}
```

**EmailService Pattern (from Story 2.7):**
```java
@Async
public void sendPaymentApprovedEmail(String to, String gameName, String rank,
                                     BigDecimal amount, Long transactionId,
                                     String accountUsername, String accountPassword) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🎉 Thanh toán thành công! Đây là thông tin tài khoản");

        String htmlContent = buildPaymentApprovedEmail(gameName, rank, amount,
                                                       transactionId, accountUsername, accountPassword);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Payment approved email sent to {}", to);
    } catch (MailException e) {
        log.error("Failed to send payment approved email to {}: {}", to, e.getMessage());
        // Don't throw - email failure shouldn't block transaction
    }
}
```

### Integration Points

**Dependencies:**
- TransactionRepository (to update transaction status)
- GameAccountRepository (to update listing status, get credentials)
- UserRepository (to get buyer email)
- EmailService (already configured from Story 2.7)

**Controller Endpoints:**
```
GET /admin/transactions/pending - Show pending transactions page
POST /admin/transactions/approve/{id} - Approve transaction and send credentials
POST /admin/transactions/reject/{id} - Reject transaction with reason
```

### Testing Requirements

**Unit Tests:**
```java
@TransactionalServiceTest
class TransactionServiceTest {
    - testApproveTransaction_UpdatesStatusToVerified()
    - testApproveTransaction_UpdatesListingToSold()
    - testApproveTransaction_SetsSoldAtTimestamp()
    - testApproveTransaction_SendsEmailWithCredentials()
    - testRejectTransaction_UpdatesStatusToRejected()
    - testRejectTransaction_StoresRejectionReason()
    - testRejectTransaction_SendsRejectionEmail()
    - testEmailFailure_DoesNotBlockTransactionUpdate()
}

@EmailServiceTest
class EmailServiceTest {
    - testSendPaymentApprovedEmail_Success()
    - testSendPaymentRejectedEmail_Success()
    - testEmailContent_ContainsAllRequiredFields()
}
```

**Integration Tests:**
```java
@AdminControllerTest
class AdminControllerTest {
    - testApproveTransaction_AsAdmin()
    - testRejectTransaction_AsAdmin()
    - testPendingTransactionsPage_RequiresAdminRole()
}
```

### References

**Source: epics.md**
- Epic 3: Simple Buying
- Story 3.2: Admin Approve/Reject Transaction & Send Emails (lines 509-614)

**Source: architecture.md**
- Data Architecture: Entity Relationship Pattern (lines 194-264)
- Email Service Integration (lines 461-516)
- Spring Events Pattern (lines 936-974)
- Error Handling Pattern (lines 1022-1060)

**Source: project-context.md**
- Technology Stack (lines 15-36)
- Security Requirements (lines 86-106)
- Error Handling (lines 108-129)
- Spring Events Pattern (lines 268-298)
- Localization (lines 252-267)

**Source: Story 2.7 (Listing Email Notifications)**
- EmailService implementation pattern
- AsyncConfiguration for @Async support
- Email template structure
- Gmail SMTP configuration

**Source: Story 2.4 (Admin Approve/Reject Listings)**
- AdminController pattern for approve/reject operations
- Success message pattern in Vietnamese

**Source: Story 2.5 (Mark Listing as Sold)**
- SOLD status update pattern
- sold_at timestamp setting pattern

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (or current model)

### Debug Log References

### Completion Notes List

### File List

