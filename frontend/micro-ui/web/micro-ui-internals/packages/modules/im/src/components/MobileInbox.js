import React from "react";
import { useTranslation } from "react-i18next";
import { Loader, Card } from "@selco/digit-ui-react-components";
import { ComplaintCard } from "./inbox/ComplaintCard";
import ComplaintsLink from "./inbox/ComplaintLinks";
import { LOCALE } from "../constants/Localization";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";

const GetSlaCell = (value) => {
  return value < 0 ? <span className="sla-cell-error">{value}</span> : <span className="sla-cell-success">{value}</span>;
};

const MobileInbox = ({ data, onFilterChange, onSearch, isLoading, searchParams }) => {
  const { t } = useTranslation();
  console.log("datadatadata",data)
  const localizedData = data?.combinedRes?.map(({ tenantId,phcType, incidentType, incidentId, incidentSubType, sla, status, taskOwner }) => ({
    [t("CS_COMMON_TICKET_NO")]: 
    incidentId && (typeof incidentId === "string" || typeof incidentId === "number") ? (
      <Link to={`/${window.contextPath}/employee/im/complaint/details/${incidentId}/${tenantId}`} style={{ color: "#7a2829" }}>
        {incidentId}
      </Link>
    ) : (
      incidentId?.toString() ? (
        <Link to={`/${window.contextPath}/employee/im/complaint/details/${incidentId.toString()}/${tenantId}`} style={{ color: "#7a2829" }}>
          {incidentId.toString()}
        </Link>
      ) : ""
    ),
    [t("CS_TICKET_TYPE")]: t(`SERVICEDEFS.${incidentType.toUpperCase()}`),
    [t("CS_TICKET_SUB_TYPE")]: t(`SERVICEDEFS.${incidentSubType.toUpperCase()}`),
    [t("CS_TICKET_DETAILS_CURRENT_STATUS")]: t(`CS_COMMON_${status}`),
    [t("CS_COMPLAINT_PHC_TYPE")]:t(phcType),
    [t("WF_INBOX_HEADER_CURRENT_OWNER")]: taskOwner,
    [t("WF_INBOX_HEADER_SLA_DAYS_REMAINING")]: sla,
    [t("TenantID")]:tenantId
    // status,
  }));

  let result;
  if (isLoading) {
    result = <Loader />;
  } else {
    result = (
      <ComplaintCard
        data={localizedData}
        onFilterChange={onFilterChange}
        serviceRequestIdKey={t("CS_COMMON_COMPLAINT_NO")}
        onSearch={onSearch}
        searchParams={searchParams}
      />
    );
  }

  return (
    <div style={{ padding: 0 }}>
      <div className="inbox-container">
        <div className="filters-container">
          <ComplaintsLink isMobile={true} />
          {result}
        </div>
      </div>
    </div>
  );
};
MobileInbox.propTypes = {
  data: PropTypes.any,
  onFilterChange: PropTypes.func,
  onSearch: PropTypes.func,
  isLoading: PropTypes.bool,
  searchParams: PropTypes.any,
};

MobileInbox.defaultProps = {
  onFilterChange: () => {},
  searchParams: {},
};

export default MobileInbox;
