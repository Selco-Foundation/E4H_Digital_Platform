WITH new_rows (tenantid, incidenttype, incidentsubtype, systemfunctional, priority) AS (
    VALUES
        ('in', 'Reinstall', 'ReinstallSolarSystem', NULL, 'HIGH'),
        ('in', 'Uninstall', 'UninstallSolarSystem', NULL, 'HIGH'),
        ('in', 'Theft', 'TheftPanel', NULL, 'HIGH'),
        ('in', 'Theft', 'TheftBattery', NULL, 'HIGH'),
        ('in', 'Theft', 'TheftInverter', NULL, 'HIGH'),
        ('in', 'Theft', 'TheftRMS', NULL, 'HIGH'),
        ('in', 'Theft', 'TheftEarthingConnection', NULL, 'HIGH')
)
INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT n.tenantid, n.incidenttype, n.incidentsubtype, n.systemfunctional, n.priority
FROM new_rows n
LEFT JOIN im_services_priority isp
    ON isp.tenantid = n.tenantid
   AND isp.incidenttype IS NOT DISTINCT FROM n.incidenttype
   AND isp.incidentsubtype IS NOT DISTINCT FROM n.incidentsubtype
   AND isp.systemfunctional IS NOT DISTINCT FROM n.systemfunctional
WHERE isp.id IS NULL;
