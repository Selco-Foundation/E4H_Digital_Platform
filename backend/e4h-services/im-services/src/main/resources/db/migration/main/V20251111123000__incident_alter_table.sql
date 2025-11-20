-- Add facilityId and boundaryCode columns to eg_incident_v2
ALTER TABLE public.eg_incident_v2 ADD COLUMN IF NOT EXISTS facilityid CHARACTER VARYING(64);
ALTER TABLE public.eg_incident_v2 ADD COLUMN IF NOT EXISTS boundarycode CHARACTER VARYING(256);

-- Add foreign key constraint for facilityId
ALTER TABLE public.eg_incident_v2 
ADD CONSTRAINT fk_eg_incident_v2_facility 
FOREIGN KEY (facilityid) REFERENCES public.facility(id);