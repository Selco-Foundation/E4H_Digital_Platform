-- =============================================================================
-- Livelihood Platform — Schema reference (derived from E4H migrations)
-- Source: E4H_SCHEMA_FROM_MIGRATIONS.sql + LIVELIHOOD_PLATFORM_CHANGES_CONCISE.md
-- Tenant: livelihood (separate deployment; not shared E4H DB)
--
-- WARNING: Documentation/reference only. Apply schema via Flyway per service.
--   Section 5 lists PROPOSED Livelihood additive columns (not yet in repo migrations).
-- =============================================================================

-- =============================================================================
-- EXCLUDED FROM LIVELIHOOD (vs full E4H schema)
-- =============================================================================
-- Services omitted (Phase 1 out of scope):
--   - e4h-services/amc-scheduler-service
--   - e4h-services/rms-service
-- Tables omitted (E4H-only / not used by Livelihood):
--   - facility_rms_inactive_incident
--   - facility_tenant_id_map
--   - im_services_priority
--   - project_beneficiary
-- Not in migration extract: egov-user, egov-otp, egov-localization, egov-persister
--   (external backbone — own schemas, not in backend/ Flyway paths scanned)
-- Optional services (no tables in this extract): inbox, processor-services, ingestion-service

-- =============================================================================
-- SECTION 1: TABLE CATALOG (61 tables included)
-- =============================================================================

-- --- core-services/boundary-service ---
--   boundary
--     backend/core-services/boundary-service/src/main/resources/db/migration/main/V20230728110535__boundary-service_ddl.sql
--   boundary_hierarchy
--     backend/core-services/boundary-service/src/main/resources/db/migration/main/V20231025110679__boundary_hierarchy_ddl.sql
--   boundary_relationship
--     backend/core-services/boundary-service/src/main/resources/db/migration/main/V20231031120752__boundary_relationship_ddl.sql

-- --- core-services/egov-filestore ---
--   eg_filestoremap
--     backend/core-services/egov-filestore/src/main/resources/db/migration/ddl/V20170420135841__egfilestore_tenant_ddl.sql

-- --- core-services/egov-idgen ---
--   id_generator
--     backend/core-services/egov-idgen/src/main/resources/db/migration/main/V20170614121459__DDL_id_generation_create_ddl.sql

-- --- core-services/egov-mdms-service-v2 ---
--   eg_mdms_data
--     backend/core-services/egov-mdms-service-v2/src/main/resources/db/migration/main/V20230531144020__mdms_data_create_ddl.sql
--   eg_mdms_schema_definition
--     backend/core-services/egov-mdms-service-v2/src/main/resources/db/migration/main/V20230531114515__schema_definition_ddl.sql

-- --- core-services/egov-workflow-v2 ---
--   eg_wf_action_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20181204120036__wf_create_ddl.sql
--   eg_wf_assignee_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20191211105434__wf_modified_assignee_ddl.sql
--   eg_wf_businessservice_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20181204120036__wf_create_ddl.sql
--   eg_wf_Document_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20181204120036__wf_create_ddl.sql
--   eg_wf_processinstance_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20181204120036__wf_create_ddl.sql
--   eg_wf_state_v2
--     backend/core-services/egov-workflow-v2/src/main/resources/db/migration/main/V20181204120036__wf_create_ddl.sql

-- --- core-services/health-facility-registry ---
--   facility
--     backend/core-services/health-facility-registry/src/main/resources/db/migration/main/V1202508190221__create_facility_registry_schema_ddl.sql
--   facility_address
--     backend/core-services/health-facility-registry/src/main/resources/db/migration/main/V1202508190221__create_facility_registry_schema_ddl.sql

-- --- e4h-services/asset-registry ---
--   asset
--     backend/e4h-services/asset-registry/src/main/resources/db/migration/main/V20250520141800__asset-service_ddl.sql
--   asset_documents
--     backend/e4h-services/asset-registry/src/main/resources/db/migration/main/V20250520141800__asset-service_ddl.sql

-- --- e4h-services/egov-hrms ---
--   eg_hrms_assignment
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_deactivationdetails
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_departmentaltests
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_educationaldetails
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_empdocuments
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_employee
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_jurisdiction
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql
--   eg_hrms_reactivationdetails
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20201223230836__eg_hrms_employee_reactivation_details_index_ddl.sql
--   eg_hrms_servicehistory
--     backend/e4h-services/egov-hrms/src/main/resources/db/migration/main/V20190122152236__create_hrms_employee_table_ddl.sql

-- --- e4h-services/field-planner [Phase 2 — installation track] ---
--   activities
--     backend/e4h-services/field-planner/src/main/resources/db/migration/main/V20250901180100__fieldPlanner_create_ddl.sql
--   activity_assignments
--     backend/e4h-services/field-planner/src/main/resources/db/migration/main/V20250901180100__fieldPlanner_create_ddl.sql
--   facility_activities
--     backend/e4h-services/field-planner/src/main/resources/db/migration/main/V20250901180100__fieldPlanner_create_ddl.sql
--   field_plan_facilities
--     backend/e4h-services/field-planner/src/main/resources/db/migration/main/V20250901180100__fieldPlanner_create_ddl.sql
--   field_plans
--     backend/e4h-services/field-planner/src/main/resources/db/migration/main/V20250901180100__fieldPlanner_create_ddl.sql

-- --- e4h-services/field-planner-activity [Phase 2 — installation track] ---
--   activity_facility_transaction
--     backend/e4h-services/field-planner-activity/src/main/resources/db/migration/main/V20251015163200__activity_facility_transaction_comment_create.sql
--   activity_facility_transaction_comment
--     backend/e4h-services/field-planner-activity/src/main/resources/db/migration/main/V20251015163200__activity_facility_transaction_comment_create.sql
--   ACTIVITY_FACILITY_USERS
--     backend/e4h-services/field-planner-activity/src/main/resources/db/migration/main/V20251030135800__activity_facility_assigned_users_ddl.sql
--   bom
--     backend/e4h-services/field-planner-activity/src/main/resources/db/migration/main/V20250919180100__bom_create_ddl.sql
--   bom_document
--     backend/e4h-services/field-planner-activity/src/main/resources/db/migration/main/V20250924180100__bom_document_create_ddl.sql

-- --- e4h-services/im-services ---
--   eg_incident_address_v2
--     backend/e4h-services/im-services/src/main/resources/db/migration/main/V20240501115019__create_table.sql
--   eg_incident_v2
--     backend/e4h-services/im-services/src/main/resources/db/migration/main/V20240501115019__create_table.sql

-- --- e4h-services/project ---
--   ADDRESS
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20220103131500__project_address_ddl.sql
--   project
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20221202180100__project_create_ddl.sql
--   project_address
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20230213143700__project_address_create_ddl.sql
--   project_document
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20230213142900__project_document_ddl.sql
--   PROJECT_FACILITY
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20230224122200__project_facility_ddl.sql
--   project_resource
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20230220171200__project_resource_create_ddl.sql
--   PROJECT_STAFF
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20221208135800__project_staff_ddl.sql
--   PROJECT_TASK
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20220103130700__task_create_ddl.sql
--   project_transaction
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20250620163200__project_transaction_comment_create.sql
--   project_transaction_comment
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20250620163200__project_transaction_comment_create.sql
--   TASK_RESOURCE
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20230103161300__task_resource_ddl.sql
--   USER_ACTION
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20240711175500__user_action_ddl.sql
--   USER_LOCATION
--     backend/e4h-services/project/src/main/resources/db/migration/main/V20240711175300__user_location_ddl.sql

