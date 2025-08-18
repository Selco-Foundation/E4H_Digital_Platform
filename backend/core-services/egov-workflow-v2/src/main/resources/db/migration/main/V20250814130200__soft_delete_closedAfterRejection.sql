UPDATE eg_wf_processinstance_v2 t
SET isactive = false,
    lastmodifiedby = 'migration_soft_delete_closed_after_rejection',
    lastmodifiedtime = (extract(epoch FROM now()) * 1000)::bigint
WHERE t.action = 'CLOSE'
  AND t.isactive = true
  AND EXISTS (
    SELECT 1
    FROM eg_wf_processinstance_v2 r
    WHERE r.businessid = t.businessid
      AND r.tenantid = t.tenantid
      AND r.action = 'REJECT'
      AND r.isactive = true
  );
