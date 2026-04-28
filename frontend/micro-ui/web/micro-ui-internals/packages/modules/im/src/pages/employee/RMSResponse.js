import React from "react";
import { Card, Banner, CardText, SubmitBar } from "@selco/digit-ui-react-components";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

const BannerPicker = ({ response }) => {
  const { t } = useTranslation();

  if (response.success) {
    return (
      <Banner
        message={response.message}
        complaintNumber={response.facilityId}
        successful={true}
      />
    );
  } else {
    return <Banner message={t("CS_COMMON_COMPLAINT_NOT_SUBMITTED")} successful={false} />;
  }
};

const RMSResponse = () => {
  const { t } = useTranslation();
  const appState = useSelector((state) => state)["pgr"];

  return (
    <Card>
      {appState?.rms?.response && <BannerPicker response={appState?.rms?.response} />}
      <CardText>{appState?.rms?.response?.cardText}</CardText>
      <Link to={`/${window.contextPath}/employee`}>
        <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
      </Link>
    </Card>
  );
};

export default RMSResponse;
