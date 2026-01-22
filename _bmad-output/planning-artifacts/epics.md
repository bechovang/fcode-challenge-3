---
stepsCompleted: [1, 2, 3]
inputDocuments:
  - prd.md
  - architecture.md
---

# fcode project - Epic Breakdown (Newbie MVP)

## Overview

This document provides the **simplified newbie MVP** epic and story breakdown for fcode project - a game account marketplace platform.

**Goal**: Build a working MVP with ~18 stories that's approachable for beginners while still delivering core value.

**Technology Stack**: Spring Boot 3.5.0, Java 17, MySQL 8.0, Bootstrap 5, Thymeleaf

## MVP Scope Summary

| Feature | Stories | Complexity |
|---------|---------|------------|
| Basic Authentication | 3 | Easy |
| Listings & Ratings | 7 | Easy-Medium |
| Simple Buying with PayOS + Email | 5 | Medium |
| Dashboard & Profiles | 3 | Easy-Medium |
| **Total** | **18** | **Newbie-Friendly** |

## Epic List

### Epic 1: Basic Authentication
Establish the foundation - user accounts and secure access. Every feature depends on this.

### Epic 2: Listings & Ratings
The core marketplace - sellers list accounts with images and credentials, buyers browse with search/filter, ratings build trust. Email notifications for listing approvals/rejections.

### Epic 3: Simple Buying + Seller Tools
Complete the transaction flow with PayOS payment gateway and automated credential delivery via email. Sellers manage listings with filters and track revenue. Admin processes payouts to sellers. Buyers rate and review purchases.

### Epic 4: Dashboard & Profiles
Give sellers a profile page to showcase their listings and ratings, and admins platform oversight.

---

## Epic 1: Basic Authentication

Enable users to register accounts, log in securely, and access the system. This epic establishes the foundational authentication layer that all other features depend on.

### Story 1.1: Initialize Spring Boot Project

As a **developer**,
I want **to initialize the Spring Boot project with all required dependencies**,
So that **the team has a working foundation to build upon**.

**Acceptance Criteria:**

**Given** the project uses Spring Initializr
**When** I execute the initialization command
**Then** a Spring Boot 3.5.0 project is created with Java 17
**And** the project includes dependencies: web, data-jpa, security, mysql, validation, thymeleaf
**And** the package structure is com.gameaccountshop
**And** Maven wrapper is included
**And** the project runs on port 8080
**And** MySQL connection is configured in application.yml

**Technical Notes:**
- Command: `spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf --build=maven --java-version=17 --boot-version=3.5.0 --package-name=com.gameaccountshop game-account-shop`
- Database: MySQL 8.0, database name: gameaccountshop
- Default admin account should be created after first run

---

### Story 1.2: User Registration & Login

As a **guest user**,
I want **to register a new account and log in**,
So that **I can access the platform**.

**Acceptance Criteria:**

**Given** I am on the registration page
**When** I submit valid username (unique), password (min 6 chars), and email
**Then** a new user account is created
**And** the password is hashed using BCrypt (10 rounds)
**And** the user is assigned role "USER"
**And** I am redirected to the home page
**And** a success message "Đăng ký thành công!" is displayed

**Given** I am a registered user
**When** I submit valid username and password
**Then** I am authenticated successfully
**And** a session is created
**And** I am redirected to the home page
**And** my username is displayed in the navigation

**Given** I submit invalid username or password
**When** I attempt to log in
**Then** an error message "Tên đăng nhập hoặc mật khẩu không đúng" is displayed
**And** I remain on the login page

**Given** I have been inactive for 30 minutes
**When** I attempt to perform any action
**Then** my session has expired
**And** I am redirected to the login page

**Given** I am logged in
**When** I click the logout button
**Then** my session is invalidated
**And** I am redirected to the home page
**And** a success message "Đăng xuất thành công!" is displayed

---

### Story 1.3: Default Admin Account

As a **system administrator**,
I want **a default admin account to be created when the system first starts**,
So that **I can access admin features without manual database setup**.

**Acceptance Criteria:**

**Given** the database is empty (no users exist)
**When** the application starts for the first time
**Then** a default admin account is created
**And** username is "admin"
**And** password is "admin123"
**And** role is "ADMIN"
**And** a startup log message indicates default admin was created

**Given** the database already has users
**When** the application starts
**Then** no additional admin accounts are created
**And** the existing users remain unchanged

