-- CO2 Dashboard reference tables (LLD #2421)

CREATE TABLE IF NOT EXISTS grid_intensity_factor (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    financial_year VARCHAR(16) NOT NULL,
    grid_intensity_factor NUMERIC(10, 6),
    projected_grid_intensity_factor NUMERIC(10, 6),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grid_intensity_tenant_fy UNIQUE (tenant_id, financial_year)
);

CREATE TABLE IF NOT EXISTS archetype_lookup (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    state VARCHAR(128) NOT NULL,
    facility_type VARCHAR(64) NOT NULL,
    archetype VARCHAR(8) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_archetype_lookup UNIQUE (tenant_id, state, facility_type)
);

CREATE TABLE IF NOT EXISTS archetype_properties (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    archetype VARCHAR(8) NOT NULL,
    year_one_annual_consumption_kwh NUMERIC(14, 6) NOT NULL,
    alpha NUMERIC(10, 8) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_archetype_properties UNIQUE (tenant_id, archetype)
);

CREATE TABLE IF NOT EXISTS state_sunshine_hours (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    state VARCHAR(128) NOT NULL,
    sunshine_hours_per_day NUMERIC(4, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_state_sunshine UNIQUE (tenant_id, state)
);

CREATE INDEX IF NOT EXISTS idx_archetype_lookup_tenant ON archetype_lookup (tenant_id);
COMMENT ON COLUMN archetype_properties.year_one_annual_consumption_kwh IS 'Archetype year-one annual facility consumption (kWh/year); PRD CY1';
COMMENT ON COLUMN archetype_properties.alpha IS 'Solar share of consumption (0-1); used as alpha in archetype emission formula';

CREATE INDEX IF NOT EXISTS idx_archetype_properties_tenant ON archetype_properties (tenant_id);
