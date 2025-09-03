-- Extends project management with field planning capabilities
CREATE TABLE field_plans (
                             id VARCHAR PRIMARY KEY,
                             tenant_id VARCHAR NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             project_id VARCHAR NOT NULL, -- References existing project
                             health_facility_number INT NOT NULL,
                             start_date BIGINT NOT NULL,
                             end_date BIGINT NOT NULL,
                             geography_scope JSONB NOT NULL, -- District/block selection
                             selected_activities JSONB NOT NULL DEFAULT '[]',
                             created_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
                             status VARCHAR DEFAULT 'ACTIVE',
                             isDeleted boolean,
                             last_modified_by    character varying(64),
                             created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                             last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                             additional_details JSONB DEFAULT '{}',

                             CONSTRAINT valid_date_range CHECK (start_date < end_date)
);

CREATE INDEX idx_field_plans_tenant ON field_plans(tenant_id);
CREATE INDEX idx_field_plans_project ON field_plans(tenant_id, project_id);
CREATE INDEX idx_field_plans_created_by ON field_plans(tenant_id, created_by);

-- Links field plans to specific facilities from existing facility table
CREATE TABLE field_plan_facilities (
                                       id VARCHAR PRIMARY KEY,
                                       tenant_id VARCHAR NOT NULL,
                                       field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
                                       facility_id VARCHAR NOT NULL, -- References existing facility.id
                                       status VARCHAR DEFAULT 'ACTIVE',
                                       created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                       last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                       additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_field_plan_facilities_tenant ON field_plan_facilities(tenant_id);
CREATE INDEX idx_field_plan_facilities_plan ON field_plan_facilities(tenant_id, field_plan_id);
CREATE INDEX idx_field_plan_facilities_facility ON field_plan_facilities(tenant_id, facility_id);
CREATE UNIQUE INDEX uniq_field_plan_facility ON field_plan_facilities(tenant_id, field_plan_id, facility_id);

-- Master data for field activities with configurable conditions
CREATE TABLE activities (
                            id VARCHAR PRIMARY KEY,
                            tenant_id VARCHAR NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            code VARCHAR(50) NOT NULL,
                            default_conditions JSONB NOT NULL DEFAULT '{}', -- Activation conditions
                            required_roles JSONB NOT NULL DEFAULT '[]', -- Required roles for activity
                            sequence_order INTEGER DEFAULT 0,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                            last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                            additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_activities_tenant ON activities(tenant_id);
CREATE INDEX idx_activities_code ON activities(tenant_id, code);
CREATE UNIQUE INDEX uniq_activity_code ON activities(tenant_id, code);


-- Assigns activities to SPOCs within field plans
CREATE TABLE activity_assignments (
                                      id VARCHAR PRIMARY KEY,
                                      tenant_id VARCHAR NOT NULL,
                                      field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
                                      activity_id VARCHAR NOT NULL REFERENCES activities(id),
                                      assigned_to VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
                                      assigned_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
                                      start_date BIGINT NOT NULL,
                                      end_date BIGINT NOT NULL,
                                      status VARCHAR DEFAULT 'ACTIVE',
                                      created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                      last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                      additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_activity_assignments_tenant ON activity_assignments(tenant_id);
CREATE INDEX idx_activity_assignments_plan ON activity_assignments(tenant_id, field_plan_id);
CREATE INDEX idx_activity_assignments_assigned_to ON activity_assignments(tenant_id, assigned_to);

-- Tracks facility-level activity execution with conditional activation
CREATE TABLE facility_activities (
                                     id VARCHAR PRIMARY KEY,
                                     tenant_id VARCHAR NOT NULL,
                                     facility_id VARCHAR NOT NULL, -- References existing facility.id
                                     activity_id VARCHAR NOT NULL REFERENCES activities(id),
                                     field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
                                     status VARCHAR DEFAULT 'SCHEDULED', -- SCHEDULED, ACTIVE, COMPLETED, CANCELLED
                                     conditions_met JSONB DEFAULT '{}', -- Tracks which conditions are satisfied
                                     assigned_user VARCHAR, -- References eg_hrms_employee.uuid
                                     scheduled_at BIGINT,
                                     activated_at BIGINT,
                                     completed_at BIGINT,
                                     created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                     last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                     additional_details JSONB DEFAULT '{}'
);

CREATE INDEX idx_facility_activities_tenant ON facility_activities(tenant_id);
CREATE INDEX idx_facility_activities_facility ON facility_activities(tenant_id, facility_id);
CREATE INDEX idx_facility_activities_status ON facility_activities(tenant_id, status);
CREATE INDEX idx_facility_activities_assigned ON facility_activities(tenant_id, assigned_user);
CREATE INDEX idx_facility_activities_composite ON facility_activities(tenant_id, facility_id, activity_id, field_plan_id);