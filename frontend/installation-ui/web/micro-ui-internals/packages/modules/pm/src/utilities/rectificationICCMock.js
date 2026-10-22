// TEMPORARY — mocks the rectification dimension on /field-planner/v1/field-plans/facility/system_type_capacity/_search
// until field-planner returns `isRectification` per (systemType, capacity). Delete this file and its call in
// services/FieldPlan.js once the backend is live.

export const MOCK_RECTIFICATION_ICC_ROWS = true;

// Every combination stays a regular row, and also gets a rectification twin so step 3 shows both kinds.
export const applyRectificationCapacityMock = (systemTypeCapacities = []) => {
  if (!MOCK_RECTIFICATION_ICC_ROWS || !Array.isArray(systemTypeCapacities)) {
    return systemTypeCapacities;
  }

  // The backend already sends the dimension: use it as-is.
  if (systemTypeCapacities.some((item) => item && "isRectification" in item)) {
    return systemTypeCapacities;
  }

  return systemTypeCapacities.flatMap((item) => [
    { ...item, isRectification: false },
    { ...item, isRectification: true },
  ]);
};
