import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CheckBox, PopUp, Button } from "@egovernments/digit-ui-components";

const getDocumentByModule = (documents, module) => {
  return Array.isArray(documents) ? documents.find((document) => document?.module === module) : null;
};

const PrivacyComponent = ({ onSelect, formData, control, formState, ...props }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [isChecked, setIsChecked] = useState(false);
  const [showPopUp, setShowPopUp] = useState(false);
  const moduleName = Digit.Utils.getConfigModuleName();

  const { data: policyDocuments } = Digit.Hooks.useCustomMDMS(tenantId, moduleName, [{ name: "PrivacyPolicy" }, { name: "TermsOfUse" }], {
    select: (data) => {
      return {
        PrivacyPolicy: getDocumentByModule(data?.[moduleName]?.PrivacyPolicy, props?.props?.module),
        TermsOfUse: getDocumentByModule(data?.[moduleName]?.TermsOfUse, props?.props?.module),
      };
    },
  });
  const privacy = policyDocuments?.PrivacyPolicy;
  const privacyContents = Array.isArray(privacy?.contents) ? privacy.contents : [];
  const hasPrivacyContents = privacyContents.length > 0;

  const handleCheckboxChange = (event) => {
    setIsChecked(event.target.checked);
  };

  useEffect(() => {
    onSelect("check", isChecked);
  }, [isChecked]);

  const onButtonClick = () => {
    setShowPopUp(true);
  };

  const handleScrollToElement = (id) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <React.Fragment>
      <div className="digit-privacy-checkbox digit-privacy-checkbox-align">
        <CheckBox label={t("ES_BY_CLICKING")} checked={isChecked} onChange={handleCheckboxChange} id={"privacy-component-check"}></CheckBox>
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
          heading={privacy?.header ? t(privacy?.header) : t("ES_PRIVACY_POLICY")}
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
                setIsChecked(true), setShowPopUp(false);
              }}
            />,
          ]}
          sortFooterChildren={true}
          onClose={() => {
            setShowPopUp(false);
          }}
        >
          {hasPrivacyContents ? (
            <React.Fragment>
              <div>
                <div className="privacy-table">{t("DIGIT_TABLE_OF_CONTENTS")}</div>
                <ul>
                  {privacyContents.map((content, index) => (
                    <li key={index} style={{ display: "flex", alignItems: "center" }}>
                      <span style={{ marginRight: "0.5rem" }}>{index + 1}. </span>
                      <Button
                        label={t(content?.header)}
                        variation={"link"}
                        size={"medium"}
                        onClick={(e) => {
                          e.preventDefault();
                          handleScrollToElement(content?.header);
                        }}
                        style={{ justifyContent: "flex-start" }}
                      ></Button>
                    </li>
                  ))}
                </ul>
              </div>
              {privacyContents.map((content, index) => (
                <div key={index} id={content?.header}>
                  <div
                    style={{
                      fontWeight: "bold",
                      paddingLeft: content?.isSpaceRequired ? "1rem" : "0",
                    }}
                  >
                    {t(content?.header)}
                  </div>
                  {(Array.isArray(content?.descriptions) ? content.descriptions : []).map((description, subIndex) => (
                    <div key={subIndex} style={{ paddingLeft: description?.isSpaceRequired ? "1rem" : "0", marginBottom: "0.5rem" }}>
                      <div
                        style={{
                          fontWeight: description?.isBold ? 700 : 400,
                          display: "flex",
                          alignItems: "center",
                        }}
                      >
                        {description?.type === "points" && <span style={{ marginRight: "0.5rem", listStyleType: "disc" }}>&#8226;</span>}
                        {description?.type === "step" && <span style={{ marginRight: "0.5rem", listStyleType: "decimal" }}>{subIndex + 1}. </span>}
                        {t(description?.text)}
                      </div>
                      {Array.isArray(description?.subDescriptions) && description.subDescriptions.length > 0 && (
                        <div className="policy-subdescription">
                          {description.subDescriptions.map((subDesc, subSubIndex) => (
                            <div key={subSubIndex} className="policy-subdescription-points">
                              {subDesc?.type === "points" && (
                                <span style={{ marginRight: "0.5rem", listStyleType: "disc", paddingLeft: "1rem" }}>&#8226;</span>
                              )}
                              {subDesc?.type === "step" && (
                                <span style={{ marginRight: "0.5rem", listStyleType: "decimal", paddingLeft: "1rem" }}>{subSubIndex + 1}. </span>
                              )}
                              {subDesc?.type === null && <span style={{ marginRight: "0.5rem", paddingLeft: "1rem" }}> </span>}
                              {t(subDesc?.text)}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ))}
            </React.Fragment>
          ) : (
            <div>{t("ES_COMMON_NO_DATA")}</div>
          )}
        </PopUp>
      )}
    </React.Fragment>
  );
};

export default PrivacyComponent;
