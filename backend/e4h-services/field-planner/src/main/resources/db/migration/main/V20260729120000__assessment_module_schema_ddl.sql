-- Assessment module schema (LLD §5): field_plans.plan_type, facility_activities workflow columns,
-- field_plan_facilities handoff trail, eg_assessment_submission, optional assessment_plans view.
-- Status columns are nullable with no table default so existing installation facility_activities rows are unchanged.

-- ---------------------------------------------------------------------------
-- field_plans: distinguish assessment plans from installation field plans
-- ---------------------------------------------------------------------------
ALTER TABLE field_plans
    ADD COLUMN IF NOT EXISTS plan_type VARCHAR(32);

UPDATE field_plans
SET plan_type = 'FIELD_PLAN'
WHERE plan_type IS NULL;

ALTER TABLE field_plans
    ALTER COLUMN plan_type SET DEFAULT 'FIELD_PLAN',
    ALTER COLUMN plan_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_field_plans_tenant_project_plan_type
    ON field_plans (tenant_id, project_id, plan_type);

CREATE INDEX IF NOT EXISTS idx_field_plans_tenant_status
    ON field_plans (tenant_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uniq_field_plans_tenant_project_name
    ON field_plans (tenant_id, project_id, name)
    WHERE COALESCE(isdeleted, false) = false;

CREATE OR REPLACE VIEW assessment_plans AS
SELECT *
FROM field_plans
WHERE plan_type = 'ASSESSMENT';

-- ---------------------------------------------------------------------------
-- facility_activities: remote / on-site / overall assessment workflow + handoff
-- ---------------------------------------------------------------------------
ALTER TABLE facility_activities
    ADD COLUMN IF NOT EXISTS phone_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS field_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS overall_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS assessment_completion_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS installation_field_plan_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS field_plan_facility_id VARCHAR(64);

ALTER TABLE facility_activities
    ADD CONSTRAINT chk_facility_activities_phone_status
        CHECK (
            phone_status IS NULL
            OR phone_status IN (
                'PENDING',
                'PENDING_NO_ANSWER',
                'PENDING_WRONG_NUMBER',
                'QUALIFIED',
                'NOT_QUALIFIED'
            )
        ),
    ADD CONSTRAINT chk_facility_activities_field_status
        CHECK (
            field_status IS NULL
            OR field_status IN ('PENDING', 'QUALIFIED', 'NOT_QUALIFIED')
        ),
    ADD CONSTRAINT chk_facility_activities_overall_status
        CHECK (
            overall_status IS NULL
            OR overall_status IN ('PENDING', 'ELIGIBLE', 'NOT_ELIGIBLE')
        ),
    ADD CONSTRAINT chk_facility_activities_assessment_completion_status
        CHECK (
            assessment_completion_status IS NULL
            OR assessment_completion_status IN (
                'ENROLLED',
                'ELIGIBLE',
                'NOT_ELIGIBLE',
                'MOVED_TO_FIELD_PLAN',
                'EXPIRED'
            )
        ),
    ADD CONSTRAINT chk_facility_activities_handoff_installation_fp
        CHECK (
            assessment_completion_status IS DISTINCT FROM 'MOVED_TO_FIELD_PLAN'
            OR installation_field_plan_id IS NOT NULL
        );

ALTER TABLE facility_activities
    ADD CONSTRAINT fk_facility_activities_installation_field_plan
        FOREIGN KEY (installation_field_plan_id) REFERENCES field_plans (id),
    ADD CONSTRAINT fk_facility_activities_field_plan_facility
        FOREIGN KEY (field_plan_facility_id) REFERENCES field_plan_facilities (id);

CREATE INDEX IF NOT EXISTS idx_facility_activities_plan_phone_status
    ON facility_activities (field_plan_id, phone_status);

CREATE INDEX IF NOT EXISTS idx_facility_activities_plan_field_status
    ON facility_activities (field_plan_id, field_status);

CREATE INDEX IF NOT EXISTS idx_facility_activities_plan_overall_status
    ON facility_activities (field_plan_id, overall_status);

CREATE INDEX IF NOT EXISTS idx_facility_activities_plan_assessment_completion_status
    ON facility_activities (field_plan_id, assessment_completion_status);

CREATE INDEX IF NOT EXISTS idx_facility_activities_installation_field_plan_id
    ON facility_activities (installation_field_plan_id)
    WHERE installation_field_plan_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- field_plan_facilities: trace assessment source on installation handoff (§5.5 LLD)
-- ---------------------------------------------------------------------------
ALTER TABLE field_plan_facilities
    ADD COLUMN IF NOT EXISTS source_plan_facility_id VARCHAR(64);

ALTER TABLE field_plan_facilities
    ADD CONSTRAINT fk_field_plan_facilities_source_plan_facility
        FOREIGN KEY (source_plan_facility_id) REFERENCES facility_activities (id);

CREATE INDEX IF NOT EXISTS idx_field_plan_facilities_source_plan_facility_id
    ON field_plan_facilities (source_plan_facility_id)
    WHERE source_plan_facility_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- eg_assessment_submission: immutable phone / field form submissions (§5.4 LLD)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eg_assessment_submission (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    plan_id VARCHAR(64) NOT NULL REFERENCES field_plans (id),
    plan_facility_id VARCHAR(64) NOT NULL REFERENCES facility_activities (id),
    facility_id VARCHAR(64) NOT NULL,
    assessment_phase VARCHAR(16) NOT NULL,
    form_type VARCHAR(64) NOT NULL,
    submitted_by VARCHAR(64) NOT NULL,
    submitted_by_name VARCHAR(256),
    submission_data JSONB NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    client_submission_time BIGINT,
    server_received_time BIGINT NOT NULL,
    created_time BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
    CONSTRAINT chk_eg_assessment_submission_phase
        CHECK (assessment_phase IN ('PHONE', 'FIELD')),
    CONSTRAINT chk_eg_assessment_submission_outcome
        CHECK (outcome IN ('QUALIFIED', 'NOT_QUALIFIED')),
    CONSTRAINT uniq_eg_assessment_submission_plan_facility_phase
        UNIQUE (plan_facility_id, assessment_phase)
);

CREATE INDEX IF NOT EXISTS idx_eg_assessment_submission_plan_id
    ON eg_assessment_submission (plan_id);

CREATE INDEX IF NOT EXISTS idx_eg_assessment_submission_plan_facility_id
    ON eg_assessment_submission (plan_facility_id);

CREATE INDEX IF NOT EXISTS idx_eg_assessment_submission_facility_phase
    ON eg_assessment_submission (facility_id, assessment_phase);