---

## Epic 2: Listings & Ratings

Enable sellers to create game account listings and buyers to discover them through search and filters. Simple rating system builds trust.

### Story 2.1: Create Listing (Simplified)

As a **logged-in user (seller)**,
I want **to create a new game account listing with credentials**,
So that **buyers can see my account for sale and admin can deliver credentials after purchase**.

**Acceptance Criteria:**

**Given** I am logged in as a USER
**When** I access the create listing page
**Then** I see a form with fields:
  - Game Name (text input - e.g., "Liên Minh Huyền Thoại", "Valorant")
  - Rank/Level (text input - e.g., "Gold", "Diamond")
  - Price (number in VNĐ)
  - Description (textarea)
  - Account Username (text input - the game account username buyer will receive)
  - Account Password (text input - the game account password buyer will receive)

**Given** I fill in all required fields with valid data
**When** I submit the listing
**Then** a new GameAccount is created in the database
**And** the status is set to "PENDING"
**And** seller_id is set to my user ID
**And** account_username and account_password are stored securely
**And** created_at timestamp is set
**And** a success message "Đăng bán thành công! Chờ admin duyệt." is displayed
**And** I am redirected to the home page

**Given** I leave any required field empty
**When** I attempt to submit
**Then** an error message "Vui lòng điền đầy đủ thông tin" is displayed
**And** no listing is created

**Given** I enter a price less than or equal to 0
**When** I attempt to submit
**Then** an error message "Giá bán phải lớn hơn 0" is displayed

**Technical Notes:**
- account_username and account_password are stored in game_accounts table
- These credentials will be auto-emailed to buyer after payment approval (Story 3.2)
- Password should be masked in UI (input type="password")

---

### Story 2.2: Browse Listings with Search/Filter

As a **guest or buyer**,
I want **to browse and search listings**,
So that **I can find accounts I want to buy**.

**Acceptance Criteria:**

**Given** I am on the home page or listings page
**When** the page loads
**Then** I see all listings with status = "APPROVED"
**And** each listing card displays:
  - Image thumbnail (downscaled from uploaded image)
  - Game Name
  - Rank/Level
  - Price
  - Seller username
**And** listings are ordered by created_at (newest first)

**Given** I want to search for a specific game
**When** I type in the search box (e.g., "Liên Minh")
**Then** I see only listings matching the game name
**And** search uses SQL LIKE: `WHERE game_name LIKE '%search%'`

**Given** I want to filter by rank
**When** I select a rank from the dropdown
**Then** I see only listings with that rank

**Given** there are no approved listings matching my criteria
**When** I browse the listings page
**Then** a message "Không tìm thấy tài khoản nào" is displayed

**Technical Notes:**
- Search: `SELECT * FROM game_accounts WHERE game_name LIKE :keyword AND status = 'APPROVED'`
- Filter: `AND rank = :selectedRank`
- Both search and filter can work together

---

### Story 2.3: Listing Details with Simple Rating

As a **guest or buyer**,
I want **to view detailed information and ratings for a listing**,
So that **I can decide if I want to purchase it**.

**Acceptance Criteria:**

**Given** I am browsing listings
**When** I click on a listing
**Then** I am redirected to the listing detail page
**And** I see all listing information:
  - Full-size image
  - Game Name
  - Rank/Level
  - Price
  - Description
  - Seller's username
  - Seller's average rating (stars display ★★★★☆)
  - Number of ratings (e.g., "12 đánh giá")
**And** I see a "Mua ngay" (Buy Now) button
**And** the listing status is visible

**Given** the listing status is "SOLD"
**When** I view the detail page
**Then** the "Mua ngay" button is disabled or hidden
**And** a message "Tài khoản này đã được bán" is displayed

**Given** I am a logged-in user who has purchased this account
**When** I view the detail page
**Then** I can rate the seller from 1-5 stars
**And** I can optionally add a short comment

**Given** I submit a rating
**When** the form is submitted
**Then** a new Review record is created
**And** the seller's average rating is recalculated
**And** a success message "Cảm ơn đã đánh giá!" is displayed

**Technical Notes:**
- Rating display: Show stars with Unicode characters ★★★★☆
- Average calculation: `AVG(rating) FROM reviews WHERE seller_id = :sellerId`
- Reviews table: id, transaction_id, buyer_id, seller_id, rating (1-5), comment, created_at

