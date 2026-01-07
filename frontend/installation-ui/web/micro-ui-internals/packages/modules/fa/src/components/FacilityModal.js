import React from "react";
import { PopUp, Button } from "@egovernments/digit-ui-react-components";
import FacilityForm from "./FacilityForm";

const FacilityModal = ({ t, title, onClose, onSubmit, createdFacility }) => {
  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "700px",
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
            {t(title)}
          </div>
          <Button variation="secondary" label={t("CORE_COMMON_CLOSE")} onButtonClick={onClose} />
        </div>
        <FacilityForm t={t} createdFacility={createdFacility} onFormSubmit={onSubmit} />
      </div>
    </PopUp>
  );
};

export default FacilityModal;