CREATE TABLE facility_rms_inactive_incident(
       id                  character varying(64),
       tenantId            character varying(256)  NOT NULL,
       facilityid          character varying(256)  NOT NULL,
       incidentid		    character varying(256) NOT NULL,
       createdby           character varying(256)  NOT NULL,
       createdtime         bigint                  NOT NULL,
       lastmodifiedby      character varying(256),
       lastmodifiedtime    bigint,
       CONSTRAINT pk_facility_rms_inactive_incident PRIMARY KEY (id),
       CONSTRAINT uk_facility_rms_inactive_incident UNIQUE (incidentId)
);

CREATE INDEX IF NOT EXISTS idx_facility_rms_inactive_facility_tenant
    ON facility_rms_inactive_incident (facilityid, tenantid);