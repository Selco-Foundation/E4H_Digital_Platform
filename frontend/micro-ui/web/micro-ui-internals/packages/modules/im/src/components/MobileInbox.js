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
  const localizedData = data?.combinedRes?.map(({ tenantId, incidentType, incidentId, incidentSubType, sla, status, taskOwner, potentialDuplicate, facility }) => ({
    [t("CS_COMMON_TICKET_NO")]:
    (
      <div>
        <Link to={`/${window.contextPath}/employee/im/complaint/details/${incidentId}/${tenantId}`} style={{ color: "#7a2829" }}>
          {incidentId}
        </Link>
        {potentialDuplicate && (
          <div style={{ marginTop: "5px" }}>
            <span
              style={{
                border: "1px solid #B91900",
                borderRadius: "6px",
                backgroundColor: "#FFF5F4",
                color: "#B91900",
                width: "fit-content",
                padding: "2px 6px",
                display: "inline-block",
                fontSize: "12px",
                fontWeight: "bold",
              }}
            >
              {t("CS_INFO_POTENTIAL_DUPLICATE")}
            </span>
          </div>
        )}
      </div>
    ),
    [t("CS_TICKET_TYPE")]: t(`SERVICEDEFS.${incidentType.toUpperCase()}`),
    [t("CS_TICKET_SUB_TYPE")]: t(`SERVICEDEFS.${incidentSubType.toUpperCase()}`),
    [t("CS_TICKET_DETAILS_CURRENT_STATUS")]: t(`CS_COMMON_${status}`),
    [t("CS_COMPLAINT_PHC_TYPE")]:t(facility),
    [t("WF_INBOX_HEADER_CURRENT_OWNER")]: taskOwner,
    [t("WF_INBOX_HEADER_SLA_DAYS_REMAINING")]: sla,
    [t("TenantID")]:tenantId,
    incidentId,
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
