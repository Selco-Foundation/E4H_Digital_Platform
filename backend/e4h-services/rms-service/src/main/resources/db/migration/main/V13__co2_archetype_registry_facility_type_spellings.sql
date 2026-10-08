-- archetype_lookup rows keyed by the facility_type strings the health facility registry
-- actually stores. V7/V9/V11/V12 seeded the Catalyst Model spellings ("Primary Health
-- Center"), but the registry uses British spellings ("Primary Health Centre") and, for two
-- types, a combined label. The key built in Co2ReferenceBundle.resolveArchetype is
-- state|facility_type with only a trim, so every mismatched row resolves to no archetype and
-- CarbonEmissionCalculator estimates 0 kWh -> 0 tonnes for every projected month.
--
-- This adds the registry spelling alongside the existing row rather than renaming it, so the
-- Catalyst Model spelling stays available for reference and the change is additive.
--
-- Values are copied from the existing rows instead of being restated, so each new row carries
-- whatever archetype that (state, type) resolves to today, including V11/V12 corrections.
--
-- Two registry labels cover more than one Catalyst Model type. The priority column picks a
-- winner; it only matters where the sources disagree:
--   'Sub Centre/Health & Wellness Centre' -> HWC wins over SC, falling back to SC for the
--      states that have no HWC row (Gujarat, Maharashtra). Differs only for Manipur
--      (HWC A9 vs SC A8). The registry has no standalone SC or HWC type, so the V11 split
--      cannot be represented.
--   'Ayurvedic Centre/Hospital' -> Ayurvedic Hospital wins over Ayurvedic Center, falling
--      back to Ayurvedic Center where there is no Hospital row (Karnataka). Differs only for
--      Odisha (A8 vs A1).
--
-- ON CONFLICT DO NOTHING: none of these rows exist today, so this is a pure backfill and any
-- manual correction made later is left alone.

WITH registry_alias (registry_type, catalyst_type, priority) AS (
    VALUES
        -- Straight Center -> Centre renames.
        ('Primary Health Centre',              'Primary Health Center',              1),
        ('Mini Primary Health Centre',         'Mini Primary Health Center',         1),
        ('Urban Primary Health Centre',        'Urban Primary Health Center',        1),
        ('Block Primary Health Centre',        'Block Primary Health Center',        1),
        ('New Primary Health Centre',          'New Primary Health Center',          1),
        ('Riverine Primary Health Centre',     'Riverine Primary Health Center',     1),
        ('Community Health Centre',            'Community Health Center',            1),
        ('Subsidiary Health Centre',           'Subsidiary Health Center',           1),
        ('Urban Health Centre',                'Urban Health Center',                1),
        ('Tribal Health Centre',               'Tribal Health Center',               1),
        ('TB Centre',                          'TB Center',                          1),
        ('Ayush Health & Wellness Centre',     'Ayush Health & Wellness Center',     1),
        ('Urban Health and Wellness Centre',   'Urban Health and Wellness Center',   1),

        -- Combined registry labels; lowest priority wins.
        ('Sub Centre/Health & Wellness Centre', 'Health and Wellness Center',        1),
        ('Sub Centre/Health & Wellness Centre', 'Sub Center ',                       2),
        ('Sub Centre/Health & Wellness Centre', 'Sub Center',                        3),
        ('Ayurvedic Centre/Hospital',           'Ayurvedic Hospital',                1),
        ('Ayurvedic Centre/Hospital',           'Ayurvedic Center',                  2),

        -- Registry-only label with no Catalyst Model equivalent of its own.
        ('State Dispensary',                    'Dispensary',                        1)
),
resolved AS (
    SELECT DISTINCT ON (l.tenant_id, l.state, a.registry_type)
           l.tenant_id,
           l.state,
           a.registry_type,
           l.archetype
    FROM archetype_lookup l
    JOIN registry_alias a ON a.catalyst_type = l.facility_type
    ORDER BY l.tenant_id, l.state, a.registry_type, a.priority
)
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype)
SELECT md5(tenant_id || '|' || state || '|' || registry_type)::uuid::text,
       tenant_id,
       state,
       registry_type,
       archetype
FROM resolved
ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
