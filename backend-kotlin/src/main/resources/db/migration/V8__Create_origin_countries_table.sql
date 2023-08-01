-- Create origin_countries table
CREATE TABLE origin_countries (
    id SERIAL PRIMARY KEY,
    country_code TEXT UNIQUE
);