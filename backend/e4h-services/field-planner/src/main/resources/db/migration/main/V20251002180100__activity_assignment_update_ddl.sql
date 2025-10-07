ALTER TABLE public.activity_assignments ADD poc_number varchar NULL;
ALTER TABLE public.activities ALTER COLUMN default_conditions DROP NOT NULL;