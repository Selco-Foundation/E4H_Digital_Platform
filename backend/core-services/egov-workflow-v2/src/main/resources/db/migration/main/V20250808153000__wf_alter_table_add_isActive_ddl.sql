ALTER TABLE public.eg_wf_processinstance_v2 ADD COLUMN isactive boolean NOT NULL DEFAULT true;
CREATE INDEX IF NOT EXISTS idx_eg_wf_processinstance_v2_isactive_true
    ON public.eg_wf_processinstance_v2 (isactive)
    WHERE isactive = true;