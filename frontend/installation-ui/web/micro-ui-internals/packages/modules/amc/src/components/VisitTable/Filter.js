import React, { useEffect, useState } from "react";
import RefreshButton from "../RefreshButton";
import CustomCheckBox from "../Custom/CustomCheckBox";
import CustomFilterIcon from "../Custom/CustomFilterIcon";

const Filter = ({ t, onFilterChange, projectQueryFilter, statusesList }) => {

  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || {
    status: [],
  });

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.status.length) {
      facilityFilterQuery.status = currentFilter.status;
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter]);

  const handleStatusChange = (option, checked) => {
    const statusesChanged = [option.code];
    if (checked) {
      setCurrentFilter({
        ...currentFilter,
        status: [...currentFilter.status, ...statusesChanged]
      });
    } else {
      setCurrentFilter({
        ...currentFilter,
        status: currentFilter.status.filter(status => !statusesChanged.includes(status))
      });
    }
  }

  const onClearAll = () => {
    setCurrentFilter({
      status: [],
    });
  }

  const checkStatusFilterPresence = (status) => {
    return currentFilter.status.includes(status);
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
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "18px",
              lineHeight: "114%",
              letterSpacing: "0px",
              color: "#0B0C0C",
              alignItems: "center",
              marginBottom: "30px",
            }}
          >
            {t("CORE_COMMON_STATUS")}
          </div>
          {statusesList?.map((option, index) => {
            return (
              <div>
                <CustomCheckBox
                  key={index}
                  onChange={(e) => {handleStatusChange(option, e.target.checked)}}
                  checked={checkStatusFilterPresence(option.code)}
                  label={option.name}
                />
              </div>
            );
          })}
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;
