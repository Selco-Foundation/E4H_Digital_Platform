import React, { Fragment } from "react";
import { useTranslation } from "react-i18next";
import { CheckSvg } from "@egovernments/digit-ui-react-components";

const CustomCheckBox = ({ onChange, label, value, disable, ref, checked, inputRef, pageType, style, index, isLabelFirst, customLabelMarkup, ...props }) => {
  const { t } = useTranslation();
  const userType = pageType || Digit.SessionStorage.get("userType");
  let wrkflwStyle = props.styles;

  if (isLabelFirst) {
    return (
      <div className={"custom-checkbox-wrap"} style={wrkflwStyle ? wrkflwStyle : {}}>
        <p style={style ? style : null}> {index + 1}.</p>
        <p className={"custom-checkbox-label"}>
          {label}
        </p>
        <div className={"custom-checkbox-inner"}>
          <input
            type="checkbox"
            onChange={onChange}
            value={value || label}
            {...props}
            ref={inputRef}
            disabled={disable}
            checked={checked}
          />
          <p className={"custom-checkbox-svg"}>
            <CheckSvg />
          </p>
        </div>
      </div>
    );
  } else {
    return (
      <div className={"custom-checkbox-wrap"} style={wrkflwStyle ? wrkflwStyle : {}}>
        <div className={"custom-checkbox-inner"}>
          <input
            type="checkbox"
            onChange={onChange}
            value={value || label}
            {...props}
            ref={inputRef}
            disabled={disable}
            checked={checked}
          />
          <p className={"custom-checkbox-svg"}>
            <CheckSvg />
          </p>
        </div>
        <p className={"custom-checkbox-label"}>
          {customLabelMarkup ? (
            <>
              <p>{t("COMMON_CERTIFY_ONE")}</p>
              <br />
              <p>
                <b> {t("ES_COMMON_NOTE")}</b>
                {t("COMMON_CERTIFY_TWO")}
              </p>
            </>
          ) : (
            label
          )}
        </p>
      </div>
    );
  }
};

export default CustomCheckBox;