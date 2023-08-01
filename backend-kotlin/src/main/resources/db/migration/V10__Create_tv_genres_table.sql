-- Assuming genres table already exists
-- Create tv_genres table (join table)
CREATE TABLE tv_genres (
    tv_id INTEGER REFERENCES tv_infos_top_rated(id),
    genre_id INTEGER REFERENCES genres(id),
    PRIMARY KEY(tv_id, genre_id)
);