ALTER TABLE amc_configuration_assignments
    ADD COLUMN IF NOT EXISTS role JSONB,
    ADD COLUMN IF NOT EXISTS additional_details JSONB,
    ADD COLUMN IF NOT EXISTS poc_number VARCHAR;
