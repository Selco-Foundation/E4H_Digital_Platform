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

CREATE INDEX idx_activity_facility_users_activityfacilityid
    ON ACTIVITY_FACILITY_USERS (activityFacilityId);