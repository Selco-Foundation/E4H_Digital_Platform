import React from "react";
import PropTypes from "prop-types";

const ButtonSelector = (props) => {
  let theme = "selector-button-primary";
  const isMobile = window.Digit.Utils.browser.isMobile();
  const language=JSON.parse(sessionStorage.getItem("Digit.locale"))?.value
  switch (props.theme) {
    case "border":
      theme = "selector-button-border";
      break;
    default:
      theme = "selector-button-primary";
      break;
  }
  return (
    <button
      className={props.isDisabled ? "selector-button-primary-disabled" : theme}
      type={props.type || "submit"}
      form={props.formId}
      onClick={props.onSubmit}
      disabled={props.isDisabled}
      style={props.style ? {...props.style,height:"3rem", paddingLeft:isMobile ? "10px":"24px", paddingRight:isMobile ? "10px":"24px"} : null}
    >
      <h2 style={{ ...props?.textStyles, ...{ width: "100%",margin:"auto" } }}>{props.label}</h2>
    </button>
  );
};

ButtonSelector.propTypes = {
  /**
   * ButtonSelector content
   */
  label: PropTypes.string.isRequired,
  /**
   * button border theme
   */
  theme: PropTypes.string,
  /**
   * click handler
   */
  onSubmit: PropTypes.func,
};

ButtonSelector.defaultProps = {
  label: "",
  theme: "",
  onSubmit: undefined,
};

export default ButtonSelector;
