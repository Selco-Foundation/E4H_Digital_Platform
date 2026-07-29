import React, { useEffect, useState } from "react";
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
  const getIsMobile = () => (typeof window !== "undefined" ? window.innerWidth <= 768 : false);
  const [isMobile, setIsMobile] = useState(getIsMobile);

  useEffect(() => {
    const handleResize = () => setIsMobile(getIsMobile());
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const modalStyle = {
    background: "#ffffff",
    borderRadius: "4px",
    display: "flex",
    flexDirection: "column",
    left: "50%",
    maxHeight: isMobile ? "calc(100vh - 48px)" : "calc(100vh - 64px)",
    maxWidth: "960px",
    position: "fixed",
    top: "50%",
    transform: "translate(-50%, -50%)",
    width: isMobile ? "calc(100% - 32px)" : "min(50vw, 960px)",
    zIndex: 10001,
  };

  const headerStyle = {
    alignItems: "center",
    borderBottom: "1px solid #e0e0e0",
    display: "flex",
    flexShrink: 0,
    gap: "1rem",
    padding: isMobile ? "1rem" : "1.5rem",
  };

  const titleStyle = {
    flex: 1,
    fontSize: isMobile ? "1.5rem" : "2rem",
    fontWeight: 700,
    lineHeight: isMobile ? "1.875rem" : "2.5rem",
    margin: 0,
  };

  const footerStyle = {
    borderTop: "1px solid #e0e0e0",
    display: "flex",
    flexDirection: isMobile ? "column" : "row",
    flexShrink: 0,
    gap: "1rem",
    justifyContent: "flex-end",
    padding: isMobile ? "1rem" : "1rem 1.5rem",
  };

  const buttonStyle = {
    cursor: "pointer",
    fontSize: "1rem",
    fontWeight: 700,
    minWidth: isMobile ? 0 : "180px",
    padding: "0.75rem 1rem",
    width: isMobile ? "100%" : "auto",
  };

  return (
    <PopUp className="privacy-popUpClass">
      <div className="policy-consent-modal" style={modalStyle}>
        <div className="policy-consent-modal-header" style={headerStyle}>
          <h1 className="policy-consent-modal-title" style={titleStyle}>
            {t((documentData && documentData.header) || policyConfig.fallbackTitle)}
          </h1>
          <button
            type="button"
            onClick={onClose}
            aria-label={t("CS_COMMON_CLOSE")}
            className="policy-consent-modal-close"
            style={{
              background: "transparent",
              border: "none",
              cursor: "pointer",
              flexShrink: 0,
              fontSize: "2rem",
              lineHeight: 1,
              padding: 0,
            }}
          >
            ×
          </button>
        </div>
        <div className="policy-consent-modal-content" style={{ flex: 1, overflowY: "auto", padding: isMobile ? "1rem" : "1.5rem" }}>
          {isLoading ? <Loader /> : <PolicyDocumentContent documentData={documentData} />}
        </div>
        <div className="policy-consent-modal-footer" style={footerStyle}>
          <button
            type="button"
            onClick={onReject}
            className="policy-consent-modal-button policy-consent-modal-button-secondary"
            style={{
              ...buttonStyle,
              background: "#ffffff",
              border: "1px solid #d4351c",
              color: "#d4351c",
            }}
          >
            {t("DIGIT_I_DO_NOT_ACCEPT")}
          </button>
          <button
            type="button"
            onClick={onAccept}
            className="policy-consent-modal-button policy-consent-modal-button-primary"
            style={{
              ...buttonStyle,
              background: "#d4351c",
              border: "1px solid #d4351c",
              color: "#ffffff",
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
