-- Migrate all existing wf_documents to 'in' tenant
-- This migration updates all documents in eg_wf_document_v2 to have tenantid = 'in'

-- Update all documents to have tenantid = 'in'
UPDATE public.eg_wf_document_v2
SET tenantid = 'in'
WHERE tenantid IS NULL OR tenantid != 'in';