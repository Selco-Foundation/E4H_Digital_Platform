import type { AuthUser } from "@/shared";
import {
  BLANK_SLA_STATUSES,
  ROLE_STATUS_MAPPING,
  SLA_OVERDUE_MARKER,
} from "../constants/workflow";
import type {
  InboxIncident,
  InboxItem,
  InboxRow,
  InboxSearchResponse,
  InboxStatusMapEntry,
} from "../types/inbox";

const SLA_MS_PER_DAY = 8 * 60 * 60 * 1000;

function toSlaDays(remainingMs: number): string | number {
  return remainingMs < 0 ? SLA_OVERDUE_MARKER : Math.ceil(remainingMs / SLA_MS_PER_DAY);
}

function resolveSlaValue(
  { businessObject, ProcessInstance }: InboxItem,
  incident: InboxIncident,
  currentUserUuid: string | undefined,
  currentUserRoles: string[] | undefined,
  isEndUser: boolean | undefined,
): string | number {
  if ((BLANK_SLA_STATUSES as readonly string[]).includes(incident.applicationStatus)) {
    return "-";
  }
  if (isEndUser) {
    return toSlaDays(businessObject?.totalSlaRemaining ?? 0);
  }

  const assigneeUuid = ProcessInstance?.assignes?.[0]?.uuid;
  if (assigneeUuid && currentUserUuid === assigneeUuid) {
    return toSlaDays(businessObject?.slaRemaining ?? 0);
  }

  if (!assigneeUuid) {
    const requiredRole = ROLE_STATUS_MAPPING[incident.applicationStatus];
    if (requiredRole && currentUserRoles?.includes(requiredRole)) {
      return toSlaDays(businessObject?.slaRemaining ?? 0);
    }
  }

  return "-";
}

export function combineInboxResponses(
  items: InboxItem[],
  currentUser: AuthUser | null | undefined,
): InboxRow[] {
  const currentUserUuid = currentUser?.uuid;
  const currentUserRoles = currentUser?.roles?.map((role) => role.code).filter(Boolean) as string[];
  const isEndUser = currentUserRoles?.every(
    (role) => role === "EMPLOYEE" || role === "COMPLAINANT",
  );

  return items.map((item) => {
    const { businessObject, ProcessInstance } = item;
    const incident = businessObject?.incident ?? {
      incidentId: "",
      incidentType: "",
      applicationStatus: "",
      tenantId: "",
    };
    const assignee = ProcessInstance?.assignes?.[0];
    const slaValue = resolveSlaValue(item, incident, currentUserUuid, currentUserRoles, isEndUser);

    return {
      incidentId: incident.incidentId,
      incidentType: incident.incidentType,
      incidentSubType: incident.incidentSubType || "-",
      // Facility Registry writes each facility's localization entry as
      // `Boundary_<facilityCode>` -> facility name (see FacilityService.java on
      // the backend), keyed off `incident.boundary.facilityCode` specifically —
      // NOT `incident.boundaryCode`, which is a different, unrelated field.
      assetLabel: incident.boundary?.facilityCode
        ? `Boundary_${incident.boundary.facilityCode}`
        : "-",
      status: incident.applicationStatus,
      taskOwner: assignee?.name || "-",
      sla: `${slaValue}`,
      endUser: incident.reporter?.name || "-",
      tenantId: incident.tenantId,
      potentialDuplicate:
        (currentUserRoles?.includes("COMPLAINT_ASSESSOR") && !!incident.isPotentialDuplicate) ??
        false,
    };
  });
}

export function normalizeInboxResponse(data: InboxSearchResponse) {
  return {
    total: data.totalCount ?? 0,
    items: data.items ?? [],
    statusArray: data.statusMap ?? [],
    nearingSlaCount: data.nearingSlaCount,
  };
}

export function sumStatusCounts(
  statusArray: InboxStatusMapEntry[] | undefined,
  statuses: readonly string[],
): number {
  return (statusArray ?? []).reduce(
    (sum, entry) => (statuses.includes(entry.statusid) ? sum + (entry.count ?? 0) : sum),
    0,
  );
}
