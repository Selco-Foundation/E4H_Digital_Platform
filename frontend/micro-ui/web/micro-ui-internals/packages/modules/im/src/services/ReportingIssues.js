export const ReportingIssuesService = {
  fetch: async (tenantId) => {
    const response = await Digit.CustomService.getResponse({
      url: "/egov-mdms-service/v1/_search",
      params: { tenantId },
      useCache: false,
      body: {
        MdmsCriteria: {
          tenantId,
          moduleDetails: [{ moduleName: "Incident", masterDetails: [{ name: "Component" }, { name: "Observation" }] }],
        },
      },
    });
    const master = response?.MdmsRes?.Incident;
    if (!Array.isArray(master?.Component) || !Array.isArray(master?.Observation)) {
      throw new Error("Facility component and observation master data is unavailable");
    }
    return master.Component.filter((component) => component.active).map((component) => ({
      key: component.code,
      name: component.name,
      labelKey: `IM_COMPONENT_${component.code}`,
      order: component.order,
      category: component.category,
      subtypes: master.Observation
        .filter((observation) => observation.active && observation.componentCode === component.code)
        .sort((a, b) => a.order - b.order)
        .map((observation) => ({
          key: observation.code,
          name: observation.name,
          labelKey: `IM_OBSERVATION_${observation.code}`,
          order: observation.order,
          priority: observation.priority,
        })),
    })).sort((a, b) => a.order - b.order);
  },
};
