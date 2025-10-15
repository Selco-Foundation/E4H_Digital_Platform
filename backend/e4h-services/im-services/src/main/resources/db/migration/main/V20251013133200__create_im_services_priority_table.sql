CREATE TABLE IF NOT EXISTS im_services_priority (
    id BIGSERIAL PRIMARY KEY,
    tenantId VARCHAR(64) NOT NULL,
    incidentType VARCHAR(255),
    incidentSubType VARCHAR(255),
    systemFunctional VARCHAR(255),
    priority VARCHAR(50) NOT NULL
);

-- Create indexes on all columns except id and priority
CREATE INDEX IF NOT EXISTS idx_im_services_priority_tenantid ON im_services_priority (tenantId);
CREATE INDEX IF NOT EXISTS idx_im_services_priority_incident_type ON im_services_priority (incidentType);
CREATE INDEX IF NOT EXISTS idx_im_services_priority_incident_sub_type ON im_services_priority (incidentSubType);
CREATE INDEX IF NOT EXISTS idx_im_services_priority_system_functional ON im_services_priority (systemFunctional);