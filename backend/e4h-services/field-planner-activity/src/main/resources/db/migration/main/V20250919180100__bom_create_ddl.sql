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