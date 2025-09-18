import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";

const ActivitySelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, activityData } = props;
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

  const handleActivitySelection = (activities) => {
    setSelectedActivities(activities);
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={activityMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleActivitySelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={selectedActivities.length ? `${selectedActivities.length} Selected` : ""}
        selected={selectedActivities}
        addSelectAllCheck={true}
        selectAllLabel={t("PM_ACTION_SELECT_ALL_ACTIVITIES")}
      />
    </div>
  );
};

export default ActivitySelector;
