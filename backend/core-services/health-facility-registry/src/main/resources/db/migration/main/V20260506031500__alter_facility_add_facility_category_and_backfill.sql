ALTER TABLE facility
    ADD COLUMN IF NOT EXISTS facility_category VARCHAR;

UPDATE facility
SET facility_category = 'HEALTH'
WHERE facility_category IS NULL;
