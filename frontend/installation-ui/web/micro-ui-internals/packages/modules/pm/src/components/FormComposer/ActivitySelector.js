import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-react-components";
import _ from "lodash";

const ActivitySelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, activityData } = props;
  const [activityMenu, setActivityMenu] = useState([]);
  const [selectedActivities, setSelectedActivities] = useState(data[name] || []);

  useEffect(() => {
    if (selectedActivities.filter((activity) => activity.code !== "ALL_ACTIVITIES").length) {
      setValue(name, selectedActivities.filter((activity) => activity.code !== "ALL_ACTIVITIES"));
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedActivities]);

  useEffect(() => {
    if (activityData ) {
      setActivityMenu(
        activityData ? [
          {
            code: "ALL_ACTIVITIES",
            localeName: t(`PM_ACTION_SELECT_ALL_ACTIVITIES`),
          },
          ...activityData?.map(activity => ({
            ...activity,
            localeName: t(`PM_CREATE_FIELD_PLAN_ACTIVITY_TYPE_${activity.code}`),
          }))
        ] : []
      );
    }
  }, [t, activityData])

  const handleActivitySelection = (activities) => {
    const currentActivitySelection = activities.map(activity => activity.code).sort((a, b) => a.code?.localeCompare(b.code));
    const previousActivitySelection = selectedActivities.map((activity) => activity.code).sort((a, b) => a.code?.localeCompare(b.code));

    if (!_.isEqual(currentActivitySelection, previousActivitySelection)) {
      const currentSelectionSelectAll = currentActivitySelection.includes("ALL_ACTIVITIES");
      const previousSelectionSelectAll = previousActivitySelection.includes("ALL_ACTIVITIES");

      if (previousSelectionSelectAll) {
        if (currentSelectionSelectAll) {
          setSelectedActivities(activities.filter((activity) => activity.code !== "ALL_ACTIVITIES"));
        } else if (activities.length === activityMenu.length - 1) {
          setSelectedActivities([]);
        } else {
          setSelectedActivities(activities);
        }
      } else if (currentSelectionSelectAll || activities.length === activityMenu.length - 1) {
        setSelectedActivities(activityMenu);
      } else {
        setSelectedActivities(activities);
      }
    }
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={activityMenu}
        optionsKey={"localeName"}
        onSelect={(e) => {
          handleActivitySelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={`${selectedActivities.filter((activity) => activity.code !== "ALL_ACTIVITIES").length || ""}`}
        selected={selectedActivities}
      />
    </div>
  );
};

export default ActivitySelector;
