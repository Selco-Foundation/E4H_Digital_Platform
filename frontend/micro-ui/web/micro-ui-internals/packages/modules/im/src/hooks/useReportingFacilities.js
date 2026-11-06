import { useQuery } from "react-query";
import { getMappedFacilityCodes, ReportingFacilitiesService } from "../services/ReportingFacilities";

const useReportingFacilities = () => {
  const codes = getMappedFacilityCodes();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const userId = Digit.UserService.getUser().info.uuid;
  const query = useQuery(
    ["IM_REPORTING_FACILITIES", userId, tenantId, codes],
    () => ReportingFacilitiesService.fetch(tenantId, codes),
    { enabled: Boolean(tenantId && codes.length), retry: 1 }
  );
  return { ...query, codes, data: codes.length ? query.data : [] };
};

export default useReportingFacilities;
