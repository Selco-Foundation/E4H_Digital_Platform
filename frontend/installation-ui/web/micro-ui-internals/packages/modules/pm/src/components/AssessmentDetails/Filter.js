import React, { useMemo } from "react";
import { Dropdown, RemoveableTag } from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import useBoundary from "../../hooks/useBoundary";
import {
  REMOTE_ASSESSMENT_STATUS_CODES,
  ONSITE_ASSESSMENT_STATUS_CODES,
  ASSESSMENT_RESULT_CODES,
} from "../../utilities/AssessmentPlanData";

const EMPTY_FILTER = {
  category: [],
  facilityType: [],
  district: [],
  block: [],
  remoteStatus: [],
  onSiteStatus: [],
  result: [],
};

// A filter persisted in the URL from before this filter's keys became array-based (or with a
// key simply missing) would otherwise leave a null/undefined value here instead of [].
const normalizeFilter = (filter) => ({
  category: filter?.category || [],
  facilityType: filter?.facilityType || [],
  district: filter?.district || [],
  block: filter?.block || [],
  remoteStatus: filter?.remoteStatus || [],
  onSiteStatus: filter?.onSiteStatus || [],
  result: filter?.result || [],
});

const Filter = ({ t, onFilterChange, projectQueryFilter, assessmentPlan }) => {

  const currentFilter = normalizeFilter(projectQueryFilter.facilityFilter);
  const tenantId = Digit.ULBService.getStateId();

  const { data: mdmsData } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "facility",
    [{ name: "FacilityCategory" }, { name: "FacilityType" }],
    { select: (data) => data, enabled: !!tenantId }
  );

  const categoryOptions = mdmsData?.facility?.FacilityCategory || [];
  const facilityTypeOptions = mdmsData?.facility?.FacilityType || [];

  const { data: boundaryData } = useBoundary("State");

  // Restrict the pickable districts/blocks to the assessment plan's own geography, the same way
  // CreateAssessment.js scopes its boundary selectors to the project's geography.
  const planDistrictCodes = assessmentPlan?.geographyDetails?.districts || [];
  const planBlockCodes = assessmentPlan?.geographyDetails?.blocks || [];

  const districtOptions = useMemo(
    () => (boundaryData?.districts || [])
      .filter((district) => planDistrictCodes.includes(district.code))
      .map((district) => ({ ...district, name: t(`Boundary_${district.code}`) })),
    [boundaryData, planDistrictCodes, t]
  );

  const blockOptions = useMemo(
    () => (boundaryData?.blocks || [])
      .filter((block) => planBlockCodes.includes(block.code))
      .map((block) => ({ ...block, name: t(`Boundary_${block.code}`) })),
    [boundaryData, planBlockCodes, t]
  );

  const remoteStatusOptions = useMemo(
    () => REMOTE_ASSESSMENT_STATUS_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  const onSiteStatusOptions = useMemo(
    () => ONSITE_ASSESSMENT_STATUS_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  const resultOptions = useMemo(
    () => ASSESSMENT_RESULT_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_FACILITY_STATUS_${code}`) })),
    [t]
  );

  const selectedCategoryCodes = currentFilter.category.map((category) => category.code);
  const facilityTypeMenu = selectedCategoryCodes.length
    ? facilityTypeOptions.filter((facilityType) => selectedCategoryCodes.includes(facilityType.facilityCategory))
    : facilityTypeOptions;

  const selectedDistrictCodes = currentFilter.district.map((district) => district.code);
  const blockMenu = selectedDistrictCodes.length
    ? blockOptions.filter((block) => selectedDistrictCodes.includes(block.districtCode))
    : blockOptions;

  const emitFilterChange = (nextFilter) => {
    const facilityFilterQuery = {};

    if (nextFilter.category.length) facilityFilterQuery.facilityCategories = nextFilter.category.map((item) => item.code);
    if (nextFilter.facilityType.length) facilityFilterQuery.facilityTypes = nextFilter.facilityType.map((item) => item.code);
    if (nextFilter.district.length) facilityFilterQuery.districts = nextFilter.district.map((item) => item.code);
    if (nextFilter.block.length) facilityFilterQuery.blocks = nextFilter.block.map((item) => item.code);
    if (nextFilter.remoteStatus.length) facilityFilterQuery.phoneStatuses = nextFilter.remoteStatus.map((item) => item.code);
    if (nextFilter.onSiteStatus.length) facilityFilterQuery.fieldStatuses = nextFilter.onSiteStatus.map((item) => item.code);
    if (nextFilter.result.length) facilityFilterQuery.overallStatuses = nextFilter.result.map((item) => item.code);

    onFilterChange({
      facilityFilter: nextFilter,
      facilityFilterQuery,
    });
  };

  const handleAdd = (key) => (value) => {
    if (!value?.code || currentFilter[key].some((item) => item.code === value.code)) return;
    emitFilterChange({ ...currentFilter, [key]: [...currentFilter[key], value] });
  };

  const handleRemove = (key) => (index) => {
    const afterRemove = currentFilter[key].filter((_, i) => i !== index);

    if (key === "category") {
      const remainingCategoryCodes = afterRemove.map((item) => item.code);
      const validFacilityTypeCodes = (remainingCategoryCodes.length
        ? facilityTypeOptions.filter((facilityType) => remainingCategoryCodes.includes(facilityType.facilityCategory))
        : facilityTypeOptions
      ).map((facilityType) => facilityType.code);

      emitFilterChange({
        ...currentFilter,
        category: afterRemove,
        facilityType: currentFilter.facilityType.filter((facilityType) => validFacilityTypeCodes.includes(facilityType.code)),
      });
      return;
    }

    if (key === "district") {
      const remainingDistrictCodes = afterRemove.map((item) => item.code);
      const validBlockCodes = (remainingDistrictCodes.length
        ? blockOptions.filter((block) => remainingDistrictCodes.includes(block.districtCode))
        : blockOptions
      ).map((block) => block.code);

      emitFilterChange({
        ...currentFilter,
        district: afterRemove,
        block: currentFilter.block.filter((block) => validBlockCodes.includes(block.code)),
      });
      return;
    }

    emitFilterChange({ ...currentFilter, [key]: afterRemove });
  };

  const onClearAll = () => {
    emitFilterChange(EMPTY_FILTER);
  };

  const GetSelectOptions = (label, options, key) => (
    <div className={"custom-dropdown"} style={{ marginBottom: "15px" }}>
      <div className="filter-label">{label}</div>
      <Dropdown
        t={t}
        option={options}
        selected={{ name: "", code: "" }}
        select={handleAdd(key)}
        optionKey={"name"}
      />
      <div className="tag-container">
        {currentFilter[key].map((value, index) => (
          <RemoveableTag key={value.code} text={value.name} onClick={() => handleRemove(key)(index)} />
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
        {GetSelectOptions(t("PM_ASSESSMENT_CATEGORY"), categoryOptions, "category")}
        {GetSelectOptions(t("PM_ASSESSMENT_FACILITY_TYPE"), facilityTypeMenu, "facilityType")}
        {GetSelectOptions(t("CS_DISTRICT"), districtOptions, "district")}
        {GetSelectOptions(t("CS_BLOCK"), blockMenu, "block")}
        {GetSelectOptions(t("PM_ASSESSMENT_REMOTE_STATUS"), remoteStatusOptions, "remoteStatus")}
        {GetSelectOptions(t("PM_ASSESSMENT_ONSITE_STATUS"), onSiteStatusOptions, "onSiteStatus")}
        {GetSelectOptions(t("PM_ASSESSMENT_RESULT"), resultOptions, "result")}
      </div>
    </div>
  );
};

export default Filter;
