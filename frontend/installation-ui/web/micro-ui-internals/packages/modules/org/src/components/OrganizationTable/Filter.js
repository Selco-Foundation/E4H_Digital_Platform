import React, { useEffect, useMemo, useState } from "react";
import { TextInput } from "@egovernments/digit-ui-react-components";
import RefreshButton from "../RefreshButton";
import CustomFilterIcon from "../Custom/CustomFilterIcon";

const Filter = ({ t, onFilterChange, projectQueryFilter }) => {
  const initialSearch =
    (projectQueryFilter && projectQueryFilter.organizationSearch) || { name: "" };

  const [name, setName] = useState(initialSearch.name || "");

  // If parent restores from URL, reflect it
  useEffect(() => {
    const next =
      (projectQueryFilter &&
        projectQueryFilter.organizationSearch &&
        projectQueryFilter.organizationSearch.name) ||
      "";
    setName(next || "");
  }, [
    projectQueryFilter &&
    projectQueryFilter.organizationSearch &&
    projectQueryFilter.organizationSearch.name,
  ]);

  const debouncedName = useMemo(() => name, [name]);

  useEffect(() => {
    const id = setTimeout(() => {
      const trimmed = (debouncedName || "").trim();
      const organizationSearchQuery = trimmed ? { name: trimmed } : {};

      onFilterChange({
        organizationSearch: { name: debouncedName || "" },
        organizationSearchQuery: organizationSearchQuery,
      });
    }, 350);

    return () => clearTimeout(id);
  }, [debouncedName]); // eslint-disable-line react-hooks/exhaustive-deps

  const onClearAll = () => {
    setName("");
  };

  return (
    <div className="filter" style={{ width: "100%", height: "100%", display: "flex" }}>
      <div className="filter-card" style={{ padding: "20px", width: "100%",
        height: "100%",
        flex: 1,
        display: "flex",
        flexDirection: "column",
      }}>
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <div
            style={{
              fontFamily: "Roboto",
              fontWeight: 700,
              fontSize: "24px",
              lineHeight: "2rem",
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

        <div style={{ marginBottom: "16px" }}>
          <div className="filter-label">{t("ORG_SEARCH_NAME") || "Organization Name"}</div>
          <TextInput
            value={name}
            onChange={(e) => setName((e && e.target && e.target.value) || "")}
            placeholder={t("ORG_SEARCH_NAME_PLACEHOLDER") || "Search by name"}
            style={{ width: "100%" }}
          />
        </div>
      </div>
    </div>
  );
};

export default Filter;