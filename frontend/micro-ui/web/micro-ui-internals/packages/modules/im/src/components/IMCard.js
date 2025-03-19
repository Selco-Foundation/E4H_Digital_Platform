import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { EmployeeModuleCard } from "@selco/digit-ui-react-components";


const IMCard = () => {
  const stateTenantId = Digit.ULBService.getStateId();
  const { t } = useTranslation();
  const [total, setTotal] = useState("-");
  
  const { data, isLoading, isFetching, isSuccess } = Digit.Hooks.useNewInboxGeneral({
    tenantId: Digit.ULBService.getCurrentTenantId(),
    ModuleCode: "Incident",
    filters: { limit: 10, offset: 0, services: ["Incident"] },
    config: {
      select: (data) => {
        return {totalCount:data?.totalCount,nearingSlaCount:data?.nearingSlaCount, data:data} || "-";
      },
      enabled: Digit.Utils.pgrAccess(),
    },
    
    
  });
  sessionStorage.setItem("applicationStatus", JSON.stringify(data?.data?.statusMap));
  
  useEffect(() => {
    if (!isFetching && isSuccess) setTotal(data);
  }, [isFetching]);

  const allLinks = [
    { text: t("ES_IM_INBOX"), link: `/${window.contextPath}/employee/im/inbox` },
    { text: t("ES_IM_NEW_COMPLAINT"), link: `/${window.contextPath}/employee/im/incident/create`, accessTo: ["COMPLAINANT"] },
  ];

  if (!Digit.Utils.pgrAccess()) {
    return null;
  }

  const isCodePresent = (array, codeToCheck) =>{
    return array.some(item => item.code === codeToCheck);
  }
  
  console.log("total", total)
  sessionStorage.setItem("inboxTotal", JSON.stringify(total?.totalCount));
  let tenantId = window.Digit.SessionStorage.get("Employee.tenantId");
  let newTenant = window.Digit.SessionStorage.get("Tenants")
  useEffect(() => {
    (async () => {
      const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
      if (isCodePresent(userRoles, "COMPLAINT_RESOLVER")) {
        const tenantCode = Digit.SessionStorage.get("citizen.userRequestObject").info.roles.map((ulb) => {
          return ulb.tenantId
        })
        const uniqueTenant = Array.from(new Set(tenantCode))
        const codes = uniqueTenant.filter(item => item !== stateTenantId)
          .map(item => item)
          .join(',');
        tenantId = tenantId == stateTenantId ? codes : tenantId
      }
      // let response = await Digit.PGRService.count(tenantId, {});
      // if (response?.count) {
      //   setTotal(response.count);
      // }
    })();
  }, []);
 
  const Icon = () => <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
    <path d="M0 0h24v24H0z" fill="#7a2829"></path>
    <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z" fill="white"></path>
  </svg>

  let propsForCSR =[
    {
      label: t("ES_IM_NEW_INCIDENT"),
      link: `/${window.contextPath}/employee/im/incident/create`,
      role: "COMPLAINANT" || "EMPLOYEE"
    }
  ]

  propsForCSR = propsForCSR.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role) );
console.log("propsForCSR",propsForCSR,Digit.Utils.didEmployeeHasRole("COMPLAINT"))
  const propsForModuleCard = {
    Icon: <Icon />,
    moduleName: t("ES_IM_INCIDENTS"),
    kpis: [
        {
            count: isLoading ? "-" : total?.totalCount,
            label: t("TOTAL_IM"),
            link: `/${window.contextPath}/employee/im/inbox`
        },
        {
            count: total?.nearingSlaCount,
            label: t("TOTAL_NEARING_SLA"),
            link: `/${window.contextPath}/employee/im/inbox?nearingSLA=1`
        }
    ],
    links: [
    {
        label: t("ES_IM_INBOX"),
        link: `/${window.contextPath}/employee/im/inbox`
    },
    ...propsForCSR
    ]
}

  return <EmployeeModuleCard {...propsForModuleCard} />
};
export default IMCard;