---

### Story 2.4: Admin Approve/Reject Listings

As an **admin user**,
I want **to approve or reject pending listings**,
So that **the marketplace shows only quality listings**.

**Acceptance Criteria:**

**Given** I am logged in as ADMIN
**When** I navigate to the admin review page
**Then** I see all listings with status = "PENDING"
**And** each pending listing displays:
  - Game Name
  - Rank
  - Price
  - Seller username
  - Description
  - Created Date
**And** I see an "Approve" button for each listing
**And** I see a "Reject" button with reason input for each listing

**Given** I click the "Approve" button for a listing
**When** the approval completes
**Then** the listing status is updated to "APPROVED"
**And** the approved listing is removed from the review queue
**And** a success message "Đã duyệt tài khoản" is displayed
**And** the page refreshes to show remaining pending listings

**Given** I click the "Reject" button and enter a reason
**When** the rejection completes
**Then** the listing status is updated to "REJECTED"
**And** the rejection reason is stored
**And** the rejected listing is removed from the review queue
**And** a success message "Đã từ chối tài khoản" is displayed

**Given** there are no pending listings
**When** I navigate to the admin review page
**Then** a message "Không có tài khoản nào chờ duyệt" is displayed

---

### Story 2.5: Mark Listing as "Sold"

As an **admin or system**,
I want **to mark a listing as sold after purchase**,
So that **buyers know it's no longer available**.

**Acceptance Criteria:**

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

---

### Story 2.6: Image Upload for Listing

As a **seller creating a listing**,
I want **to upload an image showcasing the game account**,
So that **buyers can see visual proof of the account quality**.

**Acceptance Criteria:**

**Given** I am on the create listing page
**When** I view the form
**Then** I see an image upload field
**And** the field accepts image files (JPG, PNG)
**And** there is a preview area to show the uploaded image

**Given** I select an image file and submit the form
**When** the upload completes
**Then** the image is uploaded to ImgBB via API
**And** the image URL is stored in game_accounts.image_url
**And** a thumbnail is generated for the listing card
**And** the full-size image is available on the detail page

**Given** I do not upload an image
**When** I attempt to submit the form
**Then** an error message "Vui lòng tải lên ảnh minh họa" is displayed
**And** no listing is created

**Technical Notes:**
- ImgBB API: `POST https://api.imgbb.com/1/upload`
- API Key from application.yml: `imgbb.api-key`
- Convert image to Base64 before uploading
- Store returned URL in database
- Thumbnail: Use CSS to downscale image on listing cards (max-width: 200px)
- Max file size: 32MB (ImgBB limit)
- Form: `<input type="file" name="image" accept="image/*" required/>`

**application.yml configuration:**
```yaml
imgbb:
  api-key: 326b60d80445ca87cc53be21178a4c62
```

---

### Story 2.7: Listing Email Notifications

As a **seller**,
I want **to receive email notifications when my listing is approved or rejected**,
So that **I know the status of my listing without checking the website**.

**Acceptance Criteria:**

**Given** I am a seller who created a listing
**When** an admin approves my listing
**Then** I receive an email with subject "✅ Listing của bạn đã được duyệt!"
**And** the email contains:
  - Game name
  - Rank
  - Price
  - A message that my listing is now visible on the homepage
  - Link to view the listing

**Given** I am a seller who created a listing
**When** an admin rejects my listing
**Then** I receive an email with subject "❌ Listing của bạn đã bị từ chối"
**And** the email contains:
  - Game name
  - Rank
  - Price
  - The rejection reason
  - A message to fix and resubmit
  - Contact info for support

