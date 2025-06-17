import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

const useComplaintStatus = () => {
  const { t } = useTranslation();
  const [complaintStatus, setComplaintStatus] = useState([]);
  const tenantId = Digit.ULBService.getCurrentTenantId();
  let assignee="";
  const appFilters=JSON.parse(sessionStorage.getItem("appFilters"));
 // const { limit, offset }=appFilters;
  const { data, isLoading, isFetching, isSuccess } = Digit.Hooks.useNewInboxGeneral({
    tenantId: Digit.ULBService.getCurrentTenantId(),
    ModuleCode: "Incident",
    filters: { limit: 10, offset: 0, services: ["Incident"] },
    config: {
      select: (data) => {
        return {data:data};
      },
      enabled: Digit.Utils.pgrAccess(),
    },
    
    
  });

  useEffect(() => {
    let WorkflowService = null;
    // const user = Digit.UserService.getUser();
    // const tenantId = user?.info?.tenantId;
    (async () => {
      // let stateCode =
      //   Digit.SessionStorage.get("userType") == "employee"
      //     ? Digit.SessionStorage.get("Employee.tenantId")
      //     : Digit.SessionStorage.get("Citizen.tenantId");
      WorkflowService = await Digit.WorkflowService.init(tenantId, "Incident_High");
      let applicationStatus = WorkflowService.BusinessServices[0].states
        .filter((state) => state.applicationStatus)
        .map((state) => ({
          name: t(`CS_COMMON_${state.applicationStatus}`),
          code: state.applicationStatus,
        }));
      setComplaintStatus(applicationStatus);
    })();
  }, [t, tenantId]);

  return complaintStatus;
};
export default useComplaintStatus;
