// Shared by CreateFieldPlan (step 3 gate + bulk upload) and ICCPrepopulationConfiguration (row building),
// so both agree on what makes two ICC rows the same row.

const TRUTHY_VALUES = ["true", "yes", "1"];

// Same values ingestion/field-planner treat as a rectification site (Excel column, rectificationSite flags); anything else is regular.
export const isRectificationValue = (value) => (
  value === true || TRUTHY_VALUES.includes(value?.toString?.().trim().toLowerCase())
);

// The same (systemType, capacity) can hold one regular and one rectification template.
export const withRectificationKey = (baseKey, isRectification) => (
  baseKey ? `${baseKey}-${isRectification ? "rectification" : "regular"}` : ""
);

// Rectification pre-population is optional; regular rows still need a template file.
export const isICCRowComplete = (row = {}) => Boolean(
  row.systemType && row.totalSystemCapacity && (row.isRectification || row.file)
);