-- --- e4h-services/vendor-registry ---
--   eg_org
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_address
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_address_boundary
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_address_geo_location
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_contact_detail
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_document
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_function
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_jurisdiction
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql
--   eg_org_user
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20250929135800__organization_user_ddl.sql
--   eg_tax_identifier
--     backend/e4h-services/vendor-registry/src/main/resources/db/migration/main/V20230301120030__create_table.sql

-- =============================================================================
-- SECTION 2: CREATE TABLE statements (Livelihood subset)
-- =============================================================================

-- ##### SERVICE: core-services/boundary-service #####

-- --- V20230728110535__boundary-service_ddl.sql ---
CREATE TABLE boundary (
  id VARCHAR (64),
  tenantId VARCHAR (64) NOT NULL,
  code VARCHAR (64) NOT NULL,
  geometry JSONB,
  additionalDetails JSONB,
  createdtime BIGINT NOT NULL,
  createdby VARCHAR (64) NOT NULL,
  lastmodifiedtime BIGINT,
  lastmodifiedby VARCHAR (64),

  CONSTRAINT unique_code_tenantId UNIQUE (code, tenantId),
  PRIMARY KEY (id)
);

-- --- V20231025110679__boundary_hierarchy_ddl.sql ---
CREATE TABLE boundary_hierarchy (
    id VARCHAR(64),
    tenantId VARCHAR(64) NOT NULL,
    hierarchyType VARCHAR(64) NOT NULL,
    boundaryHierarchy JSONB NOT NULL,
    createdtime BIGINT,
    createdby VARCHAR(64),
    lastmodifiedtime BIGINT,
    lastmodifiedby VARCHAR(64),
    CONSTRAINT pk_boundary_hierarchy PRIMARY KEY (id),
    CONSTRAINT uk_boundary_hierarchy UNIQUE (tenantId , hierarchyType)
);

-- --- V20231031120752__boundary_relationship_ddl.sql ---
CREATE TABLE boundary_relationship (
    id VARCHAR(64),
    tenantId VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    hierarchyType VARCHAR(64) NOT NULL,
    boundaryType VARCHAR(64) NOT NULL,
    parent VARCHAR(64),
    ancestralMaterializedPath TEXT,
    createdtime BIGINT,
    createdby VARCHAR(64),
    lastmodifiedtime BIGINT,
    lastmodifiedby VARCHAR(64),
    CONSTRAINT uk_boundary_relationship UNIQUE (id),
    CONSTRAINT pk_boundary_relationship PRIMARY KEY (tenantId, code, hierarchyType)
);

-- ##### SERVICE: core-services/egov-filestore #####

-- --- V20170420135841__egfilestore_tenant_ddl.sql ---
CREATE TABLE eg_filestoremap (
    id bigint NOT NULL,
    filestoreid character varying(36) NOT NULL,
    filename character varying(100) NOT NULL,
    contenttype character varying(100),
    module character varying(256),
    tag character varying(256),
    tenantid character varying(256) not null,
    version bigint
);

-- ##### SERVICE: core-services/egov-idgen #####

-- --- V20170614121459__DDL_id_generation_create_ddl.sql ---
CREATE TABLE id_generator
(
    id bigserial NOT NULL,
    idname character varying(200) NOT NULL,
    tenantid character varying(200) NOT NULL,
    format character varying(200) NOT NULL,
    sequencenumber  integer NOT NULL,
    
    CONSTRAINT pk_id_generator PRIMARY KEY (id)
);

-- ##### SERVICE: core-services/egov-mdms-service-v2 #####

-- --- V20230531114515__schema_definition_ddl.sql ---
CREATE TABLE eg_mdms_schema_definition (
    id VARCHAR(64) NOT NULL,
    tenantid VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(512),
    definition JSONB NOT NULL,
    isactive BOOLEAN NOT NULL,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT pk_eg_schema_definition PRIMARY KEY (tenantId,code)
);

-- --- V20230531144020__mdms_data_create_ddl.sql ---
CREATE TABLE eg_mdms_data (
    id VARCHAR(64) NOT NULL,
    tenantid VARCHAR(255) NOT NULL,
    uniqueidentifier VARCHAR(255),
    schemacode VARCHAR(255) NOT NULL,
    data JSONB NOT NULL,
    isactive BOOLEAN NOT NULL,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT pk_eg_mdms_data PRIMARY KEY (tenantId,schemacode,uniqueidentifier),
    CONSTRAINT uk_eg_mdms_data UNIQUE(id)
);

-- ##### SERVICE: core-services/egov-workflow-v2 #####

-- --- V20181204120036__wf_create_ddl.sql ---
CREATE TABLE eg_wf_processinstance_v2(

    id character varying(64),
    tenantid character varying(128),
    businessService character varying(128),
    businessId character varying(128),
    action character varying(128),
    status character varying(128),
    comment character varying(128),
    assigner character varying(128),
    assignee character varying(128),
    sla bigint,
    previousStatus character varying(128),
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,
    CONSTRAINT uk_eg_wf_processinstance UNIQUE (id)
);

