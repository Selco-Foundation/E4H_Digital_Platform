import { Request } from "@egovernments/digit-ui-libraries";
import { MODULE_ROLE } from "../constants/Roles";

export const FacilityService = {
  fetchFacilities: async (queryFilter) => {
    const endpoint = "/facility-service/v2/facility/_bulk-search";
    const headers = {
      "Content-Type": "application/json",
      /*
       * The role this screen is acting as. Facility search is scoped per role, so a user who is a
       * Facility Administrator in one state and a Project Manager in another gets the right area
       * for the screen they are on rather than the union of both.
       *
       * This cannot grant anything: the server checks the role against the ones on the caller's
       * own token, so the header only ever chooses among scopes they already hold.
       */
      "x-acting-role": MODULE_ROLE,
    };
    const data = {
      Facility: queryFilter,
    };

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: data,
      headers: headers,
    });
  },
};
