-- Rectification sites get their own ICC pre-population template, so the same
-- fieldPlan + systemType + totalCapacity combination may hold both a regular and a rectification template.
ALTER TABLE field_plan_template ADD COLUMN IF NOT EXISTS rectification_site BOOLEAN NOT NULL DEFAULT FALSE;

DROP INDEX IF EXISTS uniq_field_plan_template_combo;

CREATE UNIQUE INDEX uniq_field_plan_template_combo
    ON field_plan_template(field_plan_id, system_type, total_capacity, rectification_site);
