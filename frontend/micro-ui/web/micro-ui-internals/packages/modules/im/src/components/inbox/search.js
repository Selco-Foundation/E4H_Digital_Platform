import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { TextInput, Label, SubmitBar, LinkLabel, ActionBar, CloseSvg, Dropdown } from "@selco/digit-ui-react-components";
export const isCodePresent = (array, codeToCheck) => {
  return array.some((item) => item.code === codeToCheck);
};
const SearchComplaint = ({ onSearch, type, onClose, searchParams }) => {
  const [complaintNo, setComplaintNo] = useState(searchParams?.search?.applicationNumber || "");
  const [viewportHeight, setViewportHeight] = useState(window.innerHeight);
  const bottomPosition = window.innerHeight - viewportHeight;
  const { register, errors, handleSubmit, reset } = useForm();
  const { t } = useTranslation();

  const onSubmitInput = (data) => {
    if (!Object.keys(errors).filter((i) => errors[i]).length) {
      if (data.serviceRequestId !== "") {
        onSearch({ applicationNumber: data.serviceRequestId });
      } else {
        onSearch({});
      }

      if (type === "mobile") {
        onClose();
      }
    }
  };

  function clearSearch() {
    reset();
    onSearch({});
    setComplaintNo("");
  }

  const clearAll = () => {
    return (
      <LinkLabel className="clear-search-label" style={{ color: "#7a2829" }} onClick={clearSearch}>
        {t("ES_COMMON_CLEAR_SEARCH")}
      </LinkLabel>
    );
  };

  function setComplaint(e) {
    setComplaintNo(e.target.value);
  }

  useEffect(() => {
    const handleResize = () => {
      // Update the viewport height when the keyboard opens/closes
      setViewportHeight(window.visualViewport ? window.visualViewport.height : window.innerHeight);
    };

    // Add event listeners for viewport resize
    window.addEventListener("resize", handleResize);
    if (window.visualViewport) {
      window.visualViewport.addEventListener("resize", handleResize);
    }

    // Cleanup event listeners
    return () => {
      window.removeEventListener("resize", handleResize);
      if (window.visualViewport) {
        window.visualViewport.removeEventListener("resize", handleResize);
      }
    };
  }, []);

  return (
    <form onSubmit={handleSubmit(onSubmitInput)} style={{ marginLeft: "24px" }}>
      <React.Fragment>
        <div className="search-container" style={{ width: "auto" }}>
          <div className="search-complaint-container">
            {type === "mobile" && (
              <div className="complaint-header">
                <h2> {t("CS_COMMON_SEARCH_BY")}:</h2>
                <span onClick={onClose}>
                  <CloseSvg />
                </span>
              </div>
            )}
            <div className="complaint-input-container" style={{ display: "grid", height: "83px" }}>
              <span className="complaint-input">
                <Label style={{ marginTop: "5px" }}>{t("CS_COMMON_TICKET_NO")}</Label>
                <TextInput
                  name="serviceRequestId"
                  value={complaintNo}
                  onChange={setComplaint}
                  inputRef={register({
                    pattern: /(?!^$)([^\s])/,
                  })}
                  style={{ marginBottom: "8px" }}
                ></TextInput>
              </span>

              {type === "desktop" && (
                <div style={{ display: "flex", marginTop: "32px", marginLeft: "50px" }}>
                  <SubmitBar
                    style={{ marginLeft: "10px" }}
                    label={t("ES_COMMON_SEARCH")}
                    submit={true}
                    disabled={Object.keys(errors).filter((i) => errors[i]).length}
                  />
                  <span className="clear-search" style={{ color: "#7a2829", marginTop: "10px", marginLeft: "50px" }}>
                    {clearAll()}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
        {type === "mobile" && (
          <ActionBar style={{ bottom: `${bottomPosition}px` }}>
            <SubmitBar label="Search" submit={true} />
          </ActionBar>
        )}
      </React.Fragment>
    </form>
  );
};

export default SearchComplaint;
