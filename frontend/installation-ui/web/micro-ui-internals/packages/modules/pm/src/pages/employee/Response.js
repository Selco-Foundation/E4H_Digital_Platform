import React from "react";
import { Card, Banner, CardText, SubmitBar } from "@egovernments/digit-ui-react-components";
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
        return t(`PM_LABEL_PROJECT_CREATED`);
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
                whichSvg="tick"
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
            <div style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "flex-end",
                gap: "16px"
            }}>
                <Link to={`/${window.contextPath}/employee`} style={{ textDecoration: "none" }}>
                    <SubmitBar label={t("CS_COMMON_HOME")}
                               style={{
                                   backgroundColor: "transparent",
                                   color: "#C84C0E" ,
                                   border: "1px solid #C84C0E",
                                   boxShadow: "none",
                                   fontWeight: "600"
                               }} />
                </Link>
                <Link to={`/${window.contextPath}/employee`}>
                    <SubmitBar label={t("PM_LABEL_CREATE_FIELD_PLAN")} />
                </Link>
            </div>
        </Card>
    );
};

export default Response;