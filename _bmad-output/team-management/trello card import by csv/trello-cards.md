# Trello Cards - Game Account Shop MVP

**How to import to Trello:**
1. Create a new Trello board
2. Create lists: Backlog, Sprint 1, Sprint 2, Sprint 3, In Progress, Review, Done
3. Copy each card below to the appropriate list

---

## 📋 Epic 1: Project Setup & Authentication

### Card 1.1: Initialize Spring Boot Project
**Labels:** 🟢 Setup | ⚡ Critical Path
**Estimate:** 0.5 day
**Assigned:** Dev 1 (Backend)

**Description:**
Initialize the Spring Boot project using Spring Initializr with all required dependencies.

**Acceptance Criteria:**
- [ ] Spring Boot 3.5.0 project created with Java 17
- [ ] Dependencies: web, data-jpa, security, mysql, validation, thymeleaf
- [ ] Package: com.gameaccountshop
- [ ] Maven wrapper included
- [ ] Runs on port 8080
- [ ] MySQL connection configured

**Command:**
```bash
spring init --dependencies=web,data-jpa,security,mysql,validation,thymeleaf --build=maven --java-version=17 --boot-version=3.5.0 --package-name=com.gameaccountshop game-account-shop
```

**Checklist:**
- [ ] Run spring init command
- [ ] Configure application.yml
- [ ] Create database in MySQL
- [ ] Test server starts on localhost:8080

---

### Card 1.2: User Registration
**Labels:** 🔵 Backend | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 1 (Backend)

**Description:**
Create user registration with username, password, email. Password must be hashed with BCrypt.

**Acceptance Criteria:**
- [ ] User entity created
- [ ] UserRepository created
- [ ] UserService with BCrypt password hashing
- [ ] Registration form (register.html)
- [ ] AuthController with POST /auth/register
- [ ] Success message: "Đăng ký thành công!"
- [ ] Error for duplicate username
- [ ] Error for short password (<6 chars)
- [ ] Redirect to login after success

**Database Tables:**
- `users` (id, username, password, email, role, created_at)

**Checklist:**
- [ ] Create User entity
- [ ] Create UserRepository
- [ ] Create UserService with BCrypt
- [ ] Create register.html template
- [ ] Create AuthController
- [ ] Test: valid registration
- [ ] Test: duplicate username
- [ ] Test: short password

---

### Card 1.3: User Login
**Labels:** 🔵 Backend | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 1 (Backend)

**Description:**
Create login functionality with username/password authentication using Spring Security.

**Acceptance Criteria:**
- [ ] SecurityConfig created with BCrypt
- [ ] Login form (login.html)
- [ ] POST /auth/login endpoint
- [ ] Session creation on successful login
- [ ] 30-minute session timeout
- [ ] Error: "Tên đăng nhập hoặc mật khẩu không đúng"
- [ ] Redirect to home page after login
- [ ] Display username in navigation when logged in

**Checklist:**
- [ ] Configure Spring Security
- [ ] Create login.html template
- [ ] Implement login endpoint
- [ ] Configure session timeout (30 min)
- [ ] Test: valid login
- [ ] Test: invalid credentials
- [ ] Test: session timeout

---

### Card 1.4: User Logout
**Labels:** 🔵 Backend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 1 (Backend)

**Description:**
Implement logout functionality that invalidates the session.

**Acceptance Criteria:**
- [ ] Logout button in navigation
- [ ] POST /auth/logout endpoint
- [ ] Session invalidation
- [ ] Redirect to login page
- [ ] Success message: "Đăng xuất thành công!"

**Checklist:**
- [ ] Add logout button
- [ ] Implement logout endpoint
- [ ] Test: logout works
- [ ] Test: redirect to login
- [ ] Test: cannot access protected pages after logout

---

### Card 1.5: Default Admin Account
**Labels:** 🔵 Backend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 1 (Backend)

**Description:**
Create default admin account (admin/admin123) on first startup if database is empty.

**Acceptance Criteria:**
- [ ] Check if users table is empty on startup
- [ ] Create admin account if empty
- [ ] Username: admin, Password: admin123
- [ ] Role: ADMIN
- [ ] Log message when default admin created
- [ ] Don't create if users already exist

**Checklist:**
- [ ] Create startup logic
- [ ] Test: first run creates admin
- [ ] Test: subsequent runs don't duplicate

---

## 📋 Epic 2: Account Listings Management

### Card 2.1: Create Listing
**Labels:** 🟣 Full-Stack | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Allow logged-in users to create game account listings with game name, rank, level, price, description.

