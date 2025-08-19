import React, { useState } from "react";
import {
  TextInput,
  SubmitBar,
  LinkLabel,
  TickMark,
  DownloadIcon, SearchIconSvg, SearchIcon
} from "@egovernments/digit-ui-react-components";

const SearchActionCentre = ({ t, projectQueryFilter, mainCheckBox, selectedFacilities, onSearch }) => {
  const [textToSearch, setTextToSearch] = useState(projectQueryFilter.facilitySearch.name || "");

  const handleSearch = (name) => {
    const facilitySearchQuery = {};
    if (name) {
      facilitySearchQuery.name = [name];
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
    <React.Fragment>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "10px",
          width: "90%",
          marginLeft: "auto",
          marginRight: "auto",
          height: "20px",
          minWidth: "fit-content",
        }}
      >
        {mainCheckBox || selectedFacilities?.length > 0 ? (
          <div style={{ fontSize: "16px", fontWeight: "bold", color: "#004d66" }}>
            {mainCheckBox ? t("CORE_COMMON_ALL") : selectedFacilities.length} {t("QC_HEALTH_FACILITIES_SELECTED")}
          </div>
        ) : (
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
                name="serviceRequestId"
                value={textToSearch}
                placeholder={t("QC_SEARCH_HEALTH_FACILITIES")}
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
        <div
          style={{
            display: "flex",
            gap: "10px",
            alignItems: "center",
          }}
        >
          {(mainCheckBox || selectedFacilities.length > 0) && (
            <button
              style={{
                border: "1px solid #d35400",
                backgroundColor: "#d35400",
                padding: "8px 20px",
                cursor: "pointer",
                color: "white",
                fontWeight: "bold",
                fontSize: "16px",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                gap: "10px",
                height: "40px"
              }}
            >
              <span>{t("CORE_COMMON_APPROVE")}</span>
              <div style={{ transform: "scale(1.4)" }}>
                <TickMark />
              </div>
            </button>
          )}
          <button
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
      </div>
    </React.Fragment>
  );
};

export default SearchActionCentre;
