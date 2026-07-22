import React, { useState } from "react";
import { TextInput, LinkLabel, DownloadIcon, SearchIcon } from "@egovernments/digit-ui-react-components";

const SearchAction = ({ t, projectQueryFilter, selectedFacilityIds, onSearch, onDownload }) => {

  const [textToSearch, setTextToSearch] = useState(projectQueryFilter.facilitySearch?.name || "");

  const handleSearch = (name) => {
    const facilitySearchQuery = {};
    if (name) {
      facilitySearchQuery.name = name;
    }

    onSearch({
      facilitySearch: {
        name,
      },
      facilitySearchQuery
    })
  }

  const handleClear = () => {
    setTextToSearch("");
    handleSearch("");
  }

  const ClickableSearchIcon = () => (
    <button
      style={{
        backgroundColor: "white",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <SearchIcon />
    </button>
  )

  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "10px",
        minWidth: "fit-content",
      }}
    >
      {selectedFacilityIds?.length > 0 ? (
        <div style={{ fontSize: "16px", fontWeight: "bold", color: "#004d66" }}>
          {selectedFacilityIds.length} {t("PM_ASSESSMENT_FACILITIES_SELECTED")}
        </div>
      ) : (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSearch(textToSearch);
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <TextInput
              name="facilityName"
              value={textToSearch}
              placeholder={t("PM_ASSESSMENT_SEARCH_FACILITY_NAME")}
              onChange={(e) => {
                setTextToSearch(e.target.value);
              }}
              className={"search-action"}
              signature={true}
              signatureImg={<ClickableSearchIcon />}
              style={{
                marginTop: "auto",
                marginBottom: "auto",
                border: "none",
                width: "300px",
                height: "38px"
              }}
            />
            <LinkLabel
              style={{
                fontSize: "18px",
                marginTop: "auto",
                marginBottom: "auto",
                minWidth: "fit-content",
              }}
              onClick={handleClear}
            >
              {t("CORE_COMMON_CLEAR")}
            </LinkLabel>
          </div>
        </form>
      )}
      <button
        type="button"
        onClick={onDownload}
        style={{
          backgroundColor: "white",
          border: "1px solid #d35400",
          color: "#d35400",
          padding: "8px 20px",
          cursor: "pointer",
          fontWeight: "bold",
          fontSize: "16px",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "5px",
          height: "40px"
        }}
      >
        <span>{t("CORE_COMMON_DOWNLOAD")}</span>
        <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
          <DownloadIcon fill={"#d35400"} />
        </div>
      </button>
    </div>
  );
};

export default SearchAction;
