import React, { useEffect, useState } from "react";
import {
  Dropdown,
  RemoveableTag,
  Loader
} from "@egovernments/digit-ui-react-components";
import RefreshButton from "../../RefreshButton";
import CustomFilterIcon from "../../Custom/CustomFilterIcon";
import CustomDropdown from "../../Custom/CustomDropdown";

const Filter = ({ t, onFilterChange, projectQueryFilter }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || {
    activityCode: []
  });

  const { isLoading, data: activityData } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "common-masters",
    [
      {
        name: "Activities",
      },
    ],
    {
      select: (data) => {
        return (data?.["common-masters"]?.["Activities"] || []).filter((activity) => activity.code !== "AMC");
      },
      enabled: !!tenantId,
    }
  );

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.activityCode.length > 0) {
      facilityFilterQuery.activityCode = currentFilter.activityCode.map((activity) => activity.code);
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter]);

  const onRemove = (index, key) => {
    let afterRemove = currentFilter[key].filter((value, i) => i !== index);
    setCurrentFilter({ ...currentFilter, [key]: afterRemove });
  };

  if (isLoading) {
    return <Loader />
  }

  const GetSelectOptions = (label, options, selected = null, select, optionKey, onRemove, key) => {
    selected = selected || { [optionKey]: "", code: "" };

    return (
      <div>
        <div className="filter-label">{label}</div>
        {<CustomDropdown t={t} option={options} selected={selected} select={(value) => select(value, key)} optionKey={optionKey} />}

        <div className="tag-container">
          {currentFilter[key].length > 0 &&
            currentFilter[key].map((value, index) => {
              return <RemoveableTag key={index} text={`${value[optionKey]} ...`} onClick={() => onRemove(index, key)} />;
            })}
        </div>
      </div>
    );
  };

  const handleActivityChange = (selectedActivity) => {
    if (currentFilter.activityCode.every((activityCode) => activityCode.code !== selectedActivity.code)) {
      setCurrentFilter({
        ...currentFilter,
        activityCode: [...currentFilter.activityCode, selectedActivity],
      });
    }
  };

  const onClearAll = () => {
    setCurrentFilter({
      activityCode: [],
    });
  }

  return (
    <React.Fragment>
      <div className="filter" style={{ width: "100%" }}>
        <div className="filter-card" style={{ padding: "20px" }}>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <div
              style={{
                fontFamily: "Roboto",
                fontWeight: 700,
                fontSize: "24px",
                lineHeight: "2rem",
                letterSpacing: "0px",
                color: "#0B0C0C",
                display: "flex",
                gap: "15px",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >
              <CustomFilterIcon fill={"#0B4B66"} />
              {t("CORE_COMMON_FILTER")}
            </div>
            <button
              type="button"
              style={{
                cursor: "pointer",
                border: "1px solid #C84C0E",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontWeight: 500,
                height: "2rem",
                width: "2rem",
                fontSize: "24px",
                background: "transparent"
              }}
              onClick={onClearAll}
            >
              <RefreshButton fill={"#C84C0E"} />
            </button>
          </div>
          <div>
            {
              GetSelectOptions(
                t("CS_ACTIVITY_TYPE"),
                activityData,
                null,
                handleActivityChange,
                "name",
                onRemove,
                "activityCode"
              )
            }
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
