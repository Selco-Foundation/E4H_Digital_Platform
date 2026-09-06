import type { AuthUser } from "@/shared";
import type { ImInboxFilters } from "../types/inbox";
import { isAssigneeScopedUser } from "../utils/access";

export function buildDefaultInboxRoleFilters(
  user: AuthUser | null | undefined,
): ImInboxFilters {
  const userUuid = user?.uuid ?? "";
  const roles = user?.roles;

  if (isAssigneeScopedUser(roles)) {
    return {
      wfFilters: { assignee: [{ code: userUuid }] },
      pgrfilters: {
        assetType: [],
        facility: [],
        state: [],
        district: [],
        block: [],
        applicationStatus: [],
      },
    };
  }

  return {
    wfFilters: { assignee: [] },
    pgrfilters: {
      assetType: [],
      facility: [],
      state: [],
      district: [],
      block: [],
      applicationStatus: [],
    },
  };
}

export function buildSummaryRoleFilters(
  user: AuthUser | null | undefined,
): Record<string, unknown> {
  const userUuid = user?.uuid ?? "";
  const roles = user?.roles;

  if (isAssigneeScopedUser(roles)) {
    return { assignee: userUuid };
  }

  return {};
}
