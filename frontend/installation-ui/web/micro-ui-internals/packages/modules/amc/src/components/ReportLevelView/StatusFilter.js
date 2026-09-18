import React, { useState } from "react";
import RefreshButton from "../RefreshButton";
import CustomCheckBox from "../Custom/CustomCheckBox";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import CustomDropdown from "../Custom/CustomDropdown";

const StatusFilter = ({ t, onFilterChange, onSearchableFilterChange, selectedStatuses, searchableFilters, statusesList, filterOptions }) => {

  const [currentStatuses, setCurrentStatuses] = useState(selectedStatuses || []);
  const [currentSearchableFilters, setCurrentSearchableFilters] = useState(searchableFilters || {});

  const handleStatusChange = (option, checked) => {
    // Notify the parent only after user interaction to avoid duplicate fetches on mount.
    let nextStatuses;
    if (checked) {
      nextStatuses = [...currentStatuses, option.code];
    } else {
      nextStatuses = currentStatuses.filter(status => status !== option.code);
    }
    setCurrentStatuses(nextStatuses);
    // Empty status list means show all AMC reports.
    onFilterChange(nextStatuses);
  }

  const onClearAll = () => {
    // Clear both local UI state and parent query state.
    setCurrentStatuses([]);
    setCurrentSearchableFilters({});
    onFilterChange([]);
    onSearchableFilterChange({});
  }

  const handleSearchableFilterChange = (key, option) => {
    // Keep child dropdown selections valid when a parent location changes.
    const nextFilters = {
      ...currentSearchableFilters,
      [key]: option,
    };

    // State change makes old district and block selections invalid.
    if (key === "state") {
      delete nextFilters.district;
      delete nextFilters.block;
    }

    // District change makes old block selection invalid.
    if (key === "district") {
      delete nextFilters.block;
    }

    const filters = { ...nextFilters };
    setCurrentSearchableFilters(filters);
    // Dropdown filters are applied on report-level list data.
    onSearchableFilterChange(filters);
  }

  const getTranslatedOptions = (options = []) => {
    return options.map((option) => ({
      ...option,
      name: option.type === "boundary" ? t(option.name) : option.name,
    }));
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
              aria-label="Clear all filters"
              title="Clear all filters"
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
              <div key={index}>
                <CustomCheckBox
                  key={index}
                  onChange={(e) => {handleStatusChange(option, e.target.checked)}}
                  checked={currentStatuses.includes(option.code)}
                  label={option.name}
                />
              </div>
            );
          })}
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "18px",
              lineHeight: "114%",
              letterSpacing: "0px",
              color: "#0B0C0C",
              alignItems: "center",
              marginBottom: "20px",
              marginTop: "30px",
            }}
          >
            {t("CS_COMMON_LOCATION")}
          </div>
          {/* Searchable location filters for report-level view. */}
          <CustomDropdown
            t={t}
            option={getTranslatedOptions(filterOptions?.states)}
            selected={currentSearchableFilters.state}
            select={(option) => handleSearchableFilterChange("state", option)}
            placeholder={t("PM_PROJECT_INFO_STATE")}
            style={{ width: "100%", marginBottom: "15px" }}
          />
          <CustomDropdown
            t={t}
            option={getTranslatedOptions(filterOptions?.districts)}
            selected={currentSearchableFilters.district}
            select={(option) => handleSearchableFilterChange("district", option)}
            placeholder={t("CS_DISTRICT")}
            style={{ width: "100%", marginBottom: "15px" }}
          />
          <CustomDropdown
            t={t}
            option={getTranslatedOptions(filterOptions?.blocks)}
            selected={currentSearchableFilters.block}
            select={(option) => handleSearchableFilterChange("block", option)}
            placeholder={t("CS_BLOCK")}
            style={{ width: "100%", marginBottom: "15px" }}
          />
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "18px",
              lineHeight: "114%",
              letterSpacing: "0px",
              color: "#0B0C0C",
              alignItems: "center",
              marginBottom: "20px",
              marginTop: "15px",
            }}
          >
            {t("AMC_ASSIGNED_VENDOR")}
          </div>
          {/* Searchable vendor filter for report-level view. */}
          <CustomDropdown
            t={t}
            option={filterOptions?.vendors || []}
            selected={currentSearchableFilters.vendor}
            select={(option) => handleSearchableFilterChange("vendor", option)}
            placeholder={t("AMC_ASSIGNED_VENDOR")}
            style={{ width: "100%" }}
          />
        </div>
      </div>
    </React.Fragment>
  );
};

export default StatusFilter;
