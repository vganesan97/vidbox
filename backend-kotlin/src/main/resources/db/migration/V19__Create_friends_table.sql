CREATE TABLE friends (
    id SERIAL PRIMARY KEY,
    friendA integer NOT NULL,
    friendB integer NOT NULL,
    FOREIGN KEY (friendA) REFERENCES users(id),
    FOREIGN KEY (friendB) REFERENCES users(id)
);



