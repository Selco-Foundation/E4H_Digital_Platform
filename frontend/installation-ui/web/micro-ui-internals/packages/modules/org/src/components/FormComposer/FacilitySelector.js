import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const FacilitySelector = ({ data = {}, setValue, props }) => {
  const { t, name, blockIdentifier, boundaryData, disable } = props;
  const [selectedBlock, setSelectedBlock] = useState(data[blockIdentifier]);
  const [facilityMenu, setFacilityMenu] = useState([]);
  const [selectedFacility, setSelectedFacility] = useState(data[name]);

  useEffect(() => {
    setSelectedBlock(data[blockIdentifier]);
  }, [data, blockIdentifier]);

  useEffect(() => {
    if (boundaryData?.facilities && selectedBlock?.code) {
      const newFacilityMenu = boundaryData.facilities
        .filter((facility) => facility.parentCode === selectedBlock.code)
        .map((facility) => ({
          ...facility,
          name: t(`Boundary_${facility.code}`),
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));
      setFacilityMenu(newFacilityMenu);

      if (selectedFacility && selectedFacility.parentCode !== selectedBlock.code) {
        setSelectedFacility(null);
      }
    } else {
      setFacilityMenu([]);
      setSelectedFacility(null);
    }
  }, [t, boundaryData, selectedBlock, selectedFacility]);

  useEffect(() => {
    setValue(name, selectedFacility);
  }, [name, selectedFacility, setValue]);

  const handleFacilitySelection = (facility) => {
    setSelectedFacility(facility);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable || !selectedBlock}
        t={t}
        option={facilityMenu}
        selected={selectedFacility}
        select={handleFacilitySelection}
        optionKey={"name"}
        optionsCardStyle={{
          zIndex: 10000000,
          maxHeight: "400px"
        }}
      />
    </div>
  );
};

export default FacilitySelector;


