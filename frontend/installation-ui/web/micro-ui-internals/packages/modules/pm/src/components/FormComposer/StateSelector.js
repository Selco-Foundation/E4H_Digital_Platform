import React, {useEffect, useMemo, useState} from "react";
import {Dropdown} from "@egovernments/digit-ui-react-components";

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
          name: t(`STATE_${state.code.toUpperCase()}`),
        }))
      );
    }
  }, [t, boundaryData]);

  const handleStateSelection = (state) => {
    setSelectedState(state);
  }

  return (
    <div className={"employee-select-wrap"}>
      <Dropdown
        disable={disable}
        t={t}
        option={stateMenu}
        optionKey={"name"}
        select={handleStateSelection}
        selected={selectedState}
      />
    </div>
  );
};

export default StateSelector;
