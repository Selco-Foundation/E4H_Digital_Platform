-- Soft delete for scheduled visits.
--
-- Regenerating a visit series (a change of durationMonths / visitFrequencyMonths, or an explicit
-- regenerateExisting) used to physically remove the not-yet-due visits. Deactivating them instead
-- keeps the trail of what had been planned and by whom, which matters when a schedule is disputed.

ALTER TABLE scheduled_visits
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- visit_number is user-facing ("Visit #2"), so the live series must stay contiguous and restart the
-- numbering after a regeneration. That is only possible if deactivated rows stop reserving their
-- number: as a plain UNIQUE constraint, regenerating would collide with the tombstones it just left
-- behind. Replaced by a partial unique index over live rows only.
ALTER TABLE scheduled_visits
    DROP CONSTRAINT IF EXISTS ux_scheduled_visits_unique_visit_per_amc;

CREATE UNIQUE INDEX IF NOT EXISTS ux_scheduled_visits_unique_visit_per_amc
    ON scheduled_visits (amc_configuration_id, visit_number)
    WHERE is_active;

-- Every visit search filters on it, and the column is heavily skewed towards TRUE.
CREATE INDEX IF NOT EXISTS idx_scheduled_visits_is_active
    ON scheduled_visits (id)
    WHERE NOT is_active;
