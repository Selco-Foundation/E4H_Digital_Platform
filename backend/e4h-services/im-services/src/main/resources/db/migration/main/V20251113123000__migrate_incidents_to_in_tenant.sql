-- Migrate all existing incidents to 'in' tenant
-- This migration updates all incidents in eg_incident_v2 to have tenantid = 'in'

-- Update all incidents to have tenantid = 'in'
UPDATE public.eg_incident_v2
SET tenantid = 'in'
WHERE tenantid IS NULL OR tenantid != 'in';


