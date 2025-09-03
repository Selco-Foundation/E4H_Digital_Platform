import React, {useEffect, useMemo, useState} from "react";
import useBoundary from "../../hooks/useBoundary";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";

const StateSelector = ({
  data = {},
  setValue,
  props,
  setError,
  clearErrors,
}) => {

  const [stateMenu, setStateMenu] = useState([]);
  const [selectedState, setSelectedState] = useState(data.state);
  const { t, name, boundaryData } = props;

  useEffect(() => {
    setValue(name, selectedState);
  }, [selectedState, setError, clearErrors, name]);

  useEffect(() => {
    if (boundaryData) {
      setStateMenu(
        boundaryData.states.map((state) => ({
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
