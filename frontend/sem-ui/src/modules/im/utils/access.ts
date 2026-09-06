export const IM_ROLES = [
  "COMPLAINT_RESOLVER",
  "LIVELIHOOD_POC",
  "COMPLAINANT",
  "LIVELIHOOD_VENDOR",
  "VIEWER",
] as const;

export function hasRole(
  roles: Array<{ code?: string }> | undefined,
  code: string,
): boolean {
  return roles?.some((role) => role.code === code) ?? false;
}

export function hasImAccess(roles: Array<{ code?: string }> | undefined): boolean {
  if (!roles?.length) {
    return false;
  }
  return roles.some(
    (role) => role.code && IM_ROLES.includes(role.code as (typeof IM_ROLES)[number]),
  );
}

export function isEndUser(roles: Array<{ code?: string }> | undefined): boolean {
  return (
    roles?.every((role) => role.code === "EMPLOYEE" || role.code === "COMPLAINANT") ?? false
  );
}

const INCIDENT_CREATE_ROLES = ["COMPLAINANT", "LIVELIHOOD_POC"] as const;

export function canCreateIncident(roles: Array<{ code?: string }> | undefined): boolean {
  return INCIDENT_CREATE_ROLES.some((role) => hasRole(roles, role));
}

export function isAssigneeScopedUser(
  roles: Array<{ code?: string }> | undefined,
): boolean {
  return hasRole(roles, "LIVELIHOOD_VENDOR") || hasRole(roles, "COMPLAINT_RESOLVER");
}
