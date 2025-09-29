ALTER TABLE public.activity_assignments ADD "role" jsonb NULL;
ALTER TABLE public.activity_assignments ADD emailsent boolean NULL DEFAULT false;
ALTER TABLE public.activity_assignments ADD isdeleted boolean NULL DEFAULT false;