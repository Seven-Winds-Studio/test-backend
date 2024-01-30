ALTER TABLE Budget
    ADD COLUMN author_id INT REFERENCES Author (id);