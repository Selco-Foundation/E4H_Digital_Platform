import React, { useEffect } from "react";

import PopUp from "../atoms/PopUp";
import HeaderBar from "../atoms/HeaderBar";
import ButtonSelector from "../atoms/ButtonSelector";
import Toast from "../atoms/Toast";

const Modal = ({
  headerBarMain,
  headerBarEnd,
  popupStyles,
  children,
  actionCancelLabel,
  actionCancelOnSubmit,
  actionSaveLabel,
  actionSaveOnSubmit,
  error,
  setError,
  formId,
  isDisabled,
  hideSubmit,
  style = {},
  popupModuleMianStyles,
  headerBarMainStyle,
  isOBPSFlow = false,
  popupModuleActionBarStyles = {},
  popmoduleClassName = "",
  popUpContainerClassName = "",
  popupModuleActionBarClass = "",
  popupMainModuleClass = "",
  customTheme = "",
  actionSingleLabel,
  actionSingleSubmit,
}) => {
  /**
   * TODO: It needs to be done from the desgin changes
   */
  const mobileView = Digit.Utils.browser.isMobile() ? true : false;
  useEffect(() => {
    document.body.style.overflowY = "hidden";
    return () => {
      document.body.style.overflowY = "auto";
    };
  }, []);
  const styles = {
    ...style,
    backgroundColor: "#7a2829",
  };

  const popUpstyles = {
    ...popupStyles,
    width: "460px",
  };
  const isMobile = window.Digit.Utils.browser.isMobile();
  return (
    <PopUp className={popUpContainerClassName}>
      <div className={`popup-module ${popmoduleClassName}`} style={mobileView ? { ...popUpstyles, width: "300px" } : popUpstyles}>
        <HeaderBar main={headerBarMain} end={headerBarEnd} style={headerBarMainStyle ? headerBarMainStyle : {}} />
        <div className={`popup-module-main ${popupMainModuleClass}`} style={popupModuleMianStyles ? popupModuleMianStyles : {}}>
          {children}
          <div
            className={`popup-module-action-bar ${popupModuleActionBarClass}`}
            style={
              isOBPSFlow
                ? !mobileView
                  ? { marginRight: "18px" }
                  : { position: "absolute", bottom: "5%", right: "10%", left: window.location.href.includes("employee") ? "0%" : "7%" }
                : { ...popupModuleActionBarStyles, gap: "10px" }
            }
          >
            {actionCancelLabel ? (
              <ButtonSelector
                textStyles={{ margin: "0px", color: "white" }}
                theme="border"
                label={actionCancelLabel}
                onSubmit={actionCancelOnSubmit}
                style={isMobile ? { ...styles, height: "3rem" } : styles}
              />
            ) : null}
            {!hideSubmit ? (
              <ButtonSelector
                textStyles={{ margin: "0px" }}
                label={actionSaveLabel}
                onSubmit={actionSaveOnSubmit}
                formId={formId}
                isDisabled={isDisabled}
                style={isMobile ? { ...styles, height: "3rem" } : styles}
              />
            ) : null}
          </div>
        </div>
      </div>
      {error && <Toast label={error} onClose={() => setError(null)} error />}
    </PopUp>
  );
};

export default Modal;
