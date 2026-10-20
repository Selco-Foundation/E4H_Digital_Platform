--------------------------------------------------------------------
-- amc_plans
--------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS amc_plans (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    name VARCHAR(255) NOT NULL,
    project_id VARCHAR NOT NULL,
    health_facility_number INT NOT NULL,
    start_date BIGINT NOT NULL,
    end_date BIGINT NOT NULL,
    geography_scope JSONB NOT NULL,
    selected_activities JSONB NOT NULL DEFAULT '[]',
    created_by VARCHAR NOT NULL,
    status VARCHAR DEFAULT 'ACTIVE',
    isdeleted BOOLEAN DEFAULT FALSE,
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    additional_details JSONB DEFAULT '{}',

    CONSTRAINT valid_date_range CHECK (start_date < end_date)
    );

ALTER TABLE amc_plans
    ADD CONSTRAINT fk_amcplan_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_amc_plans_tenant ON amc_plans(tenant_id);
CREATE INDEX IF NOT EXISTS idx_amc_plans_project_id ON amc_plans(project_id);
