import React, { useEffect, useMemo, useState } from "react";
import { Dropdown } from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import {
  DUMMY_ASSESSMENT_FACILITIES,
  ASSESSMENT_FACILITY_CATEGORY_CODES,
  REMOTE_ASSESSMENT_STATUS_CODES,
  ONSITE_ASSESSMENT_STATUS_CODES,
  ASSESSMENT_RESULT_CODES,
} from "../../utilities/AssessmentPlanData";

const EMPTY_FILTER = {
  category: null,
  district: null,
  facilityType: null,
  remoteStatus: null,
  onSiteStatus: null,
  result: null,
};

const Filter = ({ t, onFilterChange, projectQueryFilter }) => {

  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || EMPTY_FILTER);

  const categoryMenu = useMemo(
    () => ASSESSMENT_FACILITY_CATEGORY_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_CATEGORY_${code}`) })),
    [t]
  );

  const districtMenu = useMemo(
    () => Array.from(new Set(DUMMY_ASSESSMENT_FACILITIES.map((facility) => facility.district))).map((district) => ({ code: district, name: district })),
    []
  );

  const facilityTypeMenu = useMemo(
    () => Array.from(new Set(DUMMY_ASSESSMENT_FACILITIES.map((facility) => facility.facilityType))).map((facilityType) => ({ code: facilityType, name: facilityType })),
    []
  );

  const remoteStatusMenu = useMemo(
    () => REMOTE_ASSESSMENT_STATUS_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  const onSiteStatusMenu = useMemo(
    () => ONSITE_ASSESSMENT_STATUS_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  const resultMenu = useMemo(
    () => ASSESSMENT_RESULT_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.category?.code) {
      facilityFilterQuery.category = [currentFilter.category.code];
    }

    if (currentFilter.district?.code) {
      facilityFilterQuery.district = [currentFilter.district.code];
    }

    if (currentFilter.facilityType?.code) {
      facilityFilterQuery.facilityType = [currentFilter.facilityType.code];
    }

    if (currentFilter.remoteStatus?.code) {
      facilityFilterQuery.remoteStatus = [currentFilter.remoteStatus.code];
    }

    if (currentFilter.onSiteStatus?.code) {
      facilityFilterQuery.onSiteStatus = [currentFilter.onSiteStatus.code];
    }

    if (currentFilter.result?.code) {
      facilityFilterQuery.result = [currentFilter.result.code];
    }

    onFilterChange({
      facilityFilter: {
        ...currentFilter
      },
      facilityFilterQuery
    });
  }, [currentFilter]);

  const handleChange = (key) => (value) => {
    setCurrentFilter({
      ...currentFilter,
      [key]: value,
    });
  };

  const onClearAll = () => {
    setCurrentFilter(EMPTY_FILTER);
  }

  const GetSelectOptions = (label, options, selected, select) => (
    <div className={"custom-dropdown"} style={{ marginBottom: "15px" }}>
      <div className="filter-label">{label}</div>
      <Dropdown t={t} option={options} selected={selected || { name: "", code: "" }} select={select} optionKey={"name"} />
    </div>
  );

  return (
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
        {GetSelectOptions(t("PM_ASSESSMENT_CATEGORY"), categoryMenu, currentFilter.category, handleChange("category"))}
        {GetSelectOptions(t("CS_DISTRICT"), districtMenu, currentFilter.district, handleChange("district"))}
        {GetSelectOptions(t("PM_ASSESSMENT_FACILITY_TYPE"), facilityTypeMenu, currentFilter.facilityType, handleChange("facilityType"))}
        {GetSelectOptions(t("PM_ASSESSMENT_REMOTE_STATUS"), remoteStatusMenu, currentFilter.remoteStatus, handleChange("remoteStatus"))}
        {GetSelectOptions(t("PM_ASSESSMENT_ONSITE_STATUS"), onSiteStatusMenu, currentFilter.onSiteStatus, handleChange("onSiteStatus"))}
        {GetSelectOptions(t("PM_ASSESSMENT_RESULT"), resultMenu, currentFilter.result, handleChange("result"))}
      </div>
    </div>
  );
};

export default Filter;
