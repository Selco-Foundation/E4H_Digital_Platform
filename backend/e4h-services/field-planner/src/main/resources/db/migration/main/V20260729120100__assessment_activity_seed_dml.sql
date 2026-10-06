-- Seed ASSESSMENT activity master for assessment plan facility rows (LLD §5.2).
-- Id is stable so services can reference it; per-tenant rows use ON CONFLICT on (tenant_id, code).

INSERT INTO activities (
    id,
    tenant_id,
    name,
    code,
    default_conditions,
    required_roles,
    sequence_order,
    is_active,
    created_time,
    last_modified_time,
    additional_details
)
VALUES (
    '00000000-0000-4000-8000-000000000001',
    'in',
    'Assessment',
    'ASSESSMENT',
    '{}'::jsonb,
    '[
        {"code": "ENUMERATOR", "name": "Remote Assessor"},
        {"code": "FIELD_POC", "name": "Field POC"}
    ]'::jsonb,
    0,
    TRUE,
    EXTRACT(EPOCH FROM NOW()) * 1000,
    EXTRACT(EPOCH FROM NOW()) * 1000,
    '{"module": "assessment"}'::jsonb
)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
    name = EXCLUDED.name,
    required_roles = EXCLUDED.required_roles,
    is_active = EXCLUDED.is_active,
    last_modified_time = EXCLUDED.last_modified_time,
    additional_details = EXCLUDED.additional_details;
