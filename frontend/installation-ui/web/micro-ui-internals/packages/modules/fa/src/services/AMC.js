import { Request } from "@egovernments/digit-ui-libraries";

export const AMCService = {

  fetchAMCConfigurations: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/asset-amc/v1/configuration/_search";
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    };

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params: params,
      headers: headers,
    });
  },

  fetchAMCAssets: async (queryFilter, limit, offset) => {
    const endpoint = "/asset-amc/v1/asset/_search";
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    };

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params: params,
      headers: headers,
    });
  },

  fetchAMCVisits: async (queryFilter, limit, offset) => {
    const endpoint = "/asset-amc/v1/visit/_search";
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    };

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params: params,
      headers: headers,
    });
  },

};
