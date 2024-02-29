UPDATE budget
SET type = CASE
               WHEN type = 'Комиссия' THEN 'Расход'
               ELSE type
    END;
