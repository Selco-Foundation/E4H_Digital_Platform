--------------------------------------------------------------------
-- 1) amc_configuration
--------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS amc_configuration (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    vendor_id VARCHAR,
    facility_id VARCHAR,
    asset_types JSONB,                -- Or JSONB if you prefer
    project_id VARCHAR,
    duration_months INT NOT NULL,
    visit_frequency_months INT NOT NULL,
    configuration_start_date BIGINT,
    configuration_end_date BIGINT,
    status VARCHAR(32),       -- ACTIVE, EXPIRED, CANCELLED
    additional_details JSONB,
    created_by VARCHAR(128),
    created_time BIGINT,
    last_modified_by VARCHAR(128),
    last_modified_time BIGINT
    );

-- Unique entry per installation (tenant + facility + project + vendor)
CREATE UNIQUE INDEX IF NOT EXISTS ux_amc_configuration_unique_installation
    ON amc_configuration (tenant_id, facility_id, project_id, vendor_id);

ALTER TABLE amc_configuration
    ADD CONSTRAINT fk_amcconf_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id) ON DELETE SET NULL;

ALTER TABLE amc_configuration
    ADD CONSTRAINT fk_amcconf_project
        FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_amc_configuration_tenant ON amc_configuration(tenant_id);
CREATE INDEX IF NOT EXISTS idx_amcconf_facility_id ON amc_configuration(facility_id);
CREATE INDEX IF NOT EXISTS idx_amcconf_project_id ON amc_configuration(project_id);
CREATE INDEX IF NOT EXISTS idx_amcconf_vendor_id ON amc_configuration(vendor_id);

--------------------------------------------------------------------
-- 2) amc_configuration_assignments
--------------------------------------------------------------------
-- Create table amc_configuration_assignments
CREATE TABLE IF NOT EXISTS amc_configuration_assignments (
     id VARCHAR PRIMARY KEY,
     amc_configuration_id VARCHAR NOT NULL,
     assigned_user VARCHAR NOT NULL,
     is_active BOOLEAN DEFAULT TRUE,
     tenant_id VARCHAR(128) NOT NULL,
     created_by VARCHAR(128),
     created_time BIGINT,
     last_modified_by VARCHAR(128),
     last_modified_time BIGINT
);

-- Foreign key to amc_configuration
ALTER TABLE amc_configuration_assignments ADD CONSTRAINT fk_amc_conf_assignment_amc_config FOREIGN KEY (amc_configuration_id)
            REFERENCES amc_configuration(id) ON DELETE SET NULL;

-- Optional: FK to employee table (HRMS)
-- Uncomment and adjust table/column name:
-- ALTER TABLE amc_configuration_assignments
--     ADD CONSTRAINT fk_amc_conf_assignment_employee
--     FOREIGN KEY (assigned_user)
--     REFERENCES employee(id)
--     ON DELETE SET NULL;

-- Unique constraint if a user cannot be assigned twice to a configuration
CREATE UNIQUE INDEX IF NOT EXISTS ux_amc_conf_assignment_unique
    ON amc_configuration_assignments (amc_configuration_id, assigned_user);

-- Index to optimize search by amc configuration
CREATE INDEX IF NOT EXISTS idx_amc_conf_assign_config_id
    ON amc_configuration_assignments (amc_configuration_id);

-- Index to optimize search by user
CREATE INDEX IF NOT EXISTS idx_amc_conf_assign_user
    ON amc_configuration_assignments (assigned_user);


--------------------------------------------------------------------
-- 3) asset_amc
--------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asset_amc (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    asset_id VARCHAR,
    amc_configuration_id VARCHAR,
    amc_start_date BIGINT,
    amc_end_date BIGINT,
    status VARCHAR(32) NOT NULL,       -- ACTIVE, EXPIRED, UNDER_MAINTENANCE, INACTIVE
    is_legacy_asset BOOLEAN DEFAULT FALSE,
    additional_details JSONB,
    created_by VARCHAR(128),
    created_time BIGINT,
    last_modified_by VARCHAR(128),
    last_modified_time BIGINT
    );

CREATE INDEX IF NOT EXISTS idx_asset_amc_asset_id ON asset_amc(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_amc_amc_conf_id ON asset_amc(amc_configuration_id);
CREATE INDEX IF NOT EXISTS idx_asset_amc_tenant ON asset_amc(tenant_id);

ALTER TABLE asset_amc
    ADD CONSTRAINT fk_assetamc_asset
        FOREIGN KEY (asset_id) REFERENCES asset(asset_id) ON DELETE SET NULL;

ALTER TABLE asset_amc
    ADD CONSTRAINT fk_assetamc_amcconf
        FOREIGN KEY (amc_configuration_id) REFERENCES amc_configuration(id) ON DELETE SET NULL;


--------------------------------------------------------------------
-- 4) scheduled_visits
--------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scheduled_visits (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    amc_configuration_id VARCHAR,
    facility_id VARCHAR,
    visit_number INT NOT NULL,
    scheduled_date BIGINT,
    actual_visit_date BIGINT,
    status VARCHAR(64),
    visit_report JSONB,
    additional_details JSONB,
    created_by VARCHAR(128),
    created_time BIGINT,
    last_modified_by VARCHAR(128),
    last_modified_time BIGINT,
    CONSTRAINT ux_scheduled_visits_unique_visit_per_amc
    UNIQUE (amc_configuration_id, visit_number)
    );

CREATE INDEX IF NOT EXISTS idx_scheduled_visits_facility ON scheduled_visits(facility_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_visits_amc_conf ON scheduled_visits(amc_configuration_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_visits_tenant ON scheduled_visits(tenant_id);

ALTER TABLE scheduled_visits
    ADD CONSTRAINT fk_scheduledvisits_amcconf
        FOREIGN KEY (amc_configuration_id) REFERENCES amc_configuration(id) ON DELETE SET NULL;

ALTER TABLE scheduled_visits
    ADD CONSTRAINT fk_scheduledvisits_facility
        FOREIGN KEY (facility_id) REFERENCES facility(id) ON DELETE SET NULL;


--------------------------------------------------------------------
-- 5) scheduled_visit_assignments
--------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS scheduled_visit_assignments (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    scheduled_visit_id VARCHAR NOT NULL,
    assigned_user VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(128),
    created_time BIGINT,
    last_modified_by VARCHAR(128),
    last_modified_time BIGINT
    );

CREATE INDEX IF NOT EXISTS idx_visit_assignments_scheduled_visit
    ON scheduled_visit_assignments(scheduled_visit_id);
CREATE INDEX IF NOT EXISTS idx_visit_assignments_user
    ON scheduled_visit_assignments(assigned_user);

ALTER TABLE scheduled_visit_assignments
    ADD CONSTRAINT fk_visitassignments_scheduledvisit
        FOREIGN KEY (scheduled_visit_id) REFERENCES scheduled_visits(id) ON DELETE CASCADE;


CREATE TABLE IF NOT EXISTS public.visit_transaction (
    id VARCHAR(64) PRIMARY KEY,
    visit_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(256),
    visit_report JSONB,
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    CONSTRAINT fk_visit_transaction FOREIGN KEY (visit_id)
    REFERENCES public.scheduled_visits(id)
    ON DELETE SET NULL;
);
CREATE INDEX IF NOT EXISTS idx_visit_transaction_visit_id
    ON visit_transaction(visit_id);

CREATE INDEX IF NOT EXISTS idx_visit_transaction_process_instance_id
    ON visit_transaction(process_instance_id);
