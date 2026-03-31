import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const StateSelector = ({ data = {}, setValue, props }) => {
  const { t, name, countryIdentifier, boundaryData, disable } = props;
  const [selectedCountry, setSelectedCountry] = useState(data[countryIdentifier]);
  const [stateMenu, setStateMenu] = useState([]);
  const [selectedState, setSelectedState] = useState(data[name]);

  useEffect(() => {
    setSelectedCountry(data[countryIdentifier]);
  }, [data, countryIdentifier]);

  useEffect(() => {
    if (boundaryData?.states && selectedCountry?.code) {
      const newStateMenu = boundaryData.states
        .filter((state) => state.parentCode === selectedCountry.code)
        .map((state) => ({
          ...state,
          name: t(`Boundary_${state.code}`),
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));
      setStateMenu(newStateMenu);

      if (selectedState && selectedState.parentCode !== selectedCountry.code) {
        setSelectedState(null);
      }
    } else {
      setStateMenu([]);
      setSelectedState(null);
    }
  }, [t, boundaryData, selectedCountry, selectedState]);

  useEffect(() => {
    setValue(name, selectedState);
  }, [name, selectedState, setValue]);

  const handleStateSelection = (state) => {
    setSelectedState(state);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable || !selectedCountry}
        t={t}
        option={stateMenu}
        selected={selectedState}
        select={handleStateSelection}
        optionKey={"name"}
        optionsCardStyle={{
          zIndex: 10000000,
          maxHeight: "400px",
        }}
      />
    </div>
  );
};

export default StateSelector;


