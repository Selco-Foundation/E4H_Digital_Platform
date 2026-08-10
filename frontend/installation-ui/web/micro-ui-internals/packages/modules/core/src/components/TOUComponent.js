import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {Button, CheckBox, PopUp} from "@egovernments/digit-ui-components";
import { CONSENT_COOKIE_KEYS, getConsentCookie } from "../utilities/consentCookies";
import PolicyDocumentContent from "./PolicyDocumentContent";
import usePolicyDocument from "../hooks/usePolicyDocument";

const TOUComponent = ({ onSelect, formData, control, formState, ...props }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const hasStoredConsent = getConsentCookie(CONSENT_COOKIE_KEYS.terms);
  const [isChecked, setIsChecked] = useState(hasStoredConsent);
  const [showPopUp, setShowPopUp] = useState(false);
  const moduleName=Digit.Utils.getConfigModuleName();

  const { data: privacy } = usePolicyDocument({ type: "terms", module: props?.props?.module, moduleName, tenantId });
  const handleCheckboxChange = (event) => {
    if (hasStoredConsent) {
      return;
    }
    setIsChecked(event.target.checked);
  };
  useEffect(() => {
    onSelect("touCheck", isChecked);
  }, [isChecked]);
  const onButtonClick = () => {
    setShowPopUp(true);
  };

  if (hasStoredConsent) {
    return null;
  }

  return (
    <React.Fragment>
      <div className="digit-privacy-checkbox digit-privacy-checkbox-align" style={{ alignItems: "center", display: "flex" }}>
        <div style={{ flexShrink: 0, height: "24px", overflow: "hidden", position: "relative", width: "24px" }}>
          <div style={{ left: 0, position: "absolute", top: "75%", transform: "translateY(-50%) scale(0.63)", transformOrigin: "left center" }}>
            <CheckBox
              label=""
              checked={isChecked}
              onChange={handleCheckboxChange}
              id={"terms-of-use-component-check"}
              disabled={hasStoredConsent}
              disable={hasStoredConsent}
            ></CheckBox>
          </div>
        </div>
        <label htmlFor="terms-of-use-component-check" style={{ cursor: hasStoredConsent ? "default" : "pointer", lineHeight: "24px", margin: "0 0 0 0" }}>
          {t("ES_BY_CLICKING")}
        </label>
        <button
          type="button"
          onClick={onButtonClick}
          style={{
            background: "transparent",
            border: 0,
            color: "#d4351c",
            cursor: "pointer",
            font: "inherit",
            lineHeight: "24px",
            margin: 0,
            padding: "0 0 0 0.2rem",
            textDecoration: "underline",
          }}
        >
          {t("ES_TERMS_OF_USE")}
        </button>
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

export default TOUComponent;