**Acceptance Criteria:**
- [ ] GameAccount entity created
- [ ] GameAccountRepository created
- [ ] ListingService created
- [ ] Create listing form (create-listing.html)
- [ ] POST /listings endpoint
- [ ] Status set to PENDING on creation
- [ ] seller_id set to current user
- [ ] Success message: "Đăng bán thành công! Chờ admin duyệt."
- [ ] Validation: all fields required
- [ ] Validation: price > 0

**Database Tables:**
- `game_accounts` (id, game_name, account_name, rank, level, price, description, status, seller_id, created_at, sold_at)

**Checklist:**
- [ ] Create GameAccount entity
- [ ] Create GameAccountRepository
- [ ] Create ListingService
- [ ] Create create-listing.html
- [ ] Implement POST endpoint
- [ ] Test: create listing with valid data
- [ ] Test: validation errors

---

### Card 2.2: View My Listings
**Labels:** 🟢 Frontend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Show logged-in user all their listings with status.

**Acceptance Criteria:**
- [ ] GET /my-listings endpoint
- [ ] Query: WHERE seller_id = current_user
- [ ] my-listings.html template
- [ ] Display: Game Name, Rank, Price, Status, Date
- [ ] Order by created_at DESC
- [ ] Message: "Bạn chưa có tài khoản nào" if empty

**Checklist:**
- [ ] Create endpoint
- [ ] Create my-listings.html
- [ ] Test: display listings
- [ ] Test: empty state

---

### Card 2.3: Browse Approved Listings
**Labels:** 🟢 Frontend | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Public page showing all approved listings.

**Acceptance Criteria:**
- [ ] GET /listings endpoint
- [ ] Query: WHERE status = 'APPROVED'
- [ ] listings.html template
- [ ] Display: Game Name, Rank, Price, Seller
- [ ] Order by created_at DESC
- [ ] Message: "Chưa có tài khoản nào" if empty
- [ ] Each listing links to detail page

**Checklist:**
- [ ] Create endpoint
- [ ] Create listings.html
- [ ] Test: display approved listings
- [ ] Test: empty state
- [ ] Test: link to details

---

### Card 2.4: Listing Details
**Labels:** 🟢 Frontend | 🔴 High Priority
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Show full details of a specific listing.

**Acceptance Criteria:**
- [ ] GET /listings/{id} endpoint
- [ ] listing-details.html template
- [ ] Display all listing information
- [ ] Show "Mua ngay" button (if APPROVED)
- [ ] Hide/disable button (if SOLD)
- [ ] Message: "Tài khoản này đã được bán" (if SOLD)

**Checklist:**
- [ ] Create endpoint
- [ ] Create listing-details.html
- [ ] Test: view approved listing
- [ ] Test: view sold listing

---

### Card 2.5: Admin Review Queue
**Labels:** 🟣 Full-Stack | 🔴 High Priority
**Estimate:** 0.5 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Admin page to view all pending listings for approval.

**Acceptance Criteria:**
- [ ] GET /admin/review endpoint (ADMIN only)
- [ ] Query: WHERE status = 'PENDING'
- [ ] admin-review.html template
- [ ] Display: Game Name, Rank, Price, Seller, Date
- [ ] Approve button for each listing
- [ ] Reject button for each listing
- [ ] Message: "Không có tài khoản nào chờ duyệt" if empty

**Checklist:**
- [ ] Create endpoint
- [ ] Create admin-review.html
- [ ] Add admin authorization
- [ ] Test: view pending listings

---

### Card 2.6: Approve Listing
**Labels:** 🟣 Full-Stack | 🔴 High Priority
**Estimate:** 0.5 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Admin can approve pending listings.

**Acceptance Criteria:**
- [ ] PUT /admin/listings/{id}/approve endpoint (ADMIN only)
- [ ] Update status to APPROVED
- [ ] Remove from review queue
- [ ] Success message: "Đã duyệt tài khoản"
- [ ] Refresh page to show remaining

**Checklist:**
- [ ] Create endpoint
- [ ] Test: approve listing
- [ ] Test: listing removed from queue

---

### Card 2.7: Reject Listing
**Labels:** 🟣 Full-Stack | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Admin can reject pending listings with a reason.

**Acceptance Criteria:**
- [ ] PUT /admin/listings/{id}/reject endpoint (ADMIN only)
- [ ] Prompt for rejection reason
- [ ] Update status to REJECTED
- [ ] Store rejection reason
- [ ] Remove from review queue
- [ ] Success message: "Đã từ chối tài khoản"

**Checklist:**
- [ ] Create endpoint
- [ ] Add rejection reason prompt
- [ ] Test: reject with reason
- [ ] Test: cancel reject

---

## 📋 Epic 3: Buying Process

### Card 3.1: Initiate Purchase
**Labels:** 🟣 Full-Stack | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Buyer clicks "Buy Now" to initiate purchase flow.

