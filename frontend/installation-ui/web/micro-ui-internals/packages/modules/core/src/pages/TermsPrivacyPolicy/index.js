import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { HeaderComponent, Loader } from "@egovernments/digit-ui-components";
import PolicyDocumentContent from "../../components/PolicyDocumentContent";
import usePolicyDocument from "../../hooks/usePolicyDocument";

const POLICY_CONFIG = {
  privacy: {
    masterName: "PrivacyPolicy",
    fallbackTitle: "ES_PRIVACY_POLICY",
  },
  terms: {
    masterName: "TermsOfUse",
    fallbackTitle: "ES_TERMS_OF_USE",
  },
};

const TermsPrivacyPolicy = ({ stateCode, type = "privacy", module = "HCM" }) => {
  const { t } = useTranslation();
  const history = useHistory();
  const tenantId = Digit.ULBService.getCurrentTenantId() || Digit.ULBService.getStateId() || stateCode;
  const moduleName = "commonUiConfig";
  const policyConfig = POLICY_CONFIG[type] || POLICY_CONFIG.privacy;

  const { data: documentData, isLoading } = usePolicyDocument({ type, module, moduleName, tenantId });

  if (isLoading) {
    return <Loader page={true} variant="PageLoader" />;
  }

  return (
    <div style={{ minHeight: "100vh", background: "#f4f5f7", padding: "2rem 1rem" }}>
      <div style={{ maxWidth: "960px", margin: "0 auto 10px", color: "#9e1b32", display: "flex", justifyContent: "end", paddingRight: "15px" }}>
        <div onClick={() => history.goBack()} style={{ width: "fit-content", cursor: "pointer" }}>
          {t("PP_COMMON_BACK")}
        </div>
      </div>
      <div style={{ maxWidth: "960px", margin: "0 auto", background: "#ffffff", border: "1px solid #d6d6d6" }}>
        <div style={{ padding: "1.5rem", borderBottom: "1px solid #e0e0e0" }}>
          <HeaderComponent styles={{ margin: 0, fontSize: "2rem", lineHeight: "2.5rem", fontWeight: 700 }}>
            {t(documentData?.header || policyConfig.fallbackTitle)}
          </HeaderComponent>
        </div>
        <div style={{ padding: "1.5rem" }}>
          <PolicyDocumentContent documentData={documentData} />
        </div>
      </div>
    </div>
  );
};

export default TermsPrivacyPolicy;
