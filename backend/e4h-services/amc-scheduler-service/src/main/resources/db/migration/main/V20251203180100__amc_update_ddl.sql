ALTER TABLE public.scheduled_visits ADD project_id varchar NULL;
ALTER TABLE public.scheduled_visits ADD last_scheduled_visit_date bigint NULL;