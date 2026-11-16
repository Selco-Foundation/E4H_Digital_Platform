import { Request } from "../../atoms/Utils/Request";

/**
 * The IM roles that can define a facility scope.
 *
 * Facility search is scoped per role, so the app has to say which of its roles it is acting as.
 * Unlike the Management Hub, where one module means one role, an IM user holds several at once - a
 * CRM is both COMPLAINT_ASSESSOR and COMPLAINANT - and the facility picker is the same control for
 * all of them. So IM declares every IM role the caller holds and the server unions their
 * jurisdictions: within a jurisdiction it must not matter which IM hat the user is wearing.
 *
 * Roles outside this list are left out on purpose. The same person may also be a PROJECT_MANAGER
 * or FACILITY_ADMIN in the Management Hub, and those grants must not widen what an IM screen shows.
 *
 * This chooses among scopes the caller already has. It cannot add one: the server checks every
 * claimed role against the roles on their token and refuses the request if any is not held.
 */
const IM_SCOPE_ROLES = [
  "COMPLAINT_FACILITATOR_1", // State SPOC
  "COMPLAINT_FACILITATOR_2", // Tech POC
  "COMPLAINT_ASSESSOR", // CRM
  "COMPLAINT_RESOLVER", // vendor
  "COMPLAINANT", // facility POC
];

/**
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

}
