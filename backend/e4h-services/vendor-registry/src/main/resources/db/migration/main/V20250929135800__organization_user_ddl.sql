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