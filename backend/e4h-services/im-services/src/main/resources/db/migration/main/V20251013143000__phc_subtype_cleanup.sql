UPDATE public.eg_incident_v2
SET phcsubtype = CASE
    WHEN phcsubtype = 'SUB CENTER' THEN 'Sub Center'
    WHEN phcsubtype = 'Sub centre' THEN 'Sub Center'
    WHEN phcsubtype = 'SC' THEN 'Sub Center'
    WHEN phcsubtype = 'Primary Health Centre' THEN 'Primary Health Center'
    WHEN phcsubtype = 'TALUKA HOSPITAL' THEN 'Taluka Hospital'
    WHEN phcsubtype = 'TH' THEN 'Taluka Hospital'
    WHEN phcsubtype = 'HWC' THEN 'Health and Wellness Center'
    WHEN phcsubtype = 'UHC' THEN 'Urban Health Center'
    WHEN phcsubtype = 'SD' THEN 'State Dispensary'
    WHEN phcsubtype = 'CHC' THEN 'Community Health Center'
    WHEN phcsubtype = 'THC' THEN 'Tribal Health Center'
    WHEN phcsubtype = 'UPHC' THEN 'Urban Primary Health Center'
    WHEN phcsubtype = 'SC DELIVERY' THEN 'Sub Center Delivery'
    WHEN phcsubtype = 'TB CENTER' THEN 'TB Center'
    WHEN phcsubtype = 'DVS' THEN 'District Vaccine Storage'
    WHEN phcsubtype = 'SHC' THEN 'Subsidiary Health Center'
    WHEN phcsubtype = 'DH' THEN 'District Hospital'
    WHEN phcsubtype = 'PHC' THEN 'Primary Health Center'
    WHEN phcsubtype = 'BLOCK PHC' THEN 'Block Primary Health Center'
    WHEN phcsubtype = 'MINI PHC' THEN 'Mini Primary Health Center'
    WHEN phcsubtype = 'SDH' THEN 'Sub Divisional Hospital'
    ELSE phcsubtype
END
WHERE phcsubtype IN (
    'SD',
    'Primary Health Center',
    'HWC',
    'CHC',
    'SUB CENTER',
    'TB CENTER',
    'DVS',
    'UPHC',
    'UHC',
    'THC',
    'TH',
    'SHC',
    'DH',
    'PHC',
    'Sub centre',
    'SDH',
    'BLOCK PHC',
    'MINI PHC',
    'SC DELIVERY',
    'SC'
);
