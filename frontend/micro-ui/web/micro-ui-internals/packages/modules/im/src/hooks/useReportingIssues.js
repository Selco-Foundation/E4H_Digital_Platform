import { useMemo } from "react";
import { useQuery } from "react-query";
import { ReportingIssuesService } from "../services/ReportingIssues";
import { isFacilityUser } from "../utilities/facilityUser";

const useReportingIssues = (enabled = true) => {
  const tenantId = Digit.SessionStorage.get("Employee.tenantId") || Digit.ULBService.getCurrentTenantId();
  const facilityUser = isFacilityUser(Digit.UserService.getUser()?.info?.roles);
  const options = { enabled: Boolean(enabled && tenantId && facilityUser), retry: 1 };
  const components = useQuery(["IM_FACILITY_COMPONENT", tenantId], () => ReportingIssuesService.fetch(tenantId, "Component"), options);
  const observations = useQuery(["IM_FACILITY_SERVICEDEFS", tenantId], () => ReportingIssuesService.fetch(tenantId, "ServiceDefs"), options);
  const data = useMemo(() => components.data?.map((component) => ({
    ...component,
    subtypes: observations.data?.filter((observation) => observation.componentCode === component.key),
  })), [components.data, observations.data]);
  return { ...components, data, observations };
};

export default useReportingIssues;
