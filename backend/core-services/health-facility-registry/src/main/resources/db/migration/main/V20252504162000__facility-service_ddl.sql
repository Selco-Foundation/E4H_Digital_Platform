CREATE TABLE IF NOT EXISTS facility (
    facility_id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR(128) NOT NULL,
    facility_name VARCHAR(256),
    facility_type VARCHAR(64),
    facility_category VARCHAR(64),
    facility_ownership VARCHAR(64),
    facility_region VARCHAR(64),
    facility_details JSONB,
    address JSONB,
    wf_status VARCHAR(64),
    additional_details JSONB,
    is_active BOOLEAN,
    created_time BIGINT,
    created_by VARCHAR(128),
    last_modified_time BIGINT,
    last_modified_by VARCHAR(128)
);
