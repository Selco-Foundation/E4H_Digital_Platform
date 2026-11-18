import { Request } from "../../atoms/Utils/Request";
import { IM_SCOPE_ROLES } from "../../../utils/jurisdictionScope";

/**
 * Facility search is scoped per role, so the app has to say which of its roles it is acting as.
 * Unlike the Management Hub, where one module means one role, an IM user holds several at once - a
 * CRM is both COMPLAINT_ASSESSOR and COMPLAINANT - and the facility picker is the same control for
 * all of them. So IM declares every IM role the caller holds and the server unions their
 * jurisdictions: within a jurisdiction it must not matter which IM hat the user is wearing.
 *
 * This chooses among scopes the caller already has. It cannot add one: the server checks every
 * claimed role against the roles on their token and refuses the request if any is not held.
 *
 * The role list itself lives in jurisdictionScope, shared with the boundary pickers.
 *
 * ---
 *
 * The IM roles to declare, optionally narrowed to the ones a single screen means.
 *
 * `only` is for a page that is gated to one role and should be scoped to it alone - ticket
 * creation is COMPLAINANT's screen, so it must offer the facilities that user is POC for, not
 * everything their other IM roles can reach. It still intersects with what the caller holds,
 * because declaring an unheld role is refused outright.
 */
export const imScopeRoles = (only) => {
  const held = Digit.UserService.getUser()?.info?.roles?.map((role) => role?.code) || [];
  const mine = IM_SCOPE_ROLES.filter((role) => held.includes(role));
  return only?.length ? mine.filter((role) => only.includes(role)) : mine;
};

export const FacilityService = {

  // Exposed on Digit.FacilityService so hooks can put the declared roles in their query key; the
  // service itself falls back to it when a caller passes none.
  imScopeRoles,

  fetchFacilities : async (queryFilter, scopeRoles) => {
    const endpoint = "/facility-service/v2/facility/_bulk-search";
    const headers = {
      "Content-Type" : "application/json"
    }

    const roles = scopeRoles || imScopeRoles();
    if (roles.length) {
      headers["x-acting-role"] = roles.join(",");
    }

    const data = {
      "Facility": queryFilter,
    }

    return await Request({
      url : endpoint,
      userService : true,
      method : "POST",
      auth : true,
      data : data,
      headers : headers,
    });
  },


  /* ------------------------------------------------------------------
   * Backend: health-facility-registry -> MobileChangeRequestController
   *
   * Every call goes through `Request` with userService + auth, which fills
   * RequestInfo (authToken + logged-in userInfo). The backend trusts userInfo
   * (uuid / roles) to decide WHO is calling, so never build RequestInfo by hand.
   * ------------------------------------------------------------------ */

  /**
   * Facility user raises a request. Only `newMobileNumber` is read by the backend;
   * user, facility and current number are derived server-side from HRMS.
   * @param {{ newMobileNumber: string }} param0
   */
  createMobileChangeRequest: ({ newMobileNumber }) =>
    mobileChangeRequestCall("_create", {
      MobileChangeRequest: { newMobileNumber, tenantId: mobileChangeTenantId() },
    }),

  /**
   * Non-CRM callers always get ONLY their own requests (newest first), whatever
   * the criteria say. CRM (COMPLAINT_ASSESSOR) gets everyone's; use
   * criteria.status = ["PENDING"] for the approval queue.
   * @param {{ status?: string[], ids?: string[], tenantId?: string, userUuid?: string,
   *           facilityId?: string, limit?: number, offset?: number }} criteria
   */
  searchMobileChangeRequests: (criteria = {}) =>
    mobileChangeRequestCall("_search", { criteria: { tenantId: mobileChangeTenantId(), ...criteria } }),

  /** Requester cancels their own request, only while PENDING. */
  cancelMobileChangeRequest: ({ id, comment }) =>
    mobileChangeRequestCall("_cancel", { id, comment, tenantId: mobileChangeTenantId() }),

  /** CRM approves a PENDING request. Backend updates HRMS/egov-user + facility POC phone. */
  approveMobileChangeRequest: ({ id, comment }) =>
    mobileChangeRequestCall("_approve", { id, comment, tenantId: mobileChangeTenantId() }),

  /** CRM rejects a PENDING request. The user's number is unchanged. */
  rejectMobileChangeRequest: ({ id, comment }) =>
    mobileChangeRequestCall("_reject", { id, comment, tenantId: mobileChangeTenantId() }),

}

// The API gateway needs a tenantId OUTSIDE RequestInfo in every body (inside MobileChangeRequest for _create,
// inside criteria for _search, top level for _cancel/_approve/_reject); without it the gateway answers
const mobileChangeTenantId = () =>
  Digit.UserService?.getUser?.()?.info?.tenantId || Digit.ULBService?.getStateId?.();

const MOBILE_CHANGE_REQUEST_BASE = "/facility-service/v2/facility/mobile-change-request";

// One place for the plumbing shared by all five endpoints.
const mobileChangeRequestCall = (action, data) =>
  Request({
    url: `${MOBILE_CHANGE_REQUEST_BASE}/${action}`,
    userService: true,
    method: "POST",
    auth: true,
    data,
    headers: { "Content-Type": "application/json" },
  });
