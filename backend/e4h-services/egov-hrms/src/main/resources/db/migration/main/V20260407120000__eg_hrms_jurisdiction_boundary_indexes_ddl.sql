-- Indexes for employee search by boundary (see EmployeeQueryBuilder.addWhereClauseJurisdiction):
--   ... AND jurisdiction.boundary IN (...) AND jurisdiction.isactive = ?
-- and for joins from eg_hrms_employee to eg_hrms_jurisdiction on employeeid.

CREATE INDEX IF NOT EXISTS idx_eg_hrms_jurisdiction_boundary_isactive
    ON eg_hrms_jurisdiction (boundary, isactive);

CREATE INDEX IF NOT EXISTS idx_eg_hrms_jurisdiction_employeeid
    ON eg_hrms_jurisdiction (employeeid);
