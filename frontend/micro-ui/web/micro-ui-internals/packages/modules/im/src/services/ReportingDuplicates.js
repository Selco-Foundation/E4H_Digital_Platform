export const ReportingDuplicatesService = {
  fetch: async (tenantId, boundaryCode, component) => {
    const response = await Digit.CustomService.getResponse({
      url: "/im-services/v2/request/_duplicateCheck",
      body: { tenantId, boundaryCode, component },
      useCache: false,
      userService: true,
    });
    if (typeof response?.duplicateCheckApplicable !== "boolean" || !Array.isArray(response?.duplicates)) {
      throw new Error("Invalid duplicate check response");
    }
    return { applicable: response.duplicateCheckApplicable, duplicates: response.duplicates };
  },
};
