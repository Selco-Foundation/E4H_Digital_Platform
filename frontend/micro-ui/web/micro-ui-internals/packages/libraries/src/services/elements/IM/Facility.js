import { Request } from "../../atoms/Utils/Request";

/**
 * The IM roles that can define a facility scope, broadest first.
 *
 * Facility search is scoped per role, so the app has to say which of the caller's roles it is
 * acting as. Unlike the Management Hub, where one module means one role, an IM user can hold
 * several at once - a CRM is both COMPLAINT_ASSESSOR and COMPLAINANT - and the facility picker is
 * the same control for all of them. The broadest role they hold is the one that describes the area
 * they actually work in; picking the narrowest would hide facilities they are meant to act on.
 *
 * This chooses among scopes the caller already has. It cannot add one: the server checks the
 * claimed role against the roles on their token and refuses anything they do not hold.
 */
const FACILITY_SCOPE_ROLES = [
  "COMPLAINT_FACILITATOR_1", // State SPOC
  "COMPLAINT_FACILITATOR_2", // Tech POC
  "COMPLAINT_ASSESSOR", // CRM
  "COMPLAINT_RESOLVER", // vendor
  "COMPLAINANT", // facility POC
];

const actingRole = () => {
  const held = Digit.UserService.getUser()?.info?.roles?.map((role) => role?.code) || [];
  return FACILITY_SCOPE_ROLES.find((role) => held.includes(role));
};

export const FacilityService = {

  fetchFacilities : async (queryFilter) => {
    const endpoint = "/facility-service/v2/facility/_bulk-search";
    const headers = {
      "Content-Type" : "application/json"
    }

    const role = actingRole();
    if (role) {
      headers["x-acting-role"] = role;
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
