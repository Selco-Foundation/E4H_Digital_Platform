import React from "react";
import { PopUp, Button, Toast, Loader } from "@egovernments/digit-ui-react-components";
import OrganizationForm from "./OrganizationForm";

const OrganizationModal = ({
                             t,
                             heading,
                             onClose,
                             onSubmit,
                             formToast,
                             setFormToast,
                             isLoading,
                             orgType,
                           }) => {
  const handleClose = () => {
    if (isLoading) return;
    if (typeof onClose === "function") return onClose();
  };

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
          borderRadius: "5px",
          zIndex: 10000002,
        }}
      >
        <div style={{ position: "relative" }}>
          {/* Loader overlay (keep existing behavior) */}
          {isLoading ? (
            <div
              style={{
                position: "absolute",
                inset: 0,
                background: "rgba(255,255,255,0.75)",
                zIndex: 50,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                borderRadius: "5px",
              }}
            >
              <Loader />
            </div>
          ) : null}

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
              {heading || t("ADD_ORGANIZATION")}
            </div>

            <Button
              variation="secondary"
              label={t("CORE_COMMON_CLOSE") || "Close"}
              onButtonClick={handleClose}
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
                minWidth: "120px",
              }}
            />
          </div>

          <div style={{ padding: "0 30px 30px 30px" }}>
            {/* Keep formToast inside modal (mostly for errors) */}
            {formToast ? (
              <Toast
                error={formToast.key === "error"}
                warning={formToast.key === "warning"}
                label={formToast.label}
                isDleteBtn={true}
                style={{
                  zIndex: 100000000,
                  ...(formToast.key === "error" ? { backgroundColor: "#B91900" } : {}),
                }}
                onClose={() => (typeof setFormToast === "function" ? setFormToast(null) : null)}
              />
            ) : null}

            <OrganizationForm t={t} onSubmit={onSubmit} orgType={orgType} />
          </div>
        </div>
      </div>
    </PopUp>
  );
};

export default OrganizationModal;