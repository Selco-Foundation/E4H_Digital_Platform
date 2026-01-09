import React, { useEffect, useMemo, useRef, useState } from "react";
import { FormComposerV2, Loader, Toast, TextInput } from "@egovernments/digit-ui-react-components";
import { Stepper } from "@egovernments/digit-ui-components";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";

import CommonUtils from "../../utilities/CommonUtils";
import UnsavedDataAlert from "../../components/UnsavedDataAlert";
import useBoundary from "../../hooks/useBoundary";
import { BoundaryService } from "../../services/Boundary";
import CustomDropdown from "../../components/Custom/CustomDropdown";
import CustomSwapHorizontalCircle from "../../components/Custom/CustomSwapHorizontalCircle";

const safeOnSelect = (onSelect, key, value) => {
  if (typeof onSelect === "function") onSelect(key, value);
};

const getCode = (val) => {
  if (!val) return "";
  if (typeof val === "string") return val;
  if (typeof val === "object" && val.code) return val.code;
  return "";
};

const ToggleLink = ({ label, onClick }) => {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        border: "none",
        background: "transparent",
        cursor: "pointer",
        padding: 0,
        color: "#0B0C0C",
        fontSize: "16px",
        fontFamily: "Roboto Condensed",
        fontWeight: 600,
        whiteSpace: "nowrap",
        flexShrink: 0,
        transform: "translateY(-4px)"
      }}
    >
      <CustomSwapHorizontalCircle size={28} color="#0B0C0C" style={{opacity: 0.6}}/>
      <span style={{ opacity: 0.6 }}>{label}</span>
    </button>
  );
};

const FAStateToggleField = (props) => {
  const onSelect = props?.onSelect;
  const formData = props?.formData || {};
  const customProps = props?.customProps || props?.config?.customProps || {};

  const t = customProps?.t || props?.t;
  const boundaryData = customProps?.boundaryData;
  const isTextMode = !!customProps?.isTextMode;
  const setIsTextMode = customProps?.setIsTextMode;

  const stateValue = getCode(formData?.state);

  const states = (boundaryData && boundaryData.states) || [];
  const stateOptions = (states || []).map((s) => ({
    code: s.code,
    name: s.code,
  }));

  const selectedState = stateOptions.find((o) => o.code === stateValue) || null;

  const clearBelow = () => {
    safeOnSelect(onSelect, "district", "");
    safeOnSelect(onSelect, "block", "");
  };

  const toggleMode = () => {
    const next = !isTextMode;
    clearBelow();
    if (typeof setIsTextMode === "function") setIsTextMode(next);
  };

  const onStateDropdownSelect = (opt) => {
    safeOnSelect(onSelect, "state", (opt && opt.code) || "");
    clearBelow();
  };

  const onStateTextChange = (e) => {
    safeOnSelect(onSelect, "state", (e && e.target && e.target.value) || "");
  };

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "12px", width: "100%" }}>
      <div style={{ flex: 1 }}>
        {isTextMode ? (
          <TextInput value={stateValue} onChange={onStateTextChange} placeholder={t("CS_STATE")} />
        ) : (
          <CustomDropdown
            t={t}
            option={stateOptions}
            optionKey={"name"}
            selected={selectedState}
            select={onStateDropdownSelect}
            style={{ width: "100%" }}
          />
        )}
      </div>

      <ToggleLink
        label={isTextMode ? t("FA_TOGGLE_SELECT_STATE"): t("FA_TOGGLE_ADD_NEW_STATE")}
        onClick={toggleMode}
      />
    </div>
  );
};

const FADistrictToggleField = (props) => {
  const onSelect = props?.onSelect;
  const formData = props?.formData || {};
  const customProps = props?.customProps || props?.config?.customProps || {};

  const t = customProps?.t || props?.t;
  const boundaryData = customProps?.boundaryData;

  const isTextMode = !!customProps?.isTextMode;
  const setIsTextMode = customProps?.setIsTextMode;
  const stateIsTextMode = !!customProps?.stateIsTextMode;

  const stateValue = getCode(formData?.state);
  const districtValue = getCode(formData?.district);

  const districts = (boundaryData && boundaryData.districts) || [];
  const districtOptions = (districts || [])
    .filter((d) => !!stateValue && d.stateCode === stateValue)
    .map((d) => ({
      code: d.code,
      name: d.code,
    }));

  const selectedDistrict = districtOptions.find((o) => o.code === districtValue) || null;

  const clearBelow = () => {
    safeOnSelect(onSelect, "block", "");
  };

  const toggleMode = () => {
    const next = !isTextMode;
    clearBelow();
    if (typeof setIsTextMode === "function") setIsTextMode(next);
  };

  const onDistrictDropdownSelect = (opt) => {
    safeOnSelect(onSelect, "district", (opt && opt.code) || "");
    clearBelow();
  };

  const onDistrictTextChange = (e) => {
    safeOnSelect(onSelect, "district", (e && e.target && e.target.value) || "");
  };

  const dropdownDisabled = !stateValue || stateIsTextMode;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "12px", width: "100%" }}>
      <div
        style={{
          flex: 1,
          opacity: !isTextMode && dropdownDisabled ? 0.6 : 1,
          pointerEvents: !isTextMode && dropdownDisabled ? "none" : "auto",
        }}
      >
        {isTextMode ? (
          <TextInput value={districtValue} onChange={onDistrictTextChange} placeholder={t("CS_DISTRICT")} />
        ) : (
          <CustomDropdown
            t={t}
            option={districtOptions}
            optionKey={"name"}
            selected={selectedDistrict}
            select={onDistrictDropdownSelect}
            style={{ width: "100%" }}
          />
        )}
      </div>

      <ToggleLink
        label={
          isTextMode ? t("FA_TOGGLE_SELECT_DISTRICT") : t("FA_TOGGLE_ADD_NEW_DISTRICT")
        }
        onClick={toggleMode}
      />
    </div>
  );
};

