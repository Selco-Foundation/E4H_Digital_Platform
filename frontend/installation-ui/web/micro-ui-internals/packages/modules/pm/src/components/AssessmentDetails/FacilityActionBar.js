import React from "react";
import { Button, DownloadIcon } from "@egovernments/digit-ui-react-components";

const FacilityActionBar = ({ t, selectedFacilityIds, bulkActions, onDownload }) => {

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "10px",
        minWidth: "fit-content",
      }}
    >
      {selectedFacilityIds?.length > 0 && bulkActions?.length ? (
        <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
          {bulkActions.map((action) => (
            <Button
              key={action.key}
              variation={"secondary"}
              label={action.label}
              isDisabled={action.disabled}
              onButtonClick={action.onClick}
              style={{
                border: "none",
                padding: "8px 20px",
                cursor: action.disabled ? "default" : "pointer",
                fontWeight: "bold",
                fontSize: "16px",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                height: "40px",
                color: "white",
                backgroundColor: action.disabled ? "#D6D5D4" : action.backgroundColor,
              }}
            />
          ))}
        </div>
      ) : (
        <div style={{ fontSize: "20px", fontWeight: "bold", fontFamily: "Roboto Condensed", color: "#0B0C0C" }}>
          {t("PM_ASSESSMENT_FACILITIES_TITLE")}
        </div>
      )}
      <button
        type="button"
        onClick={onDownload}
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
          height: "40px"
        }}
      >
        <span>{t("CORE_COMMON_DOWNLOAD")}</span>
        <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
          <DownloadIcon fill={"#d35400"} />
        </div>
      </button>
    </div>
  );
};

export default FacilityActionBar;