-- --- V20181204120036__wf_create_ddl.sql ---
CREATE TABLE eg_wf_Document_v2(
    id character varying(64),
    tenantId character varying(64),
    documentType character varying(64),
    documentUid character varying(64),
    filestoreid character varying(64),
    processinstanceid character varying(64),
    active boolean,
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,

    CONSTRAINT uk_eg_wf_Document PRIMARY KEY (id),
    CONSTRAINT fk_eg_wf_Document FOREIGN KEY (processinstanceid) REFERENCES eg_wf_processinstance_v2 (id)

    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- --- V20181204120036__wf_create_ddl.sql ---
CREATE TABLE eg_wf_businessservice_v2
(
  businessservice character varying(256) NOT NULL,
  business character varying(256) NOT NULL,
  tenantid character varying(256) NOT NULL,
  uuid character varying(256) NOT NULL,
  geturi character varying(1024),
  posturi character varying(1024),
  createdby character varying(256) NOT NULL,
  createdtime bigint,
  lastmodifiedby character varying(256) NOT NULL,
  lastmodifiedtime bigint,

  CONSTRAINT pk_eg_wf_businessservice PRIMARY KEY (uuid),
  CONSTRAINT uk_eg_wf_businessservice UNIQUE (tenantid,businessService)
);

-- --- V20181204120036__wf_create_ddl.sql ---
CREATE TABLE eg_wf_state_v2
(
  uuid character varying(256) NOT NULL,
  tenantid character varying(256) NOT NULL,
  businessserviceid character varying(256) NOT NULL, --Foreign key uuid of eg_wf_businessservice_v2
  state character varying(256),
  applicationStatus character varying(256),
  sla bigint,
  docuploadrequired boolean,
  isstartstate boolean,
  isterminatestate boolean,
  createdby character varying(256) NOT NULL,
  createdtime bigint,
  lastmodifiedby character varying(256) NOT NULL,
  lastmodifiedtime bigint,

  CONSTRAINT uk_eg_wf_state PRIMARY KEY (uuid),
  CONSTRAINT fk_eg_wf_state FOREIGN KEY (businessserviceid) REFERENCES eg_wf_businessservice_v2 (uuid)

  ON UPDATE CASCADE
  ON DELETE CASCADE
);

-- --- V20181204120036__wf_create_ddl.sql ---
CREATE TABLE eg_wf_action_v2
(
  uuid character varying(256) NOT NULL,
  tenantid character varying(256) NOT NULL,
  currentstate character varying(256),
  action character varying(256) NOT NULL,
  nextstate character varying(256),
  roles character varying(1024) NOT NULL,
  createdby character varying(256) NOT NULL,
  createdtime bigint,
  lastmodifiedby character varying(256) NOT NULL,
  lastmodifiedtime bigint,

   CONSTRAINT uk_eg_wf_action PRIMARY KEY (uuid),
   CONSTRAINT fk_eg_wf_action FOREIGN KEY (currentstate) REFERENCES eg_wf_state_v2 (uuid)

   ON UPDATE CASCADE
   ON DELETE CASCADE
);

-- --- V20191211105434__wf_modified_assignee_ddl.sql ---
CREATE TABLE eg_wf_assignee_v2(

    processinstanceid character varying(64),
    tenantid character varying(128),
    assignee character varying(128),
    createdBy character varying(64),
    lastModifiedBy character varying(64),
    createdTime bigint,
    lastModifiedTime bigint,

    CONSTRAINT fk_eg_wf_assignee_v2 FOREIGN KEY (processinstanceid) REFERENCES eg_wf_processinstance_v2 (id)

    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- ##### SERVICE: core-services/health-facility-registry #####

-- --- V1202508190221__create_facility_registry_schema_ddl.sql ---
CREATE TABLE facility_address (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    doorNo VARCHAR(64),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    locationAccuracy INT,
    type VARCHAR(64),
    addressLine1 VARCHAR(256),
    addressLine2 VARCHAR(256),
    landmark VARCHAR(256),
    city VARCHAR(256),
    pincode VARCHAR(64),
    buildingName VARCHAR(256),
    street VARCHAR(256),
    localityCode VARCHAR(256)
);

-- --- V1202508190221__create_facility_registry_schema_ddl.sql ---
CREATE TABLE facility (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    facility_category VARCHAR,
    facility_type VARCHAR,
    facility_subtype VARCHAR,
    facility_name VARCHAR(256),
    facility_ownership VARCHAR,
    facility_region VARCHAR,
    addressId VARCHAR,
    facility_details JSONB,
    wf_status VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    additional_details JSONB,
    created_by VARCHAR,
    created_at BIGINT,
    updated_by VARCHAR,
    updated_at BIGINT,
    boundary_code VARCHAR,

    -- Add foreign key constraint on addressId
    CONSTRAINT fk_facility_address FOREIGN KEY (addressId)
        REFERENCES facility_address(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- ##### SERVICE: e4h-services/asset-registry #####

-- --- V20250520141800__asset-service_ddl.sql ---
CREATE TABLE IF NOT EXISTS asset (
    asset_id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    system VARCHAR NOT NULL,
    facility_id VARCHAR NOT NULL,
    asset_type_id VARCHAR(64) NOT NULL,
    serial_number VARCHAR(128) NOT NULL,
    model_number VARCHAR(128) NOT NULL,
    brand_id VARCHAR(64) NOT NULL,
    asset_details JSONB,
    warranty_start_date BIGINT,
    warranty_duration INT,
    warranty_end_date BIGINT,
    wf_status VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    additional_details JSONB,
    created_by VARCHAR,
    created_time BIGINT,
    last_modified_by VARCHAR,
    last_modified_time BIGINT
);

-- --- V20250520141800__asset-service_ddl.sql ---
CREATE TABLE IF NOT EXISTS asset_documents (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    asset_id VARCHAR NOT NULL,
    filestore_id VARCHAR NOT NULL,
    document_type VARCHAR,
    uploaded_at BIGINT,
    additional_details JSONB,
    created_by VARCHAR,
    created_time BIGINT,
    updated_by VARCHAR,
    updated_time BIGINT,
    CONSTRAINT fk_document_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id)
);

-- ##### SERVICE: e4h-services/egov-hrms #####

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_employee (
	id BIGINT NOT NULL,
	uuid CHARACTER VARYING(1024) NOT NULL,
	code CHARACTER VARYING(250),
	phone CHARACTER VARYING(250),
	name CHARACTER VARYING(250),
    dateOfAppointment BIGINT,
    employeestatus CHARACTER VARYING(250),
    employeetype CHARACTER VARYING(250),
    active BOOLEAN,
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_employee PRIMARY KEY (uuid),
	CONSTRAINT uk_eghrms_employee_code UNIQUE (code)
);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_assignment (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	position BIGINT,
	department CHARACTER VARYING(250),
	designation CHARACTER VARYING(250),
    fromdate BIGINT,
    todate BIGINT,
	govtordernumber CHARACTER VARYING(250),
	reportingto CHARACTER VARYING(250),
	isHOD BOOLEAN,
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_assignment PRIMARY KEY (uuid),
    CONSTRAINT ck_eghrms_employee_fromTo CHECK (fromdate <= todate),
	CONSTRAINT fk_eghrms_assignment_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_educationaldetails (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	qualification CHARACTER VARYING(250),
	stream CHARACTER VARYING(250),
    yearofpassing BIGINT,
    university CHARACTER VARYING(250),
	remarks CHARACTER VARYING(250),
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_educationaldetails PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_educationaldetails_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_departmentaltests (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	test CHARACTER VARYING(250),
    yearofpassing BIGINT,
	remarks CHARACTER VARYING(250),
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_departmentaltests PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_departmentaltests_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_empdocuments (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	documentid CHARACTER VARYING(250) NOT NULL,
    documentname CHARACTER VARYING(250),
	referencetype CHARACTER VARYING(250),
	referenceid CHARACTER VARYING(250) NOT NULL,
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_empdocuments PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_empdocuments_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_servicehistory (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	servicestatus CHARACTER VARYING(250),
	servicefrom BIGINT,
	serviceto BIGINT,
	ordernumber CHARACTER VARYING(250),
	isCurrentPosition BOOLEAN,
	location CHARACTER VARYING(250),
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_servicehistory  PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_servicehistory_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_jurisdiction (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	hierarchy CHARACTER VARYING(250) NOT NULL,
	boundarytype CHARACTER VARYING(250) NOT NULL,
	boundary CHARACTER VARYING(250) NOT NULL,
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_jurisdiction  PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_jurisdiction_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20190122152236__create_hrms_employee_table_ddl.sql ---
CREATE TABLE eg_hrms_deactivationdetails (
	uuid CHARACTER VARYING(1024) NOT NULL,
	employeeid CHARACTER VARYING(1024) NOT NULL,
	reasonfordeactivation CHARACTER VARYING(250),
	effectivefrom BIGINT,
	ordernumber CHARACTER VARYING(250),
	typeOfDeactivation CHARACTER VARYING(250),
	tenantid CHARACTER VARYING(250) NOT NULL,
	createdby CHARACTER VARYING(250) NOT NULL,
	createddate BIGINT NOT NULL,
	lastmodifiedby CHARACTER VARYING(250),
	lastModifiedDate BIGINT,
	
	CONSTRAINT pk_eghrms_deactivationdetails  PRIMARY KEY (uuid),
	CONSTRAINT fk_eghrms_deactivationdetails_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

);

-- --- V20201223230836__eg_hrms_employee_reactivation_details_index_ddl.sql ---
CREATE TABLE eg_hrms_reactivationdetails (
 	uuid CHARACTER VARYING(1024) NOT NULL,
 	employeeid CHARACTER VARYING(1024) NOT NULL,
 	reasonforreactivation CHARACTER VARYING(250),
 	effectivefrom BIGINT,
 	ordernumber CHARACTER VARYING(250),
 	remarks CHARACTER VARYING(250),
 	tenantid CHARACTER VARYING(250) NOT NULL,
 	createdby CHARACTER VARYING(250) NOT NULL,
 	createddate BIGINT NOT NULL,
 	lastmodifiedby CHARACTER VARYING(250),
 	lastModifiedDate BIGINT,

 	CONSTRAINT pk_eghrms_reactivationdetails  PRIMARY KEY (uuid),
 	CONSTRAINT fk_eghrms_reactivationdetails_employeeid FOREIGN KEY (employeeid) REFERENCES eg_hrms_employee (uuid)  ON DELETE CASCADE

 );

-- ##### SERVICE: e4h-services/field-planner [Phase 2] #####

-- --- V20250901180100__fieldPlanner_create_ddl.sql ---
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
                             isdeleted boolean,
                             last_modified_by    character varying(64),
                             created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                             last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                             additional_details JSONB DEFAULT '{}',

                             CONSTRAINT valid_date_range CHECK (start_date < end_date)
);

-- --- V20250901180100__fieldPlanner_create_ddl.sql ---
CREATE TABLE field_plan_facilities (
                                       id VARCHAR PRIMARY KEY,
                                       tenant_id VARCHAR NOT NULL,
                                       field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
                                       facility_id VARCHAR NOT NULL, -- References existing facility.id
                                       status VARCHAR DEFAULT 'ACTIVE',
                                       created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                       last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                                       additional_details JSONB DEFAULT '{}',
                                       last_modified_by    character varying(64),
                                       is_deleted         boolean
);

-- --- V20250901180100__fieldPlanner_create_ddl.sql ---
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

-- --- V20250901180100__fieldPlanner_create_ddl.sql ---
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

-- --- V20250901180100__fieldPlanner_create_ddl.sql ---
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

-- ##### SERVICE: e4h-services/field-planner-activity [Phase 2] #####

-- --- V20250919180100__bom_create_ddl.sql ---
CREATE TABLE bom (
                     id VARCHAR PRIMARY KEY,
                     tenant_id VARCHAR NOT NULL,
                     name VARCHAR(255) NOT NULL,
                     assign_user VARCHAR(255) NOT NULL,
                     facility_id VARCHAR, -- References existing facility.id
                     data JSONB NOT NULL DEFAULT '{}', -- bom json data
                     is_active BOOLEAN DEFAULT TRUE,
                     created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                     last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
                     additional_details JSONB DEFAULT '{}'
);

-- --- V20250924180100__bom_document_create_ddl.sql ---
CREATE TABLE public.bom_document (
                                         id varchar(64) NOT NULL,
                                         bomid varchar(64) NOT NULL,
                                         documenttype varchar(256) NULL,
                                         filestoreid varchar(256) NOT NULL,
                                         documentuid varchar(64) NULL,
                                         additionaldetails jsonb NULL,
                                         status varchar(64) NULL,
                                         createdby varchar(64) NOT NULL,
                                         lastmodifiedby varchar(64) NULL,
                                         createdtime int8 NULL,
                                         lastmodifiedtime int8 NULL,
                                         CONSTRAINT uk_bom_document_id PRIMARY KEY (id)
);

-- --- V20251015163200__activity_facility_transaction_comment_create.sql ---
CREATE TABLE IF NOT EXISTS public.activity_facility_transaction (
    id VARCHAR(64) PRIMARY KEY,
    activity_facility_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(256),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    CONSTRAINT fk_activity_facility_transaction FOREIGN KEY (activity_facility_id)
        REFERENCES public.facility_activities(id)
        ON DELETE CASCADE
);

-- --- V20251015163200__activity_facility_transaction_comment_create.sql ---
CREATE TABLE IF NOT EXISTS public.activity_facility_transaction_comment (
    id VARCHAR(64) PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    comment_message TEXT,
    asset_type VARCHAR(64),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT
);

-- --- V20251030135800__activity_facility_assigned_users_ddl.sql ---
CREATE TABLE ACTIVITY_FACILITY_USERS
(
    id                character varying(64),
    tenantId          character varying(1000),
    activityFacilityId 		  character varying(64),
    userId 		  character varying(64),
    additionalDetails jsonb,
    createdBy         character varying(64),
    lastModifiedBy    character varying(64),
    createdTime       bigint,
    lastModifiedTime  bigint,
    isDeleted         boolean DEFAULT false,
    CONSTRAINT uk_activity_facility_user_id PRIMARY KEY (id)
);

-- ##### SERVICE: e4h-services/im-services #####

-- --- V20240501115019__create_table.sql ---
CREATE TABLE eg_incident_v2(

id                  character varying(64),
tenantId            character varying(256),
incidentType        character varying(256)  NOT NULL,
incidentId		    character varying(256),
additionalDetails   JSONB,
applicationStatus   character varying(128),
assigner 			character varying(128),
assignee			character varying(128),
createdby           character varying(256)  NOT NULL,
createdtime         bigint                  NOT NULL,
lastmodifiedby      character varying(256),
lastmodifiedtime    bigint,
CONSTRAINT uk_eg_incident_v2 UNIQUE (id),
CONSTRAINT pk_eg_incidentReq_v2 PRIMARY KEY (tenantId,incidentId)
);

-- --- V20240501115019__create_table.sql ---
CREATE TABLE eg_incident_address_v2 (

tenantId          CHARACTER VARYING(256)  NOT NULL,
id                CHARACTER VARYING(256)  NOT NULL,
parentid         	CHARACTER VARYING(256)  NOT NULL,
district          CHARACTER VARYING(256),
block          CHARACTER VARYING(256),
phctype          CHARACTER VARYING(256),
phcsubtype          CHARACTER VARYING(256),
createdby        	CHARACTER VARYING(128)  NOT NULL,
createdtime      	BIGINT NOT NULL,
lastmodifiedby   	CHARACTER VARYING(128),
lastmodifiedtime 	BIGINT,
additionaldetails JSONB,

CONSTRAINT pk_eg_incident_address_v2 PRIMARY KEY (id),
CONSTRAINT fk_eg_incident_address_v2 FOREIGN KEY (parentid) REFERENCES eg_incident_v2 (id)
);

-- ##### SERVICE: e4h-services/project #####

-- --- V20220103130700__task_create_ddl.sql ---
CREATE TABLE PROJECT_TASK
(
    id                                  character varying(64),
    clientReferenceId                   character varying(64),
    tenantId                            character varying(1000),
    projectId                           character varying(64),
    projectBeneficiaryId                character varying(64),
    projectBeneficiaryClientReferenceId character varying(64),
    plannedStartDate                    bigint,
    plannedEndDate                      bigint,
    actualStartDate                     bigint,
    actualEndDate                       bigint,
    addressId                           character varying(1000),
    status                              character varying(1000),
    additionalDetails                   jsonb,
    createdBy                           character varying(64),
    createdTime                         bigint,
    lastModifiedBy                      character varying(64),
    lastModifiedTime                    bigint,
    rowVersion                          bigint,
    isDeleted                           boolean,
    CONSTRAINT uk_project_task_id PRIMARY KEY (id),
    CONSTRAINT uk_task_clientReference_id UNIQUE (clientReferenceId)
);

-- --- V20220103131500__project_address_ddl.sql ---
CREATE TABLE IF NOT EXISTS ADDRESS
(
    id                character varying(64),
    tenantId          character varying(1000),
    doorNo            character varying(64),
    latitude          double precision,
    longitude         double precision,
    locationAccuracy  int,
    type              character varying(64),
    addressLine1      character varying(256),
    addressLine2      character varying(256),
    landmark          character varying(256),
    city              character varying(256),
    pincode           character varying(64),
    buildingName      character varying(256),
    street            character varying(256),
    localityCode      character varying(256),
    CONSTRAINT uk_address_id PRIMARY KEY (id)
);

-- --- V20221202180100__project_create_ddl.sql ---
CREATE TABLE project
(
    id                character varying(64),
    tenantId          character varying(1000),
    projectTypeId     character varying(64),
    addressId         character varying(64),
    startDate         bigint,
    endDate           bigint,
    isTaskEnabled     boolean,
    parent            character varying(64),
    projectHierarchy  text,
    additionalDetails jsonb,
    createdBy         character varying(64),
    createdTime       bigint,
    lastModifiedBy    character varying(64),
    lastModifiedTime  bigint,
    rowVersion        bigint,
    isDeleted         boolean,
    CONSTRAINT uk_project_id PRIMARY KEY (id)
);

-- --- V20221208135800__project_staff_ddl.sql ---
CREATE TABLE PROJECT_STAFF
(
    id                character varying(64),
    tenantId          character varying(1000),
    projectId 		  character varying(64),
    staffId 		  character varying(64),
    startDate         bigint,
    endDate           bigint,
    additionalDetails jsonb,
    createdBy         character varying(64),
    lastModifiedBy    character varying(64),
    createdTime       bigint,
    lastModifiedTime  bigint,
    rowVersion        bigint,
    isDeleted         boolean,
    CONSTRAINT uk_project_staff_id PRIMARY KEY (id)
);

-- --- V20230103161300__task_resource_ddl.sql ---
CREATE TABLE TASK_RESOURCE
(
    id                    character varying(64),
    tenantId              character varying(1000),
    productVariantId      character varying(64),
    taskId                character varying(64),
    quantity              bigint,
    isDelivered           boolean,
    reasonIfNotDelivered  character varying(1000),
    createdBy             character varying(64),
    createdTime           bigint,
    lastModifiedBy        character varying(64),
    lastModifiedTime      bigint,
    isDeleted             boolean,
    CONSTRAINT uk_task_resource_id PRIMARY KEY (id)
);

-- --- V20230213142900__project_document_ddl.sql ---
CREATE TABLE project_document
(
    id                character varying(64),
    projectId         character varying(64) NOT NULL,
    documentType      character varying(256),
    filestoreId       character varying(256) NOT NULL,
    documentUid       character varying(64),
    additionalDetails jsonb,
    status            character varying(64),
    createdBy         character varying(64)  NOT NULL,
    lastModifiedBy    character varying(64),
    createdTime       bigint,
    lastModifiedTime  bigint,
    CONSTRAINT uk_project_document_id PRIMARY KEY (id)
);

-- --- V20230213143700__project_address_create_ddl.sql ---
CREATE TABLE project_address
(
    id               character varying(64),
    tenantId         character varying(64) NOT NULL,
    projectId        character varying(64) NOT NULL,
    door_no          character varying(64),
    latitude         bigint,
    longitude        bigint,
    locationAccuracy bigint,
    type             character varying(64),
    addressLine1     character varying(256),
    addressLine2     character varying(256),
    landmark         character varying(256),
    city             character varying(256),
    pinCode          character varying(64),
    buildingName     character varying(256),
    street           character varying(256),
    locality         character varying(128),
    CONSTRAINT uk_project_address_id PRIMARY KEY (id)
);

-- --- V20230220171200__project_resource_create_ddl.sql ---
CREATE TABLE project_resource
(
    id                character varying(64),
    tenantId          character varying(1000),
    projectId         character varying(64),
    productVariantId  character varying(64),
    isBaseUnitVariant boolean,
    type              character varying(1000),
    startDate         bigint,
    endDate           bigint,
    createdBy         character varying(64),
    createdTime       bigint,
    lastModifiedBy    character varying(64),
    lastModifiedTime  bigint,
    rowVersion        bigint,
    isDeleted         boolean,
    CONSTRAINT uk_project_resource_id PRIMARY KEY (id)
);

-- --- V20230224122200__project_facility_ddl.sql ---
CREATE TABLE PROJECT_FACILITY
(
    id                character varying(64),
    tenantId          character varying(1000),
    projectId 		  character varying(64),
    facilityId 		  character varying(64),
    additionalDetails jsonb,
    createdBy         character varying(64),
    lastModifiedBy    character varying(64),
    createdTime       bigint,
    lastModifiedTime  bigint,
    rowVersion        bigint,
    isDeleted         boolean,
    CONSTRAINT uk_project_facility_id PRIMARY KEY (id)
);

-- --- V20240711175300__user_location_ddl.sql ---
CREATE TABLE IF NOT EXISTS USER_LOCATION (
    id                                  CHARACTER VARYING(64),
    clientReferenceId                   CHARACTER VARYING(64),
    tenantId                            CHARACTER VARYING(1000) NOT NULL,
    projectId                           CHARACTER VARYING(64) NOT NULL,
    latitude                            DOUBLE PRECISION NOT NULL,
    longitude                           DOUBLE PRECISION NOT NULL,
    locationAccuracy                    INTEGER NOT NULL,
    boundaryCode                        CHARACTER VARYING(256) NOT NULL,
    action                              CHARACTER VARYING(256),
    createdBy                           CHARACTER VARYING(64) NOT NULL,
    createdTime                         BIGINT NOT NULL,
    lastModifiedBy                      CHARACTER VARYING(64) NOT NULL,
    lastModifiedTime                    BIGINT  NOT NULL,
    clientCreatedTime                   BIGINT,
    clientLastModifiedTime              BIGINT,
    clientCreatedBy                     CHARACTER VARYING(64),
    clientLastModifiedBy                CHARACTER VARYING(64),
    additionalDetails                   jsonb,
    CONSTRAINT pk_user_location PRIMARY KEY (id)
);

-- --- V20240711175500__user_action_ddl.sql ---
CREATE TABLE IF NOT EXISTS USER_ACTION (
id                          character varying(64),
clientReferenceId           character varying(64),
tenantId                    character varying(1000) not null,
projectId                   character varying(64) not null,
latitude                    double precision not null,
longitude                   double precision not null,
locationAccuracy            INTEGER not null,
boundaryCode                CHARACTER VARYING(256) not null,
action                      CHARACTER VARYING(256) not null,
beneficiaryTag              CHARACTER VARYING(64),
resourceTag                 CHARACTER VARYING(64),
status                      character varying(1000),
additionalDetails           jsonb,
createdBy                   character varying(64) not null,
createdTime                 bigint not null,
lastModifiedBy              character varying(64) not null,
lastModifiedTime            bigint not null,
clientCreatedTime           bigint,
clientLastModifiedTime 	    bigint,
clientCreatedBy             character varying(64),
clientLastModifiedBy        character varying(64),
rowVersion                  bigint,
    CONSTRAINT pk_user_action_id PRIMARY KEY (id),
    CONSTRAINT uk_user_action_clientReference_id UNIQUE (clientReferenceId)
);

-- --- V20250620163200__project_transaction_comment_create.sql ---
CREATE TABLE IF NOT EXISTS public.project_transaction (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(256),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    CONSTRAINT fk_project_transaction_project FOREIGN KEY (project_id)
        REFERENCES public.project(id)
        ON DELETE CASCADE
);

-- --- V20250620163200__project_transaction_comment_create.sql ---
CREATE TABLE IF NOT EXISTS public.project_transaction_comment (
    id VARCHAR(64) PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    comment_message TEXT,
    asset_type VARCHAR(64),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT
);

-- ##### SERVICE: e4h-services/vendor-registry #####

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org (

  id                     character varying(256),
  tenant_id              character varying(64) NOT NULL,
  application_number     character varying(140) NOT NULL,
  name                   character varying(140) NOT NULL,
  org_number             character varying(140),
  external_ref_number    character varying(64),
  date_of_incorporation  bigint,
  application_status     character varying(256),
  is_active              boolean,
  additional_details     jsonb,
  created_by             character varying(64),
  last_modified_by       character varying(64),
  created_time           bigint,
  last_modified_time     bigint,

CONSTRAINT uk_eg_org UNIQUE (application_number),
CONSTRAINT pk_eg_org PRIMARY KEY (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_address (

  id                  character varying(256),
  tenant_id           character varying(64),
  org_id              character varying(256) NOT NULL,
  door_no             character varying,
  plot_no             character varying,
  landmark            character varying,
  city                character varying,
  pin_code            character varying,
  district            character varying,
  region              character varying,
  state               character varying,
  country             character varying,
  boundary_code       character varying,
  boundary_type       character varying,
  building_name       character varying(64),
  street              character varying(64),
  additional_details  jsonb,


  CONSTRAINT pk_eg_org_address PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_address FOREIGN KEY (org_id) REFERENCES eg_org (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_address_boundary (
--
--  id                character varying(256),
--  address_id        character varying(256) NOT NULL,
--  code              character varying,
--  name              character varying,
--  label             character varying,
--  latitude          numeric,
--  longitude         numeric,
--
-- CONSTRAINT pk_eg_org_address_boundary PRIMARY KEY (id),
-- CONSTRAINT fk_eg_org_address_boundary FOREIGN KEY (address_id) REFERENCES eg_org_address (id)
--);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_address_geo_location (
  id                   character varying(256),
  address_id           character varying(256) NOT NULL,
  latitude             numeric,
  longitude            numeric,
  additional_details   jsonb,

  CONSTRAINT pk_eg_org_address_geo_location PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_address_geo_location FOREIGN KEY (address_id) REFERENCES eg_org_address (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_contact_detail (
  id                       character varying(256),
  tenant_id                character varying(64),
  org_id                   character varying(256) NOT NULL,
  contact_name             character varying(64),
  contact_mobile_number    character varying(15),
  contact_email            character varying(64),

  CONSTRAINT pk_eg_org_contact_detail PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_contact_detail FOREIGN KEY (org_id) REFERENCES eg_org (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_tax_identifier (
  id                     character varying(256),
  org_id                 character varying(256) NOT NULL,
  type                   character varying(64),
  value                  character varying(64),
  additional_details     jsonb,

  CONSTRAINT pk_eg_tax_identifier PRIMARY KEY (id),
  CONSTRAINT fk_eg_tax_identifier FOREIGN KEY (org_id) REFERENCES eg_org (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_jurisdiction (
  id                   character varying(256),
  org_id               character varying(256) NOT NULL,
  code                 character varying(64) NOT NULL,
  additional_details   jsonb,

  CONSTRAINT pk_eg_org_jurisdiction PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_jurisdiction FOREIGN KEY (org_id) REFERENCES eg_org (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_function (
  id                        character varying(256),
  org_id                    character varying(256) NOT NULL,
  application_number        character varying(140) NOT NULL,
  type                      character varying(256),
  category                  character varying(256),
  class                     character varying(256),
  valid_from                bigint,
  valid_to                  bigint,
  application_status        character varying(256),
  wf_status                 character varying(256),
  is_active                 boolean,
  additional_details        jsonb,
  created_by                character varying(64),
  last_modified_by          character varying(64),
  created_time              bigint,
  last_modified_time        bigint,

  CONSTRAINT pk_eg_org_function PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_function FOREIGN KEY (org_id) REFERENCES eg_org (id)
);

-- --- V20230301120030__create_table.sql ---
CREATE TABLE eg_org_document (
  id                    character varying(256),
  org_id                character varying(256),
  org_func_id           character varying(256),
  document_type         character varying,
  file_store            character varying,
  document_uid          character varying(256),
  additional_details    jsonb,

  CONSTRAINT pk_eg_org_document PRIMARY KEY (id),
  CONSTRAINT fk_eg_org_document FOREIGN KEY (org_id) REFERENCES eg_org (id),
  CONSTRAINT fk_eg_org_document_func FOREIGN KEY (org_func_id) REFERENCES eg_org_function (id)
);

-- --- V20250929135800__organization_user_ddl.sql ---
CREATE TABLE eg_org_user
(
    id                character varying(64),
    tenantId          character varying(64),
    organizationId 		  character varying(64),
    userId 		  character varying(64),
    additionalDetails jsonb,
    createdBy         character varying(64),
    lastModifiedBy    character varying(64),
    createdTime       bigint,
    lastModifiedTime  bigint,
    isdeleted  boolean,
    CONSTRAINT uk_organization_user_id PRIMARY KEY (id)
);

-- =============================================================================

-- =============================================================================
-- SECTION 2b: ALTER TABLE ADD COLUMN (Livelihood-relevant tables only)
-- =============================================================================

-- Table: activity_assignments
-- [V20250929180100__activity_assignment_update_ddl.sql] ADD "role" jsonb NULL
-- [V20250929180100__activity_assignment_update_ddl.sql] ADD emailsent boolean NULL DEFAULT false
-- [V20250929180100__activity_assignment_update_ddl.sql] ADD isdeleted boolean NULL DEFAULT false
-- [V20251002180100__activity_assignment_update_ddl.sql] ADD poc_number varchar NULL

-- Table: address
-- [V20230223043100__project_clientrefid_ddl.sql] ADD clientReferenceId character varying(64) UNIQUE

-- Table: asset
-- [V20250625141800__asset_column_add_ddl.sql] ADD is_operational BOOLEAN
-- [V20251017141800__asset_column_add_ddl.sql] ADD activity_facility_id VARCHAR

-- Table: asset_documents
-- [V20250618141800__asset_document_column_add_ddl.sql] ADD latitude DOUBLE PRECISION
-- [V20250618141800__asset_document_column_add_ddl.sql] ADD longitude DOUBLE PRECISION

-- Table: bom
-- [V20251017141800__bom_column_add_ddl.sql] ADD activity_facility_id VARCHAR

-- Table: eg_filestoremap
-- [V20170420135841__egfilestore_tenant_ddl.sql] ADD CONSTRAINT pk_filestoremap PRIMARY KEY (id)
-- [V20170420135841__egfilestore_tenant_ddl.sql] ADD CONSTRAINT uk_filestoremap_filestoreid UNIQUE (filestoreid)
-- [V20170420135841__egfilestore_tenant_ddl.sql] ADD constraint uk_filestoremap_fsid_tenant unique (filestoreid,tenantid)
-- [V20180319162241__eg_filestore_alter_ddl.sql] ADD filesource character varying(64)
-- [V20200712143311__egfilestore_audit_details.sql] ADD createdby character varying(64)
-- [V20200712143311__egfilestore_audit_details.sql] ADD lastmodifiedby character varying(64)
-- [V20200712143311__egfilestore_audit_details.sql] ADD createdtime bigint
-- [V20200712143311__egfilestore_audit_details.sql] ADD lastmodifiedtime bigint

-- Table: eg_hrms_assignment
-- [V20190130120650__alter_assgnmt_add_currentassgmt_ddl.sql] ADD iscurrentassignment BOOLEAN
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_hrms_deactivationdetails
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_hrms_departmentaltests
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_hrms_educationaldetails
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_hrms_employee
-- [V20190215120811__alter_uk_constraint_dml.sql] ADD CONSTRAINT uk_eghrms_employee_code UNIQUE (code, tenantid)
-- [V20201223230836__eg_hrms_employee_reactivation_details_index_ddl.sql] ADD reactivateemployee BOOLEAN

-- Table: eg_hrms_jurisdiction
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_hrms_servicehistory
-- [V20190301154105__alter_add_isactive_ddl.sql] ADD isActive BOOLEAN

-- Table: eg_incident_v2
-- [V20240502035019__incident_alter_table.sql] ADD district CHARACTER VARYING(256)
-- [V20240502035019__incident_alter_table.sql] ADD block CHARACTER VARYING(256)
-- [V20240502035019__incident_alter_table.sql] ADD phctype CHARACTER VARYING(256)
-- [V20240502035019__incident_alter_table.sql] ADD phcsubtype CHARACTER VARYING(256)
-- [V20240502035019__incident_alter_table.sql] ADD incidentsubtype CHARACTER VARYING(256)
-- [V20240504082919__incident_alter_table.sql] ADD accountid CHARACTER VARYING(256)
-- [V20240513110019__incident_alter_table.sql] ADD comments CHARACTER VARYING(256)
-- [V20240607101135__incident_alter2_table.sql] ADD reportertenant CHARACTER VARYING(256)
-- [V20240620115235__incident_reportertype_table.sql] ADD reportertype CHARACTER VARYING(256)
-- [V20240621115019__incident_legacy_fields.sql] ADD filed_date BIGINT
-- [V20240621115019__incident_legacy_fields.sql] ADD legacy_id CHARACTER VARYING(256)
-- [V20240621115019__incident_legacy_fields.sql] ADD migration_id CHARACTER VARYING(256)
-- [V20250529101135__incident_system_functional.sql] ADD systemfunctional CHARACTER VARYING(256)
-- [V20250626153835__incident_legacy_fields.sql] ADD fileddate BIGINT
-- [V20250626153835__incident_legacy_fields.sql] ADD legacyid CHARACTER VARYING(256)
-- [V20250626153835__incident_legacy_fields.sql] ADD migrationid CHARACTER VARYING(256)
-- [V20251023035019__incident_alter_table.sql] ADD ispotentialduplicate boolean NULL
-- [V20251111123000__incident_alter_table.sql] ADD facilityid CHARACTER VARYING(64)
-- [V20251111123000__incident_alter_table.sql] ADD boundarycode CHARACTER VARYING(256)
-- [V20251111123000__incident_alter_table.sql] ADD CONSTRAINT fk_eg_incident_v2_facility  FOREIGN KEY (facilityid) REFERENCES public.facility(id)
-- [V20260226120000__incident_warranty_status.sql] ADD warranty_status CHARACTER VARYING(256) DEFAULT 'WITHIN_WARRANTY'

-- Table: eg_org
-- [V20250427181530__alter_table.sql] ADD code character varying(256)
-- [V20251229140730__alter_table.sql] ADD org_type VARCHAR(64)
-- [V20251229140730__alter_table.sql] ADD org_subtype VARCHAR(256)
-- [V20251229140730__alter_table.sql] ADD org_poc_name VARCHAR(256)
-- [V20251229140730__alter_table.sql] ADD org_poc_phone VARCHAR(64)
-- [V20251229140730__alter_table.sql] ADD org_poc_email VARCHAR(256)
-- [V20251229140730__alter_table.sql] ADD org_poc_username VARCHAR(128)
-- [V20251229140730__alter_table.sql] ADD org_status VARCHAR(64) DEFAULT 'ACTIVE'

-- Table: eg_org_address
-- [V20250427181530__alter_table.sql] ADD hqaddress character varying(256)

-- Table: eg_org_contact_detail
-- [V20230619140730__alter_table.sql] ADD individual_id character varying(256)

-- Table: eg_org_document
-- [V20230410153230__alter_table.sql] ADD is_active boolean DEFAULT TRUE

-- Table: eg_org_function
-- [V20250427181530__alter_table.sql] ADD subtype character varying(256)

-- Table: eg_tax_identifier
-- [V20230410153230__alter_table.sql] ADD is_active boolean DEFAULT TRUE

-- Table: eg_wf_action_v2
-- [V20190117125333__wf_state_action_alter_table_ddl.sql] ADD CONSTRAINT fk_eg_wf_action_v2 FOREIGN KEY (currentstate) REFERENCES eg_wf_state_v2 (uuid)
-- [V20210423102936__wf_alter_table_active_ddl.sql] ADD active BOOLEAN DEFAULT TRUE

-- Table: eg_wf_businessservice_v2
-- [V20181226133033__wf_alter_table_ddl.sql] ADD businessServiceSla bigint

-- Table: eg_wf_processinstance_v2
-- [V20181226133033__wf_alter_table_ddl.sql] ADD moduleName character varying(64)
-- [V20181226133033__wf_alter_table_ddl.sql] ADD businessServiceSla bigint
-- [V20210203112523__wf_alter_table_ddl.sql] ADD rating SMALLINT
-- [V20210817103463__wf_alter_table_escalated_ddl.sql] ADD escalated BOOLEAN DEFAULT FALSE
-- [V20250808153000__wf_alter_table_add_isActive_ddl.sql] ADD isactive boolean NOT NULL DEFAULT true

-- Table: eg_wf_state_v2
-- [V20190117125333__wf_state_action_alter_table_ddl.sql] ADD CONSTRAINT pk_eg_wf_state_v2 PRIMARY KEY (uuid)
-- [V20190117125333__wf_state_action_alter_table_ddl.sql] ADD CONSTRAINT uk_eg_wf_state_v2  UNIQUE (state, businessserviceid)
-- [V20190322143035__wf_add_seq_ddl.sql] ADD seq INT
-- [V20190411170435__wf_add_isStateUpdatable_ddl.sql] ADD isStateUpdatable boolean
-- [V20250703120000__wf_alter_table_state_triggerparallelworkflows_ddl.sql] ADD triggerparallelworkflows jsonb

-- Table: facility
-- [V20251030113000__alter_facility_add_column_is_onm_ready.sql] ADD is_onm_ready BOOLEAN DEFAULT FALSE
-- [V20251223135300__alter_facility_add_column.sql] ADD facility_poc_name VARCHAR(256)
-- [V20251223135300__alter_facility_add_column.sql] ADD facility_poc_phone VARCHAR(64)
-- [V20251223135300__alter_facility_add_column.sql] ADD facility_poc_email VARCHAR(256)
-- [V20251223135300__alter_facility_add_column.sql] ADD hfr_id VARCHAR(128)
-- [V20251223135300__alter_facility_add_column.sql] ADD nin_id VARCHAR(128)
-- [V20251223135300__alter_facility_add_column.sql] ADD user_id VARCHAR(128)
-- [V20251223135300__alter_facility_add_column.sql] ADD facility_status VARCHAR(64)
-- [V20260506031500__alter_facility_add_facility_category_and_backfill.sql] ADD facility_category VARCHAR
-- [V20260506031500__alter_facility_add_facility_category_and_backfill.sql] ADD facility_poc_username VARCHAR

-- Table: facility_activities
-- [V20251103180100__activity_facility_update_ddl.sql] ADD isdeleted boolean NULL DEFAULT false

-- Table: id_generator
-- [V20181030123635__DELETE_DUPLICATES_AND_ALTER_PRIMARYKEY_ID_GEN_ddl.sql] ADD CONSTRAINT pk_id_generator PRIMARY KEY (idname, tenantid)

-- Table: project
-- [V20230213144100__project_column_add_ddl.sql] ADD projectNumber character varying(128) NOT NULL
-- [V20230213144100__project_column_add_ddl.sql] ADD projectSubType character varying(128) NOT NULL
-- [V20230213144100__project_column_add_ddl.sql] ADD projectType character varying(64)
-- [V20230213144100__project_column_add_ddl.sql] ADD name character varying(128)
-- [V20230213144100__project_column_add_ddl.sql] ADD department character varying(64) NOT NULL
-- [V20230213144100__project_column_add_ddl.sql] ADD description character varying(256) NOT NULL
-- [V20230213144100__project_column_add_ddl.sql] ADD referenceId character varying(100) NOT NULL
-- [V20230228120000__project_add_column_ddl.sql] ADD natureOfWork character varying(64)
-- [V20250613163200__project_workflow_alter.sql] ADD status VARCHAR(50)

-- Table: project_address
-- [V20230301143700__project_address_alter_add_column_ddl.sql] ADD boundary_type character varying(64)

-- Table: project_task
-- [V20230628191500__add_client_audit_details_in_project_ddl.sql] ADD clientCreatedTime bigint
-- [V20230628191500__add_client_audit_details_in_project_ddl.sql] ADD clientLastModifiedTime bigint
-- [V20230830122000__add_client_audit_details_in_project_ddl.sql] ADD clientCreatedBy character varying(64)
-- [V20230830122000__add_client_audit_details_in_project_ddl.sql] ADD clientLastModifiedBy character varying(64)

-- Table: task_resource
-- [V20230223043100__project_clientrefid_ddl.sql] ADD clientReferenceId character varying(64) UNIQUE
-- [V20231127145800__task_resource_additional_fields_ddl.sql] ADD additionalDetails jsonb

-- =============================================================================
-- SECTION 3: FOREIGN KEY CONSTRAINTS (30 entries)
-- =============================================================================

-- [e4h-services/field-planner] (inline column): (activity_id) -> activities(id)
--   source: V20250901180100__fieldPlanner_create_ddl.sql

-- [e4h-services/asset-registry] fk_document_asset: (asset_id) -> asset(asset_id)
--   source: V20250520141800__asset-service_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_assignment_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_educationaldetails_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_departmentaltests_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_empdocuments_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_servicehistory_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_jurisdiction_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_deactivationdetails_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20190122152236__create_hrms_employee_table_ddl.sql

-- [e4h-services/egov-hrms] fk_eghrms_reactivationdetails_employeeid: (employeeid) -> eg_hrms_employee(uuid) ON DELETE CASCADE
--   source: V20201223230836__eg_hrms_employee_reactivation_details_index_ddl.sql

-- [e4h-services/im-services] fk_eg_incident_address_v2: (parentid) -> eg_incident_v2(id)
--   source: V20240501115019__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_address: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_contact_detail: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_tax_identifier: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_jurisdiction: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_function: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_document: (org_id) -> eg_org(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_address_boundary: (address_id) -> eg_org_address(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_address_geo_location: (address_id) -> eg_org_address(id)
--   source: V20230301120030__create_table.sql

-- [e4h-services/vendor-registry] fk_eg_org_document_func: (org_func_id) -> eg_org_function(id)
--   source: V20230301120030__create_table.sql

-- [core-services/egov-workflow-v2] fk_eg_wf_state: (businessserviceid) -> eg_wf_businessservice_v2(uuid)
--   source: V20181204120036__wf_create_ddl.sql

-- [core-services/egov-workflow-v2] fk_eg_wf_Document: (processinstanceid) -> eg_wf_processinstance_v2(id)
--   source: V20181204120036__wf_create_ddl.sql

-- [core-services/egov-workflow-v2] fk_eg_wf_assignee_v2: (processinstanceid) -> eg_wf_processinstance_v2(id)
--   source: V20191211105434__wf_modified_assignee_ddl.sql

-- [core-services/egov-workflow-v2] fk_eg_wf_action: (currentstate) -> eg_wf_state_v2(uuid)
--   source: V20181204120036__wf_create_ddl.sql

-- [core-services/egov-workflow-v2] fk_eg_wf_action_v2: (currentstate) -> eg_wf_state_v2(uuid)
--   source: V20190117125333__wf_state_action_alter_table_ddl.sql

-- [e4h-services/im-services] fk_eg_incident_v2_facility: (facilityid) -> facility(id)
--   source: V20251111123000__incident_alter_table.sql

-- [e4h-services/field-planner-activity] fk_activity_facility_transaction: (activity_facility_id) -> facility_activities(id) ON DELETE CASCADE
--   source: V20251015163200__activity_facility_transaction_comment_create.sql

-- [core-services/health-facility-registry] fk_facility_address: (addressId) -> facility_address(id) ON DELETE SET NULL
--   source: V1202508190221__create_facility_registry_schema_ddl.sql

-- [e4h-services/field-planner] (inline column): (field_plan_id) -> field_plans(id)
--   source: V20250901180100__fieldPlanner_create_ddl.sql

-- [e4h-services/project] fk_project_transaction_project: (project_id) -> project(id) ON DELETE CASCADE
--   source: V20250620163200__project_transaction_comment_create.sql

-- =============================================================================
-- SECTION 4: LOGICAL RELATIONSHIPS (Livelihood domain model)
-- =============================================================================

-- eg_wf_processinstance_v2.businessId  -->  eg_incident_v2.incidentId
--   Workflow process links to incident; businessService = LivelihoodIncident

-- PROJECT_FACILITY.facilityId  -->  facility.id
--   Program site membership: Project → Facility

-- asset.facility_id  -->  facility.id
--   Many assets per facility; tickets require facilityId + assetId

-- asset.vendor_id  -->  eg_org.id
--   Asset-level vendor (Livelihood additive column); drives auto-assign

-- eg_incident_v2.asset_id  -->  asset.asset_id
--   Ticket linked to asset (Livelihood additive column)

-- eg_incident_v2.facilityid  -->  facility.id
--   Ticket facility scope

-- eg_hrms_jurisdiction.employeeid  -->  eg_hrms_employee.uuid
--   POC state scope / staff jurisdiction

-- eg_org_user.org_id  -->  eg_org.id
--   Vendor org users

-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] assigned_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] assigned_to VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] assigned_user VARCHAR, -- References eg_hrms_employee.uuid
-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] created_by VARCHAR NOT NULL, -- References eg_hrms_employee.uuid
-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] facility_id VARCHAR NOT NULL, -- References existing facility.id
-- [e4h-services/field-planner/V20250901180100__fieldPlanner_create_ddl.sql] project_id VARCHAR NOT NULL, -- References existing project
-- [e4h-services/field-planner-activity/V20250919180100__bom_create_ddl.sql] facility_id VARCHAR, -- References existing facility.id
-- [e4h-services/vendor-registry/V20230301120030__create_table.sql] CONSTRAINT fk_eg_org_address_boundary FOREIGN KEY (address_id) REFERENCES eg_org_address (id)

-- =============================================================================
-- SECTION 5: PROPOSED LIVELIHOOD ADDITIVE MIGRATIONS (apply via Flyway in fork)
-- =============================================================================
-- See LIVELIHOOD_PERSISTER.md for persister YAML alignment.

-- --- im-services: eg_incident_v2 ---
-- V{timestamp}__livelihood_incident_columns.sql
-- ALTER TABLE eg_incident_v2
--   ADD COLUMN IF NOT EXISTS asset_id CHARACTER VARYING(64),
--   ADD COLUMN IF NOT EXISTS created_on_behalf BOOLEAN DEFAULT FALSE,
--   ADD COLUMN IF NOT EXISTS entry_channel CHARACTER VARYING(64);
-- CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_asset_id ON eg_incident_v2 (tenantid, asset_id);
-- CREATE INDEX IF NOT EXISTS idx_eg_incident_v2_facility_asset ON eg_incident_v2 (facilityid, asset_id);

-- --- asset-registry: asset ---
-- V{timestamp}__livelihood_asset_columns.sql
-- ALTER TABLE asset
--   ADD COLUMN IF NOT EXISTS vendor_id CHARACTER VARYING(64),
--   ADD COLUMN IF NOT EXISTS item_code CHARACTER VARYING(128),
--   ADD COLUMN IF NOT EXISTS qr_code CHARACTER VARYING(256);
-- CREATE INDEX IF NOT EXISTS idx_asset_facility_vendor ON asset (tenant_id, facility_id, vendor_id);

-- --- project: optional justification code column ---
-- ALTER TABLE project ADD COLUMN IF NOT EXISTS justification_code CHARACTER VARYING(128);
-- CREATE INDEX IF NOT EXISTS idx_project_justification_code ON project (tenantId, justification_code);

-- --- facility: manager contact (if not using additional_details JSON only) ---
-- ALTER TABLE facility ADD COLUMN IF NOT EXISTS manager_name CHARACTER VARYING(256);
-- ALTER TABLE facility ADD COLUMN IF NOT EXISTS manager_mobile CHARACTER VARYING(32);

-- =============================================================================
-- END OF LIVELIHOOD_SCHEMA_FROM_MIGRATIONS.sql
-- =============================================================================