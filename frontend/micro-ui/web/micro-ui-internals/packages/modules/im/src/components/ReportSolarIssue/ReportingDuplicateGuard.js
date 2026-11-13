import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { CardText, Loader, SubmitBar } from "@selco/digit-ui-react-components";
import ReportingActions from "./ReportingActions";
import useReportingDuplicates from "../../hooks/useReportingDuplicates";

const ReportingDuplicateGuard = ({ facility, component, enabled = true, parentUrl, onBack, children }) => {
  const { t, i18n } = useTranslation();
  const history = useHistory();
  const query = useReportingDuplicates(facility, component, enabled);
  if (!enabled) return children;
  if (query.isLoading || query.isFetching) return <Loader />;
  if (query.isError || !query.data) return <div role="alert">
    <CardText>{t("IM_REPORT_DUPLICATE_CHECK_ERROR", { defaultValue: "We could not check for existing tickets. Please try again." })}</CardText>
    <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: "24px" }}>
      <SubmitBar label={t("IM_REPORT_RETRY", { defaultValue: "Try again" })} onSubmit={() => query.refetch()} />
      <SubmitBar label={t("CORE_COMMON_BACK", { defaultValue: "Back" })} onSubmit={onBack} />
    </div>
  </div>;
  if (!query.data.applicable || !query.data.duplicates.length) return children;
  return <section aria-label={t("IM_REPORT_EXISTING_TICKETS", { defaultValue: "Existing tickets" })}>
    <h2>{t("IM_REPORT_DUPLICATE_FOUND", { defaultValue: "A ticket for this issue is already in progress" })}</h2>
    {query.data.duplicates.map((ticket) => (
      <div key={ticket.incidentId} style={{ paddingTop: "16px" }}>
        <CardText>{t("CS_COMMON_TICKET_NO", { defaultValue: "Ticket number" })}: {ticket.incidentId}</CardText>
        <CardText>{t("IM_REPORT_DATE_RAISED", { defaultValue: "Date raised" })}: {new Date(ticket.createdTime).toLocaleDateString(i18n.language?.replace("_", "-") || "en-IN")}</CardText>
        <CardText>{t("CS_TICKET_DETAILS_CURRENT_STATUS", { defaultValue: "Current status" })}: {t(`CS_COMMON_${ticket.applicationStatus}`, { defaultValue: ticket.applicationStatus })}</CardText>
        <SubmitBar label={t("IM_REPORT_VIEW_TICKET", { defaultValue: "View ticket" })}
          onSubmit={() => history.push(`${parentUrl}/complaint/details/${encodeURIComponent(ticket.incidentId)}/${encodeURIComponent(query.tenantId)}`)} />
      </div>
    ))}
    <ReportingActions back={<SubmitBar style={{ marginLeft: "auto", marginRight: 0, flex: "0 1 auto" }} className="im-report-back" label={t("CORE_COMMON_BACK", { defaultValue: "Back" })} onSubmit={onBack} />} />
  </section>;
};

export default ReportingDuplicateGuard;