const CreateBoundary = () => {
  const { t } = useTranslation();
  const history = useHistory();

  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [backAlert, setBackAlert] = useState(null);

  const [isStateTextMode, setIsStateTextMode] = useState(false);
  const [isDistrictTextMode, setIsDistrictTextMode] = useState(false);

  const { data: boundaryData, isLoading: isBoundaryLoading } = useBoundary("State");

  useEffect(() => {
    if (Digit && Digit.ComponentRegistryService && Digit.ComponentRegistryService.setComponent) {
      Digit.ComponentRegistryService.setComponent("FAStateToggleField", FAStateToggleField);
      Digit.ComponentRegistryService.setComponent("FADistrictToggleField", FADistrictToggleField);
    }
  }, []);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (toast) setTimeout(() => setToast(null), 2500);
  }, [toast]);

  const config = useMemo(() => {
    return [
      {
        key: "1",
        body: [
          {
            inline: true,
            label: "FA_BOUNDARY_CODE",
            isMandatory: true,
            key: "code",
            type: "text",
            populators: { name: "code", error: t("CORE_COMMON_REQUIRED") },
          },
        ],
      },
      {
        key: "2",
        body: [
          {
            inline: true,
            label: "CS_STATE",
            isMandatory: true,
            key: "state",
            type: "component",
            component: "FAStateToggleField",
            customProps: {
              t,
              boundaryData,
              isTextMode: isStateTextMode,
              setIsTextMode: (next) => {
                setIsStateTextMode(!!next);
              },
            },
            populators: { name: "state", error: t("CORE_COMMON_REQUIRED") },
          },
          {
            inline: true,
            label: "CS_DISTRICT",
            isMandatory: true,
            key: "district",
            type: "component",
            component: "FADistrictToggleField",
            customProps: {
              t,
              boundaryData,
              stateIsTextMode: isStateTextMode,
              isTextMode: isDistrictTextMode,
              setIsTextMode: (next) => {
                setIsDistrictTextMode(!!next);
              },
            },
            populators: { name: "district", error: t("CORE_COMMON_REQUIRED") },
          },
          {
            inline: true,
            label: "CS_BLOCK",
            isMandatory: true,
            key: "block",
            type: "text",
            populators: { name: "block", error: t("CORE_COMMON_REQUIRED") },
          },
        ],
      },
    ];
  }, [t, boundaryData, isStateTextMode, isDistrictTextMode]);

  const filterConfig = (cfg, key) => cfg.filter((step) => parseInt(step.key) === key);
  const [filteredConfig, setFilteredConfig] = useState(filterConfig(config, currentKey));

  useEffect(() => {
    setFilteredConfig(filterConfig(config, currentKey));
  }, [config, currentKey]);

  useEffect(() => {
    switch (currentKey) {
      case 1:
        setDefaultFormData(persistedFormData.boundaryDetails || {});
        break;
      case 2:
        setDefaultFormData(persistedFormData.geographyDetails || {});
        break;
      default:
        setDefaultFormData({});
    }
  }, [persistedFormData, currentKey]);

  const setFormAccessors = ({ getValues }) => setGetFormData(() => getValues);

  const getNextActionLabel = () => (currentKey === 1 ? t("CORE_COMMON_NEXT") : t("CORE_COMMON_SUBMIT"));

  const getHeading = () => (currentKey === 1 ? t("FA_CREATE_BOUNDARY_HEAD_BOUNDARY_DETAILS") : t("FA_CREATE_BOUNDARY_HEAD_GEOGRAPHY_DETAILS"));

  const getDescription = () => {
    if (currentKey === 1) return t("FA_CREATE_BOUNDARY_HEAD_BOUNDARY_DETAILS_DESC");
    return t("FA_CREATE_BOUNDARY_HEAD_GEOGRAPHY_DETAILS_DESC");
  };

  const getDefaultValues = () => {
    if (currentKey === 1) return persistedFormData.boundaryDetails || {};
    if (currentKey === 2) return persistedFormData.geographyDetails || {};
    return {};
  };

  const onStepClick = (key) => {
    if (key + 1 >= currentKey) return;

    if (currentKey === 2) {
      const geographyDetails = {
        state: getFormData && getFormData("state"),
        district: getFormData && getFormData("district"),
        block: getFormData && getFormData("block"),
      };
      setPersistedFormData((prev) => ({ ...prev, geographyDetails }));
    }

    setCurrentKey(key + 1);
  };

  const createBoundary = async (finalData) => {
    const code = (finalData && finalData.boundaryDetails && finalData.boundaryDetails.code && finalData.boundaryDetails.code.trim()) || "";
    if (!code) {
      setToast({ key: "error", label: "CORE_COMMON_REQUIRED" });
      return;
    }

    setBlockUI(true);
    try {
      const payload = {
        Boundary: [
          {
            tenantId,
            code,
            geometry: null,
            additionalDetails: {
              geographyDetails: finalData.geographyDetails,
            },
          },
        ],
      };

      await BoundaryService.createBoundary(payload);

      setToast({ key: "success", label: "FA_TOAST_BOUNDARY_CREATION_SUCCESS" });
      history.goBack();
    } catch (e) {
      console.error("Error creating boundary", e);
      setToast({ key: "error", label: "FA_TOAST_BOUNDARY_CREATION_ERROR" });
    } finally {
      setBlockUI(false);
    }
  };

  const handleFormSubmit = async (data) => {
    if (currentKey === 1) {
      setPersistedFormData((prev) => ({ ...prev, boundaryDetails: data }));
      setCurrentKey((prev) => prev + 1);
      return;
    }

    if (currentKey === 2) {
      const finalData = {
        boundaryDetails: persistedFormData.boundaryDetails,
        geographyDetails: data,
      };
      setPersistedFormData((prev) => ({ ...prev, geographyDetails: data }));
      await createBoundary(finalData);
    }
  };

  const handleBackNavigation = () => {
    if (currentKey === 1) {
      const saved = { boundaryDetails: persistedFormData.boundaryDetails || {} };
      const current = { boundaryDetails: { code: getFormData && getFormData("code") } };

      if (CommonUtils.isNotEqual(saved, current)) {
        setBackAlert({ continueAction: () => window.history.back() });
      } else {
        window.history.back();
      }
      return;
    }

    if (currentKey === 2) {
      const geographyDetails = {
        state: getFormData && getFormData("state"),
        district: getFormData && getFormData("district"),
        block: getFormData && getFormData("block"),
      };
      setPersistedFormData((prev) => ({ ...prev, geographyDetails }));
      setCurrentKey((prev) => prev - 1);
      return;
    }

    window.history.back();
  };

  // IMPORTANT: force remount when toggles change so dropdown <-> text swap works immediately
  const formComposerKey = [
    currentKey,
    JSON.stringify(defaultFormData || {}),
    isStateTextMode ? "S1" : "S0",
    isDistrictTextMode ? "D1" : "D0",
  ].join("|");

  return (
    <div className="create-project-wrapper" style={{ padding: mobileView ? "15px" : "0px" }}>
      {(blockUI || (currentKey === 2 && isBoundaryLoading)) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000005,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}

      <Stepper
        customSteps={["FA_CREATE_BOUNDARY_HEAD_BOUNDARY_DETAILS", "FA_CREATE_BOUNDARY_HEAD_GEOGRAPHY_DETAILS"]}
        onStepClick={onStepClick}
        currentStep={currentKey}
        style={{ marginBottom: "20px" }}
      />

      <FormComposerV2
        key={formComposerKey}
        config={filteredConfig}
        onSubmit={handleFormSubmit}
        label={getNextActionLabel()}
        showSecondaryLabel={true}
        secondaryLabel={t("CORE_COMMON_BACK")}
        onSecondayActionClick={handleBackNavigation}
        heading={getHeading()}
        headingStyle={{ fontSize: "32px", marginBottom: "20px" }}
        description={getDescription()}
        descriptionStyle={{
          fontSize: "14px",
          fontFamily: "Roboto",
          fontWeight: "400",
          color: "#0B0C0C",
        }}
        isDescriptionBold={true}
        getFormAccessors={setFormAccessors}
        defaultValues={getDefaultValues()}
        showMultipleCardsWithoutNavs={true}
        noBreakLine={true}
        cardStyle={{ padding: "20px" }}
        actionClassName={"reverse-actionbar"}
      />

      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={t(toast.label)}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}

      {backAlert && <UnsavedDataAlert t={t} alert={backAlert} setAlert={setBackAlert} />}
    </div>
  );
};

export default CreateBoundary;