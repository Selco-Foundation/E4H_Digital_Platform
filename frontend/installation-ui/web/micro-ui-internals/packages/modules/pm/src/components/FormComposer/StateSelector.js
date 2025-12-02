import React, {useEffect, useMemo, useState} from "react";
import {Dropdown, CustomDropdown} from "@egovernments/digit-ui-react-components";

const StateSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, boundaryData, disable } = props;
  const [stateMenu, setStateMenu] = useState([]);
  const [selectedState, setSelectedState] = useState(data[name]);

  useEffect(() => {
    setValue(name, selectedState);
  }, [name, selectedState]);

  useEffect(() => {
    if (boundaryData) {
      setStateMenu(
        boundaryData.states?.map((state) => ({
          ...state,
          name: t(`Boundary_${state.code}`),
        }))
      );
    }
  }, [t, boundaryData]);

  const handleStateSelection = (state) => {
    setSelectedState(state);
  }

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable}
        t={t}
        onChange={handleStateSelection}
        value={selectedState}
        config={{
          name: "state",
          options: stateMenu,
          optionsKey: "name",
        }}
      />
    </div>
  );
};

export default StateSelector;
