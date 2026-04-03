INSERT INTO im_services_priority
(tenantId, incidentType, incidentSubType, systemFunctional, priority)
VALUES
    -- Low Generation
    ('mz', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('sk', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('ml', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('mn', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('nl', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('as', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('gj', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('or', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('mh', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('pg', 'PANEL', 'LowGeneration', NULL, 'HIGH'),
    ('in', 'PANEL', 'LowGeneration', NULL, 'HIGH'),

    -- Overcharge
    ('mz', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('sk', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('ml', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('mn', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('nl', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('as', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('gj', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('or', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('mh', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('pg', 'BATTERY', 'Overcharge', NULL, 'HIGH'),
    ('in', 'BATTERY', 'Overcharge', NULL, 'HIGH'),

    -- High Voltage
    ('mz', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('sk', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('ml', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('mn', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('nl', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('as', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('gj', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('or', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('mh', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('pg', 'GRID', 'HighVoltage', NULL, 'HIGH'),
    ('in', 'GRID', 'HighVoltage', NULL, 'HIGH'),

    -- Low Voltage
    ('mz', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('sk', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('ml', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('mn', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('nl', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('as', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('gj', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('or', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('mh', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('pg', 'GRID', 'LowVoltage', NULL, 'HIGH'),
    ('in', 'GRID', 'LowVoltage', NULL, 'HIGH');
