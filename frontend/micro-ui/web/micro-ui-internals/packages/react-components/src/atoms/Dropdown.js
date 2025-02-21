import PropTypes from "prop-types";
import React, { useEffect, useRef, useState } from "react";
import { ArrowDown } from "./svgindex";
import { useTranslation } from "react-i18next";

const TextField = (props) => {
  const [value, setValue] = useState(props.selectedVal ? props.selectedVal : "");

  useEffect(() => {
    if (!props.keepNull)
      if (props.selectedVal)
        setValue(props.selectedVal)
      else {
        setValue(""); props.setFilter("")
      }
    else setValue("");
  }, [props.selectedVal, props.forceSet]);

  function inputChange(e) {
    if (props.freeze) return;

    setValue(e.target.value);
    props.setFilter(e.target.value);
  }

  function broadcastToOpen() {
    if (!props.disable) {
      props.dropdownDisplay(true);
    }
  }

  function broadcastToClose() {
    props.dropdownDisplay(false);
  }

  /* Custom function to scroll and select in the dropdowns while using key up and down */
  const keyChange = (e) => {
    if (e.key === "ArrowDown") {
      props.setOptionIndex((state) => (state + 1 === props.addProps.length ? 0 : state + 1));
      if (props.addProps.currentIndex + 1 === props.addProps.length) {
        e?.target?.parentElement?.parentElement?.children?.namedItem("jk-dropdown-unique")?.scrollTo?.(0, 0);
      } else {
        props?.addProps?.currentIndex > 2 && e?.target?.parentElement?.parentElement?.children?.namedItem("jk-dropdown-unique")?.scrollBy?.(0, 45);
      }
      e.preventDefault();
    } else if (e.key === "ArrowUp") {
      props.setOptionIndex((state) => (state !== 0 ? state - 1 : props.addProps.length - 1));
      if (props.addProps.currentIndex === 0) {
        e?.target?.parentElement?.parentElement?.children?.namedItem("jk-dropdown-unique")?.scrollTo?.(100000, 100000);
      } else {
        props?.addProps?.currentIndex > 2 && e?.target?.parentElement?.parentElement?.children?.namedItem("jk-dropdown-unique")?.scrollBy?.(0, -45);
      }
      e.preventDefault();
    } else if (e.key === "Enter") {
      props.addProps.selectOption(props.addProps.currentIndex);
    }
  };

  return (
    <input
      ref={props.inputRef}
      className={`employee-select-wrap--elipses ${props.disable && "disabled"}`}
      type="text"
      value={value}
      onChange={inputChange}
      onClick={props.onClick}
      onFocus={broadcastToOpen}
      onBlur={(e) => {
        broadcastToClose();
        props?.onBlur?.(e);
        if (props.selectedVal !== props.filterVal) {
          setTimeout(() => {
            props.setforceSet((val) => val + 1);
          }, 1000);
        }
      }}
      onKeyDown={keyChange}
      readOnly={props.disable}
      autoFocus={props.autoFocus}
      placeholder={props.placeholder}
      autoComplete={"off"}
      style={{ ...props.style, zIndex: "auto" }}
    />
  );
};

const translateDummy = (text) => {
  return text;
};

