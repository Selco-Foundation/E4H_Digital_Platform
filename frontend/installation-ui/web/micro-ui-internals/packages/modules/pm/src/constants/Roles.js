/**
 * The role this module gates on - see Module.js.
 *
 * The module's data scope is this role's jurisdictions - the facility list, its filter bar and
 * the create wizards' boundary pickers all resolve against it - and it is what the app declares
 * to the backend as the role it is acting as. Kept here rather than in any one hook so they
 * cannot drift.
 */
export const MODULE_ROLE = "PROJECT_MANAGER";
