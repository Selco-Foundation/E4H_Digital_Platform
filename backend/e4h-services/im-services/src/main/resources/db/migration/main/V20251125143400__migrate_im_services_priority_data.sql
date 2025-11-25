INSERT INTO im_services_priority (tenantid, incidenttype, incidentsubtype, systemfunctional, priority)
SELECT 'in' AS tenantid,
       incidenttype,
       incidentsubtype,
       systemfunctional,
       priority
FROM im_services_priority src
WHERE tenantid = 'pg'
  AND NOT EXISTS (
      SELECT 1
      FROM im_services_priority t2
      WHERE t2.tenantid = 'in'
        AND t2.incidenttype IS NOT DISTINCT FROM src.incidenttype
        AND t2.incidentsubtype IS NOT DISTINCT FROM src.incidentsubtype
        AND t2.systemfunctional IS NOT DISTINCT FROM src.systemfunctional
  );
