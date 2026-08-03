import { Request } from "@egovernments/digit-ui-libraries";

export const AMCService = {

  fetchAMCConfigurations: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/asset-amc/v1/configuration/_search";
    const tenantId = queryFilter?.searchCriteria?.tenantId || Digit.ULBService.getCurrentTenantId();
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId,
      offset,
      limit,
    };

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params,
      headers,
    });
  },

  updateAMCConfigurations: async (request) => {
    const endpoint = "/asset-amc/v1/configuration/_update";
    const tenantId = request?.AmcConfigurations?.[0]?.tenantId || Digit.ULBService.getCurrentTenantId();
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId,
    };

    return await Request({
      url: endpoint,
      data: request,
      userService: true,
      method: "POST",
      auth: true,
      params,
      headers,
    });
  },

};