**Acceptance Criteria:**
- [ ] Transaction entity created
- [ ] TransactionRepository created
- [ ] POST /listings/{id}/buy endpoint (USER only)
- [ ] Create transaction record
- [ ] Set status to PENDING
- [ ] Calculate commission (10%)
- [ ] Redirect to payment page
- [ ] Cannot buy own listing
- [ ] Button hidden if not APPROVED

**Database Tables:**
- `transactions` (id, listing_id, buyer_id, seller_id, amount, commission, status, created_at)

**Checklist:**
- [ ] Create Transaction entity
- [ ] Create TransactionRepository
- [ ] Create TransactionService
- [ ] Implement buy endpoint
- [ ] Test: initiate purchase
- [ ] Test: cannot buy own listing

---

### Card 3.2: VNPay QR Code
**Labels:** 🔵 Backend | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 1 (Backend)

**Description:**
Generate VNPay QR code for payment.

**Acceptance Criteria:**
- [ ] payment.html template
- [ ] Display total amount (price + 10% fee)
- [ ] Generate VNPay QR code
- [ ] Display transaction ID
- [ ] Instructions: "Quét mã QR để thanh toán qua VNPay"

**Checklist:**
- [ ] Create payment.html
- [ ] Implement QR code generation
- [ ] Calculate total amount
- [ ] Test: QR code displays

---

### Card 3.3: Verify Payment (Manual)
**Labels:** 🟣 Full-Stack | 🔴 High Priority
**Estimate:** 0.5 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Admin manually verifies VNPay payments.

**Acceptance Criteria:**
- [ ] GET /admin/payments endpoint (ADMIN only)
- [ ] Query: WHERE status = 'PENDING'
- [ ] admin-payments.html template
- [ ] Display: Transaction ID, Buyer, Seller, Amount
- [ ] "Verify Payment" button
- [ ] Update status to VERIFIED
- [ ] Update listing to SOLD
- [ ] Set sold_at timestamp

**Checklist:**
- [ ] Create endpoint
- [ ] Create admin-payments.html
- [ ] Implement verify endpoint
- [ ] Test: verify payment
- [ ] Test: listing marked sold

---

### Card 3.4: Seller Deliver Credentials
**Labels:** 🟢 Frontend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Seller submits account credentials after payment verified.

**Acceptance Criteria:**
- [ ] GET /my-sold-listings endpoint
- [ ] "Gửi thông tin tài khoản" button
- [ ] credentials-form.html
- [ ] Fields: Username, Password, Notes
- [ ] Encrypt credentials before saving
- [ ] Update transaction to CREDENTIALS_SUBMITTED
- [ ] Success message: "Đã gửi thông tin tài khoản"

**Database Add:**
- `transactions` add columns: credentials_username, credentials_password, credentials_notes

**Checklist:**
- [ ] Add credential columns to transaction
- [ ] Create sold-listings page
- [ ] Create credentials form
- [ ] Implement encryption
- [ ] Test: submit credentials

---

### Card 3.5: Buyer Receive Credentials
**Labels:** 🟢 Frontend | 🔴 High Priority
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Buyer views credentials after admin verification.

**Acceptance Criteria:**
- [ ] GET /my-purchases endpoint
- [ ] Display credentials when VERIFIED + CREDENTIALS_SUBMITTED
- [ ] Show: Username, Password, Notes
- [ ] Warning: "Vui lòng đổi mật khẩu ngay sau khi đăng nhập"
- [ ] Update status to COMPLETED
- [ ] One-time display (hide after viewing)
- [ ] Show "Chờ xác nhận thanh toán" if pending

**Checklist:**
- [ ] Create purchases page
- [ ] Implement credential display
- [ ] Implement one-time view logic
- [ ] Test: receive credentials
- [ ] Test: pending state

---

## 📋 Epic 4: Admin Dashboard

### Card 4.1: Dashboard Overview
**Labels:** 🟢 Frontend | 🔴 High Priority
**Estimate:** 1 day
**Assigned:** Dev 3 (Full-Stack)

**Description:**
Admin dashboard with key platform statistics.

**Acceptance Criteria:**
- [ ] GET /admin/dashboard endpoint (ADMIN only)
- [ ] admin-dashboard.html template
- [ ] Display: Total users
- [ ] Display: Total listings
- [ ] Display: Pending listings count
- [ ] Display: Completed transactions
- [ ] Display: Total revenue
- [ ] Refresh button

**Checklist:**
- [ ] Create statistics queries
- [ ] Create dashboard endpoint
- [ ] Create dashboard.html
- [ ] Test: all statistics display

---

### Card 4.2: View All Users
**Labels:** 🟢 Frontend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Admin page to view all registered users.

