-- Story 3.4: Admin Payout System
-- Create payouts table for monthly seller payout tracking

CREATE TABLE payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEEDS_PAYMENT',
    paid_at TIMESTAMP NULL,
    received_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_id BIGINT NULL COMMENT 'Admin who marked as paid',
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_seller_status_month (seller_id, status, created_at),
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add check constraint for valid status values
ALTER TABLE payouts ADD CONSTRAINT chk_payout_status
    CHECK (status IN ('NEEDS_PAYMENT', 'PAID', 'RECEIVED'));
