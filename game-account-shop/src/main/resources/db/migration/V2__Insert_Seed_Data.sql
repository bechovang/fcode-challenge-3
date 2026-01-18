-- ===================================================================
-- Game Account Shop - Seed Data for Development/Testing
-- Flyway Migration V2
-- ===================================================================
-- This migration inserts sample data for development and testing:
-- - Sample users (sellers) with BCrypt hashed passwords
-- - Sample APPROVED listings for Story 2.2 (Browse Listings)
-- - Sample PENDING listings for Story 2.4 (Admin Approve/Reject)
-- ===================================================================

-- ===================================================================
-- INSERT SAMPLE USERS (SELLERS)
-- Password for all test users: password123 (BCrypt hashed - 10 rounds)
-- BCrypt hash for "password123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ===================================================================
INSERT INTO users (username, password, email, role) VALUES
('seller1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller1@example.com', 'USER'),
('seller2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller2@example.com', 'USER'),
('seller3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller3@example.com', 'USER'),
('seller4', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller4@example.com', 'USER'),
('seller5', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'seller5@example.com', 'USER'),
('buyer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'buyer1@example.com', 'USER');

-- ===================================================================
-- INSERT APPROVED LISTINGS (For Story 2.2 - Browse Listings)
-- These listings have status = 'APPROVED' and will appear on browse page
-- Ordered by created_at to test sorting (newest first)
-- ===================================================================

-- Note: Using DATE_SUB to create different creation dates for testing
-- Adjust the dates relative to current date for realistic data

-- Listing 1: Iron rank - lowest tier, affordable price
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(2, 'Liên Minh Huyền Thoại', 'Iron IV', 50000,
'Account Iron IV, có 60 tướng. Đã chơi 3 mùa, không có chat restriction. Rank rất dễ leo cho người mới bắt đầu.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- Listing 2: Bronze rank - budget friendly
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(2, 'Liên Minh Huyền Thoại', 'Bronze II', 150000,
'Account Bronze II, full rune page cho cả ADC và Mid. Có 20 skin, trong đó có Project: Ashe.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 9 DAY));

-- Listing 3: Silver rank - mid tier
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(3, 'Liên Minh Huyền Thoại', 'Silver I', 300000,
'Account Silver I, chuyên tướng Yasuo 500k mastery. Có email gốc, đầy đủ thông tin đổi pass.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Listing 4: Gold rank - popular tier
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(3, 'Liên Minh Huyền Thoại', 'Gold III', 500000,
'Gold III - 85 tướng, 80 skin. Chơi xếp hạng ổn định, có thể leo Diamond dễ dàng. Tài khoản chính.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Listing 5: Gold rank - good price
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(4, 'Liên Minh Huyền Thoại', 'Gold II', 750000,
'Gold II 80LP, all champion. Full rune cho mọi vị trí. Account từ season 5, rất uy tín.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Listing 6: Platinum rank - high tier
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(4, 'Liên Minh Huyền Thoại', 'Platinum IV', 1200000,
'Platinum IV, 100+ tướng. Mùa trước Diamond, mùa này bận công việc nên không chơi. Rất đẹp cho leo rank.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Listing 7: Platinum rank - expensive
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(5, 'Liên Minh Huyền Thoại', 'Platinum II', 1500000,
'Platinum II - Challenger ở season 6. Full collection, rank rất cao. Thích hợp cho người chơi pro.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- Listing 8: Diamond rank - premium
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(5, 'Liên Minh Huyền Thoại', 'Diamond IV', 2000000,
'Diamond IV, 150 tướng, full rune. Account chính, không bao giờ bị ban. Có full email và thông tin.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Listing 9: Diamond rank - very expensive
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(2, 'Liên Minh Huyền Thoại', 'Diamond II', 2500000,
'Diamond II 90LP, main Zed với 1M mastery. Kèm thêm account Nexux Blitz luôn. Full skin legendary.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Listing 10: Master rank - top tier
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(3, 'Liên Minh Huyền Thoại', 'Master', 3500000,
'Master 50LP, top 0.5% server. Full tất cả champion, rune, page. Account đời lâu, uy tín tuyệt đối.',
'APPROVED', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Listing 11: Newest listing - just created
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(4, 'Liên Minh Huyền Thoại', 'Gold I', 600000,
'Gold I vừa qua hôm qua. Account mới, chưa từng mua RP. Full rune, 70 tướng. Giá tốt!',
'APPROVED', NOW());

-- ===================================================================
-- INSERT PENDING LISTINGS (For Story 2.4 - Admin Approve/Reject)
-- These listings have status = 'PENDING' and require admin approval
-- Ordered by created_at ASC (oldest first) - for FIFO processing
-- ===================================================================

-- Pending Listing 1: Oldest pending - should be reviewed first
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(5, 'Liên Minh Huyền Thoại', 'Emerald I', 1800000,
'Emerald I, main jungle. 120+ tướng. Account sạch, không có lịch sử phạt. Cần duyệt gấp để bán.',
'PENDING', DATE_SUB(NOW(), INTERVAL 12 DAY));

-- Pending Listing 2: Needs review
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(2, 'Liên Minh Huyền Thoại', 'Gold IV', 400000,
'Gold IV, 50 tướng. Account borrow từ bạn, không có email gốc. Giá rẻ cho ai muốn thử sức.',
'PENDING', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- Pending Listing 3: Recently created
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(3, 'Liên Minh Huyền Thoại', 'Silver III', 200000,
'Silver III, newbie friendly. 30 tướng cơ bản. Account ladder để luyện tay.',
'PENDING', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- Pending Listing 4: Just submitted
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, created_at) VALUES
(4, 'Liên Minh Huyền Thoại', 'Platinum I', 1800000,
'Platinum I, full collection. Account shop, đã từng bán 3 tài khoản thành công. Uy tín 5 sao.',
'PENDING', NOW());

-- ===================================================================
-- INSERT REJECTED LISTINGS (For testing Story 2.2 - should NOT appear)
-- These have status = 'REJECTED' and should be filtered out from browse page
-- ===================================================================

-- Rejected Listing 1: Rejected with reason
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, rejection_reason, created_at) VALUES
(5, 'Liên Minh Huyền Thoại', 'Bronze IV', 100000,
'Bronze IV, giá rẻ',
'REJECTED', 'Mô tả quá ngắn, không rõ ràng. Vui lòng bổ sung thông tin chi tiết.',
DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ===================================================================
-- INSERT SOLD LISTINGS (For testing Story 2.2 - should NOT appear)
-- These have status = 'SOLD' and should be filtered out from browse page
-- ===================================================================

-- Sold Listing 1: Already sold
INSERT INTO game_accounts (seller_id, game_name, account_rank, price, description, status, sold_at, created_at) VALUES
(2, 'Liên Minh Huyền Thoại', 'Silver II', 250000,
'Silver II - ĐÃ BÁN',
'SOLD',
DATE_SUB(NOW(), INTERVAL 5 DAY),
DATE_SUB(NOW(), INTERVAL 20 DAY));

-- ===================================================================
-- SEED DATA SUMMARY
-- ===================================================================
-- Users created: 7 (1 admin + 5 sellers + 1 buyer)
--    Password for all test users: password123
--    Admin: admin
--    Sellers: seller1, seller2, seller3, seller4, seller5
--    Buyer: buyer1
--
-- Game Accounts:
--    APPROVED: 11 listings (will appear on Story 2.2 browse page)
--       Ranks: Iron, Bronze (2), Silver, Gold (3), Platinum (2), Diamond (2), Master
--       Prices: 50,000 VNĐ to 3,500,000 VNĐ
--       Sellers: seller2 (3), seller3 (3), seller4 (2), seller5 (3)
--    PENDING: 4 listings (for Story 2.4 admin review)
--       Oldest to newest for FIFO processing
--    REJECTED: 1 listing (should NOT appear on browse page)
--    SOLD: 1 listing (should NOT appear on browse page)
-- ===================================================================

-- ===================================================================
-- TESTING LOGIN CREDENTIALS
-- ===================================================================
-- Admin:  username=admin,    password=admin123
-- Seller: username=seller1,  password=password123
-- Buyer:  username=buyer1,   password=password123
-- ===================================================================

-- ===================================================================
-- MIGRATION COMPLETE
-- ===================================================================
