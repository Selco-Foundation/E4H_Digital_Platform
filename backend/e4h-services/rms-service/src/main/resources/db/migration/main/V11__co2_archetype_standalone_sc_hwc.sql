-- Align archetype_lookup to Catalyst Model v2 standalone SC / HWC only.
-- Do NOT use Excel SC/HWC combined rows. Registry maps:
--   SC  -> Sub Center
--   HWC -> Health and Wellness Center
-- V7 seeded SC/HWC values first; ON CONFLICT dropped the standalone rows.

-- Assam: SC A9, HWC A9 (was A7 from SC/HWC)
UPDATE archetype_lookup SET archetype = 'A9'
WHERE tenant_id = 'in' AND state = 'India_Assam' AND facility_type = 'Sub Center ';
UPDATE archetype_lookup SET archetype = 'A9'
WHERE tenant_id = 'in' AND state = 'India_Assam' AND facility_type = 'Health and Wellness Center';

-- Manipur: HWC A9 (SC already A8); was A8 from SC/HWC for HWC
UPDATE archetype_lookup SET archetype = 'A9'
WHERE tenant_id = 'in' AND state = 'India_Manipur' AND facility_type = 'Health and Wellness Center';

-- Meghalaya: SC A7, HWC A7 (was A6 from SC/HWC)
UPDATE archetype_lookup SET archetype = 'A7'
WHERE tenant_id = 'in' AND state = 'India_Meghalaya' AND facility_type = 'Sub Center ';
UPDATE archetype_lookup SET archetype = 'A7'
WHERE tenant_id = 'in' AND state = 'India_Meghalaya' AND facility_type = 'Health and Wellness Center';

-- Nagaland: SC A9, HWC A9 (was A7 from SC/HWC)
UPDATE archetype_lookup SET archetype = 'A9'
WHERE tenant_id = 'in' AND state = 'India_Nagaland' AND facility_type = 'Sub Center ';
UPDATE archetype_lookup SET archetype = 'A9'
WHERE tenant_id = 'in' AND state = 'India_Nagaland' AND facility_type = 'Health and Wellness Center';

-- Mizoram: SC A10, HWC A10 (was A8 from SC/HWC)
UPDATE archetype_lookup SET archetype = 'A10'
WHERE tenant_id = 'in' AND state = 'India_Mizoram' AND facility_type = 'Sub Center ';
UPDATE archetype_lookup SET archetype = 'A10'
WHERE tenant_id = 'in' AND state = 'India_Mizoram' AND facility_type = 'Health and Wellness Center';

-- Odisha: SC A7, HWC A7 (was A6 from SC/HWC)
UPDATE archetype_lookup SET archetype = 'A7'
WHERE tenant_id = 'in' AND state = 'India_Odisha' AND facility_type = 'Sub Center ';
UPDATE archetype_lookup SET archetype = 'A7'
WHERE tenant_id = 'in' AND state = 'India_Odisha' AND facility_type = 'Health and Wellness Center';

-- Already aligned (no UPDATE): Arunachal Pradesh, Karnataka, Sikkim
-- (standalone SC/HWC same as former SC/HWC values: A10 / A5 / A10)
