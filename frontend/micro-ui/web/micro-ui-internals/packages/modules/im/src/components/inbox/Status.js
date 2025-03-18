import React from "react";
import { CheckBox, Loader } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

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
  const complaintsWithCount =Digit.Hooks.pgr.useComplaintStatusCount(complaints,tenant);
  
  
  let hasFilters = pgrfilters?.applicationStatus?.length;
  return (
    <div className="status-container">
      <div className="filter-label">{t("ES_IM_FILTER_STATUS")}</div>
      <div style={{marginBottom:-20}}>
      {complaintsWithCount.length === 0 && <Loader />}
      {complaintsWithCount.map((option, index) => {
        return (
          <CheckBox
            key={index}
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
