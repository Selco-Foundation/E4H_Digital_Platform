import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";

const DistrictSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, stateIdentifier, boundaryData, selectedOptions = [] } = props;
  const [selectedState, setSelectedState] = useState(data[stateIdentifier]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistricts, setSelectedDistricts] = useState(
    data[name]?.sort((a, b) => a.code?.localeCompare(b.code)) || []
  );

  useEffect(() => {
    setSelectedState(data[stateIdentifier]);
  }, [data[stateIdentifier]]);

  useEffect(() => {
    if (selectedDistricts.length) {
      setValue(name, selectedDistricts);
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedDistricts]);

  useEffect(() => {
    if (boundaryData && selectedState) {
      const selectedStateCode = selectedState?.code;

      const newDistrictMenu = boundaryData.districts
        ?.filter((district) => district.stateCode === selectedStateCode)
        .map((district) => ({
          ...district,
          name: `DISTRICT_${district.code.toUpperCase()}`,
        }));
      setDistrictMenu(newDistrictMenu);

      const newSelectedDistricts = selectedDistricts.filter((district) => district.stateCode === selectedStateCode);
      setSelectedDistricts(newSelectedDistricts.sort((a, b) => a.code?.localeCompare(b.code)));
    }
  }, [t, boundaryData, selectedOptions, selectedState])

  const handleDistrictSelection = (districts = []) => {
    const selectedDistrictCodes = districts.map((district) => district.code);
    const selectedOptionsExcludedInSelection = selectedOptions.filter((option) => !selectedDistrictCodes.includes(option.code));
    const newSelectedDistricts = [...districts, ...selectedOptionsExcludedInSelection];
    setSelectedDistricts(newSelectedDistricts.sort((a, b) => a.code?.localeCompare(b.code)));
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={districtMenu}
        optionsKey={"name"}
        isSearchable={true}
        onSelect={() => {
          // Triggering state update here causes render issues since dropdown within is remains open
        }}
        onClose={(e) => {
          handleDistrictSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={selectedDistricts.length ? `${selectedDistricts.length} Selected` :  ""}
        selected={selectedDistricts}
        addSelectAllCheck={true}
        frozenData={[...selectedOptions]}
        selectAllLabel={t("PM_ACTION_SELECT_ALL_DISTRICTS")}
      />
    </div>
  );
};

export default DistrictSelector;
