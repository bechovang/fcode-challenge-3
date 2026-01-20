-- ===================================================================
-- Update Seed Data with Placeholder Images
-- Flyway Migration V4
-- ===================================================================
-- This migration runs AFTER V3 adds the image_url column
-- Updates all existing seed data with placeholder image
-- ===================================================================
--
-- Migration Order:
-- V1: Create tables (without image_url)
-- V2: Insert seed data (without image_url)
-- V3: Add image_url column
-- V4: Update seed data with images (this file)
--
-- Placeholder image: https://i.ibb.co/c7CMbcy/placeholder.png (direct link)
-- ===================================================================

-- Update all APPROVED listings with placeholder image
UPDATE game_accounts SET image_url = 'https://i.ibb.co/c7CMbcy/placeholder.png' WHERE status = 'APPROVED';

-- Update all PENDING listings with placeholder image
UPDATE game_accounts SET image_url = 'https://i.ibb.co/c7CMbcy/placeholder.png' WHERE status = 'PENDING';

-- Update all REJECTED listings with placeholder image
UPDATE game_accounts SET image_url = 'https://i.ibb.co/c7CMbcy/placeholder.png' WHERE status = 'REJECTED';

-- Update all SOLD listings with placeholder image
UPDATE game_accounts SET image_url = 'https://i.ibb.co/c7CMbcy/placeholder.png' WHERE status = 'SOLD';

-- ===================================================================
-- MIGRATION COMPLETE
-- ===================================================================
-- All 17 seed data listings now have placeholder image
-- Image URL (direct link): https://i.ibb.co/c7CMbcy/placeholder.png
-- ===================================================================
