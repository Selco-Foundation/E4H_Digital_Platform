import React, { useCallback } from "react";
import { CheckBox, Loader } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const Status = ({ complaints, onAssignmentChange, pgrfilters, orderedStatuses = [] }) => {

  const { t } = useTranslation();
  let tenant = Digit.ULBService.getCurrentTenantId();
  const complaintsWithCountRaw = Digit.Hooks.pgr.useComplaintStatusCount(complaints, tenant);
  const complaintsWithCount = Array.isArray(complaintsWithCountRaw) ? complaintsWithCountRaw : [];

  const countedStatusCodes = [];
  const sortedComplaints = [];
  orderedStatuses.forEach(statusObject => {
    const sortedStatusWithCount = {
      ...statusObject,
      count: complaintsWithCount
        .filter((statusWithCount) => statusObject.statuses.includes(statusWithCount.code))
        .reduce((total, statusWithCount) => total + statusWithCount.count, 0),
    };
    sortedComplaints.push(sortedStatusWithCount);
    countedStatusCodes.push(...statusObject.statuses);
  });

  complaintsWithCount.forEach(statusObject => {
    if (!countedStatusCodes.includes(statusObject.code)) {
      sortedComplaints.push({
        ...statusObject,
        statuses: [statusObject.code],
      });
    }
  });

  const isChecked = useCallback((option) => {
      const existingStatusCodes = (pgrfilters?.applicationStatus || []).map((status) => status.code);
      return option.statuses.every((status) => existingStatusCodes.includes(status));
    }, [pgrfilters?.applicationStatus]
  );

  return (
    <div className="status-container">
      <div className="filter-label">{t("ES_IM_FILTER_STATUS")}</div>
      <div style={{ marginBottom: -20 }}>
        {sortedComplaints.length === 0 && <Loader />}
        {sortedComplaints.map((option) => {
          return (
            <CheckBox
              key={option.code || option.name}
              onChange={(e) => onAssignmentChange(e, option)}
              checked={isChecked(option)}
              label={`${t(`CS_COMMON_${option.code}`)} ${option.count ? `(${option.count})` : ""}`}
            />
          );
        })}
      </div>
    </div>
  );
};

export default Status;
