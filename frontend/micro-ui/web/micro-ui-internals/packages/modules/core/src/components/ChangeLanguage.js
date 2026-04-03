import { CustomButton, Dropdown } from "@selco/digit-ui-react-components";
import React, { useState } from "react";
import {useSelector} from "react-redux";

const ChangeLanguage = (prop) => {
  const isDropdown = prop.dropdown || false;
  const languages = useSelector((state) => state.common.languages) || [];
  const selectedLanguage = Digit.StoreData.getCurrentLanguage();
  const [selected, setselected] = useState(selectedLanguage);
  const stateCode = window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID")

  const handleChangeLanguage = (language) => {
    try {
      Digit.Utils.analytics?.trackButtonClick("language_change_after_login", {
        page_path: window.location?.pathname || "/employee",
        page_title: "Post-Login Page",
        selected_language: language.value,
      });
    } catch (e) {
      console.warn("analytics: language change after login failed", e);
    }
    setselected(language.value);
    Digit.LocalizationService.changeLanguage(language.value, stateCode);
  };

  if (isDropdown) {
    return (
      <div>
        <Dropdown
          option={languages}
          selected={languages.find((language) => language.value === selectedLanguage)}
          optionKey={"label"}
          select={handleChangeLanguage}
          freeze={true}
          customSelector={<label className="cp">{languages.find((language) => language.value === selected)?.label}</label>}
        />
      </div>
    );
  } else {
    return (
      <React.Fragment>
        <div style={{ marginBottom: "5px" }}>Language</div>
        <div className="language-selector">
          {languages.map((language, index) => (
            <div className="language-button-container" key={index}>
              <CustomButton
                selected={language.value === selected}
                text={language.label}
                onClick={() => handleChangeLanguage(language)}
              ></CustomButton>
            </div>
          ))}
        </div>
      </React.Fragment>
    );
  }
};

export default ChangeLanguage;
