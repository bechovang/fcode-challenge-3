-- Add credential columns to game_accounts table
-- Story 2.1: Create Listing with Credentials
-- These fields allow sellers to enter game account credentials when creating a listing

ALTER TABLE game_accounts
ADD COLUMN account_username VARCHAR(100),
ADD COLUMN account_password VARCHAR(100);
