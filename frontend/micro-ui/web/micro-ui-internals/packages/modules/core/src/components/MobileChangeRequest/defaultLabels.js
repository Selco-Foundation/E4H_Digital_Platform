/**
 * English fallbacks for every label this feature uses.
 * The localisation service stays the source of truth: useLabel() uses the uploaded translation
 * when one exists and falls back to these only when the key is missing, so users never see raw
 * keys like MOBILE_CHANGE_BUTTON (e.g. before the localisation upload, or if it fails).
 */
const DEFAULT_LABELS = {
  MOBILE_CHANGE_NUMBER_REQUIRED: "Please enter the new mobile number",
  MOBILE_CHANGE_SAME_NUMBER: "New mobile number must be different from your registered mobile number",
  MOBILE_CHANGE_ERR_INVALID_MOBILE_NUMBER: "Enter a valid 10-digit mobile number starting with 6-9",
  MOBILE_CHANGE_ERR_ALREADY_PENDING: "You already have a mobile number change request pending with the CRM",
  MOBILE_CHANGE_ERR_NOT_PENDING: "This request has already been processed",
  MOBILE_CHANGE_ERR_NOT_FOUND: "Mobile number change request not found",
  MOBILE_CHANGE_ERR_NOT_AUTHORIZED: "You are not authorised to perform this action",
  MOBILE_CHANGE_ERR_NOT_A_FACILITY_USER: "Only facility users can request a mobile number change",
  MOBILE_CHANGE_ERR_FACILITY_NOT_FOUND: "No facility is linked to your account. Please contact the CRM",
  MOBILE_CHANGE_ERR_MULTIPLE_FACILITIES: "Your account is linked to more than one facility. Please contact the CRM",
  MOBILE_CHANGE_ERR_HRMS_UPDATE_FAILED: "Could not update the mobile number. Please try again",
  MOBILE_CHANGE_ERR_GENERIC: "Something went wrong. Please try again",
  MOBILE_CHANGE_BUTTON: "Change Mobile Number",
  MOBILE_CHANGE_NEW_NUMBER_LABEL: "New Mobile Number",
  MOBILE_CHANGE_PLACEHOLDER: "Enter new 10-digit mobile number",
  MOBILE_CHANGE_HINT: "Your current number stays active until a CRM approves this request.",
  MOBILE_CHANGE_SUBMIT: "Submit Request",
  MOBILE_CHANGE_CLOSE_FORM: "Cancel",
  MOBILE_CHANGE_REQUEST_LABEL: "Mobile number change request",
  MOBILE_CHANGE_STATUS_PENDING: "Pending",
  MOBILE_CHANGE_STATUS_REJECTED: "Rejected",
  MOBILE_CHANGE_STATUS_APPROVED: "Approved",
  MOBILE_CHANGE_REQUESTED_NUMBER: "Requested number",
  MOBILE_CHANGE_RAISED_ON: "Raised on",
  MOBILE_CHANGE_PENDING_INFO: "Awaiting CRM approval. Your current number stays active.",
  MOBILE_CHANGE_CANCEL_REQUEST: "Cancel Request",
  MOBILE_CHANGE_CANCEL_CONFIRM_TITLE: "Cancel this request?",
  MOBILE_CHANGE_CANCEL_CONFIRM_TEXT: "Your mobile number will not be changed. You can raise a new request later.",
  MOBILE_CHANGE_CANCEL_KEEP: "No, keep it",
  MOBILE_CHANGE_CANCEL_YES: "Yes, cancel request",
  MOBILE_CHANGE_CANCEL_SUCCESS: "Your request has been cancelled",
  MOBILE_CHANGE_SUBMIT_SUCCESS: "Request submitted. A CRM will review it.",
  MOBILE_CHANGE_REJECTED_INFO: "Your request was rejected by the CRM.",
  MOBILE_CHANGE_REMARK: "CRM remark",
  MOBILE_CHANGE_RAISE_NEW: "Raise New Request",
  MOBILE_CHANGE_APPROVED_INFO: "Your registered mobile number was updated on",
  MOBILE_CHANGE_RETRY: "Retry",
  CORE_COMMON_PROFILE_MOBILE_NUMBER_INVALID: "Enter a valid 10-digit mobile number starting with 6-9",
};

export default DEFAULT_LABELS;
