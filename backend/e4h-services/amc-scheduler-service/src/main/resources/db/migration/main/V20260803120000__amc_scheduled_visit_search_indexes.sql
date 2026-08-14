CREATE INDEX IF NOT EXISTS idx_scheduled_visits_status ON scheduled_visits(status);
CREATE INDEX IF NOT EXISTS idx_scheduled_visits_scheduled_date ON scheduled_visits(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_amc_configuration_geography_details_gin
    ON amc_configuration USING GIN (geography_details jsonb_path_ops);
