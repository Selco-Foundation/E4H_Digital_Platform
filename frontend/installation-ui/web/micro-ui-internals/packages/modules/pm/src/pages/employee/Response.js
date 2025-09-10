import React from "react";
import { Card, Banner, SubmitBar } from "@egovernments/digit-ui-react-components";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

const BannerPicker = ({ response, message, complaintNumber }) => {

    if (response) {
        return (
            <Banner
                message={message}
                complaintNumber={complaintNumber}
                successful={true}
                whichSvg="tick"
            />
        );
    } else {
        return <Banner message={message} successful={false} />;
    }
};

const Response = (props) => {
    const { t } = useTranslation();
    const appState = useSelector((state) => state)["pm"];
    return (
        <Card>
            <BannerPicker response={appState} message={appState.message} complaintNumber={appState.complaintNumber}/>
            <div style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "flex-end",
                gap: "16px"
            }}>
                <Link to={`/${window.contextPath}/employee`} style={{ textDecoration: "none" }}>
                    <div style={{
                        backgroundColor: "transparent",
                        color: "#C84C0E",
                        border: "1px solid #C84C0E",
                        boxShadow: "none",
                        fontWeight: "600",
                        padding: "10px 24px",
                        minWidth: "200px",
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        cursor: "pointer",
                        fontSize: "16px",
                        textAlign: "center"
                    }}>
                        {t("CS_COMMON_HOME")}
                    </div>
                </Link>
                <Link to={`/${window.contextPath}/employee`}>
                    <SubmitBar label={t("PM_LABEL_CREATE_FIELD_PLAN")}
                               style={{ boxShadow: "none" }}/>
                </Link>
            </div>
        </Card>
    );
};

export default Response;