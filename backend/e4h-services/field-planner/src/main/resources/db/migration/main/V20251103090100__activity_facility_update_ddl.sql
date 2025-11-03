DROP INDEX IF EXISTS idx_facility_activities_composite;
CREATE UNIQUE INDEX idx_facility_activities_composite ON facility_activities (tenant_id, facility_id, activity_id, field_plan_id);