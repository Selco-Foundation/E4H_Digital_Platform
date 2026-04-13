-- Indexes to optimize facility lookups by boundary for both single and bulk search.
-- Queries to support:
--   1) buildWhereClause(FacilitySearchRequest):  ... WHERE tenant_id = ? AND boundary_code ILIKE ?
--   2) buildBulkWhereClause(FacilityBulkSearchCriteria): ... WHERE fac.tenant_id IN (...) AND boundary_code IN (...)
--
-- Existing unique index:
--   uniq_facility_name_boundary ON facility (tenant_id, facility_name, boundary_code)
-- is used for name+boundary uniqueness, not for the common boundary-only search filters.

-- For bulk search by many boundary codes scoped to tenant(s).
CREATE INDEX IF NOT EXISTS idx_facility_tenant_boundary_code
    ON facility (tenant_id, boundary_code);

-- Optional index to help when searching only by boundary_code without a tenant predicate,
-- or when the planner prefers a direct boundary_code index for IN (...) filters.
CREATE INDEX IF NOT EXISTS idx_facility_boundary_code
    ON facility (boundary_code);

