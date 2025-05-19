ALTER TABLE facility
ADD COLUMN IF NOT EXISTS boundary_code VARCHAR;

CREATE INDEX IF NOT EXISTS idx_facility_tenant_id
    ON facility(tenant_id);

CREATE INDEX IF NOT EXISTS idx_facility_facility_id
    ON facility(facility_id);

CREATE INDEX IF NOT EXISTS idx_facility_facility_name
    ON facility(facility_name);

CREATE INDEX IF NOT EXISTS idx_facility_facility_type
    ON facility(facility_type);

CREATE INDEX IF NOT EXISTS idx_facility_boundary_code
    ON facility(boundary_code);

CREATE INDEX IF NOT EXISTS idx_facility_details_hfr_id
    ON facility((facility_details->>'hfrId'));

CREATE INDEX IF NOT EXISTS idx_facility_details_nin_id
    ON facility((facility_details->>'ninId'));

CREATE INDEX IF NOT EXISTS idx_facility_created_time
    ON facility(created_time);