import React from "react";
import CustomCloseSvg from "../CustomCloseSvg";

const AssetImageViewer = ({ t, image, onClose }) => {
  return (
    <div
      className="image-viewer-wrap"
      style={{
        paddingTop: "100px",
        paddingBottom: "100px",
        overflow: "auto",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "flex-start",
        gap: "16px"
      }}
    >
      <CustomCloseSvg onClick={onClose} fill="none" iconFill="white" />
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "stretch",
          gap: "16px",
          width: "100%",
          maxWidth: "700px",
          boxSizing: "border-box"
        }}
      >
        <img src={image?.src} style={{ margin: 0, width: "100%", maxWidth: "100%", height: "auto", display: "block" }} />
        <div
          style={{
            background: "white",
            color: "#0B0C0C",
            padding: "12px 16px",
            borderRadius: "4px",
            boxSizing: "border-box",
            width: "100%"
          }}
        >
          <div style={{ display: "grid", gridTemplateColumns: "minmax(0, 1fr) auto", columnGap: "16px", marginBottom: "8px" }}>
            <strong style={{ minWidth: 0, overflowWrap: "break-word" }}>{t("QC_INSTALLATION_ASSET_SERIAL_NUMBER")}</strong>
            <span style={{ whiteSpace: "nowrap", textAlign: "right" }}>{image?.serialNumber || "-"}</span>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "minmax(0, 1fr) auto", columnGap: "16px" }}>
            <strong style={{ minWidth: 0, overflowWrap: "break-word" }}>{t("QC_INSTALLATION_ASSET_CAPACITY")}</strong>
            <span style={{ whiteSpace: "nowrap", textAlign: "right" }}>{image?.capacity || "-"}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AssetImageViewer;
