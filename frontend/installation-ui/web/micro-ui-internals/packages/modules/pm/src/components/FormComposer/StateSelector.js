import React, {useEffect, useMemo} from "react";
import {CustomDropdown} from "@egovernments/digit-ui-react-components";

const StateSelector = ({
  data = {},
  props,
}) => {

  const { t, name, boundaryData, disable } = props;
  const { value, onChange } = props;
  const stateMenu = useMemo(
    () => boundaryData?.states?.map((state) => ({
      ...state,
      name: t(`Boundary_${state.code}`),
    })) || [],
    [t, boundaryData]
  );
  const selectedState = value || data?.[name];
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
    if (!displayState?.code) {
      return;
    }

    if (value?.code === displayState.code && value?.name === displayState.name) {
      return;
    }

    onChange(displayState);
  }, [displayState, onChange, value?.code, value?.name]);

  const handleStateSelection = (state) => {
    onChange(state);
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
