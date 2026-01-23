-- Story 3.1: Wallet System - Update TransactionStatus enum values
-- Change VERIFIED to COMPLETED for consistency with wallet-based flow

-- First, update any existing VERIFIED transactions to COMPLETED
UPDATE transactions SET status = 'COMPLETED' WHERE status = 'VERIFIED';

-- Modify the ENUM to use COMPLETED instead of VERIFIED
ALTER TABLE transactions MODIFY COLUMN status ENUM('PENDING', 'COMPLETED', 'REJECTED');
