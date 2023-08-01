BEGIN;

-- 1. Drop the foreign key constraint. You need to know the exact name of the constraint.
ALTER TABLE tv_origin_countries DROP CONSTRAINT tv_origin_countries_country_id_fkey;

-- 2. Rename the column.
ALTER TABLE tv_origin_countries RENAME COLUMN country_id TO country_code;

-- 3. Alter the data type of the column if necessary (replace NEW_TYPE with the actual type).
ALTER TABLE tv_origin_countries ALTER COLUMN country_code TYPE text;

COMMIT;
