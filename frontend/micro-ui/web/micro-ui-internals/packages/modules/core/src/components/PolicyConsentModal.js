import React from "react";
import { Loader, PopUp } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import PolicyDocumentContent from "./PolicyDocumentContent";
import usePolicyDocument from "../hooks/usePolicyDocument";

const POLICY_CONFIG = {
  privacy: {
    fallbackTitle: "Privacy Policy",
  },
  terms: {
    fallbackTitle: "Terms of Use",
  },
};

const PolicyConsentModal = ({ type = "privacy", module = "E4H", tenantId, onClose, onAccept, onReject }) => {
  const { t } = useTranslation();
  const policyConfig = POLICY_CONFIG[type] || POLICY_CONFIG.privacy;
  const moduleName = Digit.Utils.getConfigModuleName && Digit.Utils.getConfigModuleName();
  const { data: documentData, isLoading } = usePolicyDocument({ type, module, moduleName, tenantId });

  return (
    <PopUp className="privacy-popUpClass">
      <div
        style={{
          background: "#ffffff",
          borderRadius: "4px",
          display: "flex",
          flexDirection: "column",
          left: "50%",
          maxHeight: "74vh",
          maxWidth: "960px",
          position: "fixed",
          top: "50%",
          transform: "translate(-50%, -50%)",
          width: "50vw",
          zIndex: 10001,
        }}
      >
        <div style={{ alignItems: "center", borderBottom: "1px solid #e0e0e0", display: "flex", padding: "1.5rem" }}>
          <h1 style={{ flex: 1, fontSize: "2rem", fontWeight: 700, lineHeight: "2.5rem", margin: 0 }}>
            {t((documentData && documentData.header) || policyConfig.fallbackTitle)}
          </h1>
          <button
            type="button"
            onClick={onClose}
            aria-label={t("CS_COMMON_CLOSE")}
            style={{ background: "transparent", border: "none", cursor: "pointer", fontSize: "2rem", lineHeight: 1 }}
          >
            ×
          </button>
        </div>
        <div style={{ flex: 1, overflowY: "auto", padding: "1.5rem" }}>
          {isLoading ? <Loader /> : <PolicyDocumentContent documentData={documentData} />}
        </div>
        <div style={{ borderTop: "1px solid #e0e0e0", display: "flex", gap: "1rem", justifyContent: "flex-end", padding: "1rem 1.5rem" }}>
          <button
            type="button"
            onClick={onReject}
            style={{
              background: "#ffffff",
              border: "1px solid #d4351c",
              color: "#d4351c",
              cursor: "pointer",
              fontSize: "1rem",
              fontWeight: 700,
              minWidth: "180px",
              padding: "0.75rem 1rem",
            }}
          >
            {t("DIGIT_I_DO_NOT_ACCEPT")}
          </button>
          <button
            type="button"
            onClick={onAccept}
            style={{
              background: "#d4351c",
              border: "1px solid #d4351c",
              color: "#ffffff",
              cursor: "pointer",
              fontSize: "1rem",
              fontWeight: 700,
              minWidth: "180px",
              padding: "0.75rem 1rem",
            }}
          >
            {t("DIGIT_I_ACCEPT")}
          </button>
        </div>
      </div>
    </PopUp>
  );
};

export default PolicyConsentModal;
