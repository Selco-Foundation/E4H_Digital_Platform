import React, { useEffect, useState } from "react";
import { TextInput, SubmitBar, LinkLabel, TickMark, DownloadIcon, SearchIconSvg, SearchIcon, Toast } from "@egovernments/digit-ui-react-components";

const SearchActionCentre = ({ t, projectQueryFilter, onSearch }) => {

  const [textToSearch, setTextToSearch] = useState(projectQueryFilter.facilitySearch.name || "");
  const [toast, setToast] = useState(null);

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

  useEffect(()=>{
    if(toast){
      setTimeout(()=>{
        setToast(null);
      },2500)
    }
  },[toast])

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
          marginLeft: "10px",
          height: "20px",
          minWidth: "fit-content",
        }}
      >
        {toast && (
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            label={`${toast.message} ${toast.failedCount ? `(${toast.failedCount} ${t("QC_BULK_APPROVE_FAILED_COUNT")})` : ""}`}
            onClose={() => setToast(null)}
            style={{ maxWidth: "670px" }}
            isDleteBtn={true}
          />
        )}
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
      </div>
    </React.Fragment>
  );
};

export default SearchActionCentre;
