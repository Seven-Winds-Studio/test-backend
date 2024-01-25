ALTER TABLE budget
    ADD COLUMN author_id int
        REFERENCES author (id) DEFAULT NULL;