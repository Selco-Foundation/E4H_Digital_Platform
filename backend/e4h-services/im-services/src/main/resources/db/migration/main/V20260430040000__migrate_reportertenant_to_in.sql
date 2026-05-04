-- Migrate reporter tenant and phc type to 'in' on existing incidents
-- Aligns gateway-extracted tenantIds with state-level tenant when payloads embed nested tenantId fields

UPDATE public.eg_incident_v2
SET reportertenant = 'in'
WHERE reportertenant IS NULL OR reportertenant != 'in';
