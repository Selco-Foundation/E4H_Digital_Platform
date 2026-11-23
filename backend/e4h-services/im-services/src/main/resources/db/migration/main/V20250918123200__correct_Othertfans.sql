UPDATE public.eg_incident_v2
SET incidentsubtype = 'OtherFan'
WHERE incidentsubtype = 'OthertFan';

UPDATE eg_audit_logs
SET keyvaluemap = jsonb_set(
    keyvaluemap,
    '{incidentSubType}',
    '"OtherFan"',
    false
)
WHERE keyvaluemap ->> 'incidentSubType' = 'OthertFan';
