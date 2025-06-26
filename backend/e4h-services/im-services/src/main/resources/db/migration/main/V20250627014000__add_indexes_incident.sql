CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_applicationstatus ON eg_incident_v2 (applicationstatus);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_createdtime ON eg_incident_v2 (createdtime);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_accountid ON eg_incident_v2 (accountid);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_incidenttype ON eg_incident_v2 (incidenttype);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_tenantid ON eg_incident_v2 (tenantid);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_incidentid ON eg_incident_v2 (incidentid);
-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_tenantid_status ON eg_incident_v2 (tenantid, applicationstatus);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_tenantid_createdtime ON eg_incident_v2 (tenantid, createdtime); 