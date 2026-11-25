import React, {Fragment, useEffect, useState } from "react";
import { Loader, Button, Toast } from "@egovernments/digit-ui-react-components";
import { Tab } from "@egovernments/digit-ui-components";
import useFacilityDetails from "../../hooks/useFacilityDetails";
import { populateWorkingFacility } from "../../redux/actions";
import { useDispatch } from "react-redux";
import { useHistory } from "react-router-dom";
import { useQueryClient } from "react-query";
import { useTranslation } from "react-i18next";
import Section from "../../components/FacilityDetails/Section";
import FacilityModal from "../../components/FacilityModal";
import ActivityTable from "../../components/FacilityDetails/ActivityTable";
import AssetTable from "../../components/FacilityDetails/AssetTable";
import AMCTable from "../../components/FacilityDetails/AMCTable";
import { FacilityService } from "../../services/Facility";
import CommonUtils from "../../utilities/CommonUtils";
import ConfirmChangesModal from "../../components/ConfirmChangesModal";
import VendorDetails from "../../components/FacilityDetails/VendorDetails";
import { apiErrorText, duplicateIdentifierFields, facilityEditChanges } from "../../utilities/FacilityEdit";

const FacilityDetails = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const url = window.location.href;
  const encodedFacilityId = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityId = decodeURIComponent(encodedFacilityId);
  const [createdFacility, setCreatedFacility] = useState({});
  const [showEditFacilityModal, setShowEditFacilityModal] = useState(false);
  const dispatch = useDispatch();
  const [toast, setToast] = useState(null);
  const [formToast, setFormToast] = useState(null);
  const [activeTab, setActiveTab] = useState("ACTIVITY");
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);
  // An edit waiting on the confirmation pop-up: the form's data and what it changes.
  const [pendingEdit, setPendingEdit] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const history = useHistory();
  const queryClient = useQueryClient();

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  useEffect(() => {
    if (formToast) {
      setTimeout(() => {
        setFormToast(null);
      }, 2500);
    }
  }, [formToast]);

  const {
    isLoading, data: facilityData,
    revalidate: invalidateFacilityDetails
  } = useFacilityDetails(facilityId);

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "facility",
    [
      {
        name: "SolarSolutionDesignType",
      },
      {
        name: "FacilityType",
      },
      {
        name: "FacilityCategory",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const solarSolutionDesignTypes = mdmsResponse?.facility?.SolarSolutionDesignType || [];
  const facilityTypes = mdmsResponse?.facility?.FacilityType || [];
  const facilityCategories = mdmsResponse?.facility?.FacilityCategory || [];

  useEffect(() => {
    if (facilityData && mdmsResponse) {
      dispatch(populateWorkingFacility(facilityData));
      setCreatedFacility({
        ...facilityData,
        state: {
          code: facilityData?.stateCode,
          parentCode: "India",
          name: facilityData?.stateCode ? t(`Boundary_${facilityData?.stateCode}`) : "",
        },
        district: {
          code: facilityData?.districtCode,
          parentCode: facilityData?.stateCode,
          name: facilityData?.districtCode ? t(`Boundary_${facilityData?.districtCode}`) : "",
        },
        block: {
          code: facilityData?.blockCode,
          parentCode: facilityData?.districtCode,
          name: facilityData?.blockCode ? t(`Boundary_${facilityData?.blockCode}`) : "",
        },
        isOperational: facilityData?.isActive ? { code: "YES", name: t("TL_COMMON_YES") } : { code: "NO", name: t("TL_COMMON_NO") },
        isOnmReady: facilityData?.isOnmReady ? { code: "YES", name: t("TL_COMMON_YES") } : { code: "NO", name: t("TL_COMMON_NO") },
        solarSolutionDesignType: solarSolutionDesignTypes.find((type) => type.code === facilityData?.solarDesignCode) || {},
        facilityCategory: facilityCategories.find((type) => type.code === facilityData?.facilityCategoryCode) || {},
        facilityType: facilityTypes.find((type) => type.code === facilityData?.facilityTypeCode) || {},
      });
    }
  }, [facilityData, mdmsResponse, t]);

  /**
   * Save from the Edit form. A change to an identifier (the PoC's login) or to District/Block
   * goes through one confirmation first; anything else saves straight away. Either way it is one
   * update call.
   */
  const handleFacilityUpdate = (formData) => {
    const edit = facilityEditChanges(createdFacility, formData);
    if (edit.changes.length) {
      setPendingEdit({ formData, edit });
    } else {
      saveFacility(formData, edit);
    }
  };

  const saveFacility = async (formData, edit) => {
    setPendingEdit(null);
    try {
      setBlockUI(true);
      const facilityCategoryValue = formData?.facilityCategory;
      const facilityTypeValue = formData?.facilityType;
      const solarDesignValue = formData?.solarSolutionDesignType;

      const facilityCategoryCode = facilityCategoryValue?.code;
      const facilityTypeCode = facilityTypeValue?.code;
      const solarDesignCode = solarDesignValue?.code;
      const isOperational = formData?.isOperational?.code === "YES";
      const isOnmReady = isOperational && formData?.isOnmReady?.code === "YES";

      const payload = {
        FacilityUpdate: {
          ...facilityData?.facility,
          tenant_id: tenantId,
          facility_name: formData?.facilityName,
          facility_category: facilityCategoryCode,
          facility_type: facilityTypeCode,
          isActive: isOperational,
          isOnmReady: isOnmReady,
          address: {
            tenantId: tenantId,
            ...(formData?.latitude ? { latitude: parseFloat(formData.latitude) } : {}),
            ...(formData?.longitude ? { longitude: parseFloat(formData.longitude) } : {}),
          },
          facility_poc_name: formData?.facilityPocName,
          facility_poc_phone: formData?.facilityPocPhone,
          facility_poc_email: formData?.facilityPocEmail,
          // Only the identifiers this category's form shows; the others keep their stored value.
          // A cleared field goes as "", which the backend saves as cleared.
          ...("hfrId" in formData ? { hfr_id: `${formData.hfrId || ""}`.trim() } : {}),
          ...("ninId" in formData ? { nin_id: `${formData.ninId || ""}`.trim() } : {}),
          ...("facilityPocUsername" in formData ? { facility_poc_username: `${formData.facilityPocUsername || ""}`.trim() } : {}),
          // Present only when the block changed: the backend moves the facility, its incidents and
          // every HRMS jurisdiction on it to the new block.
          ...(edit?.blockBoundaryCode ? { blockBoundaryCode: edit.blockBoundaryCode } : {}),
          facility_details: {
            solar_solution_design_type: solarDesignCode,
          },
        },
      };

      await FacilityService.updateFacility(payload);

      if (!isOperational && !isOnmReady) {
        await queryClient.invalidateQueries(["FACILITY"]);
        setBlockUI(false);
        setShowEditFacilityModal(false);
        history.push(`/${window?.contextPath}/employee/fa/facilities`);
        return;
      }

      await invalidateFacilityDetails();
      if (edit?.blockBoundaryCode) await queryClient.invalidateQueries(["FACILITY"]);

      setBlockUI(false);
      setShowEditFacilityModal(false);
      setFieldErrors({});
      setToast({
        key: "success",
        label: t("FACILITY_UPDATION_SUCCESS"),
      });
    } catch (e) {
      console.error("Failed to update facility", e);
      setBlockUI(false);
      const clashes = duplicateIdentifierFields(e);
      setFieldErrors(Object.fromEntries(clashes.map((field) => [field, "FA_IDENTIFIER_ALREADY_USED"])));
      setFormToast({
        key: "error",
        label: apiErrorText(e) || CommonUtils.getApiErrorMessage(e) || t("FACILITY_UPDATION_FAILED"),
      });
    }
  }

  if (isLoading || mdmsLoading) {
    return <Loader />;
  }

  const InfoItem = ({ title, value }) => (
    <div
      style={{
        display: "flex",
        marginBottom: "10px",
        gap: "15px",
      }}
    >
      <div
        style={{
          fontWeight: "bold",
          width: "50%",
        }}
      >
        {title}
      </div>
      <div>{value || t("CORE_COMMON_NOT_APPLICABLE")}</div>
    </div>
  );

  return (
    <div style={{ marginTop: "20px", padding: "16px", overflow: "auto", backgroundColor: "white" }}>
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
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "20px" }}>
        <h1
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            margin: "0",
            color: "#0B0C0C",
          }}
        >
          {facilityId} {t("DETAILS")}
        </h1>
        <Button
          variation="secondary"
          label={t("CORE_COMMON_EDIT")}
          onButtonClick={() => setShowEditFacilityModal(true)}
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
        />
      </div>
      <div>
        <InfoItem title={t("FACILITY_NAME")} value={createdFacility?.facilityName} />
        <InfoItem title={t("FACILITY_CATEGORY")} value={createdFacility?.facilityCategory?.name} />
        <InfoItem title={t("FACILITY_TYPE")} value={createdFacility?.facilityType?.name} />
        <InfoItem title={t("FACILITY_SOLAR_SOLUTION_DESIGN_TYPE")} value={createdFacility?.solarSolutionDesignType?.name} />
        <InfoItem title={t("FACILITY_POC_NAME")} value={createdFacility?.facilityPocName} />
        {createdFacility?.facilityCategory?.code !== "HEALTH" && (
          <InfoItem title={t("FACILITY_POC_USERNAME")} value={createdFacility?.facilityPocUsername} />
        )}
        <InfoItem title={t("FACILITY_POC_PHONE")} value={createdFacility?.facilityPocPhone} />
        <InfoItem title={t("FACILITY_POC_EMAIL")} value={createdFacility?.facilityPocEmail} />
        {createdFacility?.facilityCategory?.code === "HEALTH" && (
          <Fragment>
            <InfoItem title={t("FACILITY_HFR_ID")} value={createdFacility?.hfrId} />
            <InfoItem title={t("FACILITY_NIN_ID")} value={createdFacility?.ninId} />
          </Fragment>
        )}
        <InfoItem title={t("FACILITY_LATITUDE")} value={createdFacility?.latitude} />
        <InfoItem title={t("FACILITY_LONGITUDE")} value={createdFacility?.longitude} />
        <Section title={t("GEOGRAPHY_DETAILS")}>
          <InfoItem title={t("CS_STATE")} value={createdFacility?.state?.name} />
          <InfoItem title={t("CS_DISTRICT")} value={createdFacility?.district?.name} />
          <InfoItem title={t("CS_BLOCK")} value={createdFacility?.block?.name} />
        </Section>
        <VendorDetails t={t} facilityId={facilityId} setToast={setToast} />
      </div>
      {showEditFacilityModal && (
        <FacilityModal
          t={t}
          title={"EDIT_FACILITY"}
          createdFacility={createdFacility}
          onSubmit={handleFacilityUpdate}
          onClose={() => {
            setShowEditFacilityModal(false);
            setFieldErrors({});
          }}
          formToast={formToast}
          setFormToast={setFormToast}
          fieldErrors={fieldErrors}
        />
      )}
      {pendingEdit && (
        <ConfirmChangesModal
          t={t}
          title={"FA_CONFIRM_FACILITY_CHANGES"}
          description={"FA_CONFIRM_FACILITY_CHANGES_DESC"}
          confirmLabel={"FA_SAVE_CHANGES"}
          warnings={[
            ...(pendingEdit.edit.loginChange
              ? [
                  pendingEdit.edit.loginChange.to
                    ? t("FA_WARN_POC_LOGIN_CHANGES", { from: pendingEdit.edit.loginChange.from || "-", to: pendingEdit.edit.loginChange.to, interpolation: { escapeValue: false } })
                    : t("FA_WARN_POC_LOGIN_REMOVED"),
                ]
              : []),
            ...(pendingEdit.edit.blockBoundaryCode ? [t("FA_WARN_FACILITY_MOVES")] : []),
          ]}
          onConfirm={() => saveFacility(pendingEdit.formData, pendingEdit.edit)}
          onCancel={() => setPendingEdit(null)}
        >
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "15px" }}>
            <thead>
              <tr style={{ textAlign: "left", borderBottom: "1px solid #D6D5D4" }}>
                <th style={{ padding: "8px 8px 8px 0" }}>{t("FA_FIELD")}</th>
                <th style={{ padding: "8px" }}>{t("FA_CURRENT_VALUE")}</th>
                <th style={{ padding: "8px 0 8px 8px" }}>{t("FA_NEW_VALUE")}</th>
              </tr>
            </thead>
            <tbody>
              {pendingEdit.edit.changes.map((change) => (
                <tr key={change.key} style={{ borderBottom: "1px solid #EEEEEE" }}>
                  <td style={{ padding: "8px 8px 8px 0", fontWeight: 700 }}>{t(change.labelKey)}</td>
                  <td style={{ padding: "8px", wordBreak: "break-word" }}>
                    {change.from ? (change.translate ? t(change.from) : change.from) : t("CORE_COMMON_NOT_APPLICABLE")}
                  </td>
                  <td style={{ padding: "8px 0 8px 8px", wordBreak: "break-word" }}>
                    {change.to ? (change.translate ? t(change.to) : change.to) : t("FA_VALUE_CLEARED")}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </ConfirmChangesModal>
      )}
      <Tab
        activeLink={activeTab}
        configItemKey={"code"}
        configDisplayKey={"name"}
        configNavItems={[
          {
            code: "ACTIVITY",
            name: "ACTIVITY",
          },
          {
            code: "ASSET",
            name: "ASSET",
          },
          {
            code: "AMC",
            name: "AMC",
          },
        ]}
        setActiveLink={(tabCode) => {
          setActiveTab(tabCode);
        }}
        showNav
      >
        <div className="tab-content-wrapper">
          {activeTab === "ACTIVITY" && <ActivityTable t={t} facilityId={facilityId} />}
          {activeTab === "ASSET" && <AssetTable t={t} facilityId={facilityId} />}
          {activeTab === "AMC" && <AMCTable t={t} facilityId={facilityId} />}
        </div>
      </Tab>
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            zIndex: 100000000,
            ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={toast.label}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default FacilityDetails;
