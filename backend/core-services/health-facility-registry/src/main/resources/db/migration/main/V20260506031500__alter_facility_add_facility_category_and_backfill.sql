ALTER TABLE facility
    ADD COLUMN IF NOT EXISTS facility_category VARCHAR;

ALTER TABLE facility
    ADD COLUMN IF NOT EXISTS facility_poc_username VARCHAR;

UPDATE facility
SET facility_category = 'HEALTH'
WHERE facility_category IS NULL;

