-- Flyway Migration Script for Facility

CREATE TABLE facility (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    facility_category VARCHAR,
    facility_type VARCHAR,
    facility_subtype VARCHAR,
    facility_name VARCHAR(256),
    facility_ownership VARCHAR,
    facility_region VARCHAR,
    addressId VARCHAR,
    facility_details JSONB,
    wf_status VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    additional_details JSONB,
    created_by VARCHAR,
    created_at BIGINT,
    updated_by VARCHAR,
    updated_at BIGINT
);

CREATE INDEX idx_facility_tenant ON facility(tenant_id);
CREATE INDEX idx_facility_category ON facility(tenant_id, facility_category);
CREATE INDEX idx_facility_type ON facility(tenant_id, facility_type);
CREATE INDEX idx_facility_ownership ON facility(tenant_id, facility_ownership);
CREATE INDEX idx_facility_region ON facility(tenant_id, facility_region);
CREATE INDEX idx_facility_name ON facility(tenant_id, facility_name);
CREATE INDEX idx_facility_status ON facility(tenant_id, wf_status);

CREATE TABLE IF NOT EXISTS ADDRESS
(
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    doorNo VARCHAR(64)
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    locationAccuracy  INT,
    type VARCHAR(64),
    addressLine1 VARCHAR(256),
    addressLine2 VARCHAR(256),
    landmark VARCHAR(256),
    city VARCHAR(256),
    pincode VARCHAR(64),
    buildingName VARCHAR(256),
    street VARCHAR(256),
    localityCode VARCHAR(256),
)

