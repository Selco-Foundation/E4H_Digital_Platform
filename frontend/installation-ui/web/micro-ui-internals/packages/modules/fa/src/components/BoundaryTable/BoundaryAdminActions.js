import React, { useEffect, useState } from "react";
import { Loader, Button, Toast } from "@egovernments/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import BoundaryModal from "../BoundaryModal";
import { BoundaryService } from "../../services/Boundary";
import { useQueryClient } from "react-query";
import { LocalizationService } from "../../services/Localization";

const getCode = (val) => {
  if (!val) return "";
  if (typeof val === "string") return val;
  if (typeof val === "object" && val.code) return val.code;
  return "";
};

const BoundaryAdminActions = ({ t }) => {

  const history = useHistory();
  const tenantId = Digit.ULBService.getStateId();
  const [blockUI, setBlockUI] = useState(false);
  const [showBoundaryModal, setShowBoundaryModal] = useState(false);
  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 836);
  const [isStateTextMode, setIsStateTextMode] = useState(false);
  const [isDistrictTextMode, setIsDistrictTextMode] = useState(false);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (toast) setTimeout(() => setToast(null), 2500);
  }, [toast]);

  useEffect(() => {
    if (formToast) setTimeout(() => setFormToast(null), 2500);
  }, [formToast]);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleBoundaryCreate = async (data) => {
    const stateVal = getCode(data?.state);
    const districtVal = getCode(data?.district);
    const blockVal = getCode(data?.block);

    if (!stateVal || !districtVal || !blockVal) {
      setToast({ key: "error", label: "CORE_COMMON_REQUIRED" });
      return;
    }

    const getServerErrorMessage = (e) => {
      const data = e?.response?.data;

      const direct = data?.message || data?.error?.message;

      if (typeof direct === "string" && direct.trim()) return direct.trim();
      const errorsArr = (data && (data.Errors || data.errors)) || [];
      if (Array.isArray(errorsArr) && errorsArr.length) {
        const msg = errorsArr
          .map((er) => er?.message || er?.description || er?.code)
          .filter(Boolean)
          .join(" | ")
          .trim();
        if (msg) return msg;
      }
      if (typeof data === "string" && data.trim()) return data.trim();
      if (typeof e?.message === "string" && e.message.trim()) return e.message.trim();
      return "";
    };

    setBlockUI(true);
    try {
      const computed = BoundaryService.computeGeographyCodes({
        country: "India",
        state: stateVal,
        district: districtVal,
        block: blockVal,
        isStateTextMode: isStateTextMode,
        isDistrictTextMode: isDistrictTextMode,
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

          if (msgFromArray.includes("already exists") || msgFromArray.includes("duplicate")) return true;
        }

        const msg = `${e?.message || ""} ${safeStringify(data)}`.toLowerCase();
        return msg.includes("already exists") || msg.includes("duplicate");
      };

      const safeStringify = (v) => {
        try {
          return typeof v === "string" ? v : JSON.stringify(v || "");
        } catch {
          return "";
        }
      };

      const createBoundaryAndRel = async ({ name, code, boundaryType, parent, geographyDetails, ignoreIfExists }) => {
        try {
          await BoundaryService.createBoundary({
            Boundary: [
              {
                tenantId,
                code,
                geometry: null,
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

        const localizationPayload = {
          tenantId: tenantId,
          messages: [
            {
              code: `Boundary_${code}`,
              message: name,
              module: "rainmaker-in",
              locale: "en_IN",
            },
          ],
        };
        await LocalizationService.upsertLocalization(localizationPayload);
      };

      if (isStateTextMode) {
        await createBoundaryAndRel({
          name: stateVal,
          code: computed.state,
          boundaryType: "State",
          parent: computed.country,
          ignoreIfExists: true,
        });
      }

      if (isDistrictTextMode) {
        await createBoundaryAndRel({
          name: districtVal,
          code: computed.district,
          boundaryType: "District",
          parent: computed.state,
          ignoreIfExists: true,
        });
      }

      await createBoundaryAndRel({
        name: blockVal,
        code: computed.block,
        boundaryType: "Block",
        parent: computed.district,
        ignoreIfExists: false,
      });

      await queryClient.invalidateQueries(["NORMALIZED_BOUNDARY"]);
      await queryClient.invalidateQueries(["BOUNDARY"]);

      const existingModules = Digit.PersistantStorage.get("Locale.en_IN.List");
      Digit.PersistantStorage.set("Locale.en_IN.List", existingModules.filter((module) => module !== "rainmaker-in"));
      await Digit.LocalizationService.getUpdatedMessages({
        modules: ["rainmaker-in"],
        locale: "en_IN",
        tenantId: tenantId,
      });
      setBlockUI(false);
      setShowBoundaryModal(false);
      setToast({ key: "success", label: "FA_TOAST_BOUNDARY_CREATION_SUCCESS" });

    } catch (e) {
      console.error("Error creating boundary / relationship", e);
      const serverMsg = getServerErrorMessage(e);
      setBlockUI(false);
      setFormToast({ key: "error", label: serverMsg || "FA_TOAST_BOUNDARY_CREATION_ERROR" });
    }
  };

  return (
    <div style={{ display: "flex", gap: "12px", justifyContent: "flex-end", flexWrap: "wrap" }}>
      {blockUI && (
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
      <Button
        variation={"secondary"}
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
          height: "40px",
        }}
        label={t("FA_ADD_BOUNDARY")}
        onButtonClick={() => setShowBoundaryModal(true)}
      />
      <Button
        variation={"secondary"}
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
          height: "40px",
        }}
        label={t("FA_BULK_ADD")}
        onButtonClick={() => history.push(`${location.pathname.replace(/\/boundaries\/?$/, "")}/boundary/upload`)}
      />
      {showBoundaryModal && (
        <BoundaryModal
          t={t}
          title={"FA_ADD_BOUNDARY"}
          onClose={() => setShowBoundaryModal(false)}
          onSubmit={handleBoundaryCreate}
          formToast={formToast}
          setFormToast={setFormToast}
          isStateTextMode={isStateTextMode}
          setIsStateTextMode={setIsStateTextMode}
          isDistrictTextMode={isDistrictTextMode}
          setIsDistrictTextMode={setIsDistrictTextMode}
        />
      )}
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
}

export default BoundaryAdminActions;