import React, { useMemo } from "react";
import { MultiSelectDropdown } from "@egovernments/digit-ui-components";
import { pruneGeography } from "../../utilities/GeographySelection";

/**
 * "Geography details" card for Bulk add facilities, laid out like the pm module's Geography step:
 * a heading and description, then State / Districts / Blocks as inline form rows whose labels and
 * required marks come from FormComposer itself. The selection lives in the page (`geography` /
 * `setGeography`) and is sent with the template download, so the template holds only these blocks.
 */

/** Heading + description, styled like the pm Geography step's (FormComposer heading/description). */
export const GeographyHeader = ({ props = {} }) => {
  const { t } = props;
  return (
    <div style={{ fontFamily: "Roboto", backgroundColor: "#fff", marginBottom: "16px" }}>
      <h2 style={{ margin: "0 0 12px", fontSize: "32px", fontWeight: "700" }}>
        {t("PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS")}
      </h2>
      <p style={{ margin: 0, fontSize: "14px", fontFamily: "Roboto", fontWeight: "400", lineHeight: "1.4", color: "#0B0C0C" }}>
        {t("FA_BULK_ADD_GEOGRAPHY_DESC")}
      </p>
    </div>
  );
};

/**
 * One level's multi-select (`level` is "states", "districts" or "blocks"). Each list offers only
 * the children of what is selected above it, and deselecting a parent drops its children.
 */
const GeographyLevelSelector = ({ props = {} }) => {
  const { t, level, geography, setGeography, boundary, isLoading } = props;

  const options = useMemo(() => {
    const parents =
      level === "districts" ? new Set(geography.states) : level === "blocks" ? new Set(geography.districts) : null;
    return (boundary?.[level] || [])
      .filter((node) => !parents || parents.has(node.parentCode))
      .map((node) => ({ code: node.code, name: `Boundary_${node.code}`, parentCode: node.parentCode }))
      .sort((a, b) => t(a.name).localeCompare(t(b.name), undefined, { sensitivity: "base" }));
  }, [boundary, level, geography.states, geography.districts, t]);

  const selected = options.filter((option) => geography[level].includes(option.code));
  const parentEmpty =
    (level === "districts" && geography.states.length === 0) || (level === "blocks" && geography.districts.length === 0);

  // MultiSelectDropdown reports its selection on close as [label, option] rows (the same handling
  // the pm selectors use); updating state on every tick would re-render it while it is open.
  const onClose = (rows) => {
    const codes = (rows || []).map((row) => row?.[1]?.code).filter(Boolean);
    setGeography(pruneGeography({ ...geography, [level]: codes }, boundary));
  };

  return (
    <div className={"employee-select-wrap fa-geography-select"}>
      {/* The app ships digit-ui-components-css 0.2.0, which lacks 0.2.1's rule that shows the
          search input of an open searchable MultiSelectDropdown, so typed text stays invisible
          (opacity 0). This is that rule, scoped to these pickers. Delete it once the app is on
          digit-ui-components-css 0.2.1 or later. */}
      <style>
        {`
      .fa-geography-select .digit-multiselectdropdown-wrap .digit-multiselectdropdown-master-active.searchable input[type=text] {
        opacity: 1;
        width: calc(100% - 24px);
      }
      .fa-geography-select .digit-multiselectdropdown-wrap .digit-multiselectdropdown-master-active.searchable .digit-multiselectdropdown-label svg {
        z-index: 1;
        cursor: pointer;
      }
    `}
      </style>
      <MultiSelectDropdown
        options={options}
        optionsKey={"name"}
        isSearchable={true}
        onSelect={() => {}}
        onClose={onClose}
        defaultLabel={selected.length ? `${selected.length} Selected` : ""}
        selected={selected}
        addSelectAllCheck={true}
        disabled={isLoading || parentEmpty}
        t={t}
      />
    </div>
  );
};

export default GeographyLevelSelector;
