import React, {useEffect, useMemo, useState} from "react";
import useBoundary from "../../hooks/useBoundary";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";
import _ from "lodash";

const StateSelector = ({
  setValue,
  props,
}) => {

  const { t, name, boundaryData, defaultValues = {} } = props;
  const [stateMenu, setStateMenu] = useState([]);
  const [selectedState, setSelectedState] = useState(defaultValues[name]);

  useEffect(() => {
    if (defaultValues[name] &&  !_.isEqual(defaultValues[name], selectedState)) {
      setSelectedState(defaultValues[name]);
    }
  }, [name, defaultValues[name]]);

  useEffect(() => {
    setValue(name, selectedState);
  }, [name, selectedState]);

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
