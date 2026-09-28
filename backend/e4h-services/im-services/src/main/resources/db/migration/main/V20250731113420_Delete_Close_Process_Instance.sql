DELETE FROM eg_wf_processinstance_v2 WHERE action = 'CLOSE' and businessid in (select businessid from eg_wf_processinstance_v2 where action='REJECT') ORDER BY businessid, lastmodifiedtime DESC;
