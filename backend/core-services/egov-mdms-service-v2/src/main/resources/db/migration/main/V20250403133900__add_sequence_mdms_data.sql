
ALTER TABLE eg_mdms_data
ALTER COLUMN id SET DEFAULT gen_random_uuid()::VARCHAR;

UPDATE eg_mdms_data
SET id = gen_random_uuid()::VARCHAR
WHERE id IS NULL;
