INSERT INTO public.activities
(
  id,
  tenant_id,
  "name",
  code,
  default_conditions,
  required_roles,
  sequence_order,
  is_active,
  created_time,
  last_modified_time,
  additional_details
)
VALUES
(
  '272e7e4a-e02f-48ac-966d-1c920d88297c',
  'in',
  'Installation',
  'INS',
  '{}'::jsonb,
  '{"roles": [{"code": "INSTALLATION_SPOC", "name": "Installation SPOC", "tenantId": "in", "description": ""}, {"code": "INSTALLATION_REVIEWER", "name": "Installation Reviewer", "tenantId": "in", "description": ""}]}'::jsonb,
  1,
  true,
  1759402867975,
  1759402867975,
  'null'::jsonb
)
ON CONFLICT (tenant_id, code) DO UPDATE
SET
  "name" = EXCLUDED."name",
  default_conditions = EXCLUDED.default_conditions,
  required_roles = EXCLUDED.required_roles,
  sequence_order = EXCLUDED.sequence_order,
  is_active = EXCLUDED.is_active,
  last_modified_time = EXCLUDED.last_modified_time,
  additional_details = EXCLUDED.additional_details;

