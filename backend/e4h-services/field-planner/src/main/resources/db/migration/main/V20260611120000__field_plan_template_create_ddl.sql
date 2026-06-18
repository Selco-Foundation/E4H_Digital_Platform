CREATE TABLE field_plan_template (
    id VARCHAR PRIMARY KEY,
    field_plan_id VARCHAR NOT NULL REFERENCES field_plans(id),
    system_type VARCHAR NOT NULL,
    total_capacity VARCHAR NOT NULL,
    template_data JSONB NOT NULL DEFAULT '{}',
    tenant_id VARCHAR NOT NULL,
    created_by VARCHAR,
    last_modified_by VARCHAR,
    created_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    last_modified_time BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
);

CREATE INDEX idx_field_plan_template_tenant ON field_plan_template(tenant_id);
CREATE INDEX idx_field_plan_template_field_plan ON field_plan_template(field_plan_id);
CREATE UNIQUE INDEX uniq_field_plan_template_combo ON field_plan_template(field_plan_id, system_type, total_capacity);
