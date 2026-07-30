import { Request } from "@egovernments/digit-ui-libraries";

export const AMCConfigurationService = {

  fetchAMCConfigurations: async (queryFilter, limit, offset) => {
    const endpoint = "/asset-amc/v1/configuration/_search";
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    }
    const headers = {
      "Content-Type": "application/json"
    }

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

}
