-- Story 3.1: Wallet System - Make listing_id nullable for TOP_UP transactions
-- Top-up transactions don't have a listing_id, so we need to allow NULL values

-- First, drop the foreign key constraint
ALTER TABLE transactions DROP FOREIGN KEY transactions_ibfk_1;

-- Make listing_id nullable
ALTER TABLE transactions MODIFY COLUMN listing_id BIGINT NULL;

-- Re-add foreign key constraint with ON DELETE SET NULL
ALTER TABLE transactions
    ADD CONSTRAINT transactions_ibfk_1
    FOREIGN KEY (listing_id) REFERENCES game_accounts(id)
    ON DELETE SET NULL;
