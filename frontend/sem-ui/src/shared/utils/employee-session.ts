import { getConfig } from "../config/global-config";
import { searchHrmsEmployee } from "../api/hrms";
import type { AuthUser } from "../stores/auth-store";
import { buildJurisdictionBoundaries } from "./boundary-util";

export function filterRolesForEmployeeTenant(user: AuthUser, employeeTenantId: string): AuthUser {
  const roles = user.roles?.filter((role) => role.tenantId === employeeTenantId) ?? [];

  return {
    ...user,
    roles,
  };
}

export function assertEmployeeRolesAllowed(user: AuthUser): void {
  const invalidRoles = getConfig("INVALIDROLES");
  const blockedRoles = Array.isArray(invalidRoles) ? invalidRoles : [];

  if (
    blockedRoles.length > 0 &&
    user.roles?.some((role) => role.code && blockedRoles.includes(role.code))
  ) {
    throw new Error("ES_ERROR_USER_NOT_PERMITTED");
  }
}

export async function hydrateEmployeeJurisdictions(
  user: AuthUser,
  accessToken: string,
) {
  const employeeCode = user.userName;
  if (!employeeCode) {
    throw new Error("Could not find employee username");
  }

  const hrmsUser = await searchHrmsEmployee(employeeCode, accessToken, user);
  if (!hrmsUser) {
    throw new Error("Could not find HRMS employee");
  }

  if (!hrmsUser.jurisdictions?.length) {
    throw new Error("Could not find HRMS employee jurisdictions");
  }

  const boundaries = buildJurisdictionBoundaries(hrmsUser.jurisdictions);

  return {
    hrmsUser,
    boundaries,
  };
}
