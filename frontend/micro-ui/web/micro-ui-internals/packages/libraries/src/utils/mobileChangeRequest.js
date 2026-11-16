/**
 * Helpers for the "registered mobile number change request" feature (SEM 4.0, #3209).
 *
 * Kept free of React and of `Digit.*` so it is trivially unit-testable.
 * Exposed to modules as `Digit.Utils.mobileChangeRequest`.
 */

/** Mirrors backend MobileChangeRequestStatus. Only PENDING can still change state. */
export const MOBILE_CHANGE_STATUS = {
  PENDING: "PENDING",
  APPROVED: "APPROVED",
  REJECTED: "REJECTED",
  CANCELLED: "CANCELLED",
};

/** Backend role codes (see project docs: HCR = COMPLAINANT, CRM = COMPLAINT_ASSESSOR). */
export const MOBILE_CHANGE_ROLES = {
  HCR: "COMPLAINANT",
  CRM: "COMPLAINT_ASSESSOR",
};

/** 10-digit Indian mobile starting 6-9. Same rule as the backend's INDIAN_MOBILE pattern. */
export const INDIAN_MOBILE_REGEX = /^[6-9][0-9]{9}$/;

/**
 * Validates the proposed number BEFORE we hit the API
 * The backend re-validates, so this is for fast feedback only, never for security.
 *
 * number may already belong to another user is deliberately NOT checked.
 *
 * @returns {string|null} a localisation key describing the problem, or null if valid
 */
export const validateNewMobileNumber = (newNumber, currentNumber) => {
  const value = String(newNumber == null ? "" : newNumber).trim();
  if (!value) return "MOBILE_CHANGE_NUMBER_REQUIRED";
  if (!INDIAN_MOBILE_REGEX.test(value)) return "CORE_COMMON_PROFILE_MOBILE_NUMBER_INVALID"; // existing key
  if (currentNumber && value === String(currentNumber).trim()) return "MOBILE_CHANGE_SAME_NUMBER";
  return null;
};

/** Does the logged-in user (Digit userInfo) hold this role code? */
const hasRole = (userInfo, code) => !!userInfo?.roles?.some((role) => role?.code === code);

/** Only HCRs (facility users) see the change-request UI on their profile. */
export const isHcrUser = (userInfo) => hasRole(userInfo, MOBILE_CHANGE_ROLES.HCR);

/** Only CRMs see the approval queue and Approve/Reject. */
export const isCrmUser = (userInfo) => hasRole(userInfo, MOBILE_CHANGE_ROLES.CRM);

/**
 * Turns one backend MobileChangeRequest into the flat shape the UI uses.
 * All UI code should go through this, so a backend rename touches only this function.
 */
export const normalizeMobileChangeRequest = (request = {}) => ({
  id: request.id,
  tenantId: request.tenantId,
  userUuid: request.userUuid,
  userName: request.userName,
  facilityId: request.facilityId,
  facilityName: request.facilityName,
  boundaryCode: request.boundaryCode,
  currentMobileNumber: request.currentMobileNumber,
  newMobileNumber: request.newMobileNumber,
  status: request.status,
  comment: request.comment, // CRM's remark, e.g. the rejection reason
  actionBy: request.actionBy,
  actionTime: request.actionTime, // epoch millis, when the CRM/user closed it
  createdTime: request.auditDetails?.createdDate, // epoch millis
  lastModifiedTime: request.auditDetails?.lastModifiedDate,
  isPending: request.status === MOBILE_CHANGE_STATUS.PENDING,
});

/** Normalises a whole search/create/action response: { requests, totalCount }. */
export const normalizeMobileChangeResponse = (response) => ({
  requests: (response?.MobileChangeRequests || []).map(normalizeMobileChangeRequest),
  totalCount: response?.totalCount || 0,
});

/** Backend error code -> localisation key shown to the user (all keys are new) */
const ERROR_CODE_TO_KEY = {
  INVALID_MOBILE_NUMBER: "MOBILE_CHANGE_ERR_INVALID_MOBILE_NUMBER",
  SAME_MOBILE_NUMBER: "MOBILE_CHANGE_SAME_NUMBER",
  MOBILE_CHANGE_REQUEST_ALREADY_PENDING: "MOBILE_CHANGE_ERR_ALREADY_PENDING",
  MOBILE_CHANGE_REQUEST_NOT_PENDING: "MOBILE_CHANGE_ERR_NOT_PENDING",
  MOBILE_CHANGE_REQUEST_NOT_FOUND: "MOBILE_CHANGE_ERR_NOT_FOUND",
  NOT_AUTHORIZED: "MOBILE_CHANGE_ERR_NOT_AUTHORIZED",
  NOT_A_FACILITY_USER: "MOBILE_CHANGE_ERR_NOT_A_FACILITY_USER",
  FACILITY_NOT_FOUND: "MOBILE_CHANGE_ERR_FACILITY_NOT_FOUND",
  MULTIPLE_FACILITIES_FOUND: "MOBILE_CHANGE_ERR_MULTIPLE_FACILITIES",
  HRMS_UPDATE_FAILED: "MOBILE_CHANGE_ERR_HRMS_UPDATE_FAILED",
  USER_NOT_FOUND: "MOBILE_CHANGE_ERR_GENERIC",
  USERINFO_UUID: "MOBILE_CHANGE_ERR_GENERIC",
};

/** First backend error code in an axios error, e.g. "SAME_MOBILE_NUMBER", or undefined. */
export const getMobileChangeErrorCode = (error) => error?.response?.data?.Errors?.[0]?.code;

/**
 * Localisation key for an axios error from any mobile-change endpoint.
 * Unknown codes and network failures fall back to a generic message instead of showing raw text.
 */
export const getMobileChangeErrorKey = (error) =>
  ERROR_CODE_TO_KEY[getMobileChangeErrorCode(error)] || "MOBILE_CHANGE_ERR_GENERIC";
