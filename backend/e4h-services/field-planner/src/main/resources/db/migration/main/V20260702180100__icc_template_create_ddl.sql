CREATE TABLE IF NOT EXISTS icc_templates (
                               id VARCHAR(64) PRIMARY KEY,
                               system_type VARCHAR(100) NOT NULL,
                               total_system_capacity VARCHAR(100),
                               filestoreid VARCHAR(64)
);

CREATE UNIQUE INDEX uk_icc_templates
    ON icc_templates (system_type, COALESCE(total_system_capacity, ''));