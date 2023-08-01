-- Add new columns to movie_infos_top_rated
ALTER TABLE movie_infos_top_rated
    ADD COLUMN adult boolean,
    ADD COLUMN original_language text,
    ADD COLUMN original_title text,
    ADD COLUMN popularity real,
    ADD COLUMN video boolean,
    ADD COLUMN vote_average real,
    ADD COLUMN vote_count integer;
