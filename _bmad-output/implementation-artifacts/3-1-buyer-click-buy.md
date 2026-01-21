# Story 3.1: Buyer Click Buy & Show VietQR Payment

Status: ready-for-dev

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

## Tasks / Subtasks

- [ ] Create Transaction entity and repository (AC: #1)
  - [ ] Create Transaction entity with all required fields
  - [ ] Create TransactionStatus enum (PENDING, VERIFIED)
  - [ ] Create TransactionRepository interface
  - [ ] Add Flyway migration for transactions table

- [ ] Create TransactionService (AC: #1)
  - [ ] Create transaction when buyer clicks "Mua ngay"
  - [ ] Calculate 10% commission on listing price
  - [ ] Set status to PENDING
  - [ ] Capture listing_id, buyer_id, seller_id, amount, commission
  - [ ] Store transaction ID in session for reference

- [ ] Create TransactionController (AC: #1, #2)
  - [ ] POST /transactions/create/{listingId} endpoint
  - [ ] Validate user is logged in
  - [ ] Validate listing status is APPROVED
  - [ ] Validate buyer is not the seller
  - [ ] Create transaction and redirect to payment page
  - [ ] GET /transactions/payment/{transactionId} endpoint
  - [ ] Show payment details with QR code

- [ ] Create payment page template (AC: #2)
  - [ ] Create payment.html Thymeleaf template
  - [ ] Display listing price, platform fee, total amount
  - [ ] Display transaction ID
  - [ ] Display QR code from VietQR.net
  - [ ] Display Vietnamese payment instructions
  - [ ] Show email notification message

- [ ] Add VietQR configuration (AC: #2)
  - [ ] Add VietQR config to application.yml
  - [ ] Configure bank-id, account-no, account-name
  - [ ] Create VietQRConfig class to load configuration
  - [ ] Create VietQRService to generate QR code URLs

- [ ] Update listing detail page (AC: #3, #4)
  - [ ] Add "Mua ngay" button for APPROVED listings
  - [ ] Hide button for non-APPROVED listings
  - [ ] Hide button for sellers viewing their own listings
  - [ ] Add link to POST /transactions/create/{listingId}

- [ ] Add validation and error handling (AC: #1, #3, #4)
  - [ ] Handle unauthenticated users
  - [ ] Handle non-APPROVED listings
  - [ ] Handle seller buying own listing
  - [ ] Handle sold listings
  - [ ] Show Vietnamese error messages

- [ ] Testing (AC: All)
  - [ ] Test transaction creation on "Mua ngay" click
  - [ ] Test commission calculation (10%)
  - [ ] Test QR code generation with correct parameters
  - [ ] Test payment page displays correctly
  - [ ] Test button visibility rules
  - [ ] Test error handling

---

## Dev Notes

### Previous Story Intelligence

**From Story 2.5 (Mark Listing as Sold):**
- GameAccount has status field with SOLD state
- sold_at timestamp is set when listing is sold
- SOLD listings don't show "Mua ngay" button

**From Story 2.7 (Listing Email Notifications):**
- EmailService is configured with Spring Boot Mail
- EmailService uses JavaMailSender with Gmail SMTP
- EmailService has @Async methods for non-blocking email sending
- application.yml has spring.mail configuration
- baseUrl is configured as app.base-url for email links

**Key Patterns to Follow:**
- Use TransactionService for business logic
- Use TransactionRepository for data access
- Use TransactionDTO for API responses (not entity)
- Return Vietnamese error messages
- Use @ControllerAdvice for global exception handling

### Architecture Compliance

**Layer Architecture:**
```
Browser → TransactionController → TransactionService → TransactionRepository → Database
```

**Entity Design (ID-Based Navigation):**
- Transaction entity uses ID references: listing_id, buyer_id, seller_id
- NO @ManyToOne relationships to User or GameAccount
- Use repository queries to fetch related entities when needed

**Security Requirements:**
- Endpoint requires authenticated user (ROLE_USER or ROLE_ADMIN)
- Use @PreAuthorize("isAuthenticated()") on controller methods
- Validate buyer is not the seller in service layer

**Database Schema:**
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    commission DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'VERIFIED') NOT NULL DEFAULT 'PENDING',
    account_username VARCHAR(100),
    account_password VARCHAR(255),
    account_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### File Structure Requirements

**New Files to Create:**
```
src/main/java/com/gameaccountshop/
├── entity/
│   └── Transaction.java              # JPA entity
├── dto/
│   ├── TransactionDTO.java           # API response DTO
│   └── TransactionCreateRequest.java # Request DTO (if needed)
├── repository/
│   └── TransactionRepository.java    # Spring Data JPA repository
├── service/
│   ├── TransactionService.java       # Business logic
│   └── VietQRService.java            # QR code URL generation
├── controller/
│   └── TransactionController.java    # HTTP endpoints
├── enums/
│   └── TransactionStatus.java        # PENDING, VERIFIED
└── config/
    └── VietQRConfig.java             # Configuration class

src/main/resources/
├── db/migration/
│   └── V2__Create_Transactions_Table.sql  # Flyway migration
└── templates/
    └── payment.html                  # Payment page template
```

**Configuration Update:**
```yaml
# application.yml
vietqr:
  bank-id: 970415
  account-no: 113366668888
  account-name: Your Name
```

### Technical Requirements

**VietQR QR Code URL Format:**
```
https://img.vietqr.io/image/{bankId}-{accountNo}-compact.png?amount={amount}&addInfo={description}
```

**Example:**
```
https://img.vietqr.io/image/970415-113366668888-compact.png?amount=550000&addInfo=TXN123456
```

**Implementation Notes:**
- URL encode the description (transaction ID) parameter
- Use URLEncoder.encode(transactionId, StandardCharsets.UTF_8)
- Display QR code as `<img>` tag in payment template

**Commission Calculation:**
```java
BigDecimal commission = amount.multiply(new BigDecimal("0.10"));
```

**Transaction ID Format:**
- Use auto-increment database ID as transaction ID
- Format: "TXN" + id (e.g., "TXN1", "TXN2")
- Or use database ID directly for simplicity

### Integration Points

**Dependencies:**
- GameAccountRepository (to fetch listing details)
- UserRepository (to fetch buyer/seller info)
- ListingService (to validate listing status)
- EmailService (already configured from Story 2.7)

**Session Management:**
- Store transaction ID in session after creation
- Use for displaying payment page and tracking

### Testing Requirements

**Unit Tests:**
```java
@TransactionalTest
class TransactionServiceTest {
    - testCreateTransaction_Success()
    - testCalculateCommission_10Percent()
    - testValidateSellerCannotBuyOwnListing()
    - testValidateListingMustBeApproved()
    - testCalculateTotalAmount()
}

@VietQRServiceTest
class VietQRServiceTest {
    - testGenerateQRCodeUrl()
    - testEncodeTransactionId()
    - testFormatAmount()
}
```

**Integration Tests:**
```java
@TransactionalControllerTest
class TransactionControllerTest {
    - testCreateTransaction_AsBuyer()
    - testCreateTransaction_FailsForSeller()
    - testCreateTransaction_FailsForNonApprovedListing()
    - testShowPaymentPage()
}
```

### References

**Source: epics.md**
- Epic 3: Simple Buying
- Story 3.1: Buy Now & Show VietQR Payment (lines 436-506)

**Source: architecture.md**
- Data Architecture: Entity Relationship Pattern (lines 194-264)
- Transaction entity definition (lines 240-251)
- API Naming Conventions (lines 688-718)
- Controller Architecture Pattern (lines 337-398)

**Source: project-context.md**
- Technology Stack (lines 15-36)
- Naming Conventions (lines 41-65)
- Architecture Pattern (lines 66-85)
- Security Requirements (lines 86-106)
- Error Handling (lines 108-129)
- API Response Pattern (lines 130-158)
- Entity Design Pattern (lines 160-193)
- Localization (lines 252-267)

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (or current model)

### Debug Log References

### Completion Notes List

### File List

