/**
 * Which roles have work of their own in the inbox.
 *
 * This decides one thing only: whether the assigned-to-me toggle starts on. What "assigned to me"
 * *means* is the backend's business — a pooled role matches the statuses it can act on, every other
 * role matches on being the assignee — and the UI deliberately knows nothing about that.
 *
 * So if this list drifts from the backend's POOLED_ROLES, the worst case is a toggle that starts in
 * the wrong position, never a wrong result. Keep it that way: no status codes here, ever.
 *
 * A complainant-only user (HCR) is absent on purpose. Their tickets are assigned to assessors and
 * vendors, so "assigned to me" would be empty for them; they default to the whole jurisdiction, and
 * the toggle still works if they want the literal reading.
 */
export const ROLES_WITH_OWN_INBOX_WORK = [
  "COMPLAINT_FACILITATOR_1", // State SPOC
  "COMPLAINT_FACILITATOR_2", // Tech POC
  "COMPLAINT_ASSESSOR", // CRM
  "COMPLAINT_RESOLVER", // vendor
];

/** Whether the assigned-to-me view should be the default for this user. */
export const defaultsToAssignedToMe = (roles) =>
  (roles || []).some((role) => ROLES_WITH_OWN_INBOX_WORK.includes(role?.code));