**Given** the email fails to send
**When** the admin approves or rejects a listing
**Then** an error message is displayed on the admin dashboard
**And** the admin is notified that the email was not delivered
**And** the listing status is still updated (email failure doesn't block the action)

**Technical Notes:**
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

---

## Epic 3: Simple Buying

Enable buyers to purchase game accounts using PayOS payment gateway with dynamic QR codes. Admin verifies payments and automatically sends account credentials via email. Buyers and sellers receive email notifications for all transaction status changes.

### Story 3.1: Buy Now & Show PayOS Payment

As a **logged-in buyer**,
I want **to click "Buy Now" and see a QR code with exact payment amount and description**,
So that **I can easily pay using my banking app**.

**Acceptance Criteria:**

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

**Technical Notes:**
- Transaction table: id, listing_id, buyer_id, seller_id, amount, commission, status, payment_link_id, created_at
- Commission = amount * 0.10
- Status flow: PENDING → VERIFIED → REJECTED
- PayOS API: `POST https://api-merchant.payos.vn/v2/payment-requests`
- Use PayOS SDK or REST API to create payment link
- Store paymentLinkId for status checking
- QR code can be rendered using QuickChart API: `https://quickchart.io/qr?text={qrCode}&size=300`
- Store transaction ID in session for reference

**application.yml configuration:**
```yaml
payos:
  client-id: ${PAYOS_CLIENT_ID:your-client-id}
  api-key: ${PAYOS_API_KEY:your-api-key}
  checksum-key: ${PAYOS_CHECKSUM_KEY:your-checksum-key}
  base-url: https://api-merchant.payos.vn
```

**PayOS Request Example:**
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

---

### Story 3.2: Admin Verify PayOS Payment & Send Emails

As an **admin**,
I want **to verify PayOS payment status and automatically send appropriate emails**,
So that **buyers receive their credentials or rejection reasons promptly**.

**Acceptance Criteria:**

**Given** A buyer has initiated a purchase
**When** I access the admin payment verification page
**Then** I see all transactions with status = "PENDING"
**And** each transaction displays:
  - Transaction ID
  - Payment Link ID (from PayOS)
  - Listing info (Game Name, Rank, Image)
  - Buyer username and email
  - Seller username
  - Amount
  - Created Date
**And** I see a "Check Payment Status" button for each transaction
**And** I see a "Verify & Send Credentials" button (enabled after payment is confirmed)
**And** I see a "Reject" button with reason input for each transaction

**Given** I click "Check Payment Status" for a transaction
**When** the PayOS API is called
**Then** the payment status from PayOS is retrieved
**And** if PayOS status is "PAID", the "Verify & Send Credentials" button is enabled
**And** if PayOS status is "PENDING", a message "Chờ thanh toán" is displayed
**And** if PayOS status is "CANCELLED", a message "Giao dịch đã bị hủy" is displayed

**Given** the payment is confirmed (PayOS status = "PAID")
**When** I click "Verify & Send Credentials"
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
- PayOS Status Check API: `GET https://api-merchant.payos.vn/v2/payment-requests/{paymentLinkId}`
- Headers: `x-client-id`, `x-api-key`
- Response statuses: PENDING, PAID, CANCELLED, EXPIRED
- Store paymentLinkId in transaction table for status checking
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

---

### Story 3.3: My Listings with Filtering + Revenue Display

As a **seller**,
I want **to view all my listings with filters and see my total earnings**,
So that **I can manage my inventory and track my revenue**.

**Acceptance Criteria:**

**Given** I am logged in as a USER
**When** I navigate to the "My Listings" page
**Then** I see all listings where seller_id = my user ID
**And** I can filter my listings by:
  - Status (All, Pending, Approved, Rejected, Sold)
  - Rank (Iron, Bronze, Silver, Gold, Platinum, Emerald, Diamond, Master, Grandmaster, Challenger)
**And** I see my revenue summary at the top:
  - Total Earnings: Sum of all SOLD listings (amount received)
  - Pending Earnings: Sum of listings that are SOLD but not yet paid out
**And** the revenue is displayed in VNĐ format (e.g., "5,500,000 VNĐ")

**Given** I select a status filter (e.g., "Approved")
**When** the filter is applied
**Then** only my listings with that status are displayed
**And** the filter selection is preserved

**Given** I select a rank filter
**When** the filter is applied
**Then** only my listings with that rank are displayed
**And** I can combine status and rank filters

**Given** I have no listings
**When** I navigate to "My Listings"
**Then** a message "Bạn chưa có tài khoản nào" is displayed
**And** a link to create a new listing is shown

**Given** I view my listings
**When** the page loads
**Then** each listing displays:
  - Image thumbnail
  - Game Name, Rank, Price
  - Status badge (Pending/Approved/Rejected/Sold)
  - Created date
  - Action button: View or Edit (if Pending)

**Technical Notes:**
- No separate dashboard - integrate into existing "My Listings" page
- Revenue calculation:
  - Total Earnings: `SELECT SUM(g.price) FROM game_accounts g WHERE g.seller_id = :sellerId AND g.status = 'SOLD'`
  - Pending Earnings: Calculate from payouts that haven't been marked as "Paid" yet
- Filter implementation: Use Thymeleaf with query parameters (e.g., `/my-listings?status=APPROVED&rank=Gold`)
- Filters use same pattern as home page search/filter

---

### Story 3.4: Admin Payout System

As an **admin**,
I want **to view sellers eligible for payout and mark them as paid after manual transfer**,
So that **sellers receive their earnings and get notified**.

**Acceptance Criteria:**

**Given** I am logged in as ADMIN
**When** I navigate to the admin payout page
**Then** I see a list of sellers who are eligible for payout
**And** a seller is eligible if they have:
  - At least one SOLD listing
  - Total earnings that haven't been paid out yet
**And** each seller in the list displays:
  - Username
  - Email
  - Total earnings to be paid (sum of unpaid sold listings)
  - Number of sold listings (unpaid)
  - Bank account info (if provided by seller)
  - "Mark as Paid" button

**Given** I click "Mark as Paid" for a seller
**When** the confirmation dialog appears
**Then** I see the payment details:
  - Seller username and email
  - Amount to transfer
  - Bank account information
**And** I confirm that I have performed the manual bank transfer

**Given** I confirm the payout
**When** the payout is processed
**Then** a Payout record is created with status "PAID"
**And** the payout amount and timestamp are saved
**And** an email notification is sent to the seller
**And** the email contains:
  - Payout amount
  - Payout date
  - Transaction reference
  - Message to check their bank account
**And** a success message "Đã đánh dấu đã thanh toán và gửi email thông báo" is displayed

**Given** there are no sellers eligible for payout
**When** I navigate to the admin payout page
**Then** a message "Không có người bán nào chờ thanh toán" is displayed

**Technical Notes:**
- Create new `payouts` table:
  - id, seller_id, amount, status (PENDING, PAID), paid_at, created_at
- Payout calculation: Group unpaid sold listings by seller_id
- Manual bank transfer - no payment gateway integration for MVP
- Email notification uses existing EmailService
- Admin performs transfer offline (internet banking, branch visit, etc.)

**Database Schema:**
```sql
CREATE TABLE payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'PAID') NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Email Template - Payout Confirmation:**
```
Subject: 💰 Thanh toán tiền bán tài khoản thành công!

Chào bạn,

Chúng tôi đã chuyển khoản khoản tiền từ việc bán tài khoản:

- Số tiền: [Amount] VNĐ
- Ngày thanh toán: [Paid Date]
- Mã giao dịch: [Payout ID]

Vui lòng kiểm tra tài khoản ngân hàng của bạn.

Cảm ơn bạn đã tham gia cùng Game Account Shop!
```

---

### Story 3.5: Post-Purchase Rating System

As a **buyer who has purchased an account**,
I want **to rate and review the account I bought**,
So that **other buyers can see honest feedback**.

**Acceptance Criteria:**

**Given** I have purchased an account (transaction status = VERIFIED)
**When** I navigate to the "My Ratings" page
**Then** I see a list of all my purchased accounts
**And** each purchase displays:
  - Listing info (Game Name, Rank, Image)
  - Transaction date
  - Seller username
  - Rating status (Pending Review, Reviewed)

**Given** I have a purchase that hasn't been reviewed yet
**When** I view the "My Ratings" page
**Then** I see a "Đánh giá ngay" (Rate Now) button
**And** the button is displayed prominently for pending reviews

**Given** I click "Đánh giá ngay" for a purchase
**When** the rating form appears
**Then** I see:
  - The listing information (Game Name, Rank, Price)
  - Star rating (1-5 stars, clickable)
  - Comment textarea (optional, max 500 characters)
  - Submit button

**Given** I select a star rating and optionally add a comment
**When** I submit the review
**Then** a new Review record is created
**And** the review links to the transaction
**And** the seller's average rating is recalculated
**And** a success message "Cảm ơn đã đánh giá!" is displayed
**And** the purchase status changes to "Reviewed"

**Given** I have already reviewed a purchase
**When** I view the "My Ratings" page
**Then** I see my existing rating and comment
**And** I do NOT see the "Đánh giá ngay" button (or it's disabled)
**And** A message "Đã đánh giá" is displayed

**Given** I have no purchased accounts
**When** I navigate to "My Ratings"
**Then** a message "Bạn chưa mua tài khoản nào" is displayed

**Technical Notes:**
- Query: `SELECT t.*, g.game_name, g.rank, g.image_url, g.price FROM transactions t JOIN game_accounts g ON t.listing_id = g.id WHERE t.buyer_id = :buyerId AND t.status = 'VERIFIED' ORDER BY t.created_at DESC`
- Check if reviewed: `SELECT COUNT(*) FROM reviews WHERE transaction_id = :transactionId`
- Only allow ONE review per transaction
- Rating page: `/my-ratings`
- Submit endpoint: `POST /ratings/submit`

**Important:** This story should be implemented **BEFORE** Story 4.2 (Seller Profile Page) because the profile page needs to display seller ratings, which depend on reviews being created.

---

## Epic 4: Dashboard & Profiles

Provide admins with platform oversight and sellers with a profile page to showcase their listings.

### Story 4.1: Simple Admin Dashboard

As an **admin user**,
I want **to view a dashboard with key statistics**,
So that **I can understand the platform's activity**.

**Acceptance Criteria:**

**Given** I am logged in as ADMIN
**When** I navigate to the admin dashboard
**Then** I see the following statistics cards:
  - Total Users (count from users table)
  - Total Listings (count from game_accounts table)
  - Pending Listings (count with status=PENDING)
  - Sold Listings (count with status=SOLD)
  - Total Transactions (count from transactions table)
  - Total Revenue (sum of all transaction amounts)

**And** I see a section with quick links:
  - "Xem Listings chờ duyệt" → Review queue
  - "Xem Thanh toán chờ xác nhận" → Payment verification
  - "Quản lý Users" → User list

**And** all statistics are calculated in real-time from the database

**And** The dashboard has a refresh button

**Technical Notes:**
- Simple queries: `SELECT COUNT(*) FROM users`
- Revenue: `SELECT SUM(amount) FROM transactions WHERE status = 'VERIFIED'`
- Use Bootstrap cards for nice visual layout

---

### Story 4.2: Seller Profile Page

> **Dependency:** This story depends on **Story 3.5 (Post-Purchase Rating System)** because the profile page displays seller ratings, which are created through the rating system.

As a **buyer or visitor**,
I want **to view a seller's profile and their listings**,
So that **I can trust the seller and see their other accounts**.

**Acceptance Criteria:**

**Given** I am viewing a listing or search results
**When** I click on a seller's username
**Then** I am redirected to the seller's profile page
**And** I see the seller's information:
  - Username
  - Member since (registration date)
  - Average rating (★★★★☆)
  - Total sales count
  - Total listings count

**And** I see a section "Tài khoản đang bán" with all their APPROVED listings
**And** Each listing shows: Game Name, Rank, Price

**And** I see a section "Đã bán" with their SOLD listings count

**Given** the seller has been verified
**When** I view their profile
**Then** I see a "Đã xác thực" ✓ badge next to their name

**Technical Notes:**
- Query: `SELECT * FROM game_accounts WHERE seller_id = :sellerId AND status = 'APPROVED'`
- Calculate total sales: `COUNT(*) WHERE seller_id = :sellerId AND status = 'SOLD'`
- Profile URL: `/seller/{username}` or `/seller/{id}`

---

### Story 4.3: My Purchases Page (Optional)

As a **buyer**,
I want **to view my purchased accounts**,
So that **I can see what I've bought**.

**Acceptance Criteria:**

**Given** I am logged in as a USER
**When** I navigate to "Mua của tôi" (My Purchases) page
**Then** I see all transactions where buyer_id = my user ID
**And** each transaction displays:
  - Listing info (Game Name, Rank, Price)
  - Transaction date
  - Status (Pending, Verified, Completed)
  - Seller username

**Given** A transaction is verified and credentials sent
**When** I view my purchases
**Then** I see a "Đã gửi email thông tin" message for that transaction

**Given** A transaction is still pending
**When** I view my purchases
**Then** I see "Chờ admin xác nhận thanh toán" status

**Technical Notes:**
- Query: `SELECT t.*, g.game_name, g.rank FROM transactions t JOIN game_accounts g ON t.listing_id = g.id WHERE t.buyer_id = :buyerId ORDER BY t.created_at DESC`
- Order by date, newest first

---
