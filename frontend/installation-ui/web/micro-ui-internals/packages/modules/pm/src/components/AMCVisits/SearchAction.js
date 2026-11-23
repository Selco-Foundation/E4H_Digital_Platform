import React, { useState } from "react";
import { TextInput, LinkLabel, SearchIcon } from "@egovernments/digit-ui-react-components";

const SearchActionCentre = ({ t, projectQueryFilter, onSearch }) => {

  const [textToSearch, setTextToSearch] = useState(projectQueryFilter.facilitySearch.name || "");

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
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSearch(textToSearch);
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "10px",
          }}
        >
          <TextInput
            name="facilityName"
            value={textToSearch}
            placeholder={t("CS_SEARCH_HEALTH_FACILITIES")}
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
    </div>
  );
};

export default SearchActionCentre;
