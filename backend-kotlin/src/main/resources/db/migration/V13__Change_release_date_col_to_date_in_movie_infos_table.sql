ALTER TABLE movie_infos_top_rated
    ALTER COLUMN release_date TYPE DATE USING TO_DATE(release_date::text, 'YYYY');

