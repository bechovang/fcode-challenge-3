-- Add credential and image columns to game_accounts table
-- Story 2.1: Create Listing with Credentials
-- Story 2.6: Image Upload for Listing
-- These fields allow sellers to enter game account credentials and upload images when creating a listing

ALTER TABLE game_accounts
ADD COLUMN account_username VARCHAR(100),
ADD COLUMN account_password VARCHAR(100),
ADD COLUMN image_url VARCHAR(500);
