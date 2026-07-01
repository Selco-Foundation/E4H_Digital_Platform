-- Rename the grid voltage variation sub-type for RMS-generated incidents from "LowVoltage" to "ReverseVoltage".
-- The RMS service now creates GRID/ReverseVoltage tickets for the new 50V-150V range.

-- Historical rows use incidenttype 'Grid' or 'GRID' depending on source/tenant.

-- 1) Update any existing incidents to use the new sub-type so dashboards/queries stay consistent.
UPDATE eg_incident_v2
SET incidentsubtype = 'ReverseVoltage'
WHERE incidenttype IN ('Grid', 'GRID')
  AND incidentsubtype = 'LowVoltage';

-- 2) Update the priority lookup so newly created Grid/ReverseVoltage tickets resolve to a priority.
UPDATE im_services_priority
SET incidentSubType = 'ReverseVoltage'
WHERE incidentType IN ('Grid', 'GRID')
  AND incidentSubType = 'LowVoltage';
