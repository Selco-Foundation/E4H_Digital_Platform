import React from "react";
import CustomCloseSvg from "./Custom/CustomCloseSvg";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";

const UnsavedDataAlert = ({ t, alert, setAlert }) => {
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
          {t("PM_ALERT_LOSE_UNSAVED_DATA")}
        </p>
        <div style={{display: "flex", justifyContent: "space-around"}}>
          <Button variation={"secondary"} label={t("CORE_COMMON_CANCEL")} onButtonClick={() => setAlert(null)} />
          <Button
            variation={"primary"}
            label={t("CORE_COMMON_CONTINUE")}
            onButtonClick={() => {
              alert.continueAction();
              setAlert(null);
            }}
          />
        </div>
      </div>
    </PopUp>
  )
}

export default UnsavedDataAlert;