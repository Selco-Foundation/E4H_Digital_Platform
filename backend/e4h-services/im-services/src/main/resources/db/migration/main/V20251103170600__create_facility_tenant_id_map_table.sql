CREATE TABLE IF NOT EXISTS facility_tenant_id_map (
    id BIGSERIAL PRIMARY KEY,
    hfr_or_nin_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    facility_id VARCHAR(255) NOT NULL,
    boundary_code VARCHAR(500) NOT NULL,
    CONSTRAINT uq_facility_tenant_map_hfr_or_nin_id UNIQUE (hfr_or_nin_id)
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_facility_tenant_map_tenant_id ON facility_tenant_id_map (tenant_id);
CREATE INDEX IF NOT EXISTS idx_facility_tenant_map_facility_id ON facility_tenant_id_map (facility_id);
CREATE INDEX IF NOT EXISTS idx_facility_tenant_map_hfr_or_nin_id ON facility_tenant_id_map (hfr_or_nin_id);