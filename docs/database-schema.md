# Database Schema - MVP (Newbie-Friendly)

**Game Account Shop Platform - Simplified MVP**

---

## Overview

This database is designed for **beginner developers** building a Game Account Marketplace MVP. It supports exactly **14 stories** across **4 epics** with minimal complexity.

**Database:** `gameaccountshop` (MySQL 8.0)
**Character Set:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`

---

## Design Principles for Newbie Developers

1. **Simple Tables:** Only 4 tables, each with a clear purpose
2. **No Complex Relationships:** Use ID references, not ORM joins
3. **Clear Naming:** `snake_case` for everything in database
4. **Minimal Fields:** Only what's needed for the stories
5. **Explicit Status:** Use ENUM for easy-to-understand states

---

## Database Diagram

```
┌──────────────┐
┌──────────────┐
│    users     │
│──────────────│
│ id (PK)      │────┐
│ username     │    │
│ password     │    │
│ email        │    │
│ role         │    │
│ created_at   │    │
└──────────────┘    │
                    │
                    │      ┌──────────────────┐
                    │      │  game_accounts   │
                    ├─────▶│──────────────────│
                    │      │ id (PK)          │
                    │      │ seller_id (FK)   │
                    │      │ game_name        │
                    │      │ account_rank     │
                    │      │ price            │
                    │      │ description      │
                    │      │ status           │
                    │      │ rejection_reason │
                    │      │ created_at       │
                    │      │ sold_at          │
                    │      └──────────────────┘
                    │                │
                    │                │
                    │      ┌─────────────────┐
                    │      │  transactions   │
                    │      │─────────────────│
                    │      │ id (PK)         │
                    │      │ listing_id (FK) │
                    │      │ buyer_id (FK)   │
                    │      │ seller_id (FK)  │
                    │      │ amount          │
                    │      │ commission      │
                    │      │ status          │
                    │      │ account_username│
                    │      │ account_password│
                    │      │ account_notes   │
                    │      │ created_at      │
                    │      │ verified_at     │
                    │      └─────────────────┘
                    │                │
                    │                │
                    │      ┌─────────────────┐
                    │      │    reviews      │
                    │      │─────────────────│
                    │      │ id (PK)         │
                    │      │ transaction_id  │
                    │      │ buyer_id (FK)   │
                    │      │ seller_id (FK)  │
                    │      │ rating (1-5)     │
                    │      │ comment         │
                    │      │ created_at      │
                    │      └─────────────────┘
                    │
                    └────────── All FKs reference users.id
```

---

## Table Definitions

### 1. users

**Purpose:** Store user accounts for login, registration, and role-based access.

**Stories:** Epic 1 (Basic Authentication)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `username` | VARCHAR(50) | Unique username (e.g., "gamer123") |
| `password` | VARCHAR(255) | BCrypt hashed password |
| `email` | VARCHAR(100) | Email address |
| `role` | ENUM | **USER** or **ADMIN** |
| `created_at` | TIMESTAMP | Account creation date |

**SQL:**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Default Admin Account:**
- Username: `admin`
- Password: `admin123` (hashed with BCrypt)
- Role: `ADMIN`

---

### 2. game_accounts

**Purpose:** Store game account listings that sellers want to sell.

**Stories:** Epic 2 (Listings & Ratings)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `seller_id` | BIGINT | Foreign key to users.id (who created listing) |
| `game_name` | VARCHAR(100) | Game name (e.g., "Liên Minh Huyền Thoại") |
| `account_rank` | VARCHAR(50) | Rank/Level (e.g., "Gold", "Diamond") |
| `price` | DECIMAL(12,2) | Price in VNĐ (e.g., 500000.00) |
| `description` | TEXT | Account description/details |
| `status` | ENUM | **PENDING** → **APPROVED** → **SOLD** (or **REJECTED**) |
| `rejection_reason` | VARCHAR(500) | Why listing was rejected (if applicable) |
| `created_at` | TIMESTAMP | When listing was created |
| `sold_at` | TIMESTAMP | When listing was sold (NULL until sold) |

**SQL:**
```sql
CREATE TABLE game_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    game_name VARCHAR(100) NOT NULL,
    account_rank VARCHAR(50) NOT NULL COMMENT 'e.g., Gold, Diamond',
    price DECIMAL(12,2) NOT NULL CHECK (price > 0),
    description TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP NULL,
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_game_name (game_name),
    INDEX idx_account_rank (account_rank),
    INDEX idx_created_at (created_at DESC),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Status Flow:**
```
PENDING (new listing)
    ↓ (admin approves)
APPROVED (visible in marketplace)
    ↓ (purchased)
SOLD (no longer available)

OR

PENDING
    ↓ (admin rejects)
REJECTED (with reason)
```

---

### 3. transactions

**Purpose:** Track purchase transactions and payment verification.

