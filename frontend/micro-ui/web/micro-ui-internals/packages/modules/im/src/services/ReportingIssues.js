export const ReportingIssuesService = {
  fetch: async (tenantId, masterName) => {
    const response = await Digit.CustomService.getResponse({
      url: "/egov-mdms-service/v1/_search",
      params: { tenantId },
      useCache: false,
      body: { MdmsCriteria: { tenantId, moduleDetails: [
        { moduleName: "Incident", masterDetails: [{ name: masterName }] },
      ] } },
    });
    const records = response?.MdmsRes?.Incident?.[masterName];
    if (!Array.isArray(records)) throw new Error(`Incident.${masterName} master data is unavailable`);
    return records.filter((record) => record.active && (masterName === "Component" || (record.channel === "HF" && !record.deprecated))).sort((a, b) => a.order - b.order).map((record) => ({
      key: masterName === "Component" ? record.code : record.serviceCode,
      name: record.name,
      labelKey: masterName === "Component" ? `IM_COMPONENT_${record.code}` : `IM_OBSERVATION_${record.serviceCode}`,
      order: record.order,
      ...(masterName === "Component" ? { category: record.category } : { componentCode: record.componentCode, priority: record.priority }),
    }));
  },
};
