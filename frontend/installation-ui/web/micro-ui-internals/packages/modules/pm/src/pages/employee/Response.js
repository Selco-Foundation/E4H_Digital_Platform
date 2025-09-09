import React from "react";
import { Card, Banner, CardText, SubmitBar } from "@selco/digit-ui-react-components";
import { Link, useRouteMatch } from "react-router-dom";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

const GetActionMessage = ({ action }) => {
    const { t } = useTranslation();
    if (action === "REOPEN") {
        return t(`CS_COMMON_COMPLAINT_REOPENED`);
    } else if (action === "RATE") {
        return t(`CS_COMMON_COMPLAINT_RATED`);
    } else {
        return t(`CS_COMMON_COMPLAINT_SUBMITTED`);
    }
};

const GetCardTextMessage = (workflow) => {
    const { t } = useTranslation();

    const action = workflow?.action;

    switch (action) {
        case "RATE":
            return t("ES_COMMON_RATED_COMPLAINT_TEXT");
        default:
            return t("ES_COMMON_TRACK_COMPLAINT_TEXT");
    }
};

const BannerPicker = ({ response }) => {
    const { t } = useTranslation();
    console.log("response", response)

    if (response) {
        return (
            <Banner
                message={GetActionMessage("SUBMITTED")}
                complaintNumber={"1234567890"}
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
    const appState = useSelector((state) => state)["pm"];
    sessionStorage.removeItem("complaintType");
    sessionStorage.removeItem("subType");
    return (
        <Card>
            <BannerPicker response={appState} />
            <CardText>{GetCardTextMessage("SUBMITTED")}</CardText>
            <Link to={`/${window.contextPath}/employee`}>
                <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
            </Link>
        </Card>
    );
};

export default Response;
