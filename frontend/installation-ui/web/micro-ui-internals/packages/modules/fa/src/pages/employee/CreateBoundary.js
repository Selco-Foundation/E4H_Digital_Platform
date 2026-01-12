import React, { useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Toast, TextInput } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";

import useBoundary from "../../hooks/useBoundary";
import CustomDropdown from "../../components/Custom/CustomDropdown";
import CustomSwapHorizontalCircle from "../../components/Custom/CustomSwapHorizontalCircle";
import { BoundaryService } from "../../services/Boundary";

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
        transform: "translateY(-4px)",
      }}
    >
      <CustomSwapHorizontalCircle size={28} color="#0B0C0C" style={{ opacity: 0.6 }} />
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

  const boundaryLabel = (code) => t(`Boundary_${code}`);
  const states = (boundaryData && boundaryData.states) || [];
  const stateOptions = (states || []).map((s) => ({
    code: s.code,
    name: boundaryLabel(s.code),
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
    <div style={{ position: "relative", width: "100%", overflow: "visible" }}>
      <div style={{ width: "100%" }}>
        {isTextMode ? (
          <TextInput value={stateValue} onChange={onStateTextChange} placeholder={t("CS_STATE")} style={{ width: "100%" }} />
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

      <div
        style={{
          position: "absolute",
          left: "calc(90%)",
          top: "50%",
          transform: "translateY(-4px)",
          whiteSpace: "nowrap",
        }}
      >
        <ToggleLink label={isTextMode ? t("FA_TOGGLE_SELECT_STATE") : t("FA_TOGGLE_ADD_NEW_STATE")} onClick={toggleMode} />
      </div>
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

  const boundaryLabel = (code) => t(`Boundary_${code}`);
  const districts = (boundaryData && boundaryData.districts) || [];
  const districtOptions = (districts || [])
    .filter((d) => !!stateValue && d.stateCode === stateValue)
    .map((d) => ({
      code: d.code,
      name: boundaryLabel(d.code),
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
    <div style={{ position: "relative", width: "100%", overflow: "visible" }}>
      <div
        style={{
          width: "100%",
          opacity: !isTextMode && dropdownDisabled ? 0.6 : 1,
          pointerEvents: !isTextMode && dropdownDisabled ? "none" : "auto",
        }}
      >
        {isTextMode ? (
          <TextInput
            value={districtValue}
            onChange={onDistrictTextChange}
            placeholder={t("CS_DISTRICT")}
            style={{ width: "100%" }}
          />
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

      {/* Keep toggle OUTSIDE the input width so input matches Block width */}
      <div
        style={{
          position: "absolute",
          left: "90%",
          top: "50%",
          transform: "translateY(-4px)",
          whiteSpace: "nowrap",
        }}
      >
        <ToggleLink
          label={isTextMode ? t("FA_TOGGLE_SELECT_DISTRICT") : t("FA_TOGGLE_ADD_NEW_DISTRICT")}
          onClick={toggleMode}
        />
      </div>
    </div>
  );
};

const CreateBoundary = () => {
  const { t } = useTranslation();
  const history = useHistory();

  const tenantId = Digit.ULBService.getStateId();

  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(false);

  const [isStateTextMode, setIsStateTextMode] = useState(false);
  const [isDistrictTextMode, setIsDistrictTextMode] = useState(false);

  const { data: boundaryData, isLoading: isBoundaryLoading } = useBoundary("State");

  if (Digit && Digit.ComponentRegistryService && Digit.ComponentRegistryService.setComponent) {
    Digit.ComponentRegistryService.setComponent("FAStateToggleField", FAStateToggleField);
    Digit.ComponentRegistryService.setComponent("FADistrictToggleField", FADistrictToggleField);
  }

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
                const n = !!next;
                setIsStateTextMode(n);
                if (n) setIsDistrictTextMode(true);
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
              setIsTextMode: (next) => setIsDistrictTextMode(!!next),
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

  const handleSubmit = async (data) => {
    const stateVal = getCode(data?.state);
    const districtVal = getCode(data?.district);
    const blockVal = getCode(data?.block);

    if (!stateVal || !districtVal || !blockVal) {
      setToast({ key: "error", label: "CORE_COMMON_REQUIRED" });
      return;
    }

    setBlockUI(true);
    try {
      const computed = BoundaryService.computeGeographyCodes({
        country: "India",
        state: stateVal,
        district: districtVal,
        block: blockVal,
      });

      const isAlreadyExists = (e) => {
        const status = e?.response?.status;
        const data = e?.response?.data;

        if (status === 409) return true;
        const errorsArr = (data && (data.Errors || data.errors)) || [];

        if (Array.isArray(errorsArr)) {
          const hasDuplicateCode = errorsArr.some((er) => String(er?.code || "").toUpperCase() === "DUPLICATE_CODED");
          if (hasDuplicateCode) return true;

          const msgFromArray = errorsArr
            .map((er) => `${er?.message || ""} ${er?.code || ""}`)
            .join(" ")
            .toLowerCase();

          if (msgFromArray.includes("already existsSS") || msgFromArray.includes("duplicateDD")) return true;
        }

        const msg = `${e?.message || ""} ${safeStringify(data)}`.toLowerCase();
        return msg.includes("already existsS") || msg.includes("duplicateS");
      };

      const safeStringify = (v) => {
        try {
          return typeof v === "string" ? v : JSON.stringify(v || "");
        } catch {
          return "";
        }
      };

      const createBoundaryAndRel = async ({ code, boundaryType, parent, geographyDetails, ignoreIfExists }) => {
        try {
          await BoundaryService.createBoundary({
            Boundary: [
              {
                tenantId,
                code,
                geometry: null,
                additionalDetails: { geographyDetails },
              },
            ],
          });
        } catch (e) {
          if (!ignoreIfExists || !isAlreadyExists(e)) throw e;
        }

        try {
          await BoundaryService.createBoundaryRelationship({
            BoundaryRelationship: {
              tenantId,
              code,
              hierarchyType: "SELCO",
              boundaryType,
              parent,
            },
          });
        } catch (e) {
          if (!ignoreIfExists || !isAlreadyExists(e)) throw e;
        }
      };

      if (isStateTextMode) {
        await createBoundaryAndRel({
          code: computed.state,
          boundaryType: "State",
          parent: computed.country,
          geographyDetails: { country: computed.country, state: computed.state },
          ignoreIfExists: true,
        });
      }

      if (isDistrictTextMode) {
        await createBoundaryAndRel({
          code: computed.district,
          boundaryType: "District",
          parent: computed.state,
          geographyDetails: { country: computed.country, state: computed.state, district: computed.district },
          ignoreIfExists: true,
        });
      }

      await createBoundaryAndRel({
        code: computed.code,
        boundaryType: "Block",
        parent: computed.district,
        geographyDetails: {
          country: computed.country,
          state: computed.state,
          district: computed.district,
          block: computed.block,
        },
        ignoreIfExists: false,
      });

      setToast({ key: "success", label: "FA_TOAST_BOUNDARY_CREATION_SUCCESS" });
      history.goBack();
    } catch (e) {
      console.error("Error creating boundary / relationship", e);
      setToast({ key: "error", label: "FA_TOAST_BOUNDARY_CREATION_ERROR" });
    } finally {
      setBlockUI(false);
    }
  };

  const formComposerKey = ["GEO", isStateTextMode ? "S1" : "S0", isDistrictTextMode ? "D1" : "D0"].join("|");

  return (
    <div className="create-project-wrapper" style={{ padding: mobileView ? "15px" : "0px" }}>
      {(blockUI || isBoundaryLoading) && (
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

      <FormComposerV2
        key={formComposerKey}
        config={config}
        onSubmit={handleSubmit}
        label={t("CORE_COMMON_SUBMIT")}
        showSecondaryLabel={true}
        secondaryLabel={t("CORE_COMMON_BACK")}
        onSecondayActionClick={() => history.goBack()}
        heading={t("FA_CREATE_BOUNDARY_HEAD_GEOGRAPHY_DETAILS")}
        headingStyle={{ fontSize: "32px", marginBottom: "20px" }}
        description={t("FA_CREATE_BOUNDARY_HEAD_GEOGRAPHY_DETAILS_DESC")}
        descriptionStyle={{
          fontSize: "14px",
          fontFamily: "Roboto",
          fontWeight: "400",
          color: "#0B0C0C",
        }}
        isDescriptionBold={true}
        defaultValues={{}}
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
    </div>
  );
};

export default CreateBoundary;