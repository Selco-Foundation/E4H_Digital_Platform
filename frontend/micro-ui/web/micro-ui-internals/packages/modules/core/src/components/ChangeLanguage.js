import { CustomButton, Dropdown } from "@selco/digit-ui-react-components";
import React, { useState } from "react";

const ChangeLanguage = (prop) => {
  const isDropdown = prop.dropdown || false;
  const { data: storeData, isLoading } = Digit.Hooks.useStore.getInitData();
  const { languages, stateInfo } = storeData || {};
  const selectedLanguage = Digit.StoreData.getCurrentLanguage();
  const [selected, setselected] = useState(selectedLanguage);
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
    Digit.LocalizationService.changeLanguage(language.value, stateInfo.code);
  };

  if (isLoading) return null;

  if (isDropdown) {
    return (
      <div>
        <Dropdown
          option={languages}
          selected={languages.find((language) => language.value === selectedLanguage)}
          optionKey={"label"}
          select={handleChangeLanguage}
          freeze={true}
          customSelector={<label className="cp">{languages.find((language) => language.value === selected).label}</label>}
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
