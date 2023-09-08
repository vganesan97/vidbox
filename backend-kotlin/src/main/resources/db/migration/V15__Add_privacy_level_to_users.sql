-- Step 1: Add the new column with default value
ALTER TABLE users ADD COLUMN privacy_level TEXT DEFAULT 'public';

-- Step 2: Update existing rows (optional, since the default value is already set)
UPDATE users SET privacy_level = 'public' WHERE privacy_level IS NULL;

