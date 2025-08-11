import React, { useState } from "react";
import { TextInput, Label, SubmitBar, LinkLabel } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";

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
        }}
      >
        {mainCheckBox || selectedFacilities?.length > 0 ? (
          <div style={{ fontSize: "16px", fontWeight: "bold", color: "#004d66" }}>
            {mainCheckBox ? "All" : selectedFacilities.length} Health Facilities Selected
          </div>
        ) : (
          <form onSubmit={(e) => {
            e.preventDefault();
            handleSearch(textToSearch);
          }}>
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
                placeholder="Search Health Facilities"
                onChange={(e) => {
                  setTextToSearch(e.target.value);
                }}
                style={{ marginTop: "auto", marginBottom: "auto" }}
              ></TextInput>
              <SubmitBar
                onSubmit={() => {
                  handleSearch(textToSearch);
                }}
                label={"Search"}
              />
              <LinkLabel
                style={{
                  fontSize: "18px",
                  marginTop: "auto",
                  marginBottom: "auto"
                }}
                onClick={handleClear}
              >
                Clear
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
                padding: "8px 12px",
                cursor: "pointer",
                color: "white",
                fontWeight: "bold",
                fontSize: "16px",
              }}
            >
              Approve
            </button>
          )}
          <button
            style={{
              backgroundColor: "white",
              border: "1px solid #d35400",
              color: "#d35400",
              padding: "8px 12px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "16px",
            }}
          >
            Download
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};

export default SearchActionCentre;
