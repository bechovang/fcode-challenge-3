# Story 3.1: Buy Now & Show PayOS Payment

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

**And** I see a QR code image generated from PayOS API
**And** the QR code contains:
  - Exact total amount
  - Transaction ID as payment description
  - PayOS checkout link for direct payment

**And** I see payment instructions:
  - "Mở ứng dụng ngân hàng của bạn"
  - "Quét mã QR bên dưới để thanh toán"
  - "Hoặc nhấn vào link thanh toán trực tiếp"
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
  - [ ] Display QR code from PayOS (using QuickChart API)
  - [ ] Display PayOS checkout link
  - [ ] Display Vietnamese payment instructions
  - [ ] Show email notification message

- [ ] Add PayOS configuration (AC: #2)
  - [ ] Add PayOS config to application.yml
  - [ ] Configure client-id, api-key, checksum-key, base-url
  - [ ] Create PayOSConfig class to load configuration
  - [ ] Create PayOSService to call payment API and get QR code

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
│   └── PayOSService.java            # PayOS API integration
├── controller/
│   └── TransactionController.java    # HTTP endpoints
├── enums/
│   └── TransactionStatus.java        # PENDING, VERIFIED
└── config/
    └── PayOSConfig.java             # Configuration class

src/main/resources/
├── db/migration/
│   └── V2__Create_Transactions_Table.sql  # Flyway migration
└── templates/
    └── payment.html                  # Payment page template
```

**Configuration Update:**

```yaml
# application.yml
payos:
  client-id: ${PAYOS_CLIENT_ID:your-client-id}
  api-key: ${PAYOS_API_KEY:your-api-key}
  checksum-key: ${PAYOS_CHECKSUM_KEY:your-checksum-key}
  base-url: https://api-merchant.payos.vn
```

### Detailed Configuration Guide

#### Step 1: Get PayOS Credentials

1. Visit [https://my.payos.vn](https://my.payos.vn) and sign up/login
2. Navigate to **Payment Channels** section
3. Create a new payment channel or use existing one
4. Copy your credentials:
   - **Client ID** - Used for API authentication
   - **API Key** - Used for API authentication
   - **Checksum Key** - Used for HMAC SHA256 signature verification

#### Step 2: Set Environment Variables

**Create `.env` file** (in project root, DO NOT commit to git):

```bash
# PayOS Configuration
PAYOS_CLIENT_ID=your_actual_client_id_here
PAYOS_API_KEY=your_actual_api_key_here
PAYOS_CHECKSUM_KEY=your_actual_checksum_key_here
```

**Or set system environment variables:**

```bash
# Linux/Mac
export PAYOS_CLIENT_ID=your_actual_client_id_here
export PAYOS_API_KEY=your_actual_api_key_here
export PAYOS_CHECKSUM_KEY=your_actual_checksum_key_here

# Windows (Command Prompt)
set PAYOS_CLIENT_ID=your_actual_client_id_here
set PAYOS_API_KEY=your_actual_api_key_here
set PAYOS_CHECKSUM_KEY=your_actual_checksum_key_here

# Windows (PowerShell)
$env:PAYOS_CLIENT_ID="your_actual_client_id_here"
$env:PAYOS_API_KEY="your_actual_api_key_here"
$env:PAYOS_CHECKSUM_KEY="your_actual_checksum_key_here"
```

#### Step 3: Complete application.yml Example

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: gameaccountshop

  # Database Configuration
  datasource:
    url: jdbc:mysql://localhost:3306/gameaccountshop
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  # Mail Configuration (for sending credentials)
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# PayOS Payment Configuration
payos:
  # Client ID from my.payos.vn
  # Use environment variable in production
  client-id: ${PAYOS_CLIENT_ID:default-client-id}

  # API Key from my.payos.vn
  # Use environment variable in production
  api-key: ${PAYOS_API_KEY:default-api-key}

  # Checksum Key for signature verification
  # Use environment variable in production
  checksum-key: ${PAYOS_CHECKSUM_KEY:default-checksum-key}

  # PayOS API Base URL
  base-url: https://api-merchant.payos.vn

# Application Base URL (for email links)
app:
  base-url: ${APP_BASE_URL:http://localhost:8080}

# Logging
logging:
  level:
    com.gameaccountshop: INFO
    com.gameaccountshop.service.PayOSService: DEBUG
```

#### Step 4: PayOSConfig Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `client-id` | String | Yes | Your PayOS Client ID from my.payos.vn |
| `api-key` | String | Yes | Your PayOS API Key for authentication |
| `checksum-key` | String | Yes | Key for HMAC SHA256 signature verification |
| `base-url` | String | No | PayOS API base URL (default: https://api-merchant.payos.vn) |

#### Development vs Production Setup

**Development (local):**

```yaml
# application-dev.yml
payos:
  client-id: test-client-id-123
  api-key: test-api-key-456
  checksum-key: test-checksum-789
  base-url: https://api-merchant.payos.vn
```

**Production:**

```yaml
# application-prod.yml
payos:
  client-id: ${PAYOS_CLIENT_ID}
  api-key: ${PAYOS_API_KEY}
  checksum-key: ${PAYOS_CHECKSUM_KEY}
  base-url: ${PAYOS_BASE_URL:https://api-merchant.payos.vn}
```

#### Security Best Practices

**❌ NEVER:**
- Commit actual API keys to git repository
- Hardcode credentials in application.yml
- Share credentials in chat/email
- Use production credentials in development

**✅ ALWAYS:**
- Use environment variables for sensitive data
- Add `.env` to `.gitignore`
- Use different credentials for dev/staging/production
- Rotate credentials periodically
- Monitor API usage for suspicious activity

#### .gitignore Configuration

Make sure your `.gitignore` includes:

```gitignore
# Environment variables
.env
.env.local
.env.*.local

# Application configuration with secrets
application-prod.yml
application-*.yml
!application-example.yml
```

#### Create application-example.yml

For reference, create `application-example.yml` (safe to commit):

```yaml
# application-example.yml - Configuration template
# Copy this to application.yml and fill in your values

payos:
  # Get your credentials from https://my.payos.vn
  client-id: YOUR_CLIENT_ID_HERE
  api-key: YOUR_API_KEY_HERE
  checksum-key: YOUR_CHECKSUM_KEY_HERE
  base-url: https://api-merchant.payos.vn
```

### Technical Requirements

**PayOS API Endpoint:**
```
POST https://api-merchant.payos.vn/v2/payment-requests
```

**PayOS Request:**
```json
{
  "orderCode": 1234567890,
  "amount": 550000,
  "description": "Thanh toan don hang TXN123456",
  "items": [{
    "name": "Game Account Purchase",
    "quantity": 1,
    "price": 550000
  }]
}
```

**PayOS Response (QR Code):**
```json
{
  "code": "00",
  "desc": "success",
  "data": {
    "qrCode": "000201010212...",
    "checkoutUrl": "https://checkout.payos.vn/web/...",
    "paymentLinkId": "124c33293c934a85be5b7f8761a27a07"
  }
}
```

**QR Code Display (QuickChart API):**
```
https://quickchart.io/qr?text={qrCode}&size=300
```

**Implementation Notes:**
- Use orderCode as unique identifier (timestamp + transaction ID)
- Call PayOS API from backend service
- Store paymentLinkId for later status checking
- Display QR code using QuickChart API with PayOS QR string
- Also provide checkoutUrl as direct payment link option

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

@PayOSServiceTest
class PayOSServiceTest {
    - testCreatePaymentRequest()
    - testGenerateQRCode()
    - testGetPaymentLinkId()
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
- Story 3.1: Buy Now & Show PayOS Payment (lines 436-506)

**Source: docs/payos-integration.md**
- PayOS API Documentation
- Request/Response formats
- Configuration examples

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

---

## Implementation Code Examples

### Entity: Transaction

**Create `Transaction.java`:**

```java
package com.gameaccountshop.entity;

import com.gameaccountshop.enums.TransactionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "commission", nullable = false, precision = 12, scale = 2)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

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
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getPaymentLinkId() { return paymentLinkId; }
    public void setPaymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    // Helper method to get total amount
    public BigDecimal getTotalAmount() {
        return amount.add(commission);
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

### DTO: PayOS Response

**Create `PayOSResponse.java`:**

```java
package com.gameaccountshop.dto;

import lombok.Data;

@Data
public class PayOSResponse {
    private String code;
    private String desc;
    private PayOSData data;

    @Data
    public static class PayOSData {
        private String qrCode;
        private String checkoutUrl;
        private String paymentLinkId;
        private Integer orderCode;
        private Integer amount;
        private String status;
    }
}
```

### DTO: PayOS Request

**Create `PayOSRequest.java`:**

```java
package com.gameaccountshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class PayOSRequest {
    private Integer orderCode;
    private Integer amount;
    private String description;
    private String cancelUrl;
    private String returnUrl;
    private List<PayOSItem> items;

    @Data
    public static class PayOSItem {
        private String name;
        private Integer quantity;
        private Integer price;
    }
}
```

### Config: PayOSConfig

**Create `PayOSConfig.java`:**

```java
package com.gameaccountshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "payos")
public class PayOSConfig {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String baseUrl = "https://api-merchant.payos.vn";
}
```

### Service: PayOSService

**Create `PayOSService.java`:**

```java
package com.gameaccountshop.service;

import com.gameaccountshop.config.PayOSConfig;
import com.gameaccountshop.dto.PayOSRequest;
import com.gameaccountshop.dto.PayOSResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PayOSService {

    private final PayOSConfig config;
    private final RestTemplate restTemplate;

    public PayOSService(PayOSConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a payment request with PayOS
     */
    public PayOSResponse.PayOSData createPayment(Long amount, String transactionId, String description) {
        String url = config.getBaseUrl() + "/v2/payment-requests";

        // Generate unique order code (timestamp + transaction ID without TXN prefix)
        int orderCode = (int) (System.currentTimeMillis() / 1000);

        PayOSRequest request = new PayOSRequest();
        request.setOrderCode(orderCode);
        request.setAmount(amount.intValue());
        request.setDescription(description);

        // Add item
        PayOSRequest.PayOSItem item = new PayOSRequest.PayOSItem();
        item.setName("Game Account Purchase");
        item.setQuantity(1);
        item.setPrice(amount.intValue());
        request.setItems(List.of(item));

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", config.getClientId());
        headers.set("x-api-key", config.getApiKey());

        HttpEntity<PayOSRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PayOSResponse> response = restTemplate.postForEntity(url, entity, PayOSResponse.class);

            if (response.getBody() != null && "00".equals(response.getBody().getCode())) {
                log.info("PayOS payment created successfully for transaction: {}", transactionId);
                return response.getBody().getData();
            }

            log.error("PayOS API error: {}", response.getBody() != null ? response.getBody().getDesc() : "Unknown error");
            throw new RuntimeException("Failed to create PayOS payment: " +
                (response.getBody() != null ? response.getBody().getDesc() : "Unknown error"));

        } catch (Exception e) {
            log.error("Failed to call PayOS API", e);
            throw new RuntimeException("Payment service unavailable", e);
        }
    }

    /**
     * Check payment status
     */
    public String checkPaymentStatus(String paymentLinkId) {
        String url = config.getBaseUrl() + "/v2/payment-requests/" + paymentLinkId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", config.getClientId());
        headers.set("x-api-key", config.getApiKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PayOSResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, PayOSResponse.class);

            if (response.getBody() != null && response.getBody().getData() != null) {
                return response.getBody().getData().getStatus();
            }

            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Failed to check PayOS payment status", e);
            return "ERROR";
        }
    }
}
```

### Service: TransactionService

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

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final GameAccountRepository gameAccountRepository;
    private final PayOSService payOSService;

    public TransactionService(TransactionRepository transactionRepository,
                              GameAccountRepository gameAccountRepository,
                              PayOSService payOSService) {
        this.transactionRepository = transactionRepository;
        this.gameAccountRepository = gameAccountRepository;
        this.payOSService = payOSService;
    }

    /**
     * Create a new transaction for buying a listing
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
        BigDecimal commission = listing.getPrice().multiply(new BigDecimal("0.10"));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setListingId(listingId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(listing.getSellerId());
        transaction.setAmount(listing.getPrice());
        transaction.setCommission(commission);
        transaction.setStatus(TransactionStatus.PENDING);

        // Create PayOS payment and store payment link ID
        String description = "Thanh toan don hang " + listingId;
        var payOSData = payOSService.createPayment(
            transaction.getTotalAmount(),
            "TXN" + listingId,
            description
        );
        transaction.setPaymentLinkId(payOSData.getPaymentLinkId());

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
    public List<Transaction> getPendingTransactions() {
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
        var totalAmount = transaction.getTotalAmount();
        model.addAttribute("totalAmount", totalAmount);

        // QR code URL using QuickChart API
        // Note: In real implementation, call PayOS API to get QR code string
        // For now, use placeholder
        model.addAttribute("qrCodeUrl", "https://quickchart.io/qr?text=PAYMENT_DEMO&size=300");

        log.info("Payment page viewed for transaction: {}", transactionId);

        return "payment";
    }
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

    .checkout-link {
      display: inline-block;
      margin-top: 15px;
      padding: 12px 24px;
      background: #208084;
      color: white;
      text-decoration: none;
      border-radius: 6px;
      font-weight: bold;
    }

    .checkout-link:hover {
      background: #1a6a6e;
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

      <!-- QR Code from PayOS (via QuickChart) -->
      <div class="qr-container">
        <h3>Quét mã QR để thanh toán</h3>
        <img th:src="${qrCodeUrl}"
             alt="PayOS Payment Code"
             class="qr-code" />
        <p style="margin-top: 15px; color: #666;">
          Hoặc nhấn vào link bên dưới để thanh toán trực tiếp
        </p>
      </div>

      <!-- Payment Instructions -->
      <div class="instructions">
        <h4>Hướng dẫn thanh toán:</h4>
        <ol>
          <li>Mở ứng dụng ngân hàng của bạn</li>
          <li>Quét mã QR bên trên hoặc nhấn link thanh toán trực tiếp</li>
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

**Add "Buy Now" button:**

```html
<!-- Buy Button (for APPROVED listings, not own listing) -->
<div th:if="${listing.status.name() == 'APPROVED' && listing.sellerId != currentUser.id}">
  <form th:action="@{/listings/{id}/buy(id=${listing.id})}" method="post">
    <button type="submit" class="btn-buy">Mua ngay</button>
  </form>
</div>
```

### Configuration: application.yml

**Add PayOS configuration:**

```yaml
# PayOS Payment Configuration
payos:
  # Get credentials from https://my.payos.vn
  # Use environment variables in production
  client-id: ${PAYOS_CLIENT_ID:your-client-id}
  api-key: ${PAYOS_API_KEY:your-api-key}
  checksum-key: ${PAYOS_CHECKSUM_KEY:your-checksum-key}
  base-url: https://api-merchant.payos.vn
```

**See "Configuration Update" section above for detailed setup guide.**

### Migration: V2__Create_Transactions_Table.sql

**Create Flyway migration:**

```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    commission DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    payment_link_id VARCHAR(100),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_payment_link_id (payment_link_id),
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Testing Checklist

- [ ] Clicking "Buy Now" creates a transaction
- [ ] Transaction has correct buyer_id, seller_id, listing_id
- [ ] Commission is calculated correctly (10%)
- [ ] Status is set to PENDING
- [ ] PayOS payment link is generated and stored
- [ ] User is redirected to payment page
- [ ] Payment page shows correct details
- [ ] QR code is generated using QuickChart API
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
| PayOS API failure | Error: "Payment service unavailable" |

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101 (or current model)

### Debug Log References

### Completion Notes List

### File List

