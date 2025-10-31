-- Add facilityId and boundaryCode columns to eg_incident_v2
ALTER TABLE public.eg_incident_v2 ADD COLUMN IF NOT EXISTS facilityid CHARACTER VARYING(64);
ALTER TABLE public.eg_incident_v2 ADD COLUMN IF NOT EXISTS boundarycode CHARACTER VARYING(256);