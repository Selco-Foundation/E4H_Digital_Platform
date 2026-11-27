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
 * Role objects for the codes a set of groups grants, ready for the payload.
 *
 * The two masters divide like this: `OrgRoleGroups` is the dropdown's vocabulary - one entry
 * per selectable thing, with `roleCodes` naming the real roles it grants - and `OrgRoles`
 * holds the role objects that go on the wire. **The group's `roleCodes` are authoritative.**
 *
 * So every code a group names is emitted, whether or not `OrgRoles` describes it. Filtering
 * the codes through `OrgRoles` instead drops any code that master is missing: today that is
 * `COMPLAINANT`, named by the `CRM` group with no `OrgRoles` entry, so picking CRM used to
 * send `COMPLAINT_ASSESSOR` alone. That also made the chip vanish on reload, because
 * collapsing back to a group - like the users table - requires *every* code to be present.
 *
 * `name` is only set when `OrgRoles` supplies one. A code's display name is never guessed:
 * nothing renders these directly, the UI shows the group's name.
 */
export const buildRolesFromGroups = (groups, orgRoles) => {
  const byCode = new Map((orgRoles || []).filter((role) => role?.code).map((role) => [role.code, role]));
  return expandGroupsToRoleCodes(groups).map((code) => byCode.get(code) || { code });
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

/*
 * How each role relates to jurisdiction comes from its `Organisation.OrgRoles` record:
 *
 * - `jurisdictionScoped: false` - jurisdiction doesn't decide what the role sees. Reviewers and
 *   field staff are limited by assignment, and the admins see every user. Such a role isn't
 *   offered under "Applies to" and isn't level-checked.
 * - `allowedJurisdictionLevels: [...]` - the boundary types the role may be granted at, for
 *   example CRM above facility level, and the vendor at facility level only.
 *
 * A missing field means no restriction, so a record without them behaves as before.
 */

const orgRoleFor = (code, orgRoles) => (orgRoles || []).find((role) => role?.code === code);

const isScopedCode = (code, orgRoles) => orgRoleFor(code, orgRoles)?.jurisdictionScoped !== false;

/**
 * Whether a group belongs in the "Applies to" options: true when any role it grants is scoped by
 * jurisdiction. A group made only of unscoped roles has nothing to narrow.
 */
export const isScopedGroup = (group, orgRoles) => (group?.roleCodes || []).some((code) => isScopedCode(code, orgRoles));

/**
 * The roles a jurisdiction applies to that may not be granted at its level, as OrgRoles entries
 * (or `{ code }` where OrgRoles has none).
 *
 * A jurisdiction with no roles applies to every role the user holds, so it is checked against
 * `userRoles` instead.
 */
export const rolesNotAllowedAtLevel = (jurisdiction, userRoles, orgRoles) => {
  const applied = jurisdiction?.roles?.length ? jurisdiction.roles : userRoles;
  const codes = [...new Set((applied || []).map((role) => role?.code || role).filter(Boolean))];
  return codes
    .map((code) => orgRoleFor(code, orgRoles) || { code })
    .filter((role) => role.jurisdictionScoped !== false)
    .filter((role) => Array.isArray(role.allowedJurisdictionLevels) && !role.allowedJurisdictionLevels.includes(jurisdiction?.boundaryType));
};

/**
 * True when every role the user holds is outside jurisdiction scoping, so no jurisdiction
 * decides anything for them. An empty list is false: no roles is not "all unscoped".
 */
export const allRolesUnscoped = (userRoles, orgRoles) => {
  const codes = (userRoles || []).map((role) => role?.code || role).filter(Boolean);
  return codes.length > 0 && codes.every((code) => !isScopedCode(code, orgRoles));
};

/**
 * The jurisdiction-scoped roles the user holds that no active jurisdiction covers, as OrgRoles
 * entries (or `{ code }` where OrgRoles has none).
 *
 * A jurisdiction covers a role when it applies to all roles or names it. Without this a scoped
 * role could be saved with no grant of its own, riding on another role's grant.
 */
export const scopedRolesWithoutJurisdiction = (jurisdictions, userRoles, orgRoles) => {
  const active = (jurisdictions || []).filter((jurisdiction) => jurisdiction?.isActive);
  const codes = [...new Set((userRoles || []).map((role) => role?.code || role).filter(Boolean))];
  return codes
    .filter((code) => isScopedCode(code, orgRoles))
    .filter((code) => !active.some((jurisdiction) =>
      appliesToAllRoles(jurisdiction) || jurisdiction.roles.some((role) => (role?.code || role) === code)))
    .map((code) => orgRoleFor(code, orgRoles) || { code });
};
