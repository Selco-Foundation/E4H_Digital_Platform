import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";

const ActivitySelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, activityData, selectedOptions = [] } = props;
  const [activityMenu, setActivityMenu] = useState([]);
  const [selectedActivities, setSelectedActivities] = useState(data[name] || []);

  useEffect(() => {
    if (selectedActivities.length) {
      setValue(name, selectedActivities);
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedActivities]);

  useEffect(() => {
    if (activityData ) {
      setActivityMenu(activityData);
    }
  }, [activityData])

  const handleActivitySelection = (activities = []) => {
    const selectedActivityCodes = activities.map((activity) => activity.code);
    const selectedOptionsExcludedInSelection = selectedOptions.filter((option) => !selectedActivityCodes.includes(option.code));
    const newSelectedActivities = [...activities, ...selectedOptionsExcludedInSelection];
    setSelectedActivities(newSelectedActivities.sort((a, b) => a.code?.localeCompare(b.code)));
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={activityMenu}
        isSearchable={true}
        optionsKey={"name"}
        onSelect={() => {
          // Triggering state update here causes render issues since dropdown within is remains open
        }}
        onClose={(e) => {
          handleActivitySelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={selectedActivities.length ? `${selectedActivities.length} Selected` : ""}
        selected={selectedActivities}
        addSelectAllCheck={true}
        frozenData={[...selectedOptions]}
        selectAllLabel={t("PM_ACTION_SELECT_ALL_ACTIVITIES")}
      />
    </div>
  );
};

export default ActivitySelector;
