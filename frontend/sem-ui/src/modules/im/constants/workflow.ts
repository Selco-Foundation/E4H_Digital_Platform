export const LIVELIHOOD_INCIDENT_BUSINESS_SERVICE = "LivelihoodIncident";

/**
 * Real SEM workflow status codes (confirmed against DIGIT-UI's own
 * `orderedStatuses`/status literals in im/components/inbox/Filter.js and
 * ComplaintDetails.js) — these replace the earlier Livelihood-specific,
 * underscored placeholders that never matched any real backend status.
 */
export const APPLICATION_STATUS = {
  PENDINGFORASSIGNMENT: "PENDINGFORASSIGNMENT",
  PENDINGRESOLUTION: "PENDINGRESOLUTION",
  OUT_OF_SCOPE: "OUT_OF_SCOPE",
  PENDING_ASSIGNMENT_OUT_OF_WARRANTY: "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
  RESOLVED: "RESOLVED",
  CLOSEDAFTERRESOLUTION: "CLOSEDAFTERRESOLUTION",
  REJECTED: "REJECTED",
  CLOSEDAFTERREJECTION: "CLOSEDAFTERREJECTION",
} as const;

/** Sentinel stored in InboxRow.sla for an overdue ticket; translated at render time. */
export const SLA_OVERDUE_MARKER = "OVERDUE";

/**
 * Closed/terminal statuses for SLA display purposes — confirmed against SEM's actual
 * im-services workflow config. Deliberately literal (not derived from APPLICATION_STATUS
 * above, which still holds unconfirmed Livelihood-specific status codes) so this doesn't
 * silently inherit that mismatch. APPLICATION_STATUS / RESOLVED_APPLICATION_STATUSES /
 * TERMINAL_APPLICATION_STATUSES / OPEN_DUPLICATE_APPLICATION_STATUSES below are still on
 * the old Livelihood codes and need the same correction before the KPI "resolved" count,
 * isClosedTicket, and duplicate-ticket detection can be trusted against SEM's real data.
 */
export const BLANK_SLA_STATUSES = [
  "RESOLVED",
  "CLOSEDAFTERRESOLUTION",
  "REJECTED",
  "CLOSEDAFTERREJECTION",
] as const;

export const RESOLVED_APPLICATION_STATUSES = [
  APPLICATION_STATUS.RESOLVED,
  APPLICATION_STATUS.CLOSEDAFTERRESOLUTION,
] as const;

/**
 * DIGIT-UI's own Take Action visibility check
 * (`applicationStatus !== "CLOSEDAFTERRESOLUTION"`) only ever tests this one
 * literal — matched exactly here rather than a broader terminal-status set.
 */
export const TERMINAL_APPLICATION_STATUSES = [
  APPLICATION_STATUS.CLOSEDAFTERRESOLUTION,
] as const;

export const OPEN_DUPLICATE_APPLICATION_STATUSES = [
  APPLICATION_STATUS.PENDINGFORASSIGNMENT,
  APPLICATION_STATUS.PENDINGRESOLUTION,
  APPLICATION_STATUS.OUT_OF_SCOPE,
  APPLICATION_STATUS.PENDING_ASSIGNMENT_OUT_OF_WARRANTY,
  APPLICATION_STATUS.RESOLVED,
].join(",");

/**
 * Maps a pending application status to the single role code allowed to see an
 * unassigned ticket's SLA — confirmed against SEM's actual im-services workflow config.
 */
export const ROLE_STATUS_MAPPING: Record<string, string> = {
  PENDINGFORASSIGNMENT: "COMPLAINT_ASSESSOR",
  PENDINGFORASSIGNMENT_THEFT: "COMPLAINT_ASSESSOR",
  PENDINGFORASSIGNMENT_RMS_DEVICE: "COMPLAINT_ASSESSOR",
  RMS_DEVICE_PENDING_TECH_POC: "COMPLAINT_FACILITATOR_2",
  PENDING_ASSIGNMENT_OUT_OF_WARRANTY: "COMPLAINT_FACILITATOR_1",
  OUT_OF_WARRANTY_PENDING_TECH_POC: "COMPLAINT_FACILITATOR_2",
  OUT_OF_WARRANTY_PENDING_TECH_POC_ROUND_2: "COMPLAINT_FACILITATOR_2",
  OUT_OF_SCOPE: "COMPLAINT_FACILITATOR_1",
  PENDING_ASSIGNMENT_SPARE_PART_NEEDED: "COMPLAINT_FACILITATOR_2",
};
