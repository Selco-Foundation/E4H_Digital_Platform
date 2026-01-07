import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const StateSelector = ({ data = {}, setValue, props }) => {
  const { t, name, boundaryData, disable } = props;
  const [stateMenu, setStateMenu] = useState([]);
  const [selectedState, setSelectedState] = useState(data[name]);

  useEffect(() => {
    if (boundaryData?.states) {
      setStateMenu(
        boundaryData.states.map((state) => ({
          ...state,
          name: t(`Boundary_${state.code}`),
        }))
      );
    }
  }, [t, boundaryData]);

  useEffect(() => {
    setValue(name, selectedState);
  }, [name, selectedState, setValue]);

  const handleStateSelection = (state) => {
    setSelectedState(state);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable}
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


