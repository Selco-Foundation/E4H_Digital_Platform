-- Community Health Center was missing from archetype_lookup (V7).
-- Archetypes aligned to Catalyst Model v2 (missing-arch.xlsx).

INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000052', 'in', 'India_Assam', 'Community Health Center', 'A4') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000054', 'in', 'India_ArunachalPradesh', 'Community Health Center', 'A6') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000056', 'in', 'India_Karnataka', 'Community Health Center', 'A4') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000058', 'in', 'India_Manipur', 'Community Health Center', 'A1') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-00000000005a', 'in', 'India_Meghalaya', 'Community Health Center', 'A1') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-00000000005c', 'in', 'India_Nagaland', 'Community Health Center', 'A3') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-00000000005e', 'in', 'India_Sikkim', 'Community Health Center', 'A6') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000060', 'in', 'India_Mizoram', 'Community Health Center', 'A3') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
INSERT INTO archetype_lookup (id, tenant_id, state, facility_type, archetype) VALUES ('00000000-0000-4000-a000-000000000062', 'in', 'India_Odisha', 'Community Health Center', 'A2') ON CONFLICT (tenant_id, state, facility_type) DO NOTHING;
