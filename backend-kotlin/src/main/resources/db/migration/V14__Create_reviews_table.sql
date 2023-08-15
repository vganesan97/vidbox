CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    user_id integer REFERENCES users(id), -- Foreign key referencing the user table
    movie_id integer REFERENCES movie_infos_top_rated(id), -- Foreign key referencing the movies_top_info table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    review_content TEXT
);
