
-- Flyway Migration Script

-- asset_details is a JSONB whose schema has to be validated against MDMS
CREATE TABLE asset (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    facility_id UUID NOT NULL,
    asset_type_id VARCHAR(64),
    serial_number VARCHAR(128),
    model_number VARCHAR(128),
    brand_id VARCHAR(64),
    asset_details JSONB,
    warranty_start_date TIMESTAMP WITH TIME ZONE,
    warranty_duration VARCHAR,
    warranty_end_date TIMESTAMP WITH TIME ZONE,
    wf_status VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    additional_details JSONB,
    created_by VARCHAR,
    created_at BIGINT,
    updated_by VARCHAR,
    updated_at BIGINT
);

CREATE INDEX idx_asset_tenant ON asset(tenant_id);
CREATE INDEX idx_asset_facility ON asset(tenant_id,facility_id);
CREATE INDEX idx_asset_brand ON asset(tenant_id,brand_id);
CREATE INDEX idx_asset_serial ON asset(tenant_id,serial_number);
CREATE INDEX idx_asset_model ON asset(tenant_id,model_number);
CREATE INDEX idx_asset_status ON asset(tenant_id,wf_status);

--- Table to store documents related to the asset. Can be images or any other artifacts. Documents are uploaded in filestore and the id
-- is referenced here
CREATE TABLE asset_documents (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    asset_id UUID NOT NULL,
    filestore_id VARCHAR NOT NULL,
    document_type VARCHAR,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    additional_details JSONB,
    created_by VARCHAR,
    created_at BIGINT,
    updated_by VARCHAR,
    updated_at BIGINT,
    CONSTRAINT fk_document_asset FOREIGN KEY (asset_id) REFERENCES asset(id)
);

CREATE INDEX idx_document_asset ON asset_documents(tenant_id, asset_id);
CREATE INDEX idx_document_filestore ON asset_documents(tenant_id, filestore_id);
