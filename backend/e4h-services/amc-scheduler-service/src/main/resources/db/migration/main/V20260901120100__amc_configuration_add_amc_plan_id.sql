--------------------------------------------------------------------
-- Link amc_configuration to its amc_plans row. Nullable for now: existing configurations were
-- created before AmcPlan existed and are not backfilled; new configurations from
-- /amcConfigurationBulkIngest always set it.
--------------------------------------------------------------------
ALTER TABLE amc_configuration ADD COLUMN IF NOT EXISTS amc_plan_id VARCHAR;

ALTER TABLE amc_configuration
    ADD CONSTRAINT fk_amcconf_amcplan
        FOREIGN KEY (amc_plan_id) REFERENCES amc_plans(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_amcconf_amc_plan_id ON amc_configuration(amc_plan_id);
