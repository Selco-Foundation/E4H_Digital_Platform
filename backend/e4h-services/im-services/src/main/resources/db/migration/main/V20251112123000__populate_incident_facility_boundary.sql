-- Populate facilityId and boundaryCode in eg_incident_v2 from facility_tenant_id_map table
-- This migration links existing incidents to their facilities based on tenantId

-- Update facilityId by matching incident tenantId with facility_tenant_id_map tenant_id
UPDATE public.eg_incident_v2 inc
SET facilityid = map.facility_id
FROM public.facility_tenant_id_map map
WHERE inc.tenantid = map.tenant_id
  AND inc.facilityid IS NULL;

-- Update boundaryCode from facility_tenant_id_map table
UPDATE public.eg_incident_v2 inc
SET boundarycode = map.boundary_code
FROM public.facility_tenant_id_map map
WHERE inc.tenantid = map.tenant_id
  AND inc.boundarycode IS NULL
  AND map.boundary_code IS NOT NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_facilityid ON public.eg_incident_v2(facilityid);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_boundarycode ON public.eg_incident_v2(boundarycode);
CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_tenantid ON public.eg_incident_v2(tenantid);

