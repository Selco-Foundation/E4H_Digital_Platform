import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";

const fetchFacilityDetails = async (queryFilter) => {
  const facilityResponse = await FacilityService.fetchFacilities(queryFilter);

  const facility = facilityResponse?.facilities?.[0];
  return {
    id: facility?.facility_id,
    facilityName: facility?.facility_name,
    facilityTypeCode: facility?.facility_type,
    solarDesignCode: facility?.facility_details?.solar_solution_design_type,
    facilityPocName: facility?.facility_poc_name,
    facilityPocPhone: facility?.facility_poc_phone,
    facilityPocEmail: facility?.facility_poc_email,
    hfrId: facility?.hfr_id,
    ninId: facility?.nin_id,
    pincode: facility?.address?.pincode,
    isActive: facility?.isActive,
    isOnmReady: facility?.isOnmReady,
    stateCode: facility?.boundary?.state,
    districtCode: facility?.boundary?.district,
    blockCode: facility?.boundary?.block,
    facility: facility,
  };
};

const useFacilityDetails = (id) => {

  const queryFilter = {
    tenantId: [Digit.ULBService.getCurrentTenantId()],
    facilityIds: [id],
  };

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY_DETAILS", queryFilter],
    () => fetchFacilityDetails(queryFilter)
  );

  return {
    isLoading,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY_DETAILS"]),
  };
};

export default useFacilityDetails;
