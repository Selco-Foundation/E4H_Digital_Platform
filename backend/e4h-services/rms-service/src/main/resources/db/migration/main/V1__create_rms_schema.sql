-- Create telemetry_data table to store raw RMS readings
CREATE TABLE IF NOT EXISTS telemetry_data (
    id VARCHAR(255) PRIMARY KEY,
    facility_id VARCHAR(255) NOT NULL,
    hfr_id VARCHAR(255),
    center_id VARCHAR(255),
    graph_type VARCHAR(100),
    reading_type VARCHAR(50),
    reading_data JSONB,
    collected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_telemetry_facility_type_time UNIQUE (facility_id, graph_type, collected_at)
);

CREATE INDEX idx_telemetry_facility_id ON telemetry_data(facility_id);
CREATE INDEX idx_telemetry_collected_at ON telemetry_data(collected_at);
CREATE INDEX idx_telemetry_graph_type ON telemetry_data(graph_type);

-- Create active_alerts table for deduplication
CREATE TABLE IF NOT EXISTS active_alerts (
    id VARCHAR(255) PRIMARY KEY,
    facility_id VARCHAR(255) NOT NULL,
    hfr_id VARCHAR(255),
    alert_type VARCHAR(50) NOT NULL,
    alert_sub_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    last_suppressed_at TIMESTAMP,
    ticket_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_active_alerts_facility_type_subtype UNIQUE (facility_id, alert_type, alert_sub_type)
);

CREATE INDEX idx_active_alerts_facility_id ON active_alerts(facility_id);
CREATE INDEX idx_active_alerts_status ON active_alerts(status);
CREATE INDEX idx_active_alerts_detected_at ON active_alerts(detected_at);
CREATE INDEX idx_active_alerts_type_subtype ON active_alerts(alert_type, alert_sub_type);
CREATE INDEX idx_active_alerts_ticket_id ON active_alerts(ticket_id);

-- Create alert_history table to track all alerts
CREATE TABLE IF NOT EXISTS alert_history (
    id VARCHAR(255) PRIMARY KEY,
    alert_id VARCHAR(255) NOT NULL,
    facility_id VARCHAR(255) NOT NULL,
    hfr_id VARCHAR(255),
    alert_type VARCHAR(50) NOT NULL,
    alert_sub_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    ticket_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES active_alerts(id)
);

CREATE INDEX idx_alert_history_alert_id ON alert_history(alert_id);
CREATE INDEX idx_alert_history_facility_id ON alert_history(facility_id);
CREATE INDEX idx_alert_history_detected_at ON alert_history(detected_at);

