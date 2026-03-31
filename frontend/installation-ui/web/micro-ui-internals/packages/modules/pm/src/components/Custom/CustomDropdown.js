import React, { useState, useRef, useEffect } from "react";
import { createPortal } from "react-dom";
import { ArrowDown } from "@egovernments/digit-ui-react-components"

const CustomDropdown = ({
  option = [],
  optionKey = "name",
  selected,
  select,
  style = {},
  placeholder = "",
  disable = false,
  t = (key) => key
}) => {

  const [isOpen, setIsOpen] = useState(false);
  const [position, setPosition] = useState(null);
  const [searchValue, setSearchValue] = useState("");
  const [displayValue, setDisplayValue] = useState("");
  const triggerRef = useRef(null);
  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    const updatePosition = () => {
      if (triggerRef.current) {
        const rect = triggerRef.current.getBoundingClientRect();
        setPosition({
          top: rect.bottom + window.scrollY,
          left: rect.left + window.scrollX,
          width: rect.width
        });
      }
    };

    if (isOpen) {
      updatePosition();
      const scrollHandler = () => updatePosition();
      document.addEventListener("scroll", scrollHandler, true);
      window.addEventListener("resize", scrollHandler);

      return () => {
        document.removeEventListener("scroll", scrollHandler, true);
        window.removeEventListener("resize", scrollHandler);
      };
    }
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setSearchValue("");
    }
    setDisplayValue(selected?.[optionKey] || "");
  }, [selected, optionKey, isOpen]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target) &&
        triggerRef.current &&
        !triggerRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isOpen]);

  const handleSelect = (item) => {
    select(item);
    setIsOpen(false);
    setSearchValue("");
  };

  const filteredOptions = option.filter(item => {
    if (!searchValue) return true;
    return item[optionKey].toLowerCase().includes(searchValue.toLowerCase());
  });

  const handleInputChange = (e) => {
    setSearchValue(e.target.value);
    setDisplayValue(e.target.value);
    if (!isOpen) {
      setIsOpen(true);
    }
  };

  return (
    <div className={"employee-select-wrap"}>
      <div
        className={"select"}
        ref={triggerRef}
        style={{
          ...style
        }}
      >
        <input
          ref={inputRef}
          type="text"
          value={displayValue}
          onChange={handleInputChange}
          onClick={() => setIsOpen(true)}
          placeholder={placeholder}
          style={{
            border: "none",
            outline: "none",
            width: "100%",
            backgroundColor: "transparent",
            padding: "8px",
            fontFamily: "Roboto",
            fontSize: "16px",
            paddingRight: "30px",
            textOverflow: "ellipsis",
          }}
        />
        <ArrowDown
          className="cp"
          onClick={() => setIsOpen(true)}
          disable={disable}
          styles={{
            cursor: "pointer",
          }}
        />
      </div>

      {isOpen && position && createPortal(
        <div
          className={"options-card"}
          ref={dropdownRef}
          style={{
            position: "absolute",
            top: `${position.top}px`,
            left: `${position.left}px`,
            width: `${position.width}px`,
            maxHeight: "200px",
            overflowY: "auto",
            backgroundColor: "white",
            color: "black",
            boxShadow: "0 8px 10px 1px rgba(0, 0, 0, 0.14)",
            zIndex: 10000,
            marginTop: "2px"
          }}
        >
          {filteredOptions.length === 0 ? (
            <div style={{ padding: "12px", color: "black" }}>
              {t("CMN_NOOPTION")}
            </div>
          ) : (
            filteredOptions.map((item, idx) => {
              return (
                <div
                  className={"profile-dropdown--item"}
                  key={idx}
                  onClick={() => handleSelect(item)}
                  style={{
                    color: "black"
                  }}
                >
                  {item?.[optionKey]}
                </div>
              );
            })
          )}
        </div>,
        document.body
      )}
    </div>
  );
};

export default CustomDropdown;