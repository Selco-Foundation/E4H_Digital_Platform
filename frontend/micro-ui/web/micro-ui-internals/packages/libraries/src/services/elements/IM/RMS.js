import { Request } from "../../atoms/Utils/Request";

export const RMSService = {

  fetchRMSPausedFacilities: async (queryFilter, limit, offset) => {
    const endpoint = "/rms-service/v1/ticket/paused_facility";
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      limit,
      offset,
    }

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: queryFilter,
      params : params,
      headers: headers,
    });
  },

  fetchFacilityStatus: async (queryFilter) => {
    const endpoint = "/rms-service/v1/ticket/pause/_search";
    const headers = {
      "Content-Type": "application/json",
    };

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: queryFilter,
      headers: headers,
    });
  },

  updateRMSTicketPause: async (queryFilter) => {
    const endpoint = "/rms-service/v1/ticket/pause";
    const headers = {
      "Content-Type": "application/json",
    };

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: queryFilter,
      headers: headers,
    });
  }

};
