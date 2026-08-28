-- The mapped vendor (the COMPLAINT_RESOLVER a ticket was handed to) used to live only on the
-- Elasticsearch document, which made every update re-read it from the index just to avoid losing it
-- on the full-document replace. Persist it alongside the incident instead.
--
-- Only the uuid is stored: name and username are HRMS's to own, and copying them here would leave
-- the ticket showing a stale name after the employee record is edited. They are resolved from HRMS
-- at indexing time.
ALTER TABLE eg_incident_v2 ADD COLUMN IF NOT EXISTS mapped_vendor_uuid CHARACTER VARYING(64);
