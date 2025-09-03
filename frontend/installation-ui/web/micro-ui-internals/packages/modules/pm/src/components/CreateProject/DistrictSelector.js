import React, {useEffect, useMemo, useState} from "react";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";

const DistrictSelector = ({
  data = {},
  setValue,
  props,
  setError,
  clearErrors,
}) => {

  const [selectedState, setSelectedState] = useState(data.state || null);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistricts, setSelectedDistricts] = useState(data.districts || []);
  const { t, name, boundaryData } = props;

  useEffect(() => {
    setSelectedState(data.state || null);
  }, [data.state]);

  useEffect(() => {
    if (selectedDistricts?.length) {
      setValue(name, selectedDistricts);
    } else {
      setValue(name, undefined);
    }
  }, [selectedDistricts, setError, clearErrors, name]);

  useEffect(() => {
    if (boundaryData && selectedState) {
      const selectedStateCode = selectedState?.code;

      const newDistrictMenu = boundaryData.districts
        .filter((district) => district.stateCode === selectedStateCode)
        .map((district) => ({
          ...district,
          name: t(`DISTRICT_${district.code.toUpperCase()}`),
        }));
      setDistrictMenu(newDistrictMenu);

      const newSelectedDistricts = selectedDistricts.filter((district) => district.stateCode === selectedStateCode);
      setSelectedDistricts(newSelectedDistricts);
    }
  }, [t, boundaryData, selectedState])

  const handleDistrictSelection = (districts) => {
    setSelectedDistricts(districts);
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={districtMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleDistrictSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        selected={selectedDistricts}
      />
    </div>
  );
};

export default DistrictSelector;
