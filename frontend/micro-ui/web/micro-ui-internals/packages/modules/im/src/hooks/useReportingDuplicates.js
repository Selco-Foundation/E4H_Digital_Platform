import { useQuery } from "react-query";
import { ReportingDuplicatesService } from "../services/ReportingDuplicates";

const useReportingDuplicates = (boundaryCode, component, enabled) => {
  const tenantId = Digit.SessionStorage.get("Employee.tenantId") || Digit.ULBService.getCurrentTenantId();
  const userId = Digit.UserService.getUser()?.info?.uuid;
  const query = useQuery(["IM_REPORTING_DUPLICATES", userId, tenantId, boundaryCode, component],
    () => ReportingDuplicatesService.fetch(tenantId, boundaryCode, component), {
      enabled: Boolean(enabled && tenantId && boundaryCode && component),
      retry: false,
      staleTime: 0,
      refetchOnMount: false,
      refetchOnReconnect: false,
      refetchOnWindowFocus: false,
    });
  return { ...query, tenantId };
};

export default useReportingDuplicates;
