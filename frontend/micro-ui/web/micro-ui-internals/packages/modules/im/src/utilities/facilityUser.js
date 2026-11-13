// Facility POCs have the complainant role and no specialist employee roles.
export const isFacilityUser = (roles) =>
  Boolean(roles?.some((role) => role.code === "COMPLAINANT")) &&
  roles.every((role) => role.code === "EMPLOYEE" || role.code === "COMPLAINANT");
