import { Request } from "@egovernments/digit-ui-libraries";

export const BoundaryService = {
  fetchStates: async () => {
    const response = await Request({
      url: "/boundary-service/boundary-relationships/_search",
      userService: true,
      method: "POST",
      auth: true,
      params: {
        tenantId: "in",
        hierarchyType: "SELCO",
        boundaryType: "State",
        includeChildren: false,
        includeParents: false,
      },
      headers: { "Content-Type": "application/json" },
    });

    return (response?.TenantBoundary?.[0]?.boundary || [])
      .filter((state) => state.code)
      .map((state) => ({ code: state.code, name: `Boundary_${state.code}`, type: "boundary" }))
      .sort((first, second) => first.code.localeCompare(second.code));
  },
};
