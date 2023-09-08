BEGIN;

-- 1. Check if constraints or triggers are associated with the password column.
-- If so, drop them here.
-- Example: ALTER TABLE users DROP CONSTRAINT some_foreign_key_fkey;

-- 2. Drop the 'password' column from 'users' table
ALTER TABLE users DROP COLUMN password;

COMMIT;
