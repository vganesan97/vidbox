-- Create tv_infos_top_rated table
CREATE TABLE tv_infos_top_rated (
    id SERIAL PRIMARY KEY,
    backdrop_path TEXT,
    first_air_date DATE,
    tv_id INTEGER,
    name TEXT,
    original_language TEXT,
    original_name TEXT,
    overview TEXT,
    popularity REAL,
    poster_path TEXT,
    vote_average REAL,
    vote_count INTEGER
);

