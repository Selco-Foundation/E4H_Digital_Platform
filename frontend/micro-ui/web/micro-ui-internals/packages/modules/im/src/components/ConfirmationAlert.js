import React from "react";
import CustomCloseSvg from "./custom/CustomCloseSvg";
import { Button, PopUp } from "@selco/digit-ui-react-components";

const ConfirmationAlert = ({ t, alert, setAlert }) => {
  if (!alert) return null;
  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "400px",
          maxWidth: "95%",
          padding: "24px",
          borderRadius: "5px",
        }}
      >
        <div
          style={{
            width: "100%",
            position: "relative",
          }}
        >
          <button
            type={"button"}
            style={{
              cursor: "pointer",
              position: "absolute",
              top: "-15px",
              right: "-15px",
              backgroundColor: "#D6D5D4",
              display: "flex",
              alignItems: "center",
              padding: "0",
              borderRadius: "3px",
            }}
            onClick={() => setAlert(null)}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>
        <h2
          style={{
            margin: "0 0 16px 0",
            fontSize: "20px",
            fontWeight: "600",
            color: "#333",
            textAlign: "center",
          }}
        >
          {t("CORE_COMMON_ALERT")}
        </h2>

        <p
          style={{
            fontSize: "16px",
            color: "#555",
            marginBottom: "24px",
            textAlign: "center",
          }}
        >
          {alert.message || t("CORE_COMMON_COFIRMATION_ALERT")}
        </p>
        <div style={{ display: "flex", justifyContent: "space-around" }}>
          <Button variation={"secondary"} label={t("TL_COMMON_NO")} onButtonClick={() => setAlert(null)} />
          <Button
            variation={"primary"}
            label={t("TL_COMMON_YES")}
            onButtonClick={() => {
              alert.continueAction();
              setAlert(null);
            }}
          />
        </div>
      </div>
    </PopUp>
  );
}

export default ConfirmationAlert;