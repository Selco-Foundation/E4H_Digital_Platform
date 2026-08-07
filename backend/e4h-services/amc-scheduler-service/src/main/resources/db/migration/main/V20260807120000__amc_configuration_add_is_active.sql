-- Soft delete for AMC configurations.
--
-- Removing a configuration must not destroy the visit history hanging off it: an APPROVED visit
-- carries a validated field report, and a PENDING_APPROVAL one carries a submission awaiting review.
-- Deactivating the configuration hides it (and its visits, via the joins in the search query
-- builders) while keeping every row on disk.

ALTER TABLE amc_configuration
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- The uniqueness rule must only apply to live configurations. Left as a full index, a facility whose
-- configuration was deactivated could never receive a new one: the INSERT would collide with the
-- deactivated row and - because persistence goes through Kafka - fail silently, with the API having
-- already answered 202.
DROP INDEX IF EXISTS ux_amc_configuration_unique_installation;

CREATE UNIQUE INDEX IF NOT EXISTS ux_amc_configuration_unique_installation
    ON amc_configuration (tenant_id, facility_id, project_id, vendor_id)
    WHERE is_active;

-- Every search filters on it, and it is highly skewed towards TRUE, so index only the deactivated rows.
CREATE INDEX IF NOT EXISTS idx_amc_configuration_is_active
    ON amc_configuration (id)
    WHERE NOT is_active;
