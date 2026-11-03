/**
 * Role scoping on jurisdictions.
 *
 * The form offers OrgRoleGroups; eg_hrms_jurisdiction_role stores one row per role *code*.
 * Everything here is the translation between the two, kept out of the component so the
 * expansion rules are in one place.
 */

/** A jurisdiction with no roles applies to every role the user holds. */
export const appliesToAllRoles = (assignment) => !assignment?.roles?.length;

/**
 * The role codes a set of groups grants, as a union.
 *
 * Union, never subtraction: two groups can share a code, so dropping one group must not
 * strip a code the other still grants.
 */
export const expandGroupsToRoleCodes = (groups) => {
  const codes = new Set();
  (groups || []).forEach((group) => {
    (group?.roleCodes || []).forEach((code) => code && codes.add(code));
  });
  return [...codes];
};

/**
 * The groups that a stored jurisdiction's role codes correspond to.
 *
 * A group matches when every code it grants is present, which is the same test the user-level
 * roles field uses to turn stored codes back into group selections.
 */
export const collapseRoleCodesToGroups = (roleCodes, availableGroups) => {
  const codes = new Set((roleCodes || []).map((role) => role?.code || role).filter(Boolean));
  if (!codes.size) return [];
  return (availableGroups || []).filter(
    (group) => group?.roleCodes?.length && group.roleCodes.every((code) => codes.has(code))
  );
};

/**
 * Drop groups the user no longer holds from one assignment's scoping.
 *
 * Returns the retained groups, or null when the assignment was never scoped in the first
 * place — an unscoped assignment is "all roles", a live rule, so there is nothing to prune.
 */
export const pruneAssignmentGroups = (assignment, heldGroups) => {
  if (appliesToAllRoles(assignment)) return null;
  const heldCodes = new Set((heldGroups || []).map((group) => group?.code).filter(Boolean));
  return assignment.roles.filter((group) => heldCodes.has(group?.code));
};

/** `State SPOC + Tech POC` — what an `All roles` chip actually resolves to right now. */
export const describeAllRoles = (heldGroups) =>
  (heldGroups || []).map((group) => group?.name).filter(Boolean).join(" + ");
