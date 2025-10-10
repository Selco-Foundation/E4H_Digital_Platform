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