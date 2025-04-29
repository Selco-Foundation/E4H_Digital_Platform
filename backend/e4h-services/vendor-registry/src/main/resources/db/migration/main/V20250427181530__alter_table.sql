ALTER TABLE eg_org
ADD COLUMN code character varying(256);

ALTER TABLE eg_org_address
ADD COLUMN hqaddress character varying(256);

ALTER TABLE eg_org_function
ADD COLUMN subtype character varying(256);