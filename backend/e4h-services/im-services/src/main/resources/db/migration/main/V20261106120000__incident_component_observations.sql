-- SEM 4.0 Ticket Reporting: component and observations chosen by the reporter
-- (codes from MDMS Incident.Component / Incident.Observation). The observations' priorities
-- set the ticket priority, so both are written once at creation and never updated.
ALTER TABLE eg_incident_v2 ADD COLUMN IF NOT EXISTS component CHARACTER VARYING(64);
ALTER TABLE eg_incident_v2 ADD COLUMN IF NOT EXISTS observations JSONB;

CREATE INDEX IF NOT EXISTS index_eg_incident_v2_boundarycode_component ON eg_incident_v2 (boundarycode, component);