**Acceptance Criteria:**
- [ ] GET /admin/users endpoint (ADMIN only)
- [ ] admin-users.html template
- [ ] Display: Username, Email, Role, Date
- [ ] Order by created_at DESC
- [ ] Pagination (20 per page)

**Checklist:**
- [ ] Create endpoint
- [ ] Create admin-users.html
- [ ] Implement pagination
- [ ] Test: display users

---

### Card 4.3: Transaction History
**Labels:** 🟢 Frontend | 🟡 Medium
**Estimate:** 0.5 day
**Assigned:** Dev 2 (Frontend)

**Description:**
Admin page to view all transactions with filtering.

**Acceptance Criteria:**
- [ ] GET /admin/transactions endpoint (ADMIN only)
- [ ] admin-transactions.html template
- [ ] Display: Transaction ID, Buyer, Seller, Amount, Status, Date
- [ ] Status filter dropdown (All, Pending, Verified, Completed)
- [ ] Pagination

**Checklist:**
- [ ] Create endpoint
- [ ] Create admin-transactions.html
- [ ] Implement status filter
- [ ] Test: display transactions
- [ ] Test: filter by status

---

### Card 4.4: Recent Activity Log
**Labels:** 🔵 Backend | 🟢 Low
**Estimate:** 0.5 day
**Assigned:** Dev 1 (Backend)

**Description:**
Show recent platform activity on dashboard.

**Acceptance Criteria:**
- [ ] Query last 10 activities
- [ ] Display on dashboard
- [ ] Show: timestamp, description
- [ ] Activities: registrations, listings, approvals, transactions
- [ ] Order by timestamp DESC

**For MVP:**
- Can query from existing tables (no separate activity_log table)

**Checklist:**
- [ ] Create activity query service
- [ ] Add activity section to dashboard
- [ ] Test: display activities

---

## 🎯 Sprint Planning Suggestions

### Sprint 1: Foundation (Week 1)
**Goal:** Complete authentication and project setup

**Cards:**
- 1.1: Initialize Project
- 1.2: User Registration
- 1.3: User Login
- 1.4: User Logout
- 1.5: Default Admin

**Success Criteria:**
- ✅ Project runs on localhost:8080
- ✅ Users can register and login
- ✅ Admin account created

---

### Sprint 2: Core Marketplace (Week 2)
**Goal:** Complete listing management

**Cards:**
- 2.1: Create Listing
- 2.2: My Listings
- 2.3: Browse Listings
- 2.4: Listing Details
- 2.5: Admin Review Queue
- 2.6: Approve Listing
- 2.7: Reject Listing

**Success Criteria:**
- ✅ Sellers can create listings
- ✅ Admin can approve/reject
- ✅ Buyers can browse listings

---

### Sprint 3: Transactions (Week 3)
**Goal:** Complete buying process

**Cards:**
- 3.1: Initiate Purchase
- 3.2: VNPay QR Code
- 3.3: Verify Payment
- 3.4: Deliver Credentials
- 3.5: Receive Credentials

**Success Criteria:**
- ✅ Buyers can purchase
- ✅ VNPay QR generated
- ✅ Credentials delivered

---

### Sprint 4: Admin & Polish (Week 4)
**Goal:** Complete dashboard and final touches

**Cards:**
- 4.1: Dashboard Overview
- 4.2: View All Users
- 4.3: Transaction History
- 4.4: Activity Log

**Success Criteria:**
- ✅ Admin dashboard complete
- ✅ All features tested
- ✅ Ready for demo

---

## 📝 Card Labels Legend

| Label Color | Meaning |
|-------------|---------|
| 🟢 Green | Frontend/UI work |
| 🔵 Blue | Backend/Logic work |
| 🟣 Purple | Full-Stack/Integration |
| 🔴 Red | High Priority/Critical |
| 🟡 Yellow | Medium Priority |
| ⚪ White | Low Priority |

---

## 🎯 Trello Board Setup

**Create these lists:**

```
┌─────────────┬──────────┬──────────┬──────────┬───────────┬───────┬─────┐
│  Backlog    │ Sprint 1 │ Sprint 2 │ Sprint 3 │ In Progress│ Review│ Done│
└─────────────┴──────────┴──────────┴──────────┴───────────┴───────┴─────┘
```

**Workflow:**
1. Start all cards in **Backlog**
2. Move to **Sprint X** when planning sprint
3. Move to **In Progress** when developer starts
4. Move to **Review** when developer thinks it's done
5. Move to **Done** after Team Lead approval

---

**Total Cards:** 21
**Estimated Duration:** 4 weeks (1 month) for 3 developers

**Good luck! 🚀**
