import React, {useEffect, useMemo, useRef, useState} from "react";
import {CustomDropdown} from "@egovernments/digit-ui-react-components";

const StateSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, boundaryData, disable } = props;
  const [selectedState, setSelectedState] = useState(data?.[name]);
  const hasAppliedInitialValue = useRef(!!data?.[name]);
  const hasUserCleared = useRef(false);
  const stateMenu = useMemo(
    () => boundaryData?.states?.map((state) => ({
      ...state,
      name: t(`Boundary_${state.code}`),
    })) || [],
    [t, boundaryData]
  );
  const displayState = useMemo(() => {
    if (!selectedState?.code) {
      return selectedState;
    }

    return stateMenu.find((option) => option.code === selectedState.code) || {
      ...selectedState,
      name: selectedState.name || t(`Boundary_${selectedState.code}`),
    };
  }, [selectedState, stateMenu, t]);

  useEffect(() => {
    if (hasAppliedInitialValue.current || hasUserCleared.current || selectedState || !data?.[name]) {
      return;
    }

    hasAppliedInitialValue.current = true;
    setSelectedState(data[name]);
  }, [data, name, selectedState]);

  useEffect(() => {
    setValue(name, displayState);
  }, [name, displayState, setValue]);

  const handleStateSelection = (state) => {
    hasUserCleared.current = !state;
    setSelectedState(state);
  }

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable}
        t={t}
        onChange={handleStateSelection}
        value={displayState}
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
