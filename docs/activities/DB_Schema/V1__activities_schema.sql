
-- Flyway Migration: V1__activities_schema.sql
-- PostgreSQL-compatible schema for Activities API
-- Uses pgcrypto for gen_random_uuid()
-- NOTE: Foreign keys to external services (fieldplan, facility) are intentionally not enforced.
--       They are modeled as VARCHAR columns with indexes, as those entities live in other microservices.

-- Enable required extension for UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Table: activities
-- =========================
CREATE TABLE IF NOT EXISTS activities (
  id UUID PRIMARY KEY DEFAULT,
  tenant_id VARCHAR(64) NOT NULL,
  fieldplan_id VARCHAR(64) NOT NULL,
  activity_type VARCHAR(64) NOT NULL,
  parent_activity_id UUID NULL,
  activity_details JSONB NULL,
  planned_start_date BIGINT NULL,
  planned_end_date BIGINT NULL,
  start_date BIGINT NULL,
  end_date BIGINT NULL,
  spoc_employee_id VARCHAR(64) NULL,
  spoc_user_id VARCHAR(64) NULL,
  facility_id VARCHAR(64) NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  additional_details JSONB NULL,
  -- DIGIT audit columns
  created_by VARCHAR(64) NOT NULL,
  created_time BIGINT NOT NULL,
  last_modified_by VARCHAR(64) NULL,
  last_modified_time BIGINT NULL,
  CONSTRAINT fk_activities_parent
    FOREIGN KEY (parent_activity_id) REFERENCES activities(id) ON DELETE SET NULL
);

-- Helpful indexes for common filters
CREATE INDEX IF NOT EXISTS idx_activities_tenant_fieldplan ON activities (tenant_id, fieldplan_id);
CREATE INDEX IF NOT EXISTS idx_activities_tenant_facility ON activities (tenant_id, facility_id);
CREATE INDEX IF NOT EXISTS idx_activities_tenant_type ON activities (tenant_id, activity_type);
CREATE INDEX IF NOT EXISTS idx_activities_tenant_spoc ON activities (tenant_id, spoc_user_id);
CREATE INDEX IF NOT EXISTS idx_activities_planned_dates ON activities (planned_start_date, planned_end_date);
CREATE INDEX IF NOT EXISTS idx_activities_start_end ON activities (start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_activities_created_time ON activities (created_time);

-- =========================
-- Table: facility_staff (staff assigned to an activity with a role)
-- =========================
CREATE TABLE IF NOT EXISTS facility_staff (
  id UUID PRIMARY KEY DEFAULT,
  activity_id UUID NOT NULL,
  role_code VARCHAR(64) NOT NULL,
  staff_employee_id VARCHAR(64) NULL,
  staff_user_id VARCHAR(64) NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  -- DIGIT audit columns
  created_by VARCHAR(64) NOT NULL,
  created_time BIGINT NOT NULL,
  last_modified_by VARCHAR(64) NULL,
  last_modified_time BIGINT NULL,
  CONSTRAINT fk_facility_staff_activity
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

-- Avoid duplicate active assignments per (activity, role, staff_user_id)
CREATE UNIQUE INDEX IF NOT EXISTS uq_facility_staff_unique_active
ON facility_staff (activity_id, role_code, staff_user_id)
WHERE is_active;

CREATE INDEX IF NOT EXISTS idx_facility_staff_activity ON facility_staff (activity_id);
CREATE INDEX IF NOT EXISTS idx_facility_staff_role ON facility_staff (role_code);
CREATE INDEX IF NOT EXISTS idx_facility_staff_staffuser ON facility_staff (staff_user_id);
CREATE INDEX IF NOT EXISTS idx_facility_staff_created_time ON facility_staff (created_time);

-- =========================
-- Table: activity_reports
-- =========================
CREATE TABLE IF NOT EXISTS activity_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id VARCHAR(64) NOT NULL,
  activity_id UUID NOT NULL,
  facility_id VARCHAR(64) NULL,
  report_type VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  report_data JSONB NULL,
  submitted_by_user_id VARCHAR(64) NULL,
  submitted_by_employee_id VARCHAR(64) NULL,
  reviewed_by_user_id VARCHAR(64) NULL,
  -- DIGIT audit columns
  created_by VARCHAR(64) NOT NULL,
  created_time BIGINT NOT NULL,
  last_modified_by VARCHAR(64) NULL,
  last_modified_time BIGINT NULL,
  CONSTRAINT fk_activity_reports_activity
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
);

-- Indexes for report search patterns
CREATE INDEX IF NOT EXISTS idx_reports_tenant_activity ON activity_reports (tenant_id, activity_id);
CREATE INDEX IF NOT EXISTS idx_reports_tenant_facility ON activity_reports (tenant_id, facility_id);
CREATE INDEX IF NOT EXISTS idx_reports_type_status ON activity_reports (tenant_id, report_type, status);
CREATE INDEX IF NOT EXISTS idx_reports_created_time ON activity_reports (created_time);

-- =========================
-- Table: activity_report_documents (attachments for reports)
-- =========================
CREATE TABLE IF NOT EXISTS activity_report_documents (
  id BIGSERIAL PRIMARY KEY,
  report_id UUID NOT NULL,
  file_store_id VARCHAR(64) NOT NULL,
  document_type VARCHAR(64) NOT NULL,
  file_name VARCHAR(256) NOT NULL,
  mime_type VARCHAR(128) NOT NULL,
  file_size BIGINT NOT NULL,
  uploaded_at BIGINT NOT NULL,
  -- DIGIT audit columns
  created_by VARCHAR(64) NOT NULL,
  created_time BIGINT NOT NULL,
  last_modified_by VARCHAR(64) NULL,
  last_modified_time BIGINT NULL,
  CONSTRAINT fk_report_documents_report
    FOREIGN KEY (report_id) REFERENCES activity_reports(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_report_documents_report ON activity_report_documents (report_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_report_documents_filestore ON activity_report_documents (report_id, file_store_id);
