CREATE TABLE author
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255),
    date_time TIMESTAMP
);

ALTER TABLE budget
    ADD COLUMN author_id INT;

ALTER TABLE budget
    ADD CONSTRAINT fk_budget_author
        FOREIGN KEY (author_id)
            REFERENCES author(id);