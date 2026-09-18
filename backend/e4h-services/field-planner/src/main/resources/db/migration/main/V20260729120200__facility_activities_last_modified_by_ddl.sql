-- Assessment workflow updates record last_modified_by on facility_activities (LLD §5.2).
ALTER TABLE facility_activities
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(64);
