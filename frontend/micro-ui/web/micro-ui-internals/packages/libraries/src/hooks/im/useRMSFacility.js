import { useQuery, useQueryClient } from "react-query";

const fetchRMSPausedFacilities = async (queryFilter, limit, offset) => {

  // const rmsPausedFacilityResponse = await Digit.RMSService.fetchRMSPausedFacilities(queryFilter, limit, offset);
  const rmsPausedFacilityResponse = {
    success: true,
    totalCount: 2,
    pausedFacilities: [
      {
        id: "1001",
        facilityId: "FAC/2026/9418",
        facilityName: "Bagalkot CHC",
        boundaryCode: "India_Karnataka_Raichur_Raichur_BagalkotWard1",
        pausedUntil: "2026-04-24T15:00:00Z",
        reason: "Maintenance extended",
        pausedBy: "crm.user1",
        updatedAt: "2026-04-22T09:30:00Z",
      },
      {
        id: "2042",
        facilityId: "FAC/2026/9416",
        facilityName: "Hosur PHC",
        boundaryCode: "India_Karnataka_Racichur_Balakot_HosurWard2",
        pausedUntil: "2026-04-23T11:00:00Z",
        reason: "Power work",
        pausedBy: "crm.user1",
        updatedAt: "2026-04-22T08:10:00Z",
      },
    ],
  };

  return {
    rmsPausedFacilities:
      rmsPausedFacilityResponse?.pausedFacilities?.map((facility) => ({
        id: facility.id,
        boundaryCode: facility.boundaryCode,
        facilityName: facility.facilityName,
        facilityId: facility.facilityId,
        pausedUntil: facility.pausedUntil,
        pausedBy: facility.pausedBy,
      })) || [],
    total: rmsPausedFacilityResponse?.totalCount,
  };
};

const useRMSPausedFacility = (queryFilter, limit, offset) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["RMS_PAUSED_FACILITY", queryFilter, limit, offset],
    () => fetchRMSPausedFacilities(queryFilter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["RMS_PAUSED_FACILITY"]),
  };
};

export default useRMSPausedFacility;
