-- Create tv_origin_countries table (join table)
CREATE TABLE tv_origin_countries (
    tv_id INTEGER REFERENCES tv_infos_top_rated(id),
    country_id INTEGER REFERENCES origin_countries(id),
    PRIMARY KEY(tv_id, country_id)
);