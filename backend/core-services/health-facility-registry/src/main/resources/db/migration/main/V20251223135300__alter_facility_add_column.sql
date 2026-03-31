ALTER TABLE facility ADD COLUMN facility_poc_name VARCHAR(256);
ALTER TABLE facility ADD COLUMN facility_poc_phone VARCHAR(64);
ALTER TABLE facility ADD COLUMN facility_poc_email VARCHAR(256);
ALTER TABLE facility ADD COLUMN hfr_id VARCHAR(128);
ALTER TABLE facility ADD COLUMN nin_id VARCHAR(128);
ALTER TABLE facility ADD COLUMN user_id VARCHAR(128);
ALTER TABLE facility ADD COLUMN facility_status VARCHAR(64);

-- Create indexes for new search fields
CREATE INDEX IF NOT EXISTS idx_facility_poc_phone ON facility(facility_poc_phone);
CREATE INDEX IF NOT EXISTS idx_facility_hfr_id ON facility(hfr_id);
CREATE INDEX IF NOT EXISTS idx_facility_nin_id ON facility(nin_id);
CREATE INDEX IF NOT EXISTS idx_facility_status ON facility(tenant_id, facility_status);
CREATE INDEX IF NOT EXISTS idx_facility_onm_ready ON facility(tenant_id, is_onm_ready);
CREATE INDEX IF NOT EXISTS idx_facility_hrms_user ON facility(user_id);