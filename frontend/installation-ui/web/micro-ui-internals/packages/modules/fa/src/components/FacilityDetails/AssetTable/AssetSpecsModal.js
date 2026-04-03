import React from "react";
import { PopUp, Button } from "@egovernments/digit-ui-react-components";

const AssetSpecsModal = ({ t, assetSpecs, onClose }) => {

  const InfoItem = ({ title, value }) => (
    <div
      style={{
        display: "flex",
        marginBottom: "10px",
        gap: "15px",
      }}
    >
      <div
        style={{
          fontWeight: "bold",
          width: "50%",
        }}
      >
        {title}
      </div>
      <div>{value || t("CORE_COMMON_NOT_APPLICABLE")}</div>
    </div>
  );

  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "500px",
          maxWidth: "95%",
          maxHeight: "90vh",
          overflowY: "auto",
          borderRadius: "5px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: "20px 30px 0px",
          }}
        >
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "24px",
              color: "#0B0C0C",
            }}
          >
            {t("ASSET_SPECS")}
          </div>
          <Button
            variation="secondary"
            label={t("CORE_COMMON_CLOSE")}
            onButtonClick={onClose}
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
          />
        </div>
        <div style={{ padding: "30px" }}>
          {Object.keys(assetSpecs)?.map((key) => (
            <InfoItem title={t(key)} value={assetSpecs[key]} />
          ))}
        </div>
      </div>
    </PopUp>
  );
};

export default AssetSpecsModal;