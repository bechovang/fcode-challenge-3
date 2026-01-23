-- Add PayOS payment integration columns to transactions table
-- Story 3.1: Buy Now & Show PayOS Payment
-- Story 3.2: Admin Verify PayOS Payment & Send Emails

-- Add payment_link_id column for storing PayOS payment link ID
ALTER TABLE transactions
ADD COLUMN payment_link_id VARCHAR(100);

-- Add rejection_reason column for rejected transactions
ALTER TABLE transactions
ADD COLUMN rejection_reason VARCHAR(500);

-- Update status enum to include REJECTED status
-- Note: MySQL doesn't support directly modifying ENUM values, need to recreate column
ALTER TABLE transactions
MODIFY COLUMN status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING';

-- Add index for payment_link_id for faster status checking
ALTER TABLE transactions
ADD INDEX idx_payment_link_id (payment_link_id);
