-- Create the genres table
CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    genre_name text NOT NULL UNIQUE,
    genre_id integer NOT NULL UNIQUE
);
