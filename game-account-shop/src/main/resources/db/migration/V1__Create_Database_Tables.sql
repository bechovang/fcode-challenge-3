-- ===================================================================
-- Game Account Shop - MVP Database Schema
-- Flyway Migration V1
-- ===================================================================
-- This migration creates all tables needed for the 14-story MVP
-- Designed for newbie developers - simple and straightforward
-- IMPORTANT: Database must be created BEFORE running this script
-- ===================================================================

-- ===================================================================
-- TABLE: users
-- Purpose: Store user accounts for authentication and authorization
-- Stories: Epic 1 (Basic Authentication)
-- ===================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    email VARCHAR(100),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User accounts for authentication - roles: USER, ADMIN';

-- ===================================================================
-- TABLE: game_accounts
-- Purpose: Store game account listings
-- Stories: Epic 2 (Listings & Ratings)
-- Status Flow: PENDING -> APPROVED -> SOLD (or REJECTED)
-- ===================================================================
CREATE TABLE game_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL COMMENT 'Foreign key to users.id',
    game_name VARCHAR(100) NOT NULL COMMENT 'e.g., Liên Minh Huyền Thoại',
    account_rank VARCHAR(50) NOT NULL COMMENT 'e.g., Gold, Diamond',
    price DECIMAL(12,2) NOT NULL CHECK (price > 0) COMMENT 'Price in VNĐ',
    description TEXT COMMENT 'Account details and description',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500) COMMENT 'Reason if listing was rejected',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sold_at TIMESTAMP NULL COMMENT 'Set when status becomes SOLD',
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_game_name (game_name) COMMENT 'For search by game name',
    INDEX idx_account_rank (account_rank) COMMENT 'For filter by rank',
    INDEX idx_created_at (created_at DESC) COMMENT 'For sorting newest first',
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Game account listings - statuses: PENDING, APPROVED, REJECTED, SOLD';

-- ===================================================================
-- TABLE: transactions
-- Purpose: Track purchase transactions and payment verification
-- Stories: Epic 3 (Simple Buying)
-- Status Flow: PENDING -> VERIFIED
-- ===================================================================
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL COMMENT 'Foreign key to game_accounts.id',
    buyer_id BIGINT NOT NULL COMMENT 'Foreign key to users.id (buyer)',
    seller_id BIGINT NOT NULL COMMENT 'Foreign key to users.id (seller)',
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0) COMMENT 'Listing price',
    commission DECIMAL(12,2) NOT NULL COMMENT 'Platform fee (10% of amount)',
    status ENUM('PENDING', 'VERIFIED') NOT NULL DEFAULT 'PENDING',
    account_username VARCHAR(100) COMMENT 'Game account credential (filled by admin)',
    account_password VARCHAR(255) COMMENT 'Game account credential (filled by admin)',
    account_notes TEXT COMMENT 'Additional notes for buyer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL COMMENT 'Set when admin verifies payment',
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC) COMMENT 'For sorting newest first',
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Purchase transactions - statuses: PENDING, VERIFIED';

-- ===================================================================
-- TABLE: reviews
-- Purpose: Store buyer ratings and reviews for sellers
-- Stories: Epic 2 (Listing Details with Simple Rating - Story 2.3)
-- ===================================================================
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE COMMENT 'Foreign key to transactions.id - one review per transaction',
    buyer_id BIGINT NOT NULL COMMENT 'Foreign key to users.id (reviewer)',
    seller_id BIGINT NOT NULL COMMENT 'Foreign key to users.id (seller being reviewed)',
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5) COMMENT 'Rating from 1 to 5 stars',
    comment TEXT COMMENT 'Optional review comment',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seller_id (seller_id) COMMENT 'For querying seller reviews',
    INDEX idx_transaction_id (transaction_id) COMMENT 'For checking if transaction has review',
    INDEX idx_rating (rating) COMMENT 'For filtering by rating',
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Buyer reviews and ratings for sellers - rating 1-5 stars';

-- ===================================================================
-- INSERT DEFAULT ADMIN ACCOUNT
-- Username: admin
-- Password: admin123 (BCrypt hashed - 10 rounds)
-- Role: ADMIN
-- ===================================================================
INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');

-- ===================================================================
-- MIGRATION COMPLETE
-- ===================================================================
-- Tables created: 4 (users, game_accounts, transactions, reviews)
-- Default admin account created
-- Ready for Spring Boot application startup
-- ===================================================================
