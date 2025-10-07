import React from "react";
import {Card, Banner, SubmitBar} from "@egovernments/digit-ui-react-components";
import {Link} from "react-router-dom";
import {useSelector} from "react-redux";
import {useTranslation} from "react-i18next";

const BannerPicker = ({response, message, complaintNumber, info}) => {

  if (response) {
    return (
      <Banner
        message={message}
        applicationNumber={complaintNumber}
        successful={true}
        info={info}
        whichSvg="tick"
      />
    );
  } else {
    return <Banner message={message} successful={false}/>;
  }
};

const Response = () => {
  const {t} = useTranslation();
  const responseData = useSelector((state) => state.pm.common.responseData);

  return (
    <Card style={{margin: "10px"}}>
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
        gap: "16px",
        marginTop: "10px",
      }}>
        <Link to={`/${window.contextPath}/employee`} style={{textDecoration: "none"}}>
          <div style={{
            backgroundColor: "transparent",
            color: "#C84C0E",
            border: "1px solid #C84C0E",
            boxShadow: "none",
            fontWeight: "500",
            padding: "7px 24px",
            minWidth: "200px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            cursor: "pointer",
            fontSize: "20px",
            textAlign: "center"
          }}>
            {t("CS_COMMON_HOME")}
          </div>
        </Link>
        {responseData?.secondaryRedirectionLabel && responseData?.onSecondaryRedirection && (
          <SubmitBar
            label={responseData.secondaryRedirectionLabel}
            style={{
              boxShadow: "none",
              width: "200px",
            }}
            headerStyle={{
              fontSize: "20px",
              fontWeight: "500",
            }}
            onSubmit={() => responseData.onSecondaryRedirection()}
          />
        )}
      </div>
    </Card>
  );
};

export default Response;