-- Rename the grid voltage variation sub-type for RMS-generated incidents from "LowVoltage" to "ReverseVoltage".
-- The RMS service now creates GRID/ReverseVoltage tickets for the new 50V-150V range.

-- 1) Update any existing incidents to use the new sub-type so dashboards/queries stay consistent.
UPDATE eg_incident_v2
SET incidentsubtype = 'ReverseVoltage'
WHERE incidenttype = 'GRID'
  AND incidentsubtype = 'LowVoltage';

-- 2) Update the priority lookup so newly created GRID/ReverseVoltage tickets resolve to a priority.
UPDATE im_services_priority
SET incidentSubType = 'ReverseVoltage'
WHERE incidentType = 'GRID'
  AND incidentSubType = 'LowVoltage';
