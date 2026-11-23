/**
 * The role this module gates on - see Module.js.
 *
 * The module's data scope is this role's jurisdictions, so both the boundary pickers and the
 * facility list resolve against it. Kept here rather than in either hook so the two cannot drift.
 */
export const MODULE_ROLE = "FACILITY_ADMIN";
