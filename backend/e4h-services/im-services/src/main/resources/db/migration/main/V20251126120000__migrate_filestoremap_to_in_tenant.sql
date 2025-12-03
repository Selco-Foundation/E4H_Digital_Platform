-- Migrate all existing filestoremap records to 'in' tenant
-- This migration updates all records in eg_filestoremap to have tenantid = 'in'

-- Update all filestoremap records to have tenantid = 'in'
UPDATE public.eg_filestoremap
SET tenantid = 'in'
WHERE tenantid IS NULL OR tenantid != 'in';

