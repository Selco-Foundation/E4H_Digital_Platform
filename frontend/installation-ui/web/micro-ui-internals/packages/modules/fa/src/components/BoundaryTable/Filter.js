import React, { useEffect, useRef, useState } from "react";
import { RemoveableTag, Loader } from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import useNormalizedBoundary from "../../hooks/useNormalizedBoundary";
import CustomFilterIcon from "../Custom/CustomFilterIcon";
import CustomDropdown from "../Custom/CustomDropdown";

const codesKey = (arr = []) =>
  (arr || [])
    .map((x) => x?.code)
    .filter(Boolean)
    .sort()
    .join("|");

const Filter = ({ t, onFilterChange, boundaryQueryFilter, type }) => {
  const [stateMenu, setStateMenu] = useState([]);
  const [districtOptions, setDistrictOptions] = useState([]);
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockOptions, setBlockOptions] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);

  const [currentFilter, setCurrentFilter] = useState(
    boundaryQueryFilter?.boundaryFilter || {
      state: [],
      district: [],
      block: [],
    }
  );

  const lastSentRef = useRef("");

  const { isLoading, data } = useNormalizedBoundary("State");

  useEffect(() => {
    if (!data) return;

    setStateMenu((data.states || []).map((state) => ({ ...state, name: t(`Boundary_${state.code}`) })));

    setDistrictOptions(
      (data.districts || []).map((district) => ({
        ...district,
        parentCode: district.parentCode || district.stateCode || "",
        name: t(`Boundary_${district.code}`),
      }))
    );

    setBlockOptions(
      (data.blocks || []).map((block) => ({
        ...block,
        parentCode: block.parentCode || block.districtCode || "",
        name: t(`Boundary_${block.code}`),
      }))
    );
  }, [data, t]);

  const stateCodesKey = codesKey(currentFilter.state);
  const districtCodesKey = codesKey(currentFilter.district);

  useEffect(() => {
    if (!districtOptions?.length && !blockOptions?.length) return;

    const selectedStates = currentFilter.state || [];
    const selectedDistricts = currentFilter.district || [];

    if (!selectedStates.length) {
      setDistrictMenu([]);
      setBlockMenu([]);
      return;
    }

    const selectedStateCodes = selectedStates.map((s) => s.code);

    const nextDistrictMenu = (districtOptions || []).filter((d) => selectedStateCodes.includes(d.parentCode));
    setDistrictMenu(nextDistrictMenu);

    const nextDistrictCodes = nextDistrictMenu.map((d) => d.code);

    const districtCodesForBlocks =
      selectedDistricts.length > 0
        ? selectedDistricts.map((d) => d.code)
        : nextDistrictCodes;

    const nextBlockMenu = (blockOptions || []).filter((b) => districtCodesForBlocks.includes(b.parentCode));
    setBlockMenu(nextBlockMenu);
  }, [stateCodesKey, districtCodesKey, districtOptions, blockOptions]); // do NOT depend on whole objects/functions

  useEffect(() => {
    const boundaryFilterQuery = {};

    if (currentFilter.block.length > 0) {
      boundaryFilterQuery.boundary = currentFilter.block.map((b) => b.code);
    } else if (currentFilter.district.length > 0) {
      boundaryFilterQuery.boundary = currentFilter.district.map((d) => d.code);
    } else if (currentFilter.state.length > 0) {
      boundaryFilterQuery.boundary = currentFilter.state.map((s) => s.code);
    }

    const payload = {
      boundaryFilter: { ...currentFilter },
      boundaryFilterQuery,
    };

    const key = JSON.stringify(payload);
    if (key !== lastSentRef.current) {
      lastSentRef.current = key;
      if (typeof onFilterChange === "function") onFilterChange(payload);
    }

  }, [currentFilter]);

  const handleStateChange = (selectedState) => {
    if (!selectedState?.code) return;
    if (currentFilter.state.every((s) => s.code !== selectedState.code)) {
      const newSelectedStates = [...currentFilter.state, selectedState];
      const selectedStateCodes = newSelectedStates.map((s) => s.code);

      const newDistrictMenu = districtOptions.filter((d) => selectedStateCodes.includes(d.parentCode));
      const newDistrictMenuCodes = newDistrictMenu.map((d) => d.code);

      const newBlockMenu = blockOptions.filter((b) => newDistrictMenuCodes.includes(b.parentCode));

      setDistrictMenu(newDistrictMenu);
      setBlockMenu(newBlockMenu);

      setCurrentFilter({
        ...currentFilter,
        state: newSelectedStates,
      });
    }
  };

  const handleDistrictChange = (selectedDistrict) => {
    if (!selectedDistrict?.code) return;
    if (currentFilter.district.every((d) => d.code !== selectedDistrict.code)) {
      const newSelectedDistricts = [...currentFilter.district, selectedDistrict];
      const selectedDistrictCodes = newSelectedDistricts.map((d) => d.code);

      const newBlockMenu = blockOptions.filter((b) => selectedDistrictCodes.includes(b.parentCode));
      setBlockMenu(newBlockMenu);

      setCurrentFilter({
        ...currentFilter,
        district: newSelectedDistricts,
      });
    }
  };

  const handleBlockChange = (selectedBlock) => {
    if (!selectedBlock?.code) return;
    if (currentFilter.block.every((b) => b.code !== selectedBlock.code)) {
      setCurrentFilter({
        ...currentFilter,
        block: [...currentFilter.block, selectedBlock],
      });
    }
  };

  const onRemove = (index, key) => {
    const afterRemove = (currentFilter[key] || []).filter((_, i) => i !== index);

    if (key === "state") {
      const remainingStateCodes = afterRemove.map((s) => s.code);

      const newDistrictMenu = districtOptions.filter((d) => remainingStateCodes.includes(d.parentCode));
      const newSelectedDistricts = (currentFilter.district || []).filter((d) => remainingStateCodes.includes(d.parentCode));
      const remainingDistrictCodes = newSelectedDistricts.map((d) => d.code);

      const newBlockMenu = blockOptions.filter((b) => remainingDistrictCodes.includes(b.parentCode));
      const newSelectedBlocks = (currentFilter.block || []).filter((b) => remainingDistrictCodes.includes(b.parentCode));

      setDistrictMenu(newDistrictMenu);
      setBlockMenu(newBlockMenu);

      setCurrentFilter({
        ...currentFilter,
        state: afterRemove,
        district: newSelectedDistricts,
        block: newSelectedBlocks,
      });
      return;
    }

    if (key === "district") {
      const remainingDistrictCodes = afterRemove.map((d) => d.code);

      const newBlockMenu = blockOptions.filter((b) => remainingDistrictCodes.includes(b.parentCode));
      const newSelectedBlocks = (currentFilter.block || []).filter((b) => remainingDistrictCodes.includes(b.parentCode));

      setBlockMenu(newBlockMenu);

      setCurrentFilter({
        ...currentFilter,
        district: afterRemove,
        block: newSelectedBlocks,
      });
      return;
    }

    setCurrentFilter({ ...currentFilter, [key]: afterRemove });
  };

  const onClearAll = () => {
    setCurrentFilter({ state: [], district: [], block: [] });
    setDistrictMenu([]);
    setBlockMenu([]);
    lastSentRef.current = "";
  };

  if (isLoading) return <Loader />;

  const GetSelectOptions = (label, options, selected = null, select, optionKey, onRemoveCb, key) => {
    selected = selected || { [optionKey]: "", code: "" };

    return (
      <div>
        <div className="filter-label">{label}</div>
        <CustomDropdown
          t={t}
          option={options}
          selected={selected}
          select={(value) => select(value, key)}
          optionKey={optionKey}
        />

        <div className="tag-container">
          {(currentFilter[key] || []).length > 0 &&
          (currentFilter[key] || []).map((value, idx) => (
            <RemoveableTag key={idx} text={`${value[optionKey]} ...`} onClick={() => onRemoveCb(idx, key)} />
          ))}
        </div>
      </div>
    );
  };

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
                background: "transparent",
              }}
              onClick={onClearAll}
            >
              <RefreshButton fill={"#C84C0E"} />
            </button>
          </div>

          <div>{GetSelectOptions(t("CS_STATE"), stateMenu, null, handleStateChange, "name", onRemove, "state")}</div>
          <div>{GetSelectOptions(t("CS_DISTRICT"), districtMenu, null, handleDistrictChange, "name", onRemove, "district")}</div>
          <div>{GetSelectOptions(t("CS_BLOCK"), blockMenu, null, handleBlockChange, "name", onRemove, "block")}</div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default Filter;