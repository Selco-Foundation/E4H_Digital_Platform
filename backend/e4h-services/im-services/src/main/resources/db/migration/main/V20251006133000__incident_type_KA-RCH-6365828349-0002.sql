UPDATE public.eg_incident_v2
SET incidenttype = 'LIGHTS & FANS',
    incidentsubtype = 'Notworkingfans'
WHERE incidentid = 'KA-RCH-6365828349-0002';

UPDATE public.eg_audit_logs
SET keyvaluemap = jsonb_set(
    jsonb_set(keyvaluemap, '{incidentType}', '"LIGHTS & FANS"', false),
    '{incidentSubType}', '"Notworkingfans"', false
)
WHERE keyvaluemap ->> 'incidentId' = 'KA-RCH-6365828349-0002';
