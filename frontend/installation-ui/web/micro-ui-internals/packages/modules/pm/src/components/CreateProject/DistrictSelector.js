import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-react-components";

const DistrictSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, stateIdentifier, boundaryData } = props;
  const [selectedState, setSelectedState] = useState(data[stateIdentifier]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistricts, setSelectedDistricts] = useState(data[name] || []);

  useEffect(() => {
    setSelectedState(data[stateIdentifier]);
  }, [data[stateIdentifier]]);

  useEffect(() => {
    if (selectedDistricts.filter((block) => block.code !== "ALL_DISTRICTS").length) {
      setValue(name, selectedDistricts.filter((block) => block.code !== "ALL_DISTRICTS"));
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedDistricts]);

  useEffect(() => {
    if (boundaryData && selectedState) {
      const selectedStateCode = selectedState?.code;

      const newDistrictMenu = boundaryData.districts
        .filter((district) => district.stateCode === selectedStateCode)
        .map((district) => ({
          ...district,
          name: `DISTRICT_${district.code.toUpperCase()}`,
        }));
      setDistrictMenu([
        {
          code: "ALL_DISTRICTS",
          name: "PM_ACTION_SELECT_ALL_DISTRICTS",
        },
        ...newDistrictMenu
      ]);

      const newSelectedDistricts = selectedDistricts.filter((district) => district.stateCode === selectedStateCode);
      setSelectedDistricts(newSelectedDistricts);
    }
  }, [t, boundaryData, selectedState])

  const handleDistrictSelection = (districts) => {
    const selectedDistrictCodes = districts.map((district) => district.code);
    if (selectedDistrictCodes.includes("ALL_DISTRICTS")) {
      setSelectedDistricts(districtMenu);
    } else {
      setSelectedDistricts(districts);
    }
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={districtMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleDistrictSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={`${selectedDistricts.filter((block) => block.code !== "ALL_DISTRICTS").length || ""}`}
        selected={selectedDistricts}
      />
    </div>
  );
};

export default DistrictSelector;
