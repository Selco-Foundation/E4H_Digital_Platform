import React, { useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import useNormalizedBoundary from "../hooks/useNormalizedBoundary";

const BoundaryForm = ({ t, onSubmit, formToast, setFormToast, isStateTextMode, setIsStateTextMode, isDistrictTextMode, setIsDistrictTextMode }) => {

  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

  const { data: boundaryData, isLoading: isBoundaryLoading } = useNormalizedBoundary("State");

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

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
        defaultValues={{}}
        heading={""}
        cardStyle={{ boxShadow: "none" }}
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
