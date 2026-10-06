import { Request } from "@egovernments/digit-ui-libraries";

export const AMCService = {

  fetchAMCPlans: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/asset-amc/v1/amc-plan/_search";
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
    request?.AmcConfigurations?.forEach((configuration) => {
      configuration.durationMonths = 1;
      configuration.visitFrequencyMonths = 1;
      configuration.configurationEndDate = 1;
    });

    const data = {
      ...request,
      AmcConfigurations: request?.AmcConfigurations?.map((configuration) => ({
        ...configuration,
        durationMonths: 1,
        visitFrequencyMonths: 1,
        configurationEndDate: 1,
      })) || [],
    };
    const tenantId = request?.AmcConfigurations?.[0]?.tenantId || Digit.ULBService.getCurrentTenantId();
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId,
    };

    return await Request({
      url: endpoint,
      data,
      userService: true,
      method: "POST",
      auth: true,
      params,
      headers,
    });
  },

};
