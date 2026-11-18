import { Request } from "../../atoms/Utils/Request";

export const FacilityService = {

  fetchFacilities : async (queryFilter) => {
    const endpoint = "/facility-service/v2/facility/_bulk-search";
    const headers = {
      "Content-Type" : "application/json"
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
