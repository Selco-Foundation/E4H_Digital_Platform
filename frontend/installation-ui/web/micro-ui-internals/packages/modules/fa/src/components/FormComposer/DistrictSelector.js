import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const DistrictSelector = ({ data = {}, setValue, props }) => {
  const { t, name, stateIdentifier, boundaryData, disable } = props;
  const [selectedState, setSelectedState] = useState(data[stateIdentifier]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [selectedDistrict, setSelectedDistrict] = useState(data[name]);

  useEffect(() => {
    setSelectedState(data[stateIdentifier]);
  }, [data, stateIdentifier]);

  useEffect(() => {
    if (boundaryData?.districts && selectedState?.code) {
      const newDistrictMenu = boundaryData.districts
        .filter((district) => district.parentCode === selectedState.code)
        .map((district) => ({
          ...district,
          name: t(`Boundary_${district.code}`),
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));
      setDistrictMenu(newDistrictMenu);

      if (selectedDistrict && selectedDistrict.parentCode !== selectedState.code) {
        setSelectedDistrict(null);
      }
    } else {
      setDistrictMenu([]);
      setSelectedDistrict(null);
    }
  }, [t, boundaryData, selectedState, selectedDistrict]);

  useEffect(() => {
    setValue(name, selectedDistrict);
  }, [name, selectedDistrict, setValue]);

  const handleDistrictSelection = (district) => {
    setSelectedDistrict(district);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable || !selectedState}
        t={t}
        option={districtMenu}
        selected={selectedDistrict}
        select={handleDistrictSelection}
        optionKey={"name"}
        optionsCardStyle={{
          zIndex: 10000000,
          maxHeight: "400px",
        }}
      />
    </div>
  );
};

export default DistrictSelector;


