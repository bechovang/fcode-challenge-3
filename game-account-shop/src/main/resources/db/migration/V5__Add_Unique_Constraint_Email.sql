-- Add unique constraint to email column in users table
-- Issue #7: Prevent duplicate email registration

-- First, remove any duplicate emails (keep the first occurrence)
DELETE u1 FROM users u1
INNER JOIN users u2
WHERE u1.id > u2.id
  AND u1.email = u2.email
  AND u1.email IS NOT NULL
  AND u1.email != '';

-- Add unique constraint on email column
ALTER TABLE users
ADD UNIQUE INDEX idx_users_email_unique (email);

-- Note: NULL values are allowed in unique constraints in MySQL,
-- but empty strings ('') will be treated as duplicates
