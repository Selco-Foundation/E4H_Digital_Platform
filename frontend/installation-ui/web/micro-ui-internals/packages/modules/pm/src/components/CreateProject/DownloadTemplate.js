import React from "react";
import { DownloadIcon } from "@egovernments/digit-ui-react-components";

const DownloadTemplate = ({ props }) => {

  const { t, heading, description } = props;

  const handleDownload = () => {
    alert("Downloading template...");
  };

  return (
    <div
      style={{
        fontFamily: "Roboto",
        backgroundColor: "#fff",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "30px",
        }}
      >
        <h2 style={{ margin: 0, fontSize: "32px", fontWeight: "700" }}>
          {t(heading)}
        </h2>
        <button
          style={{
            backgroundColor: "white",
            border: "1px solid #C84C0E",
            padding: "8px 20px",
            cursor: "pointer",
            fontWeight: "bold",
            fontSize: "16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "5px",
            height: "40px"
          }}
          type="button"
          onClick={handleDownload}
        >
          <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
            <DownloadIcon fill={"#C84C0E"} />
          </div>
          <span style={{ color: "#C84C0E", fontFamily: "Roboto", fontWeight: "600" }}>{t("PM_ACTION_DOWNLOAD_TEMPLATE")}</span>
        </button>
      </div>
      <p
        style={{
          margin: 0,
          fontSize: "14px",
          lineHeight: "1.4",
        }}
      >
        {t(description)}
      </p>
    </div>
  );
};

export default DownloadTemplate;
