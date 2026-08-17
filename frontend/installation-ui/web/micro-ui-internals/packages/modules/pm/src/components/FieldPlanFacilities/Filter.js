import React, { useEffect, useMemo, useState } from "react";
import { Dropdown, RemoveableTag } from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import CustomCheckBox from "../Custom/CustomCheckBox";
import useBoundary from "../../hooks/useBoundary";

const STATUS_CODES = [
  "SCHEDULED",
  "PENDING_INSTALLATION",
  "SUBMITTED_BY_FIELD_STAFF",
  "REJECTED_BY_FIELD_SUPERVISOR",
  "SUBMITTED_BY_SUPERVISOR",
  "PENDING_APPROVAL_FLAGGED_FOR_QC",
  "APPROVED_BY_QC_SPOC",
  "REJECTED_BY_QC_SPOC",
];

const EMPTY_FILTER = {
  district: [],
  block: [],
  status: [],
};

const Filter = ({ t, fieldPlan, onFilterChange, projectQueryFilter }) => {

  const [currentFilter, setCurrentFilter] = useState(projectQueryFilter.facilityFilter || EMPTY_FILTER);
  const [blockMenu, setBlockMenu] = useState([]);

  const { data: boundaryData } = useBoundary("State");

  const districtMenu = useMemo(() => {
    const selectedDistrictCodes = fieldPlan?.geographyDetails?.districts || [];
    return boundaryData?.districts
      ?.filter((district) => selectedDistrictCodes.includes(district.code))
      ?.map((district) => ({ ...district, name: t(`Boundary_${district.code}`) })) || [];
  }, [boundaryData, fieldPlan, t]);

  const blocksList = useMemo(() => {
    const selectedBlockCodes = fieldPlan?.geographyDetails?.blocks || [];
    return boundaryData?.blocks
      ?.filter((block) => selectedBlockCodes.includes(block.code))
      ?.map((block) => ({ ...block, name: t(`Boundary_${block.code}`) })) || [];
  }, [boundaryData, fieldPlan, t]);

  const statusMenu = useMemo(
    () => STATUS_CODES.map((code) => ({ code, name: t(`CS_${code}`) })),
    [t]
  );

  useEffect(() => {
    const selectedDistrictCodes = currentFilter.district.map((district) => district.code);
    setBlockMenu(blocksList.filter((block) => selectedDistrictCodes.includes(block.districtCode)));
  }, [currentFilter.district, blocksList]);

  useEffect(() => {
    const facilityFilterQuery = {};

    if (currentFilter.block.length > 0) {
      facilityFilterQuery.boundary = currentFilter.block.map((block) => block.code);
    } else if (currentFilter.district.length > 0) {
      const selectedDistrictCodes = currentFilter.district.map((district) => district.code);
      facilityFilterQuery.boundary = blocksList
        .filter((block) => selectedDistrictCodes.includes(block.districtCode))
        .map((block) => block.code);
    }

    if (currentFilter.status.length > 0) {
      facilityFilterQuery.status = currentFilter.status;
    }

    onFilterChange({
      facilityFilter: { ...currentFilter },
      facilityFilterQuery,
    });
  }, [currentFilter, blocksList]);

  const handleDistrictChange = (value) => {
    if (currentFilter.district.every((district) => district.code !== value.code)) {
      setCurrentFilter({
        ...currentFilter,
        district: [...currentFilter.district, value],
      });
    }
  };

  const handleBlockChange = (value) => {
    if (currentFilter.block.every((block) => block.code !== value.code)) {
      setCurrentFilter({
        ...currentFilter,
        block: [...currentFilter.block, value],
      });
    }
  };

  const handleStatusChange = (option, checked) => {
    if (checked) {
      setCurrentFilter({
        ...currentFilter,
        status: [...currentFilter.status, option.code],
      });
    } else {
      setCurrentFilter({
        ...currentFilter,
        status: currentFilter.status.filter((status) => status !== option.code),
      });
    }
  };

  const onRemove = (index, key) => {
    const afterRemove = currentFilter[key].filter((value, i) => i !== index);

    if (key === "district") {
      const newSelectedDistrictCodes = afterRemove.map((district) => district.code);
      const newSelectedBlocks = currentFilter.block.filter((block) => newSelectedDistrictCodes.includes(block.districtCode));
      setCurrentFilter({ ...currentFilter, district: afterRemove, block: newSelectedBlocks });
    } else {
      setCurrentFilter({ ...currentFilter, [key]: afterRemove });
    }
  };

  const onClearAll = () => {
    setCurrentFilter(EMPTY_FILTER);
  };

  const GetSelectOptions = (label, options, key, select) => (
    <div className={"custom-dropdown"} style={{ marginBottom: "15px" }}>
      <div className="filter-label">{label}</div>
      <Dropdown t={t} option={options} selected={{ name: "", code: "" }} select={select} optionKey={"name"} />
      <div className="tag-container">
        {currentFilter[key].map((value, index) => (
          <RemoveableTag key={index} text={value.name} onClick={() => onRemove(index, key)} />
        ))}
      </div>
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
        {GetSelectOptions(t("CS_DISTRICT"), districtMenu, "district", handleDistrictChange)}
        {GetSelectOptions(t("CS_BLOCK"), blockMenu, "block", handleBlockChange)}
        <div
          style={{
            fontFamily: "Roboto",
            fontWeight: 700,
            fontSize: "18px",
            lineHeight: "114%",
            letterSpacing: "0px",
            color: "#0B0C0C",
            marginBottom: "15px",
          }}
        >
          {t("CORE_COMMON_STATUS")}
        </div>
        {statusMenu.map((option, index) => (
          <div key={index}>
            <CustomCheckBox
              onChange={(e) => handleStatusChange(option, e.target.checked)}
              checked={currentFilter.status.includes(option.code)}
              label={option.name}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

export default Filter;
