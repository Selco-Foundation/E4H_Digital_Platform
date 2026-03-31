import { Request } from "@egovernments/digit-ui-libraries";
import { CustomRequest } from "../components/Custom/CustomRequest";

export const FacilityService = {
  fetchFacilities: async (queryFilter) => {
    const endpoint = "/facility-service/v2/facility/_bulk-search";
    const headers = {
      "Content-Type": "application/json",
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

  createFacility: async (facilityPayload) => {
    const endpoint = "/facility-service/v2/facility/create";
    const headers = {
      "Content-Type": "application/json",
    };

    const response = await CustomRequest({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: facilityPayload,
      headers: headers,
    });

    return response?.data;
  },

  updateFacility: async (facilityPayload) => {
    const endpoint = "/facility-service/v2/facility/update";
    const headers = {
      "Content-Type": "application/json",
    };

    const response = await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: facilityPayload,
      headers: headers,
    });

    return response?.data;
  },
};
