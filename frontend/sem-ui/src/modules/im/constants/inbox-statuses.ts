export const ORDERED_INBOX_STATUSES = [
  {
    code: "PENDING_FOR_RESOLUTION",
    statuses: ["PENDING_FOR_RESOLUTION"],
  },
  {
    code: "OUT_OF_SCOPE_PENDING_POC",
    statuses: ["OUT_OF_SCOPE_PENDING_POC"],
  },
  {
    code: "OUT_OF_SCOPE_PENDING_VENDOR",
    statuses: ["OUT_OF_SCOPE_PENDING_VENDOR"],
  },
  {
    code: "OUT_OF_WARRANTY_PENDING_VENDOR",
    statuses: ["OUT_OF_WARRANTY_PENDING_VENDOR"],
  },
  { code: "RESOLVED", statuses: ["RESOLVED"] },
  { code: "CLOSED_AFTER_RESOLUTION", statuses: ["CLOSED_AFTER_RESOLUTION"] },
  { code: "CLOSED_AFTER_DECLINE", statuses: ["CLOSED_AFTER_DECLINE"] },
] as const;
