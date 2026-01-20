import { CustomRequest } from "../components/Custom/CustomRequest";

export const OrganizationService = {
  /**
   * POST /vendor/organisation/v1/_search?tenantId=in&offset=0&limit=10
   * Body: { SearchCriteria: { tenantId: "in", ... } }
   * CustomRequest will attach RequestInfo automatically (auth + userInfo).
   */
  searchOrganizations: async ({ tenantId, offset = 0, limit = 10, searchCriteria = {} }) => {
    const endpoint = "/vendor/organisation/v1/_search";
    const headers = { "Content-Type": "application/json" };

    const resp = await CustomRequest({
      url: endpoint,
      method: "POST",
      auth: true,
      userService: true,
      headers,
      params: { tenantId, offset, limit },
      data: {
        SearchCriteria: {
          tenantId,
          ...searchCriteria,
        },
      },
    });

    return resp?.data;
  },

  createOrganization: async (payload) => {
    const endpoint = "/vendor/organisation/v1/_create";
    const headers = { "Content-Type": "application/json" };

    const resp = await CustomRequest({
      url: endpoint,
      method: "POST",
      auth: true,
      userService: true,
      headers,
      data: payload,
    });

    return resp?.data;
  },

  updateOrganization: async (payload) => {
    const endpoint = "/vendor/organisation/v1/_update";
    const headers = { "Content-Type": "application/json" };

    const resp = await CustomRequest({
      url: endpoint,
      method: "POST",
      auth: true,
      userService: true,
      headers,
      data: payload,
    });

    return resp?.data;
  },
};