-- Story 3.1: Wallet System - Add transaction type and approval fields to transactions
ALTER TABLE transactions
    ADD COLUMN transaction_type VARCHAR(20) NULL COMMENT 'TOP_UP, PURCHASE, WITHDRAWAL, REFUND',
    ADD COLUMN approved_by BIGINT NULL COMMENT 'User ID of admin who approved/rejected',
    ADD COLUMN approved_at TIMESTAMP NULL COMMENT 'When the transaction was approved/rejected',
    ADD INDEX idx_transaction_type (transaction_type),
    ADD INDEX idx_approved_by (approved_by);
