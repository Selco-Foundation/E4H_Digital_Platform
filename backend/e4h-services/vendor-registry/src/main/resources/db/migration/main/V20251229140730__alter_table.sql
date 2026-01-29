-- Add new columns to eg_org table
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_type VARCHAR(64); -- PLATFORM or VENDOR
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_subtype VARCHAR(256); -- NULL or AMC_VENDOR or INSTALLATION_VENDOR
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_poc_name VARCHAR(256);
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_poc_phone VARCHAR(64); -- Encrypted using encryption service
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_poc_email VARCHAR(256);
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_poc_username VARCHAR(128);
ALTER TABLE eg_org ADD COLUMN IF NOT EXISTS org_status VARCHAR(64) DEFAULT 'ACTIVE';

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_org_type ON eg_org(tenant_id, org_type);
CREATE INDEX IF NOT EXISTS idx_org_status ON eg_org(tenant_id, org_status);
CREATE INDEX IF NOT EXISTS idx_org_poc_username ON eg_org(org_poc_username);

-- Create indexes for eg_org_user table
CREATE INDEX IF NOT EXISTS idx_org_user_org_id ON eg_org_user(organizationid);
CREATE INDEX IF NOT EXISTS idx_org_user_user_uuid ON eg_org_user(userid);
CREATE INDEX IF NOT EXISTS idx_org_user_tenant ON eg_org_user(tenantid);