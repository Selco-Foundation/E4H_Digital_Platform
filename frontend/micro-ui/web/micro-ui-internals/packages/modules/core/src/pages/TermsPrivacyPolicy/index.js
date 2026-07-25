import React from "react";
import { Loader } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import PolicyDocumentContent from "../../components/PolicyDocumentContent";
import usePolicyDocument from "../../hooks/usePolicyDocument";

const POLICY_CONFIG = {
  privacy: {
    fallbackTitle: "Privacy Policy",
  },
  terms: {
    fallbackTitle: "Terms of Use",
  },
};

const TermsPrivacyPolicy = ({ stateCode, type = "privacy", module = "HCM" }) => {
  const { t } = useTranslation();
  const policyConfig = POLICY_CONFIG[type] || POLICY_CONFIG.privacy;
  const { data: documentData, isLoading } = usePolicyDocument({ type, module, moduleName: "commonUiConfig", tenantId: stateCode });

  if (isLoading) {
    return <Loader page={true} />;
  }

  return (
    <div style={{ minHeight: "100vh", background: "#f4f5f7", padding: "2rem 1rem" }}>
      <div style={{ maxWidth: "960px", margin: "0 auto", background: "#ffffff", border: "1px solid #d6d6d6" }}>
        <div style={{ padding: "1.5rem", borderBottom: "1px solid #e0e0e0" }}>
          <h1 style={{ margin: 0, fontSize: "2rem", lineHeight: "2.5rem", fontWeight: 700 }}>
            {t((documentData && documentData.header) || policyConfig.fallbackTitle)}
          </h1>
        </div>
        <div style={{ padding: "1.5rem" }}>
          <PolicyDocumentContent documentData={documentData} />
        </div>
      </div>
    </div>
  );
};

export default TermsPrivacyPolicy;
