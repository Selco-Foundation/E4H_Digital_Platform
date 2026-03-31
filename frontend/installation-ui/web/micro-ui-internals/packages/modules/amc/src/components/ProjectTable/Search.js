import React, { useState } from "react";
import { TextInput, Label, SubmitBar } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const SearchCentre = ({ queryFilter, onSearch, onClear }) => {
  const { t } = useTranslation();
  const [textToSearch, setTextToSearch] = useState(queryFilter.name || "");

  const handleSearch = () => {
    onSearch(textToSearch)
  }

  const handleClear = () => {
    setTextToSearch("");
    onClear();
  }

  return (
    <React.Fragment>
      <div style={{ width: "100%", minWidth: "700px", background: "white", marginBottom: "15px", padding: "10px" }}>
        <form onSubmit={(e) => {
          e.preventDefault();
          handleSearch();
        }}>
          <div style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "10px"
          }}>
            <span>
              <Label>{t("QC_ACTION_SEARCH_FIELD_PLAN")}</Label>
              <TextInput
                name="serviceRequestId"
                value={textToSearch}
                onChange={(e) => {
                  setTextToSearch(e.target.value);
                }}
                style={{ width: "300px" }}
              ></TextInput>
            </span>
            <div style={{ display: "flex", gap: "30px", alignItems: "center" }}>
              <span
                onClick={handleClear}
                className="clear-search"
                style={{ color: "#C84C0E", marginLeft: "15px", marginTop: "10px" }}
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
