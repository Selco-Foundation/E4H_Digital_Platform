CREATE TABLE field_plan_template (
                                     id VARCHAR(64) PRIMARY KEY,
                                     field_plan_id VARCHAR(64) NOT NULL REFERENCES field_plans(id),
                                     system_type VARCHAR(100) NOT NULL,
                                     total_capacity VARCHAR(100) NOT NULL,
                                     template_data JSONB NOT NULL DEFAULT '{}',
                                     tenant_id VARCHAR(256) NOT NULL,
                                     file_store_id VARCHAR(64) NOT NULL,
                                     created_by VARCHAR(64),
                                     last_modified_by VARCHAR(64),
                                     created_time BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000),
                                     last_modified_time BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)
);

CREATE INDEX idx_field_plan_template_tenant
    ON field_plan_template(tenant_id);

CREATE INDEX idx_field_plan_template_field_plan
    ON field_plan_template(field_plan_id);

CREATE UNIQUE INDEX uniq_field_plan_template_combo
    ON field_plan_template(field_plan_id, system_type, total_capacity);