import React, { useMemo } from "react";
import { MultiSelectDropdown } from "@egovernments/digit-ui-components";
import { pruneGeography } from "../../utilities/GeographySelection";

/**
 * "Geography details" card for Bulk add facilities: multi-select State -> District -> Block.
 * Each list offers only the children of what is selected above it, and deselecting a parent
 * drops its children. The selection lives in the page (`geography` / `setGeography`), which
 * sends it with the template download so the template holds only these blocks.
 */
const GeographySelector = ({ props = {} }) => {
  const { t, geography, setGeography, boundary, isLoading } = props;

  const toOption = (node) => ({ code: node.code, name: `Boundary_${node.code}`, parentCode: node.parentCode });
  const byCode = (a, b) => a.code.localeCompare(b.code);

  const options = useMemo(() => {
    const states = new Set(geography.states);
    const districts = new Set(geography.districts);
    return {
      states: (boundary?.states || []).map(toOption).sort(byCode),
      districts: (boundary?.districts || []).filter((node) => states.has(node.parentCode)).map(toOption).sort(byCode),
      blocks: (boundary?.blocks || []).filter((node) => districts.has(node.parentCode)).map(toOption).sort(byCode),
    };
  }, [boundary, geography.states, geography.districts]);

  const selectedOf = (level) => options[level].filter((option) => geography[level].includes(option.code));

  // MultiSelectDropdown reports its selection on close as [label, option] rows (the same
  // handling the pm module's selectors use); updating state on every tick re-renders it while open.
  const onLevelClose = (level) => (rows) => {
    const codes = (rows || []).map((row) => row?.[1]?.code).filter(Boolean);
    setGeography(pruneGeography({ ...geography, [level]: codes }, boundary));
  };

  const dropdown = (level, label, disabled) => (
    <div style={{ flex: "1 1 240px", minWidth: 0 }}>
      <p style={{ margin: "0 0 8px", fontWeight: 600 }}>
        {t(label)}
        <span style={{ color: "#B91900" }}> *</span>
      </p>
      <div className={"employee-select-wrap"}>
        <MultiSelectDropdown
          options={options[level]}
          optionsKey={"name"}
          isSearchable={true}
          onSelect={() => {}}
          onClose={onLevelClose(level)}
          defaultLabel={geography[level].length ? `${geography[level].length} Selected` : ""}
          selected={selectedOf(level)}
          addSelectAllCheck={true}
          disabled={disabled}
          t={t}
        />
      </div>
    </div>
  );

  return (
    <div style={{ fontFamily: "Roboto", backgroundColor: "#fff" }}>
      <h2 style={{ margin: "0 0 12px", fontSize: "32px", fontWeight: "700" }}>{t("PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS")}</h2>
      <p style={{ margin: "0 0 24px", fontSize: "14px", lineHeight: "1.4" }}>{t("FA_BULK_ADD_GEOGRAPHY_DESC")}</p>
      <div style={{ display: "flex", flexWrap: "wrap", gap: "24px" }}>
        {dropdown("states", "PM_CREATE_PROJECT_LABEL_STATE", isLoading)}
        {dropdown("districts", "PM_CREATE_PROJECT_LABEL_DISTRICTS", isLoading || geography.states.length === 0)}
        {dropdown("blocks", "PM_CREATE_PROJECT_LABEL_BLOCKS", isLoading || geography.districts.length === 0)}
      </div>
    </div>
  );
};

export default GeographySelector;
