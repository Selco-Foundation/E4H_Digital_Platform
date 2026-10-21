import React, { useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import useBoundary from "../hooks/useBoundary";

// LGD codes are numeric, and boundary.lgd_* columns hold at most 16 characters.
const LGD_CODE_PATTERN = /^\d{1,16}$/;
export const LGD_BLOCK_TIERS = [
  { code: "Block", name: "FA_LGD_TIER_BLOCK" },
  { code: "Sub-district", name: "FA_LGD_TIER_SUB_DISTRICT" },
  { code: "Both", name: "FA_LGD_TIER_BOTH" },
];

const BoundaryForm = ({ t, onSubmit, formToast, setFormToast, isStateTextMode, setIsStateTextMode, isDistrictTextMode, setIsDistrictTextMode }) => {

  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

  const { data: boundaryData, isLoading: isBoundaryLoading } = useBoundary("");

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // Required LGD code field, shown for every tier being created here: a state or district picked
  // from the list keeps the codes it already stores, and the tiers created under it inherit them
  // (see BoundaryAdminActions).
  const lgdCodeField = (key, label) => ({
    inline: true,
    label,
    isMandatory: true,
    key,
    type: "text",
    populators: {
      name: key,
      error: t("CORE_COMMON_REQUIRED"),
      validation: {
        maxlength: 16,
        pattern: { value: LGD_CODE_PATTERN, message: t("FA_LGD_CODE_VALIDATION_ERROR") },
      },
    },
  });

  const config = useMemo(() => [
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
              mobileView,
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
              mobileView,
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
      {
        // Grouped under their own heading, as in the livelihood Add Boundary modal.
        key: "2",
        head: "FA_LGD_CODES",
        headId: "fa-lgd-codes",
        body: [
          ...(isStateTextMode ? [lgdCodeField("lgdStateCode", "FA_LGD_STATE_CODE")] : []),
          ...(isStateTextMode || isDistrictTextMode ? [lgdCodeField("lgdDistrictCode", "FA_LGD_DISTRICT_CODE")] : []),
          lgdCodeField("lgdBlockCode", "FA_LGD_BLOCK_CODE"),
          {
            inline: true,
            label: "FA_LGD_BLOCK_TIER",
            isMandatory: true,
            key: "lgdBlockTier",
            type: "dropdown",
            populators: {
              name: "lgdBlockTier",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              options: LGD_BLOCK_TIERS,
            },
          },
        ],
      },
    ], [t, boundaryData, isStateTextMode, isDistrictTextMode, mobileView]
  );

  if (isBoundaryLoading) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "200px",
        }}
      >
        <Loader />
      </div>
    );
  }

  return (
    <div
      className={"admin-form"}
      style={{
        position: "relative",
        paddingBottom: "30px",
        maxHeight: "70vh",
        overflow: "auto",
      }}
    >
      <FormComposerV2
        config={config}
        onSubmit={onSubmit}
        label={t("CORE_COMMON_SAVE")}
        defaultValues={{ lgdBlockTier: LGD_BLOCK_TIERS[0] }}
        heading={""}
        cardStyle={{ boxShadow: "none" }}
        sectionHeadStyle={{ margin: "16px 0px 8px", fontSize: "16px", fontWeight: "700", color: "#0B0C0C" }}
        submitInForm={false}
        actionClassName={"reverse-actionbar-fixed"}
      />
      {formToast && (
        <Toast
          error={formToast.key === "error"}
          warning={formToast.key === "warning"}
          style={{
            ...(formToast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={t(formToast.label)}
          isDleteBtn={true}
          onClose={() => setFormToast(null)}
        />
      )}
    </div>
  );
};

export default BoundaryForm;
