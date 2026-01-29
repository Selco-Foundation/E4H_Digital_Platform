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

const DistrictToggleField = (props) => {
  const onSelect = props?.onSelect;
  const formData = props?.formData || {};
  const customProps = props?.customProps || props?.config?.customProps || {};

  const t = customProps?.t || props?.t;
  const boundaryData = customProps?.boundaryData;

  const isTextMode = !!customProps?.isTextMode;
  const setIsTextMode = customProps?.setIsTextMode;
  const stateIsTextMode = !!customProps?.stateIsTextMode;
  const isMobile = !!customProps?.mobileView;

  const stateValue = getCode(formData?.state);
  const districtValue = getCode(formData?.district);

  const boundaryLabel = (code) => t(`Boundary_${code}`);
  const districts = (boundaryData && boundaryData.districts) || [];
  const districtOptions = (districts || [])
    .filter((d) => !!stateValue && d.stateCode === stateValue)
    .map((d) => ({
      code: d.code,
      name: boundaryLabel(d.code),
    }))
    .sort((a, b) => a?.name?.localeCompare(b?.name));

  const selectedDistrict = districtOptions.find((o) => o.code === districtValue) || null;

  const clearBelow = () => {
    safeOnSelect(onSelect, "block", "");
  };

  const clearSelfAndBelow = () => {
    safeOnSelect(onSelect, "district", "");
    safeOnSelect(onSelect, "block", "");
  };

  const toggleMode = () => {
    const next = !isTextMode;
    clearSelfAndBelow();
    if (typeof setIsTextMode === "function") setIsTextMode(next);
  };

  const onDistrictDropdownSelect = (opt) => {
    safeOnSelect(onSelect, "district", (opt && opt.code) || "");
    clearBelow();
  };

  const onDistrictTextChange = (e) => {
    const next = (e && e.target && e.target.value) || "";
    safeOnSelect(onSelect, "district", next);
    if (next !== districtValue) clearBelow();
  };

  const dropdownDisabled = !stateValue || stateIsTextMode;

  return (
    <div style={{ display: "flex" }}>
      <div
        style={{
          minWidth: isMobile ? "90%" : "100%",
          opacity: !isTextMode && dropdownDisabled ? 0.6 : 1,
          pointerEvents: !isTextMode && dropdownDisabled ? "none" : "auto",
        }}
      >
        {stateIsTextMode || isTextMode ? (
          <TextInput value={districtValue} onChange={onDistrictTextChange} style={{ width: "100%" }} />
        ) : (
          <CustomDropdown
            t={t}
            option={districtOptions}
            optionKey={"name"}
            selected={selectedDistrict}
            select={onDistrictDropdownSelect}
            style={{ width: "100%" }}
            optionsCardStyle={{
              zIndex: 10000000,
              maxHeight: "150px",
            }}
          />
        )}
      </div>

      <div style={{ padding: "5px" }}>
        <ToggleLink disable={stateIsTextMode} label={""} onClick={toggleMode} />
      </div>
    </div>
  );
};

export default DistrictToggleField;