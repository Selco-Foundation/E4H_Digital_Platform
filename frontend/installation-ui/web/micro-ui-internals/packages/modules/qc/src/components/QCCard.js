import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { EmployeeModuleCard } from "@egovernments/digit-ui-react-components";
import { useHistory } from "react-router-dom";

const ArrowRight = ({ color = "white" }) => (
  <svg width="17" height="16" viewBox="0 0 17 16" fill={color} xmlns="http://www.w3.org/2000/svg">
    <path d="M8.5 0L7.09 1.41L12.67 7H0.5V9H12.67L7.09 14.59L8.5 16L16.5 8L8.5 0Z" fill={color} />
  </svg>
);
const MenuIcon = ({ color = "white" }) => (
  <svg width="24" height="14" viewBox="0 0 24 14" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path
      d="M0 8.33203H2.66667V5.66536H0V8.33203ZM0 13.6654H2.66667V10.9987H0V13.6654ZM0 2.9987H2.66667V0.332031H0V2.9987ZM5.33333 8.33203H24V5.66536H5.33333V8.33203ZM5.33333 13.6654H24V10.9987H5.33333V13.6654ZM5.33333 0.332031V2.9987H24V0.332031H5.33333Z"
      fill={color}
    />
  </svg>
);

// const QCCard = () => {
//   const stateTenantId = Digit.ULBService.getStateId();
//   const { t } = useTranslation();
//   const [total, setTotal] = useState("-");
//   const { uuid } = Digit.UserService.getUser().info;
//   const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
//
//   const isCodePresent = (array, codeToCheck) =>{
//     return array.some(item => item.code === codeToCheck);
//   }
//
//   const { data, isLoading, isFetching, isSuccess } = Digit.Hooks.useNewInboxGeneral({
//     tenantId: Digit.ULBService.getCurrentTenantId(),
//     ModuleCode: "Incident",
//     filters: { limit: 10, offset: 0, services: ["Incident"], ...(isCodePresent(userRoles, "COMPLAINT_RESOLVER") && { assignee: uuid }) },
//     config: {
//       select: (data) => {
//         return {totalCount:data?.totalCount,nearingSlaCount:data?.nearingSlaCount, data:data} || "-";
//       },
//       enabled: Digit.Utils.pgrAccess(),
//     },
//   })
//   sessionStorage.setItem("applicationStatus", JSON.stringify(data?.data?.statusMap));
//
//   useEffect(() => {
//     if (!isFetching && isSuccess) setTotal(data);
//   }, [isFetching]);
//
//   // if (!Digit.Utils.pgrAccess()) {
//   //   return null;
//   // }
//
//   sessionStorage.setItem("inboxTotal", JSON.stringify(total?.totalCount));
//   let tenantId = window.Digit.SessionStorage.get("Employee.tenantId");
//   let newTenant = window.Digit.SessionStorage.get("Tenants")
//   useEffect(() => {
//     (async () => {
//       if (isCodePresent(userRoles, "COMPLAINT_RESOLVER")) {
//         const tenantCode = Digit.SessionStorage.get("citizen.userRequestObject").info.roles.map((ulb) => {
//           return ulb.tenantId
//         })
//         const uniqueTenant = Array.from(new Set(tenantCode))
//         const codes = uniqueTenant.filter(item => item !== stateTenantId)
//           .map(item => item)
//           .join(',');
//         tenantId = tenantId == stateTenantId ? codes : tenantId
//       }
//       // let response = await Digit.PGRService.count(tenantId, {});
//       // if (response?.count) {
//       //   setTotal(response.count);
//       // }
//     })();
//   }, []);
//
//   const Icon = () => <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
//     <path d="M0 0h24v24H0z" fill="#7a2829"></path>
//     <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z" fill="white"></path>
//   </svg>
//
//   let propsForCSR = [
//     // {
//     //   label: t("ES_IM_NEW_INCIDENT"),
//     //   link: `/${window.contextPath}/employee/im/incident/create`,
//     //   role: "COMPLAINANT" || "EMPLOYEE"
//     // }
//   ]
//
//   propsForCSR = propsForCSR.filter(link => link.role && Digit.Utils.didEmployeeHasRole(link.role) );
//
//   const propsForModuleCard = {
//     Icon: <Icon />,
//     moduleName: t("ES_IM_INCIDENTS"),
//     kpis: [
//         {
//             count: isLoading ? "-" : total?.totalCount,
//             label: t("TOTAL_QC"),
//             link: `/${window.contextPath}/employee/im/field-plan`
//         },
//         {
//             count: total?.nearingSlaCount,
//             label: t("TOTAL_NEARING_SLA"),
//             link: `/${window.contextPath}/employee/im/field-plan?nearingSLA=1`
//         }
//     ],
//     links: [
//     {
//         label: t("ES_QC_INBOX"),
//         link: `/${window.contextPath}/employee/im/field-plan`
//     },
//     ...propsForCSR
//     ]
//   }
//
//   return <EmployeeModuleCard {...propsForModuleCard} />
// };

const QCCard = () => {
  const history = useHistory();
  const { t } = useTranslation();
  const windowWidth = window.innerWidth;

  const userType = "employee";

  const handleClick = () => {
    history.push(`/${window?.contextPath}/employee/qc/field-plan`);
  };

  return (
    <div className={`user-profile`}>
      <div
        style={{
          display: "flex",
          marginLeft: "0px",
          flex: 1,
          flexDirection: windowWidth < 768 || userType === "citizen" ? "column" : "row",
          margin: userType === "citizen" ? "8px" : "0px",
          gap: userType === "citizen" ? "" : "0 24px",
          boxShadow: userType === "citizen" ? "1px 1px 4px 0px rgba(0,0,0,0.2)" : "",
          background: userType === "citizen" ? "white" : "",
          borderRadius: userType === "citizen" ? "4px" : "",
          width: userType === "citizen" ? "960px" : "",
        }}
      >
        <section
          style={{
            position: "relative",
            width: "fit-content",
            maxWidth: "400px",
            borderRadius: "4px",
            boxShadow: userType === "citizen" ? "" : "1px 1px 4px 0px rgba(0,0,0,0.2)",
            background: "white",
            padding: userType === "citizen" ? "8px" : "16px",
          }}
        >
          <div style={{ marginBottom: "10px", padding: "8px", paddingLeft: 0, display: "flex", gap: "16px", alignItems: "center" }}>
            <MenuIcon color="#B91900" />
            <div
              style={{
                fontFamily: "Roboto",
                fontWeight: "700",
                fontSize: "24px",
                lineHeight: "100%",
                letterSpacing: "0px",
                color: "#0B4B66",
                width: "70%",
              }}
            >
              Home
            </div>
          </div>
          <div
            style={{
              marginBottom: "10px",
              fontFamily: "Roboto",
              fontWeight: 400,
              fontSize: "16px",
              lineHeight: "24px",
              letterSpacing: "0px",
              color: "#0B0C0C",
            }}
          >
            View new activities that have been assigned to you, assign activities among your team and review their work.
          </div>
          <div
            style={{
              width: 116,
              height: 32,
              display: "flex",
              gap: "8px",
              justifyContent: "center",
              paddingTop: "8px",
              paddingRight: "20px",
              paddingBottom: "8px",
              paddingLeft: "20px",
              background: "#C84C0E",
              color: "white",
              cursor: "pointer",
            }}
            onClick={handleClick}
          >
            View <ArrowRight color={"white"} />
          </div>
        </section>
      </div>
    </div>
  );
}

export default QCCard;
