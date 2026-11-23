import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";
import {inSelectionOrder} from "../../utilities/GeographyUtils";

const DistrictSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, stateIdentifier, boundaryData, selectedOptions = [], keepSelectionOrder = false } = props;
  // Alphabetical by code unless the caller needs the order the user picked them in.
  const ordered = (list) => (keepSelectionOrder ? list : list.sort((a, b) => a.code?.localeCompare(b.code)));
  const [selectedState, setSelectedState] = useState(data[stateIdentifier]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistricts, setSelectedDistricts] = useState(
    ordered([...(data[name] || [])])
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
          name: `Boundary_${district.code}`,
        }));
      setDistrictMenu(newDistrictMenu);

      const newSelectedDistricts = selectedDistricts.filter((district) => district.stateCode === selectedStateCode);
      setSelectedDistricts(ordered(newSelectedDistricts));
    }
  }, [t, boundaryData, selectedOptions, selectedState])

  const handleDistrictSelection = (districts = []) => {
    const selectedDistrictCodes = districts.map((district) => district.code);
    const selectedOptionsExcludedInSelection = selectedOptions.filter((option) => !selectedDistrictCodes.includes(option.code));
    const newSelectedDistricts = [...districts, ...selectedOptionsExcludedInSelection];
    setSelectedDistricts(keepSelectionOrder ? inSelectionOrder(selectedDistricts, newSelectedDistricts) : ordered(newSelectedDistricts));
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
