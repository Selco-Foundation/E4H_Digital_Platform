import React from "react";
import { TextInput } from "@egovernments/digit-ui-react-components";
import CustomDropdown from "../Custom/CustomDropdown";
import ToggleLink from "./ToggleLink";

const safeOnSelect = (onSelect, key, value) => {
  if (typeof onSelect === "function") onSelect(key, value);
};

const getCode = (val) => {
  if (!val) return "";
  if (typeof val === "string") return val;
  if (typeof val === "object" && val.code) return val.code;
  return "";
};

const StateToggleField = (props) => {
  const onSelect = props?.onSelect;
  const formData = props?.formData || {};
  const customProps = props?.customProps || props?.config?.customProps || {};

  const t = customProps?.t || props?.t;
  const boundaryData = customProps?.boundaryData;
  const isTextMode = !!customProps?.isTextMode;
  const setIsTextMode = customProps?.setIsTextMode;
  const isMobile = !!customProps?.mobileView;

  const stateValue = getCode(formData?.state);

  const boundaryLabel = (code) => t(`Boundary_${code}`);
  const states = (boundaryData && boundaryData.states) || [];
  const stateOptions = (states || [])
    .map((s) => ({
      code: s.code,
      name: boundaryLabel(s.code),
    }))
    .sort((a, b) => a?.name?.localeCompare(b?.name));

  const selectedState = stateOptions.find((o) => o.code === stateValue) || null;

  const clearBelow = () => {
    safeOnSelect(onSelect, "district", "");
    safeOnSelect(onSelect, "block", "");
  };

  const clearAll = () => {
    safeOnSelect(onSelect, "state", "");
    safeOnSelect(onSelect, "district", "");
    safeOnSelect(onSelect, "block", "");
  };

  const toggleMode = () => {
    const next = !isTextMode;
    clearAll();
    if (typeof setIsTextMode === "function") setIsTextMode(next);
  };

  const onStateDropdownSelect = (opt) => {
    safeOnSelect(onSelect, "state", (opt && opt.code) || "");
    clearBelow();
  };

  const onStateTextChange = (e) => {
    const next = (e && e.target && e.target.value) || "";
    safeOnSelect(onSelect, "state", next);
    if (next !== stateValue) clearBelow();
  };

  return (
    <div style={{ display: "flex" }}>
      <div style={{ minWidth: isMobile ? "90%" : "100%" }}>
        {isTextMode ? (
          <TextInput value={stateValue} onChange={onStateTextChange} style={{ width: "100%" }} />
        ) : (
          <CustomDropdown
            t={t}
            option={stateOptions}
            optionKey={"name"}
            selected={selectedState}
            select={onStateDropdownSelect}
            style={{ width: "100%" }}
            optionsCardStyle={{
              zIndex: 10000000,
              maxHeight: "150px",
            }}
          />
        )}
      </div>

      <div style={{ padding: "5px" }}>
        <ToggleLink label={""} onClick={toggleMode} />
      </div>
    </div>
  );
};

export default StateToggleField;