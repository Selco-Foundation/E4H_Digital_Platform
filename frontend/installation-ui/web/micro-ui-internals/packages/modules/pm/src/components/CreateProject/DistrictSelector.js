import React, {useEffect, useMemo, useState} from "react";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";
import dateRange from "@egovernments/digit-ui-module-dss/src/components/DateRange";
import _ from "lodash";

const DistrictSelector = ({
  setValue,
  props,
}) => {

  const { t, name, stateIdentifier, boundaryData, defaultValues = {} } = props;
  const [selectedState, setSelectedState] = useState(defaultValues[stateIdentifier] || null);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistricts, setSelectedDistricts] = useState([]);

  useEffect(() => {
    setSelectedState(defaultValues[stateIdentifier]);
  }, [defaultValues[stateIdentifier]]);

  useEffect(() => {
    if (defaultValues[name] && !_.isEqual(_.sortBy(defaultValues[name], "code"), _.sortBy(selectedDistricts, "code"))) {
      setSelectedDistricts(defaultValues[name]);
    }
  }, [defaultValues[name]])

  useEffect(() => {
    if (selectedDistricts?.length) {
      setValue(name, selectedDistricts);
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
