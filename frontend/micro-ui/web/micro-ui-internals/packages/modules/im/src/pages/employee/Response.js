import React from "react";
import { Card, Banner, CardText, SubmitBar } from "@selco/digit-ui-react-components";
import { Link, useRouteMatch } from "react-router-dom";
import { useSelector } from "react-redux";
import { PgrRoutes, getRoute } from "../../constants/Routes";
import { useTranslation } from "react-i18next";

const GetActionMessage = ({ action }) => {
  const { t } = useTranslation();
  if (action === "REOPEN") {
    return t(`CS_COMMON_COMPLAINT_REOPENED`);
  } else {
    return t(`CS_COMMON_COMPLAINT_SUBMITTED`);
  }
};

const BannerPicker = ({ response }) => {
  console.log("response", response)
    const { complaints } = response;

  if (complaints && complaints.response && complaints.response.responseInfo) {
    return (
      <Banner
        message={GetActionMessage(complaints.response.IncidentWrappers[0].workflow)}
        complaintNumber={complaints.response.IncidentWrappers[0].incident.incidentId}
        successful={true}
      />
    );
  } else {
    return <Banner message={t("CS_COMMON_COMPLAINT_NOT_SUBMITTED")} successful={false} />;
  }
};

const Response = (props) => {
  const { t } = useTranslation();
  const { match } = useRouteMatch();
  const appState = useSelector((state) => state)["pgr"];
  sessionStorage.removeItem("complaintType");
sessionStorage.removeItem("subType");
  return (
    <Card>
      {appState.complaints.response && <BannerPicker response={appState} />}
      <CardText>{t("ES_COMMON_TRACK_COMPLAINT_TEXT")}</CardText>
      <Link to={`/${window.contextPath}/employee`}>
        <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
      </Link>
    </Card>
  );
};

export default Response;
