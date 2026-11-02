-- Pairs each jurisdiction with the roles the employee holds AT that boundary, so scope can be
-- resolved per role rather than as one flat union across all of the employee's roles.
-- A jurisdiction with no rows here applies to every role the employee holds.
--
-- Only the role code is stored. The name is not copied: on search it is filled from the
-- employee's own user.roles entry with the same code.
--
-- No isactive column. An HRMS update deletes the employee and re-inserts it, so a removed
-- role is simply not written back. The cascade below is what makes that work:
--   eg_hrms_employee -> eg_hrms_jurisdiction -> eg_hrms_jurisdiction_role

CREATE TABLE IF NOT EXISTS eg_hrms_jurisdiction_role (
    uuid              CHARACTER VARYING(256)  NOT NULL,
    jurisdictionid    CHARACTER VARYING(1024) NOT NULL,
    rolecode          CHARACTER VARYING(256)  NOT NULL,
    tenantid          CHARACTER VARYING(250)  NOT NULL,
    createdby         CHARACTER VARYING(250)  NOT NULL,
    createddate       BIGINT                  NOT NULL,
    lastmodifiedby    CHARACTER VARYING(250),
    lastmodifieddate  BIGINT,

    CONSTRAINT pk_eghrms_jurisdiction_role PRIMARY KEY (uuid),
    CONSTRAINT uk_eghrms_jurisdiction_role UNIQUE (jurisdictionid, rolecode),
    CONSTRAINT fk_eghrms_jurisdiction_role_jurisdictionid
        FOREIGN KEY (jurisdictionid) REFERENCES eg_hrms_jurisdiction (uuid) ON DELETE CASCADE
);

-- Search by role across boundaries ("who holds COMPLAINT_FACILITATOR_1 anywhere in this set"),
-- which is the direction the assignment pickers and the inbox scope resolver read in.
CREATE INDEX IF NOT EXISTS idx_eghrms_jurisdiction_role_rolecode
    ON eg_hrms_jurisdiction_role (rolecode);
