-- Soft delete for AMC configurations.
--
-- Removing a configuration must not destroy the visit history hanging off it: an APPROVED visit
-- carries a validated field report, and a PENDING_APPROVAL one carries a submission awaiting review.
-- Deactivating the configuration hides it (and its visits, via the joins in the search query
-- builders) while keeping every row on disk.

ALTER TABLE amc_configuration
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- 1. Nettoyage des doublons ACTIVE existants.
--    Pour chaque installation, seule la configuration
--    la plus récente reste ACTIVE.
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY tenant_id, facility_id, project_id, vendor_id
            ORDER BY created_time DESC, id DESC
        ) AS rn
    FROM amc_configuration
    WHERE status = 'ACTIVE'
)
UPDATE amc_configuration ac
SET status = 'EXPIRED', is_active = FALSE
    FROM ranked r
WHERE ac.id = r.id
  AND r.rn > 1;

-- The uniqueness rule must only apply to live configurations. Left as a full index, a facility whose
-- configuration was deactivated could never receive a new one: the INSERT would collide with the
-- deactivated row and - because persistence goes through Kafka - fail silently, with the API having
-- already answered 202.
DROP INDEX IF EXISTS ux_amc_configuration_unique_installation;

CREATE UNIQUE INDEX IF NOT EXISTS ux_amc_configuration_unique_installation
    ON amc_configuration (tenant_id, facility_id, project_id, vendor_id)
    WHERE status = 'ACTIVE';

-- Every search filters on it, and it is highly skewed towards TRUE, so index only the deactivated rows.
CREATE INDEX IF NOT EXISTS idx_amc_configuration_is_active
    ON amc_configuration (id)
    WHERE NOT is_active;
