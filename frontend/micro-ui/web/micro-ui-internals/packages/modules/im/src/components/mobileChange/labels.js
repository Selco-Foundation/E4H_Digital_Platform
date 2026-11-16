import { useTranslation } from "react-i18next";

/**
 * English fallbacks for the CRM mobile-number-change page.
 * the page shows these texts instead of raw keys.
 * Texts may contain {{placeholders}}; they are filled from the `vars` passed to the label function.
 */
export const DEFAULT_LABELS = {
  // card link + page
  MOBILE_CHANGE_CRM_LINK: "Mobile Number Change Requests",
  MOBILE_CHANGE_CRM_TITLE: "Mobile Number Change Requests",
  MOBILE_CHANGE_CRM_TOTAL: "Total: {{count}}",
  MOBILE_CHANGE_CRM_BACK: "Back",
  MOBILE_CHANGE_CRM_RETRY: "Retry",
  MOBILE_CHANGE_CRM_LOAD_ERROR: "Could not load the requests. Please try again",
  MOBILE_CHANGE_CRM_NO_ACCESS: "You are not authorised to view this page",

  // status tabs
  MOBILE_CHANGE_TAB_PENDING: "Pending",
  MOBILE_CHANGE_TAB_APPROVED: "Approved",
  MOBILE_CHANGE_TAB_REJECTED: "Rejected",
  MOBILE_CHANGE_TAB_CANCELLED: "Cancelled",
  MOBILE_CHANGE_TAB_ALL: "All",

  // empty states (one per tab so the message reads naturally)
  MOBILE_CHANGE_EMPTY_PENDING: "No pending requests",
  MOBILE_CHANGE_EMPTY_APPROVED: "No approved requests",
  MOBILE_CHANGE_EMPTY_REJECTED: "No rejected requests",
  MOBILE_CHANGE_EMPTY_CANCELLED: "No cancelled requests",
  MOBILE_CHANGE_EMPTY_ALL: "No requests found",

  // table columns
  MOBILE_CHANGE_COL_RAISED_ON: "Raised On",
  MOBILE_CHANGE_COL_REQUESTED_BY: "Requested By",
  MOBILE_CHANGE_COL_FACILITY: "Facility",
  MOBILE_CHANGE_COL_CURRENT: "Current Number",
  MOBILE_CHANGE_COL_NEW: "New Number",
  MOBILE_CHANGE_COL_STATUS: "Status",
  MOBILE_CHANGE_COL_ACTION: "Action",
  MOBILE_CHANGE_COL_REMARK: "Remark",
  MOBILE_CHANGE_COL_ACTION_REMARK: "Action / Remark", // heading on the All tab, where rows can be either
  MOBILE_CHANGE_DECIDED_ON: "Decided on {{date}}",

  // status badge (reuses the tab wording)
  MOBILE_CHANGE_STATUS_PENDING: "Pending",
  MOBILE_CHANGE_STATUS_APPROVED: "Approved",
  MOBILE_CHANGE_STATUS_REJECTED: "Rejected",
  MOBILE_CHANGE_STATUS_CANCELLED: "Cancelled",

  // actions
  MOBILE_CHANGE_APPROVE: "Approve",
  MOBILE_CHANGE_REJECT: "Reject",

  // decision popup
  MOBILE_CHANGE_APPROVE_TITLE: "Approve mobile number change",
  MOBILE_CHANGE_REJECT_TITLE: "Reject mobile number change",
  MOBILE_CHANGE_APPROVE_TEXT: "The registered number of {{userName}} will change from {{current}} to {{next}}.",
  MOBILE_CHANGE_REJECT_TEXT: "The registered number of {{userName}} will stay {{current}}. The user will see your remark.",
  MOBILE_CHANGE_COMMENT_LABEL: "Comment",
  MOBILE_CHANGE_COMMENT_OPTIONAL: "Comment (optional)",
  MOBILE_CHANGE_COMMENT_PLACEHOLDER: "Enter a comment",
  MOBILE_CHANGE_COMMENT_REQUIRED: "Please enter the reason for rejecting this request",
  MOBILE_CHANGE_GO_BACK: "Go Back",
  MOBILE_CHANGE_CONFIRM_APPROVE: "Approve",
  MOBILE_CHANGE_CONFIRM_REJECT: "Reject",

  // toasts
  MOBILE_CHANGE_APPROVED_TOAST: "Request approved. The registered mobile number has been updated",
  MOBILE_CHANGE_REJECTED_TOAST: "Request rejected",

  // error messages for the CRM actions (same keys as the profile screen, so one CSV row each)
  MOBILE_CHANGE_ERR_NOT_PENDING: "This request has already been processed",
  MOBILE_CHANGE_ERR_NOT_FOUND: "Mobile number change request not found",
  MOBILE_CHANGE_ERR_NOT_AUTHORIZED: "You are not authorised to perform this action",
  MOBILE_CHANGE_ERR_HRMS_UPDATE_FAILED: "Could not update the mobile number. Please try again",
  MOBILE_CHANGE_ERR_GENERIC: "Something went wrong. Please try again",
};

const fillPlaceholders = (text, vars = {}) => String(text).replace(/{{\s*(\w+)\s*}}/g, (match, name) => (vars[name] !== undefined ? vars[name] : match));

/**
 * Translate function for this feature: `tr(key, vars)`.
 * Uses the localisation service's text when the key exists, otherwise the English default above.
 * (i18next returns the key itself for a missing key, which is how we detect "not uploaded yet".)
 */
export const useLabel = () => {
  const { t } = useTranslation();
  return (key, vars) => {
    const translated = t(key, { defaultValue: DEFAULT_LABELS[key], ...vars });
    if (translated && translated !== key) return translated;
    return fillPlaceholders(DEFAULT_LABELS[key] || key, vars);
  };
};
