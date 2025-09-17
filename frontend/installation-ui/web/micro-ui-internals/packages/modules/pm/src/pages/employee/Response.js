import React from "react";
import { Card, Banner, SubmitBar } from "@egovernments/digit-ui-react-components";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { useTranslation } from "react-i18next";

const BannerPicker = ({ response, message, complaintNumber, info }) => {

    if (response) {
        return (
            <Banner
                message={message}
                complaintNumber={complaintNumber}
                successful={true}
                info={info}
                whichSvg="tick"
            />
        );
    } else {
        return <Banner message={message} successful={false} />;
    }
};

const Response = () => {
    const { t } = useTranslation();
    const responseData = useSelector((state) => state.pm.common.responseData);

    return (
        <Card>
            {responseData ? (
                <BannerPicker
                  response={responseData.response}
                  message={responseData.message}
                  complaintNumber={responseData.createdId}
                  info={responseData.info}
                />
            ) : (
                <span>{t("CORE_COMMON_RESPONSE_PAGE_INFO")}</span>
            )}
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
                {responseData?.secondaryRedirectionLabel && responseData?.onSecondaryRedirection && (
                    <SubmitBar
                      label={responseData.secondaryRedirectionLabel}
                      style={{ boxShadow: "none" }}
                      onSubmit={() => responseData.onSecondaryRedirection()}
                    />
                )}
            </div>
        </Card>
    );
};

export default Response;