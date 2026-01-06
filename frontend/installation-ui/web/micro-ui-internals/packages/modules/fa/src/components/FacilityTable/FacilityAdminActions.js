import React, { useEffect, useState } from "react";
import { DownloadIcon, Toast } from "@egovernments/digit-ui-react-components";
import CustomUploadIcon from "../Custom/CustomUploadIcon";

const FacilityAdminActions = ({ t }) => {

  const [toast, setToast] = useState(null);

  useEffect(()=>{
    if (toast) {
      setTimeout(()=>{
        setToast(null);
      },2500)
    }
  },[toast])

  const handleAddFacility = () => {

  }

  const handleBulkAddTemplateDownload = () => {

  }

  const handleBulkAddUpload = () => {

  }

  return (
    <React.Fragment>
      <div
        style={{
          display: "flex",
          justifyContent: "end",
          alignItems: "center",
          gap: "16px",
          minWidth: "fit-content",
        }}
      >
        <button
          id={"faAddFacilityBtn"}
          style={{
            backgroundColor: "white",
            border: "1px solid #d35400",
            color: "#d35400",
            padding: "8px 20px",
            cursor: "pointer",
            fontWeight: "bold",
            fontSize: "16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "5px",
            height: "40px",
          }}
          onClick={handleAddFacility}
        >
          <span>{t("ADD_FACILITY")}</span>
        </button>
        <button
          id={"faBulkAddTemplateDownloadBtn"}
          style={{
            backgroundColor: "white",
            border: "1px solid #d35400",
            color: "#d35400",
            padding: "8px 20px",
            cursor: "pointer",
            fontWeight: "bold",
            fontSize: "16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "5px",
            height: "40px",
          }}
          onClick={handleBulkAddTemplateDownload}
        >
          <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
            <DownloadIcon fill={"#d35400"} />
          </div>
          <span>{t("BULK_ADD_TEMPLATE")}</span>
        </button>
        <button
          id={"faBulkAddUploadBtn"}
          style={{
            backgroundColor: "white",
            border: "1px solid #d35400",
            color: "#d35400",
            padding: "8px 20px",
            cursor: "pointer",
            fontWeight: "bold",
            fontSize: "16px",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: "5px",
            height: "40px",
          }}
          onClick={handleBulkAddUpload}
        >
          <CustomUploadIcon fill={"#C84C0E"} height={"25"} width={"25"} />
          <span>{t("BULK_ADD")}</span>
        </button>
        {toast && (
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            label={`${toast.message} ${toast.failedCount ? `(${toast.failedCount} ${t("QC_BULK_APPROVE_FAILED_COUNT")})` : ""}`}
            onClose={() => setToast(null)}
            style={{ maxWidth: "670px" }}
            isDleteBtn={true}
          />
        )}
      </div>
    </React.Fragment>
  );
};

export default FacilityAdminActions;
