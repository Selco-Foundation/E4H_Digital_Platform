CREATE TABLE IF NOT EXISTS eg_org_poc_change_audit
(
    id                 character varying(64) NOT NULL,
    tenant_id          character varying(64),
    organisation_id    character varying(64) NOT NULL,
    old_poc_name       character varying(256),
    old_poc_phone      character varying(64), -- Encrypted, as eg_org.org_poc_phone
    old_poc_email      character varying(256),
    old_poc_username   character varying(128),
    new_poc_name       character varying(256),
    new_poc_phone      character varying(64), -- Encrypted, as eg_org.org_poc_phone
    new_poc_email      character varying(256),
    new_poc_username   character varying(128),
    previous_user_id   character varying(64),
    new_user_id        character varying(64),
    changed_by         character varying(64),
    changed_time       bigint,
    CONSTRAINT pk_eg_org_poc_change_audit PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_org_poc_change_audit_org ON eg_org_poc_change_audit (organisation_id);
