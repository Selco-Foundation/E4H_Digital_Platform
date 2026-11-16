// Use the full login mapping, not the facility currently selected in the header.
export const getMappedFacilityCodes = () =>
  [...new Set(Digit.SessionStorage.get("Jurisdiction.Boundaries")?.facility || [])].filter(Boolean).sort();

export const ReportingFacilitiesService = {
  fetch: async (tenantId, codes) => {
    const response = await Digit.FacilityService.fetchFacilities({
      tenantId: [tenantId],
      boundaryCodes: codes,
      sendNonPaginatedResponse: true,
    });
    if (!Array.isArray(response?.facilities)) throw new Error("Invalid facility response");
    const allowedCodes = new Set(codes);
    const seen = new Set();
    return response.facilities.filter((facility) => {
      if (!allowedCodes.has(facility.boundaryCode) || seen.has(facility.boundaryCode)) return false;
      seen.add(facility.boundaryCode);
      return true;
    }).map((facility) => ({
      code: facility.boundaryCode,
      id: facility.facility_id,
      name: facility.facility_name,
      status: facility.facility_status,
    }));
  },
};
