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
      <div className="popup-module" style={{ position: "relative" }}>
        {/* ✅ FA style loader overlay */}
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
            }}
          >
            <Loader />
          </div>
        ) : null}

        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "30px 30px 0 30px",
          }}
        >
          <h1 style={{ margin: 0, fontWeight: 700 }}>
            {heading || t("ADD_ORGANIZATION")}
          </h1>

          <Button
            variation="secondary"
            label={t("CORE_COMMON_CLOSE") || "Close"}
            onButtonClick={handleClose}
            style={{ minWidth: "120px", height: "40px" }}
          />
        </div>

        <div style={{ padding: "0 30px 30px 30px" }}>
          {formToast ? (
            <Toast
              error={formToast.key === "error"}
              label={formToast.label}
              onClose={() => (typeof setFormToast === "function" ? setFormToast(null) : null)}
            />
          ) : null}

          <OrganizationForm t={t} onSubmit={onSubmit} orgType={orgType} />
        </div>
      </div>
    </PopUp>
  );
};

export default OrganizationModal;