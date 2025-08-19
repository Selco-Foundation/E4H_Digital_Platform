import React, { useState } from "react";
import { TextInput, Label, SubmitBar } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const SearchCentre = ({ queryFilter, onSearch, onClear }) => {
  const { t } = useTranslation();
  const [textToSearch, setTextToSearch] = useState(queryFilter.Project.name || "");

  const handleSearch = () => {
    onSearch(textToSearch)
  }

  const handleClear = () => {
    setTextToSearch("");
    onClear();
  }

  return (
    <React.Fragment>
      <div style={{ width: "100%", background: "white", height: "fit-content", marginBottom: "15px", padding: "10px" }}>
        <form onSubmit={(e) => {
          e.preventDefault();
          handleSearch();
        }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span>
              <Label>Search Field Plan Code</Label>
              <TextInput
                name="serviceRequestId"
                value={textToSearch}
                onChange={(e) => {
                  setTextToSearch(e.target.value);
                }}
                style={{ width: "300px" }}
              ></TextInput>
            </span>
            <div style={{ display: "flex", gap: "30px" }}>
              <span
                onClick={handleClear}
                className="clear-search"
                style={{ color: "#7a2829", marginLeft: "15px", marginTop: "10px" }}
              >
                {t("CORE_COMMON_CLEAR_SEARCH")}
              </span>
              <SubmitBar onSubmit={handleSearch} style={{ marginLeft: "10px" }} label={t("CORE_COMMON_SEARCH")} />
            </div>
          </div>
        </form>
      </div>
    </React.Fragment>
  );
};

export default SearchCentre;
