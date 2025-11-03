-- Populate facilityId and boundaryCode in eg_incident_v2 from facility table
-- This migration links existing incidents to their facilities based on tenantId

-- Update facilityId by matching incident tenantId with facility tenant_id
UPDATE public.eg_incident_v2 inc
SET facilityid = f.id
FROM public.facility f
WHERE inc.tenantid = f.tenant_id
  AND inc.facilityid IS NULL;

-- Update boundaryCode from facility additional_details JSONB field
-- The boundaryCode is stored in facility_details->boundaryCode in the facility table
UPDATE public.eg_incident_v2 inc
SET boundarycode = f.additional_details->'facility_details'->>'boundaryCode'
FROM public.facility f
WHERE inc.tenantid = f.tenant_id
  AND inc.boundarycode IS NULL
  AND f.additional_details->'facility_details'->>'boundaryCode' IS NOT NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_facilityid ON public.eg_incident_v2(facilityid);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_boundarycode ON public.eg_incident_v2(boundarycode);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_tenantid ON public.eg_incident_v2(tenantid);

