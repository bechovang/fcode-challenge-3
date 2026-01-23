-- Add QR code and checkout URL columns to transactions table
-- Story 3.1: Buy Now & Show PayOS Payment
-- Review follow-up: Store actual PayOS QR code and checkout URL

-- Add checkout_url column for storing PayOS checkout URL
ALTER TABLE transactions
ADD COLUMN checkout_url VARCHAR(500);

-- Add qr_code column for storing PayOS QR code data
ALTER TABLE transactions
ADD COLUMN qr_code VARCHAR(1000);

-- Add index for checkout_url for faster lookups
ALTER TABLE transactions
ADD INDEX idx_checkout_url (checkout_url);
