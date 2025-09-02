import React from "react";
import { CheckBox, Loader } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const statusOrder = [
  "PENDINGFORASSIGNMENT",
  "PENDINGRESOLUTION",
  "RESOLVED",
  "CLOSEDAFTERRESOLUTION",
  "REJECTED",
  "CLOSEDAFTERREJECTION",
  "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
  "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
  "PENDING_RESOLUTION_SPARE_PART_NEEDED",
  "PENDING_RESOLUTION_OUT_OF_WARRANTY",
];

const Status = ({ complaints, onAssignmentChange, pgrfilters, statusArray }) => {

  const { t } = useTranslation();
  let tenant = Digit.ULBService.getCurrentTenantId();
  const stateTenantId = Digit.ULBService.getStateId();
  const isCodePresent = (array, codeToCheck) =>{
    return array.some(item => item.code === codeToCheck);
  }
  const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
  if(pgrfilters?.phcType.length >0)
  {
     tenant = pgrfilters?.phcType.map((ulb)=> {return ulb.code}).join(",")
    
  }
  else if (isCodePresent(userRoles, "COMPLAINT_RESOLVER") && (pgrfilters?.phcType.length ==0) && Digit.SessionStorage.get("Employee.tenantId") == stateTenantId)
  {
    const codes = Digit.SessionStorage.get("Tenants").filter(item => item.code !== stateTenantId)
    .map(item => item.code)
    .join(',');
    tenant = codes

  }
  const complaintsWithCountRaw = Digit.Hooks.pgr.useComplaintStatusCount(complaints,tenant);
  const complaintsWithCount = Array.isArray(complaintsWithCountRaw) ? complaintsWithCountRaw : [];

  const sortedComplaints = [...complaintsWithCount].sort((a, b) => {
    const indexA = statusOrder.indexOf(a.code);
    const indexB = statusOrder.indexOf(b.code);
    if (indexA === -1 && indexB === -1) return 0;
    if (indexA === -1) return 1;
    if (indexB === -1) return -1;
    return indexA - indexB;
  });

  let hasFilters = pgrfilters?.applicationStatus?.length;
  return (
    <div className="status-container">
      <div className="filter-label">{t("ES_IM_FILTER_STATUS")}</div>
      <div style={{marginBottom:-20}}>
        {sortedComplaints.length === 0 && <Loader />}
        {sortedComplaints.map((option) => {
          return (
            <CheckBox
              key={option.code || option.name}
              onChange={(e) => onAssignmentChange(e, option)}
              checked={hasFilters ? (pgrfilters.applicationStatus.filter((e) => e.code === option.code).length !== 0 ? true : false) : false}
              label={`${option.name} ${option.count ? `(${option.count})` : ""}`}
            />
          );
        })}
      </div>
    </div>
  );
};

export default Status;
