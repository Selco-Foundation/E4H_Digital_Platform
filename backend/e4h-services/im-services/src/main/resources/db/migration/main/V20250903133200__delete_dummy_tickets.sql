DELETE FROM public.eg_incident_v2
WHERE tenantid = 'pg.dummy'
  AND incidentid IN (
    'KA-RCH-123-0053',
    'KA-RCH-123-0054',
    'KA-RCH-123-0055',
    'KA-RCH-123-0056',
    'KA-RCH-123-0057',
    'KA-RCH-123-0058',
    'KA-RCH-123-0059',
    'KA-RCH-123-0060'
  );
