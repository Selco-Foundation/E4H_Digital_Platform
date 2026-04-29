CREATE TABLE IF NOT EXISTS rms_ticket_pause_config (
    id VARCHAR(255) PRIMARY KEY,
    facility_id VARCHAR(255) NOT NULL UNIQUE,
    tenant_id VARCHAR(255) NOT NULL DEFAULT 'in',
    facility_name VARCHAR(500),
    boundary_code VARCHAR(500),
    paused_until TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    requested_by VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rms_pause_facility_id ON rms_ticket_pause_config(facility_id);
CREATE INDEX IF NOT EXISTS idx_rms_pause_tenant_id ON rms_ticket_pause_config(tenant_id);
CREATE INDEX IF NOT EXISTS idx_rms_pause_is_active ON rms_ticket_pause_config(is_active);
CREATE INDEX IF NOT EXISTS idx_rms_pause_paused_until ON rms_ticket_pause_config(paused_until);
CREATE INDEX IF NOT EXISTS idx_rms_pause_boundary_code ON rms_ticket_pause_config(boundary_code);
