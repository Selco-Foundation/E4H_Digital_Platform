INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in' as tenantid, incidenttype, incidentsubtype, systemfunctional, priority
FROM im_services_priority
where tenantid = 'pg';