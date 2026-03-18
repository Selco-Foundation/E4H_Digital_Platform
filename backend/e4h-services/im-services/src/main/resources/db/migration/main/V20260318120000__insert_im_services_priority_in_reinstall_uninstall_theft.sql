INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Reinstall', 'ReinstallSolarSystem', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Reinstall'
      AND isp.incidentsubtype = 'ReinstallSolarSystem'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Uninstall', 'UninstallSolarSystem', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Uninstall'
      AND isp.incidentsubtype = 'UninstallSolarSystem'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Theft', 'TheftPanel', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Theft'
      AND isp.incidentsubtype = 'TheftPanel'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Theft', 'TheftBattery', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Theft'
      AND isp.incidentsubtype = 'TheftBattery'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Theft', 'TheftInverter', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Theft'
      AND isp.incidentsubtype = 'TheftInverter'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Theft', 'TheftRMS', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Theft'
      AND isp.incidentsubtype = 'TheftRMS'
      AND isp.systemfunctional IS NULL
);

INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in', 'Theft', 'TheftEarthingConnection', NULL, 'HIGH'
WHERE NOT EXISTS (
    SELECT 1
    FROM im_services_priority isp
    WHERE isp.tenantid = 'in'
      AND isp.incidenttype = 'Theft'
      AND isp.incidentsubtype = 'TheftEarthingConnection'
      AND isp.systemfunctional IS NULL
);

