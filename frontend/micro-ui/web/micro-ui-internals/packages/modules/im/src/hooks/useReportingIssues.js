import { useQuery } from "react-query";
import { ReportingIssuesService } from "../services/ReportingIssues";
import { isFacilityUser } from "../utilities/facilityUser";

const useReportingIssues = (enabled = true) => {
  const tenantId = Digit.SessionStorage.get("Employee.tenantId") || Digit.ULBService.getCurrentTenantId();
  const facilityUser = isFacilityUser(Digit.UserService.getUser()?.info?.roles);
  return useQuery(["IM_FACILITY_COMPONENT_OBSERVATION", tenantId], () => ReportingIssuesService.fetch(tenantId), {
    enabled: Boolean(enabled && tenantId && facilityUser),
    retry: 1,
  });
};

export default useReportingIssues;
