-- Step 1: Add the new column with default value
ALTER TABLE movie_infos_top_rated ADD COLUMN plot TEXT DEFAULT '';

