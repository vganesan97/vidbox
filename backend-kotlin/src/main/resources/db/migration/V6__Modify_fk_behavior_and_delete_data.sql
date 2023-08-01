-- First, we need to drop the old constraint
ALTER TABLE movie_likes
    DROP CONSTRAINT movie_likes_movieid_fkey;

-- Then we can add a new constraint with the desired behavior
ALTER TABLE movie_likes
    ADD CONSTRAINT movie_likes_movieid_fkey
        FOREIGN KEY (movie_id) REFERENCES movie_infos_top_rated(id)
            ON DELETE SET NULL;

-- Delete all data from the movie_infos_top_rated table
DELETE FROM movie_infos_top_rated;
