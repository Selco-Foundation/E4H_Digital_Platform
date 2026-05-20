-- CO2 Dashboard: solar/RMS dates and installed capacity (LLD #2420)

ALTER TABLE facility
    ADD COLUMN IF NOT EXISTS solar_installation_date DATE,
    ADD COLUMN IF NOT EXISTS rms_installation_date DATE,
    ADD COLUMN IF NOT EXISTS solar_system_capacity_kwp NUMERIC(12, 4);

COMMENT ON COLUMN facility.solar_installation_date IS 'Solar system commissioned date for CO2 lifecycle calculations';
COMMENT ON COLUMN facility.rms_installation_date IS 'Elmeasure RMS install date; NULL indicates non-RMS facility';
COMMENT ON COLUMN facility.solar_system_capacity_kwp IS 'Installed solar PV capacity (kWp) for CO2 dashboard and facility reporting';

CREATE INDEX IF NOT EXISTS idx_facility_solar_installation_date ON facility (solar_installation_date);
CREATE INDEX IF NOT EXISTS idx_facility_rms_installation_date ON facility (rms_installation_date);
