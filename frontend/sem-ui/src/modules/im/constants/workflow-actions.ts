/**
 * Real DIGIT-UI im-module workflow action codes (confirmed against
 * ComplaintDetails.js / ComplaintDetailsModal — these are the exact strings
 * the workflow business-service config emits in `nextActions`). The earlier
 * version of this file used renamed placeholders (DECLINE, DECLINE_POC,
 * OUT_OF_SCOPE, ASSIGN_VENDOR, REVISE_QUOTATION) that never match a real
 * `nextActions` entry, so Take Action silently showed nothing for most
 * tickets.
 */
export const SUPPORTED_WORKFLOW_ACTIONS = [
  "ASSIGN",
  "REASSIGN",
  "REJECT",
  "REOPEN",
  "REOPEN_RMS",
  "SENDBACK",
  "MARK_OUT_OF_SCOPE",
  "OUT_OF_WARRANTY",
  "SUBMIT",
  "SPARE_PART_NEEDED",
  "RESOLVE",
  "STATUS_UPDATE",
  "APPROVE",
  "REVISE",
  "CLOSE",
] as const;

export type SupportedWorkflowAction = (typeof SUPPORTED_WORKFLOW_ACTIONS)[number];

export const SUPPORTED_WORKFLOW_ACTION_SET = new Set<string>(SUPPORTED_WORKFLOW_ACTIONS);

/** MDMS `Incident` masters holding each action's reason dropdown options. */
export type ReasonMaster = "OutOfScopeReasons" | "RejectReasons" | "SendBackReasons";

export interface WorkflowActionConfig {
  comment: "required" | "optional";
  documents: "required" | "optional" | "none";
  reasonMaster?: ReasonMaster;
  /** REOPEN/REOPEN_RMS use a fixed 5-option reason menu, not an MDMS master. */
  fixedReopenReasons?: boolean;
  /** ASSIGN/REASSIGN require picking an employee to assign the ticket to. */
  needsAssignee?: boolean;
}

export const WORKFLOW_ACTION_CONFIG: Record<SupportedWorkflowAction, WorkflowActionConfig> = {
  ASSIGN: { comment: "optional", documents: "optional", needsAssignee: true },
  REASSIGN: { comment: "optional", documents: "optional", needsAssignee: true },
  REJECT: { comment: "required", documents: "optional", reasonMaster: "RejectReasons" },
  REOPEN: { comment: "required", documents: "optional", fixedReopenReasons: true },
  REOPEN_RMS: { comment: "required", documents: "optional", fixedReopenReasons: true },
  SENDBACK: { comment: "required", documents: "none", reasonMaster: "SendBackReasons" },
  MARK_OUT_OF_SCOPE: {
    comment: "required",
    documents: "optional",
    reasonMaster: "OutOfScopeReasons",
  },
  OUT_OF_WARRANTY: { comment: "optional", documents: "required" },
  SUBMIT: { comment: "optional", documents: "required" },
  SPARE_PART_NEEDED: { comment: "required", documents: "required" },
  RESOLVE: { comment: "required", documents: "optional" },
  STATUS_UPDATE: { comment: "required", documents: "required" },
  APPROVE: { comment: "optional", documents: "none" },
  REVISE: { comment: "required", documents: "optional" },
  CLOSE: { comment: "optional", documents: "none" },
};

/** DIGIT-UI's fixed CS_REOPEN_OPTION_ONE..FIVE reopen-reason menu. */
export const REOPEN_REASON_OPTIONS = [
  "CS_REOPEN_OPTION_ONE",
  "CS_REOPEN_OPTION_TWO",
  "CS_REOPEN_OPTION_THREE",
  "CS_REOPEN_OPTION_FOUR",
  "CS_REOPEN_OPTION_FIVE",
] as const;

export function isSupportedWorkflowAction(
  action: string,
): action is SupportedWorkflowAction {
  return SUPPORTED_WORKFLOW_ACTION_SET.has(action);
}

export function getWorkflowActionConfig(action: string): WorkflowActionConfig | null {
  return isSupportedWorkflowAction(action) ? WORKFLOW_ACTION_CONFIG[action] : null;
}

export function isQuotationRequiredAction(action: string): boolean {
  return action === "OUT_OF_WARRANTY" || action === "SUBMIT";
}
