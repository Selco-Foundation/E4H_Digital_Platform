CREATE TABLE icc_templates (
                                       id VARCHAR PRIMARY KEY,
                                       system_type VARCHAR NOT NULL, -- References existing facility.id
                                       total_system_capacity VARCHAR,
                                       filestoreid VARCHAR
);
CREATE UNIQUE INDEX uk_icc_templates ON icc_templates (system_type, COALESCE(total_system_capacity, ''));