import { ORG_ADMIN, ORG_PLATFORM_ADMIN, VENDOR_ADMIN } from "../constants/Roles";

/** what an ORG_ADMIN may see and do in the org user list, plus who may reset a password.
 *
 * Only NEW rules live here. Existing VENDOR_ADMIN / ORG_PLATFORM_ADMIN behaviour is unchanged:
 *
 * Roles are matched on CODE, never on name (see constants/Roles.js for why).
 *
 * The API enforces every one of these rules itself. Hiding things here is so
 * nobody is offered an action that will be refused - it is not the security.
 */

/** Role codes from a roles array ([{ code, name, ... }]). Tolerates null / missing entries. */
const getRoleCodes = (roles) => (Array.isArray(roles) ? roles : []).map((role) => role?.code).filter(Boolean);

const hasRole = (roles, code) => getRoleCodes(roles).includes(code);

/** ORG_PLATFORM_ADMIN: manages every organisation (unscoped by design). */
export const isPlatformAdmin = (roles) => hasRole(roles, ORG_PLATFORM_ADMIN);

/**
 * An ORG_ADMIN whose access is actually limited to one org. A user who ALSO holds
 * ORG_PLATFORM_ADMIN is unscoped - the wider role wins, as it does in the API (MH2-05).
 */
export const isScopedOrgAdmin = (roles) => hasRole(roles, ORG_ADMIN) && !isPlatformAdmin(roles);

/**
 * An admin of exactly ONE organisation: VENDOR_ADMIN (a Vendor Org) or ORG_ADMIN (a Platform Org).
 * They get no org list - the ORG card opens their own org and the "Organizations" crumb is dropped.
 *
 * The one check behind Module.js, ORGCard.js and pages/employee/index.js, which used to compute
 * `isVendorAdminUser` separately - three copies drifting is how one ends up checking only one role.
 * VENDOR_ADMIN behaves exactly as before; ORG_ADMIN counts only while it is scoped (see above).
 */
export const isSingleOrgAdmin = (roles) => hasRole(roles, VENDOR_ADMIN) || isScopedOrgAdmin(roles);

/**
 * Role groups to offer on the user form .
 * An ORG_ADMIN must not be offered ORG_PLATFORM_ADMIN (the API rejects it anyway). ORG_ADMIN itself
 * stays in the list: an org admin may create peers. Everyone else gets the list unchanged.
 */
export const getAssignableRoleGroups = (roleGroups, callerRoles) => {
  const groups = Array.isArray(roleGroups) ? roleGroups : [];
  if (!isScopedOrgAdmin(callerRoles)) return groups;
  return groups.filter((group) => !(group?.roleCodes || []).includes(ORG_PLATFORM_ADMIN));
};

/**
 * Whether to show Edit / Delete on a row of the org user list.
 * Unchanged for everyone except an ORG_ADMIN looking at a Platform Admin: ORG_PLATFORM_ADMIN is not
 * in their role list, so saving the edit form would silently strip that role from the user.
 */
export const canManageOrgUser = (callerRoles, targetUser) =>
  !(isScopedOrgAdmin(callerRoles) && isPlatformAdmin(targetUser?.roles));

/**
 * Whether to offer "Reset password" on a row :
 *   ORG_PLATFORM_ADMIN -> anyone
 *   ORG_ADMIN          -> their own org's users, never an ORG_PLATFORM_ADMIN
 *   VENDOR_ADMIN       -> their own org's users
 * "Their own org" is already true of every row: the user list is fetched for one org, and the API
 * only returns an admin's own org.
 * Never on the admin's own row - changing your own password must ask for your current one, which
 * an admin reset deliberately does not.
 */
export const canResetOrgUserPassword = (callerInfo, targetUser) => {
  if (!targetUser?.uuid) return false; // nothing to address the reset to
  if (callerInfo?.uuid && targetUser.uuid === callerInfo.uuid) return false;

  const callerRoles = callerInfo?.roles;
  if (isPlatformAdmin(callerRoles)) return true;
  if (isScopedOrgAdmin(callerRoles)) return !isPlatformAdmin(targetUser?.roles);
  return hasRole(callerRoles, VENDOR_ADMIN);
};
