
CREATE TABLE IF NOT EXISTS asset (
    asset_id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    system VARCHAR NOT NULL,
    facility_id VARCHAR NOT NULL,
    asset_type_id VARCHAR(64) NOT NULL,
    serial_number VARCHAR(128) NOT NULL,
    model_number VARCHAR(128) NOT NULL,
    brand_id VARCHAR(64) NOT NULL,
    asset_details JSONB,
    warranty_start_date BIGINT,
    warranty_duration INT,
    warranty_end_date BIGINT,
    wf_status VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    additional_details JSONB,
    created_by VARCHAR,
    created_time BIGINT,
    last_modified_by VARCHAR,
    last_modified_time BIGINT
);

CREATE INDEX IF NOT EXISTS idx_asset_tenant ON asset(tenant_id);
CREATE INDEX IF NOT EXISTS idx_asset_facility ON asset(tenant_id,facility_id);
CREATE INDEX IF NOT EXISTS idx_asset_brand ON asset(tenant_id,brand_id);
CREATE INDEX IF NOT EXISTS idx_asset_serial ON asset(tenant_id,serial_number);
CREATE INDEX IF NOT EXISTS idx_asset_model ON asset(tenant_id,model_number);
CREATE INDEX IF NOT EXISTS idx_asset_status ON asset(tenant_id,wf_status);

--- Table to store documents related to the asset. Can be images or any other artifacts. Documents are uploaded in filestore and the id
-- is referenced here
CREATE TABLE IF NOT EXISTS asset_documents (
    id VARCHAR PRIMARY KEY,
    tenant_id VARCHAR NOT NULL,
    asset_id VARCHAR NOT NULL,
    filestore_id VARCHAR NOT NULL,
    document_type VARCHAR,
    uploaded_at BIGINT,
    additional_details JSONB,
    created_by VARCHAR,
    created_time BIGINT,
    updated_by VARCHAR,
    updated_time BIGINT,
    CONSTRAINT fk_document_asset FOREIGN KEY (asset_id) REFERENCES asset(asset_id)
);

CREATE INDEX IF NOT EXISTS idx_document_asset ON asset_documents(tenant_id, asset_id);
CREATE INDEX IF NOT EXISTS idx_document_filestore ON asset_documents(tenant_id, filestore_id);