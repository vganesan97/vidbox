-- Create the movie_genre table
CREATE TABLE movie_genre (
    movie_id integer REFERENCES movie_infos_top_rated(id),
    genre_id integer REFERENCES genres(id),
    PRIMARY KEY (movie_id, genre_id)
);
