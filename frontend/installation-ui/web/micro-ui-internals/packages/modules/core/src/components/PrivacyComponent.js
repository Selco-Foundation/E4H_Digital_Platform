import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CheckBox, PopUp, Button } from "@egovernments/digit-ui-components";
import { CONSENT_COOKIE_KEYS, getConsentCookie } from "../utilities/consentCookies";
import PolicyDocumentContent from "./PolicyDocumentContent";
import usePolicyDocument from "../hooks/usePolicyDocument";

const PrivacyComponent = ({ onSelect, formData, control, formState, ...props }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const hasStoredConsent = getConsentCookie(CONSENT_COOKIE_KEYS.privacy);
  const [isChecked, setIsChecked] = useState(hasStoredConsent);
  const [showPopUp, setShowPopUp] = useState(false);
  const moduleName=Digit.Utils.getConfigModuleName();

  const { data: privacy } = usePolicyDocument({ type: "privacy", module: props?.props?.module, moduleName, tenantId });
  const handleCheckboxChange = (event) => {
    if (hasStoredConsent) {
      return;
    }
    setIsChecked(event.target.checked);
  };
  useEffect(() => {
    onSelect("check", isChecked);
  }, [isChecked]);
  const onButtonClick = () => {
    setShowPopUp(true);
  };

  if (hasStoredConsent) {
    return null;
  }

  return (
    <React.Fragment>
      <div className="digit-privacy-checkbox digit-privacy-checkbox-align">
        <CheckBox
          label={t("ES_BY_CLICKING")}
          checked={isChecked}
          onChange={handleCheckboxChange}
          id={"privacy-component-check"}
          disabled={hasStoredConsent}
          disable={hasStoredConsent}
        ></CheckBox>
        <Button
          label={t(`ES_PRIVACY_POLICY`)}
          variation={"link"}
          size={"small"}
          onClick={onButtonClick}
          // isSuffix={true}
          style={{ marginBottom: "1.18rem", paddingLeft: "0.2rem" }}
        ></Button>
      </div>
      {showPopUp && (
        <PopUp
          type={"default"}
          className={"privacy-popUpClass"}
          footerclassName={"popUpFooter"}
          heading={t(privacy?.header)}
          onOverlayClick={() => {
            setShowPopUp(false);
          }}
          footerChildren={[
            <Button
              type={"button"}
              size={"large"}
              variation={"secondary"}
              label={t("DIGIT_I_DO_NOT_ACCEPT")}
              onClick={() => {
                if (hasStoredConsent) {
                  setShowPopUp(false);
                  return;
                }
                setIsChecked(false), setShowPopUp(false);
              }}
            />,
            <Button
              type={"button"}
              size={"large"}
              variation={"primary"}
              label={t("DIGIT_I_ACCEPT")}
              className={"accept-class"}
              onClick={() => {
                if (hasStoredConsent) {
                  setShowPopUp(false);
                  return;
                }
                setIsChecked(true), setShowPopUp(false);
              }}
            />,
          ]}
          sortFooterChildren={true}
          onClose={() => {
            setShowPopUp(false);
          }}
        >
          <PolicyDocumentContent documentData={privacy} />
        </PopUp>
      )}
    </React.Fragment>
  );
};

export default PrivacyComponent;