**Stories:** Epic 3 (Simple Buying)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `listing_id` | BIGINT | Foreign key to game_accounts.id |
| `buyer_id` | BIGINT | Foreign key to users.id (who is buying) |
| `seller_id` | BIGINT | Foreign key to users.id (who is selling) |
| `amount` | DECIMAL(12,2) | Listing price |
| `commission` | DECIMAL(12,2) | Platform fee (10% of amount) |
| `status` | ENUM | **PENDING** → **VERIFIED** |
| `account_username` | VARCHAR(100) | Game account credential (filled by admin) |
| `account_password` | VARCHAR(255) | Game account credential (filled by admin) |
| `account_notes` | TEXT | Additional notes for buyer |
| `created_at` | TIMESTAMP | When transaction was created |
| `verified_at` | TIMESTAMP | When admin verified payment |

**SQL:**
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

**Status Flow:**
```
PENDING (buyer clicked "Buy Now")
    ↓ (admin verified VNPay payment)
VERIFIED (credentials sent to buyer's email)
```

---

### 4. reviews

**Purpose:** Store buyer ratings and reviews for sellers.

**Stories:** Epic 2 (Listings & Ratings - Story 2.3)

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key, auto-increment |
| `transaction_id` | BIGINT | Foreign key to transactions.id (UNIQUE) |
| `buyer_id` | BIGINT | Foreign key to users.id (who wrote review) |
| `seller_id` | BIGINT | Foreign key to users.id (who was reviewed) |
| `rating` | INT | Rating from 1 to 5 stars |
| `comment` | TEXT | Optional review comment |
| `created_at` | TIMESTAMP | When review was created |

**SQL:**
```sql
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id),
    INDEX idx_transaction_id (transaction_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Epic to Table Mapping

| Epic | Stories | Tables Used |
|------|---------|-------------|
| **Epic 1: Basic Authentication** | 1.1, 1.2, 1.3 | `users` |
| **Epic 2: Listings & Ratings** | 2.1, 2.2, 2.3, 2.4, 2.5 | `game_accounts`, `reviews` |
| **Epic 3: Simple Buying** | 3.1, 3.2, 3.3 | `transactions`, `game_accounts` |
| **Epic 4: Dashboard & Profiles** | 4.1, 4.2, 4.3 | All tables (for queries) |

---

## Common Queries for Newbies

### Get All Approved Listings
```sql
SELECT ga.*, u.username as seller_name
FROM game_accounts ga
JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
ORDER BY ga.created_at DESC;
```

### Search Listings by Game Name
```sql
SELECT ga.*, u.username as seller_name
FROM game_accounts ga
JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
  AND ga.game_name LIKE '%Liên Minh%'
ORDER BY ga.created_at DESC;
```

### Filter Listings by Rank
```sql
SELECT ga.*, u.username as seller_name
FROM game_accounts ga
JOIN users u ON ga.seller_id = u.id
WHERE ga.status = 'APPROVED'
  AND ga.account_rank = 'Gold'
ORDER BY ga.created_at DESC;
```

### Get Seller's Average Rating
```sql
SELECT
    COUNT(*) as total_reviews,
    COALESCE(AVG(rating), 0) as average_rating
FROM reviews
WHERE seller_id = ?;
```

### Admin Dashboard Statistics
```sql
-- Total Users
SELECT COUNT(*) FROM users;

-- Total Listings
SELECT COUNT(*) FROM game_accounts;

-- Pending Listings
SELECT COUNT(*) FROM game_accounts WHERE status = 'PENDING';

-- Sold Listings
SELECT COUNT(*) FROM game_accounts WHERE status = 'SOLD';

-- Total Transactions
SELECT COUNT(*) FROM transactions;

-- Total Revenue
SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'VERIFIED';
```

### My Purchases
```sql
SELECT t.*, ga.game_name, ga.account_rank, u.username as seller_name
FROM transactions t
JOIN game_accounts ga ON t.listing_id = ga.id
JOIN users u ON t.seller_id = u.id
WHERE t.buyer_id = ?
ORDER BY t.created_at DESC;
```

---

## Summary for Newbie Developers

**Total Tables:** 4
- `users` - User accounts and authentication
- `game_accounts` - Game account listings
- `transactions` - Purchase tracking
- `reviews` - Seller ratings

**Foreign Keys:** All reference `users.id`
- Simple pattern: `{table}_id` points to `users.id`

**Status Enums:**
- `users.role`: USER, ADMIN
- `game_accounts.status`: PENDING, APPROVED, REJECTED, SOLD
- `transactions.status`: PENDING, VERIFIED
- `reviews.rating`: 1-5 (integer)

**Indexes:** Created automatically on foreign keys and frequently searched columns

---

**Next Steps:**
1. Run the Flyway migration script to create these tables
2. Start with Story 1.1 (Initialize Spring Boot Project)
3. Build entities in this order: User → GameAccount → Transaction → Review

Good luck with your MVP! 🚀