const Dropdown = React.forwardRef((props, ref) => {
  const user_type = Digit.SessionStorage.get("userType");
  const [dropdownStatus, setDropdownStatus] = useState(false);
  const [selectedOption, setSelectedOption] = useState(props.selected ? props.selected : null);
  const [filterVal, setFilterVal] = useState("");
  const [forceSet, setforceSet] = useState(0);
  const [optionIndex, setOptionIndex] = useState(-1);
  const [touched, setTouched] = useState(false); // New state for touched
  const [error, setError] = useState(""); // New state for error message
  const optionRef = useRef(null);
  const hasCustomSelector = props.customSelector ? true : false;
  const {t} = useTranslation()

  useEffect(() => {
    setSelectedOption(props.selected);
  }, [props.selected]);

  useEffect(() => {
    // Expose validation and error methods through ref
    if (ref) {
      ref.current = {
        validate,
        getError,
        clearError
      };
    }
  }, []);

  // Method to validate the dropdown
  const validate = () => {
    console.log("selectedOption",selectedOption,!selectedOption,props,props.onBlur)
    setTouched(true); // Set touched to true on blur
    if (props.required && (!selectedOption || Object.keys(props.selected).length == 0) ){
      setError(t("CS_FIELD_REQUIRED"));
    } else {
      setError("");
    }
  };
const clearError = ()=>{
return setError("")
}
  // Method to get the error message
  const getError = () => {
    return error;
  };

  function dropdownSwitch() {
    if (!props.disable) {
      var current = dropdownStatus;
      if (!current) {
        document.addEventListener("mousedown", handleClick, false);
      }
      setDropdownStatus(!current);
      props?.onBlur?.();
    }
  }

  function handleClick(e) {
    if (!optionRef.current || !optionRef.current.contains(e.target)) {
      document.removeEventListener("mousedown", handleClick, false);
      setDropdownStatus(false);
    }
  }

  function dropdownOn(val) {
    const waitForOptions = () => setTimeout(() => setDropdownStatus(val), 500);
    const timerId = waitForOptions();
    return () => {
      clearTimeout(timerId);
    };
  }

  function onSelect(val) {
    setTouched(true); // Set touched to true when an option is selected
    setSelectedOption(val);
    setDropdownStatus(false);
    setFilterVal("")
    if (val !== selectedOption || props.allowMultiselect) {
      props.select(val);
      setError(""); // Clear error on valid selection
    } else {
      setforceSet(forceSet + 1);
    }
  }

  function setFilter(val) {
    setFilterVal(val);
  }

  let filteredOption =
    (props?.option && props?.option?.filter((option) => t(option?.[props.optionKey])?.toUpperCase()?.indexOf(filterVal?.toUpperCase()) > -1)) || [];
  function selectOption(ind) {
    onSelect(filteredOption[ind]);
  }

  if (props.isBPAREG && selectedOption) {
    let isSelectedSameAsOptions = props.option?.filter((ob) => ob?.code === selectedOption?.code)?.length > 0;
    if (!isSelectedSameAsOptions) setSelectedOption(null)
  }
  const isMobile = window.Digit.Utils.browser.isMobile();
  return (
    <div
      className={`${user_type === "employee" ? "employee-select-wrap" : "select-wrap"} ${props?.className ? props?.className : ""}`}
      style={{ ...props.style }}
    >
      {hasCustomSelector && (
        <div className={props.showArrow ? "cp flex-right column-gap-5" : "cp"} onClick={dropdownSwitch} style={isMobile?{display:"flex", flexDirection:"column"}:{}}>
          {props.customSelector}
          {props.showArrow && <ArrowDown onClick={dropdownSwitch} className={props.disable && "disabled"} />}
        </div>
      )}
      {!hasCustomSelector && (
        <div
          className={`${dropdownStatus ? "select-active" : "select"} ${props.disable && "disabled"}`}
          style={props.errorStyle ? { border: "1px solid red" } : {}} >
            <TextField
              autoComplete={props.autoComplete}
              setFilter={setFilter}
              forceSet={forceSet}
              setforceSet={setforceSet}
              setOptionIndex={setOptionIndex}
              keepNull={props.keepNull}
              selectedVal={
                selectedOption
                  ? props.t
                    ? props.isMultiSelectEmp
                      ? `${selectedOption} ${t("BPA_SELECTED_TEXT")}`
                      : props.t(props.optionKey ? selectedOption[props.optionKey] : selectedOption)
                    : props.optionKey
                      ? selectedOption[props.optionKey]
                      : selectedOption
                  : null
              }
              filterVal={filterVal}
              addProps={{ length: filteredOption.length, currentIndex: optionIndex, selectOption: selectOption }}
              dropdownDisplay={dropdownOn}
              handleClick={handleClick}
              disable={props.disable}
              freeze={props.freeze ? true : false}
              autoFocus={props.autoFocus}
              placeholder={props.placeholder}
              onBlur={(e) => {
                props?.onBlur?.(e);
                console.log("e",e.target.value,selectedOption)
                setTouched(true); // Set touched to true on blur
                
                if (props.required && (!selectedOption || Object.keys(selectedOption).length == 0)) {
                  setError(t("CS_FIELD_REQUIRED"));
                } else {
                  setError("");
                }
              }}
              inputRef={props.ref}
            />
            <ArrowDown onClick={dropdownSwitch} className="cp" disable={props.disable} />
          </div>
        )}
        {dropdownStatus ? (
          props.optionKey ? (
            <div
              id="jk-dropdown-unique"
              className={`${hasCustomSelector ? "margin-top-10 display: table" : ""} options-card`}
              style={{ ...props.optionCardStyles }}
              ref={optionRef}
            >
              {filteredOption &&
                filteredOption.map((option, index) => {
                  return (
                    <div
                      className={`cp profile-dropdown--item display: flex `}
                      style={
                        index === optionIndex
                          ? {
                            opacity: 1,
                            backgroundColor: "rgba(238, 238, 238, var(--bg-opacity))",
                          }
                          : {}
                      }
                      key={index}
                      onClick={() => onSelect(option)}
                    >
                      {option.icon && <span className="icon"> {option.icon} </span>}
                      {props.isPropertyAssess ? <div>{props.t ? props.t(option[props.optionKey]) : option[props.optionKey]}</div> :
                        <span> {props.t ? props.t(option[props.optionKey]) : option[props.optionKey]}</span>}
                    </div>
                  );
                })}
              {filteredOption && filteredOption.length === 0 && (
                <div className={`cp profile-dropdown--item display: flex `} key={"-1"} onClick={() => {
  
                }}>
                  {<span> {props.t ? props.t("CMN_NOOPTION") : t("CMN_NOOPTION")}</span>}
                </div>
              )}
            </div>
          ) : (
            <div
              className="options-card"
              style={{ ...props.optionCardStyles, overflow: "scroll", maxHeight: "350px" }}
              id="jk-dropdown-unique"
              ref={optionRef}
            >
              {props.option
                .filter((option) => option?.toUpperCase().indexOf(filterVal?.toUpperCase()) > -1)
                .map((option, index) => {
                  return (
                    <p
                      key={index}
                      style={
                        index === optionIndex
                          ? {
                            opacity: 1,
                            backgroundColor: "rgba(238, 238, 238, var(--bg-opacity))",
                          }
                          : {}
                      }
                      onClick={() => onSelect(option)}
                    >
                      {option}
                    </p>
                  );
                })}
            </div>
          )
        ) : null}
        {error && <div style={{ color: "#7a2829", fontSize:"12px",marginTop:"5px" }}>{error}</div>}
      </div>
    );
  });
  
  Dropdown.propTypes = {
    customSelector: PropTypes.any,
    showArrow: PropTypes.bool,
    selected: PropTypes.any,
    style: PropTypes.object,
    option: PropTypes.array,
    optionKey: PropTypes.any,
    select: PropTypes.any,
    t: PropTypes.func,
    required: PropTypes.bool, // Add this line to define the required prop
  };
  
  Dropdown.defaultProps = {
    customSelector: null,
    showArrow: true,
  };
  
  export default Dropdown;
  
