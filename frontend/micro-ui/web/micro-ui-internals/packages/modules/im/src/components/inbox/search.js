import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { TextInput, Label, SubmitBar, LinkLabel, ActionBar, CloseSvg, Dropdown } from "@selco/digit-ui-react-components";
export const isCodePresent = (array, codeToCheck) => {
  return array.some((item) => item.code === codeToCheck);
};
const SearchComplaint = ({ onSearch, type, onClose, searchParams }) => {
  const [complaintNo, setComplaintNo] = useState(searchParams?.search?.serviceRequestId || "");
  const stateTenantId = Digit.ULBService.getStateId();
  let healthcareTenant = Digit.SessionStorage.get("Tenants").filter((item) => item.code !== stateTenantId);
  const [phcType, setPhcType] = useState();

  const state = Digit.ULBService.getStateId();
  const { isMdmsLoading, data: mdmsData } = Digit.Hooks.pgr.useMDMS(state, "Incident", ["District", "Block"]);
  const { data: phcMenu } = Digit.Hooks.pgr.useMDMS(state, "tenant", ["tenants"]);
  const phcMenus =
    Digit.SessionStorage.get("Employee.tenantId") !== stateTenantId
      ? Digit.SessionStorage.get("Tenants")
      : Digit.SessionStorage.get("Employee.tenantId") == stateTenantId
      ? isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER")
        ? healthcareTenant
        : Digit.SessionStorage.get("IM_TENANTS").filter((item) => item.code !== stateTenantId)
      : Digit.SessionStorage.get("IM_TENANTS").filter((item) => item.code !== stateTenantId);
  let sortedPhcMenu = [];
  if (phcMenus.length > 0) {
    sortedPhcMenu = phcMenus.sort((a, b) => a.name.localeCompare(b.name));
  }
  const [mobileNo, setMobileNo] = useState(searchParams?.search?.mobileNumber || "");
  const [viewportHeight, setViewportHeight] = useState(window.innerHeight);
  const bottomPosition = window.innerHeight - viewportHeight;
  const { register, errors, handleSubmit, reset } = useForm();
  const { t } = useTranslation();

  const onSubmitInput = (data) => {
    if (!Object.keys(errors).filter((i) => errors[i]).length) {
      if (data.serviceRequestId !== "" && phcType?.code !== "") {
        onSearch({ applicationNumber: data.serviceRequestId, phcType: phcType?.code });
      } else if (data.code !== "") {
        onSearch({ phcType: phcType?.code });
      } else if (data.serviceRequestId !== "") {
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
    setPhcType("");
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
    if (Digit.SessionStorage.get("Employee.tenantId") !== stateTenantId ? Digit.SessionStorage.get("Tenants") : Digit.SessionStorage.get("IM_TENANTS")) {
      let empTenant = Digit.SessionStorage.get("Employee.tenantId");
      let filtered = Digit.SessionStorage.get("IM_TENANTS").filter((abc) => abc.code == empTenant);

      if (filtered?.[0].code !== stateTenantId) {
        setPhcTypeFunction(filtered?.[0]);
      }
    }
  }, []);

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

  function setPhcTypeFunction(value) {
    setPhcType(value);
  }
  function setMobile(e) {
    setMobileNo(e.target.value);
  }

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
                  <span className="clear-search" style={{ color: "#7a2829", marginLeft: "15px", marginTop: "10px", marginLeft: "50px" }}>
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
