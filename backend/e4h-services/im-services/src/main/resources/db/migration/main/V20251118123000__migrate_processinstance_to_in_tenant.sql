-- Migrate all existing wf_processinstance to 'in' tenant
-- This migration updates all processinstance in eg_wf_processinstance_v2 to have tenantid = 'in'

-- Update all processinstance to have tenantid = 'in'
UPDATE public.eg_wf_processinstance_v2
SET tenantid = 'in'
WHERE tenantid IS NULL OR tenantid != 'in';