CREATE TABLE friend_requests (
    id SERIAL PRIMARY KEY,
    requester integer NOT NULL,
    requested integer NOT NULL,
    status text NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (requester) REFERENCES users(id),
    FOREIGN KEY (requested) REFERENCES users(id)
);



