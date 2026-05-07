import React, { useEffect, useState } from "react";
import { Dropdown, RemoveableTag, ActionBar, ApplyFilterBar, CloseSvg } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const getDefaultFilters = () => ({
  state: [],
  district: [],
  block: [],
});
const areFiltersEqual = (left = {}, right = {}) => JSON.stringify(left || getDefaultFilters()) === JSON.stringify(right || getDefaultFilters());

const RMSPausedFilter = ({ onFilterChange, searchParams, type, onClose }) => {
  const { t } = useTranslation();
  const jurisdictionCurrentBoundary = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {};
  const jurisdictionCurrentBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionCurrentBoundary);
  const { data: boundaryData } = Digit.Hooks.im.useBoundary(jurisdictionCurrentBoundaryCodes);

  const [stateMenu, setStateMenu] = useState([]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [filters, setFilters] = useState(searchParams?.filters || getDefaultFilters());

  useEffect(() => {
    const nextFilters = searchParams?.filters || getDefaultFilters();
    setFilters((prev) => (areFiltersEqual(prev, nextFilters) ? prev : nextFilters));
  }, [searchParams]);

  useEffect(() => {
    const response = boundaryData?.states;
    if (response?.length) {
      const uniqueStates = {};
      const nextStates = response
        .filter((item) => {
          if (!uniqueStates[item.code]) {
            uniqueStates[item.code] = true;
            return true;
          }
          return false;
        })
        .map((item) => ({ code: item.code, name: t(`Boundary_${item.code}`) }))
        .sort((a, b) => a.name.localeCompare(b.name));
      setStateMenu(nextStates);
    }
  }, [boundaryData, t]);

  useEffect(() => {
    const selectedState = filters?.state?.[0];
    if (selectedState?.code && boundaryData?.districts?.length) {
      const nextDistricts = boundaryData.districts
        .filter((item) => item.parentCode === selectedState.code)
        .map((item) => ({ code: item.code, name: t(`Boundary_${item.code}`) }))
        .sort((a, b) => a.name.localeCompare(b.name));
      setDistrictMenu(nextDistricts);
    } else {
      setDistrictMenu([]);
    }
  }, [filters?.state, boundaryData, t]);

  useEffect(() => {
    const selectedDistrict = filters?.district?.[0];
    if (selectedDistrict?.code && boundaryData?.blocks?.length) {
      const nextBlocks = boundaryData.blocks
        .filter((item) => item.parentCode === selectedDistrict.code)
        .map((item) => ({ code: item.code, name: t(`Boundary_${item.code}`) }))
        .sort((a, b) => a.name.localeCompare(b.name));
      setBlockMenu(nextBlocks);
    } else {
      setBlockMenu([]);
    }
  }, [filters?.district, boundaryData, t]);

  useEffect(() => {
    if (type !== "mobile") {
      if (!areFiltersEqual(searchParams?.filters, filters)) {
        onFilterChange(filters);
      }
    }
  }, [type, filters, onFilterChange, searchParams]);

  const handleStateChange = (selectedState) => {
    if (!selectedState?.code) return;
    const previouslySelectedState = filters.state[0];
    if (previouslySelectedState?.code !== selectedState.code) {
      setFilters({
        ...filters,
        state: [selectedState],
        district: [],
        block: [],
      });
    }
  };

  const handleDistrictChange = (selectedDistrict) => {
    if (!selectedDistrict?.code) return;
    const previouslySelectedDistrict = filters.district[0];
    if (previouslySelectedDistrict?.code !== selectedDistrict.code) {
      setFilters({
        ...filters,
        district: [selectedDistrict],
        block: [],
      });
    }
  };

  const handleBlockChange = (selectedBlock) => {
    if (!selectedBlock?.code) return;
    const previouslySelectedBlock = filters.block[0];
    if (previouslySelectedBlock?.code !== selectedBlock.code) {
      setFilters({
        ...filters,
        block: [selectedBlock],
      });
    }
  };

  const onRemove = (index, key) => {
    if (key === "state") {
      setFilters({ ...filters, state: [], district: [], block: [] });
      return;
    }
    if (key === "district") {
      setFilters({ ...filters, district: [], block: [] });
      return;
    }
    const nextValues = filters[key].filter((_, i) => i !== index);
    setFilters({ ...filters, [key]: nextValues });
  };

  const clearAll = () => {
    setFilters(getDefaultFilters());
  };

  const applyFiltersAndClose = () => {
    onFilterChange(filters);
    onClose();
  };

  const renderSelect = (label, options, key, selected, onSelect) => {
    const disableSelection = (!options || options.length === 1)
    if (disableSelection && options?.length) {
      onSelect(options[0]);
    }
    return (
      <div>
        <div className="filter-label">{label}</div>
        <Dropdown disable={disableSelection} option={options} selected={selected} select={onSelect} optionKey="name" />
        <div className="tag-container">
          {filters[key]?.map((value, index) => (
            <RemoveableTag disabled={disableSelection} key={`${value.code}-${index}`} text={`${value.name} ...`} onClick={() => onRemove(index, key)} />
          ))}
        </div>
      </div>
    )
  };

  return (
    <React.Fragment>
      <div className="filter">
        <div className="filter-card">
          <div className="heading">
            <div className="filter-label">{t("ES_COMMON_FILTER_BY")}:</div>
            {type === "desktop" && (
              <span className="clear-search" style={{ color: "#7a2829" }} onClick={clearAll}>
                {t("ES_COMMON_CLEAR_ALL")}
              </span>
            )}
            {type === "mobile" && (
              <span onClick={onClose}>
                <CloseSvg />
              </span>
            )}
          </div>
          <div>
            {renderSelect(t("CS_STATE"), stateMenu, "state", null, handleStateChange)}
            {renderSelect(t("CS_DISTRICT"), districtMenu, "district", null, handleDistrictChange)}
            {renderSelect(t("CS_BLOCK"), blockMenu, "block", null, handleBlockChange)}
          </div>
        </div>
      </div>
      <ActionBar>
        {type === "mobile" && (
          <ApplyFilterBar
            labelLink={t("ES_COMMON_CLEAR_ALL")}
            buttonLink={t("ES_COMMON_FILTER")}
            onClear={clearAll}
            onSubmit={applyFiltersAndClose}
          />
        )}
      </ActionBar>
    </React.Fragment>
  );
};

export default RMSPausedFilter;
