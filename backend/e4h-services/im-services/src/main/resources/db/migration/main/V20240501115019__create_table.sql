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


