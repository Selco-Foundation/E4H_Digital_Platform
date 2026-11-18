import { CustomRequest } from "../components/Custom/CustomRequest";

export const OrganizationService = {

  searchOrganizations: async ({ tenantId, offset = 0, limit = 10, searchCriteria = {} }) => {
    const endpoint = "/vendor/organisation/v1/_search";
    const headers = { "Content-Type": "application/json" };

    const resp = await CustomRequest({
      url: endpoint,
      method: "POST",
      auth: true,
      userService: true,
      headers,
      // Pagination goes in the body: OrgSearchRequest reads it from there, and this endpoint
      // ignores limit/offset query params entirely. Sending them there served the backend
      // default (10 rows from offset 0) on every page. The sibling /user/_search is the other
      // way round - it binds URLParams - so the two cannot be made to look alike here.
      params: { tenantId },
      data: {
        SearchCriteria: {
          tenantId,
          ...searchCriteria,
        },
        Pagination: { limit, offset },
      },
    });

    return resp?.data;
  },

  searchOrgUsers: async (tenantId, userIds = [], offset = 0, limit = 1) => {
    const endpoint = "/vendor/organisation/v1/user/_search";
    const headers = { "Content-Type": "application/json" };

    const resp = await CustomRequest({
      url: endpoint,
      method: "POST",
      auth: true,
      userService: true,
      headers,
      params: { tenantId, offset, limit },
      data: {
        OrgUser: {
          userIds,
          tenantId,
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