ALTER TABLE public.scheduled_visits
    ADD COLUMN IF NOT EXISTS facility_name VARCHAR;

CREATE INDEX IF NOT EXISTS idx_scheduled_visits_facility_name
    ON public.scheduled_visits (facility_name);
