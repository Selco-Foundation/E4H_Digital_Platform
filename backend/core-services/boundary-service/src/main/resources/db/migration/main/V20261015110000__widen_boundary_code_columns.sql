-- Widen boundary codes from VARCHAR(64).
--
-- Codes are the full path through the hierarchy, so onboarding every LGD state, district and
-- block overflows 64 characters on the names alone:
--   India_DadraAndNagarHaveliAndDamanAndDiu_DadraAndNagarHaveli_DadraAndNagarHaveli   (79)
--   India_Maharashtra_ChhatrapatiSambhajinagar_ChhatrapatiSambhajinagar               (67)
--
-- It also overflows below Block. health-facility-registry builds a facility boundary as
-- '<blockBoundaryCode>_<facilityId>', so a facility code runs longer than its block - which
-- puts well over a hundred of the new blocks past 64 the moment a facility is added under one.
--
-- 256 matches the boundary code columns elsewhere in E4H (eg_incident_v2.boundarycode,
-- user_location.boundaryCode) and stays clear of the btree key limit: the widest possible
-- boundary_relationship primary key is 64 + 256 + 64 bytes, well under ~2704.
--
-- PostgreSQL treats varchar(n) -> varchar(m > n) as binary-coercible, so there is no table
-- rewrite and dependent indexes and constraints are left alone - unique_code_tenantId,
-- idx_boundary_tenantId_code and pk_boundary_relationship all stay valid. Expect a brief
-- ACCESS EXCLUSIVE lock. Confirm on a restored dump first: pg_relation_filenode('boundary')
-- should be unchanged afterwards, which proves no rewrite happened.

ALTER TABLE boundary              ALTER COLUMN code   TYPE VARCHAR(256);
ALTER TABLE boundary_relationship ALTER COLUMN code   TYPE VARCHAR(256);
ALTER TABLE boundary_relationship ALTER COLUMN parent TYPE VARCHAR(256);

-- ancestralMaterializedPath is already TEXT and needs no change.
