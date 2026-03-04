ALTER TABLE eg_incident_v2
    ADD COLUMN IF NOT EXISTS warranty_status CHARACTER VARYING(256) DEFAULT 'WITHIN_WARRANTY';

UPDATE eg_incident_v2
SET warranty_status = 'OUT_OF_WARRANTY'
WHERE applicationStatus LIKE '%OUT_OF_WARRANTY%';

UPDATE eg_incident_v2
SET warranty_status = 'WITHIN_WARRANTY'
WHERE warranty_status IS NULL;

