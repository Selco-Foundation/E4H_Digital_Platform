import React, { useEffect, useState } from "react";
import { Loader, Button, Toast } from "@egovernments/digit-ui-react-components";
import { Tab } from "@egovernments/digit-ui-components";
import useFacilityDetails from "../../hooks/useFacilityDetails";
import { populateWorkingFacility } from "../../redux/actions";
import { useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import Section from "../../components/FacilityDetails/Section";
import FacilityModal from "../../components/FacilityModal";
import ActivityTable from "../../components/FacilityDetails/ActivityTable";
import AssetTable from "../../components/FacilityDetails/AssetTable";
import AMCTable from "../../components/FacilityDetails/AMCTable";
import { FacilityService } from "../../services/Facility";

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
  const [activeTab, setActiveTab] = useState("ACTIVITY");
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [blockUI, setBlockUI] = useState(null);

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

  const { isLoading, data: facilityData} = useFacilityDetails(facilityId);
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
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const solarSolutionDesignTypes = mdmsResponse?.facility?.SolarSolutionDesignType || [];
  const facilityTypes = mdmsResponse?.facility?.FacilityType || [];

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
        facilityType: facilityTypes.find((type) => type.code === facilityData?.facilityTypeCode) || {},
      });
    }
  }, [facilityData, mdmsResponse, t]);

  const handleFacilityUpdate = async (formData) => {
    try {
      setBlockUI(true);
      const facilityTypeValue = formData?.facilityType;
      const solarDesignValue = formData?.solarSolutionDesignType;

      const facilityTypeCode = facilityTypeValue?.code;
      const solarDesignCode = solarDesignValue?.code;

      const payload = {
        FacilityUpdate: {
          ...facilityData?.facility,
          tenant_id: tenantId,
          facility_name: formData?.facilityName,
          facility_type: facilityTypeCode,
          isActive: formData?.isActive?.code === "YES",
          isOnmReady: formData?.isOnmReady?.code === "YES",
          address: {
            tenantId: tenantId,
            ...(formData?.latitude ? { latitude: formData.latitude } : {}),
            ...(formData?.longitude ? { longitude: formData.longitude } : {}),
          },
          facility_poc_name: formData?.facilityPocName,
          facility_poc_phone: formData?.facilityPocPhone,
          facility_poc_email: formData?.facilityPocEmail,
          facility_details: {
            solar_solution_design_type: solarDesignCode,
          },
        },
      };

      await FacilityService.updateFacility(payload);

      setBlockUI(false);
      setShowEditFacilityModal(false);
      setToast({
        key: "success",
        label: t("FACILITY_UPDATION_SUCCESS"),
      });
    } catch (e) {
      console.error("Failed to upload facility", e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: t("FACILITY_UPDATION_FAILED"),
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
        <InfoItem title={t("FACILITY_TYPE")} value={createdFacility?.facilityType?.name} />
        <InfoItem title={t("FACILITY_SOLAR_SOLUTION_DESIGN_TYPE")} value={createdFacility?.solarSolutionDesignType?.name} />
        <InfoItem title={t("FACILITY_POC_NAME")} value={createdFacility?.facilityPocName} />
        <InfoItem title={t("FACILITY_POC_PHONE")} value={createdFacility?.facilityPocPhone} />
        <InfoItem title={t("FACILITY_POC_EMAIL")} value={createdFacility?.facilityPocEmail} />
        <InfoItem title={t("FACILITY_HFR_ID")} value={createdFacility?.hfrId} />
        <InfoItem title={t("FACILITY_NIN_ID")} value={createdFacility?.ninId} />
        <InfoItem title={t("FACILITY_PINCODE")} value={createdFacility?.pincode} />
        <Section title={t("GEOGRAPHY_DETAILS")}>
          <InfoItem title={t("CS_STATE")} value={createdFacility?.state?.name} />
          <InfoItem title={t("CS_DISTRICT")} value={createdFacility?.district?.name} />
          <InfoItem title={t("CS_BLOCK")} value={createdFacility?.block?.name} />
        </Section>
      </div>
      {showEditFacilityModal && (
        <FacilityModal
          t={t}
          title={"EDIT_FACILITY"}
          createdFacility={createdFacility}
          onSubmit={handleFacilityUpdate}
          onClose={() => setShowEditFacilityModal(false)}
        />
      )}
      <Tab
        activeLink={activeTab}
        configItemKey={"code"}
        configDisplayKey={"name"}
        configNavItems={[
          {
            code: "ACTIVITY",
            name: "Activity",
          },
          {
            code: "ASSET",
            name: "Asset",
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
