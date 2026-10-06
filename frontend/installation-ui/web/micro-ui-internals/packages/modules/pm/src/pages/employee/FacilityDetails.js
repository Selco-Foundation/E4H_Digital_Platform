import React, {Fragment, useEffect, useState } from "react";
import { Loader } from "@egovernments/digit-ui-react-components";
import { Tab } from "@egovernments/digit-ui-components";
import useFacilityDetails from "../../hooks/useFacilityDetails";
import { populateWorkingFacilityDetails } from "../../redux/actions";
import { useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import Section from "../../components/FacilityDetails/Section";
import ActivityTable from "../../components/FacilityDetails/ActivityTable";
import AssetTable from "../../components/FacilityDetails/AssetTable";
import AMCTable from "../../components/FacilityDetails/AMCTable";

const FacilityDetails = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const url = window.location.href;
  const encodedFacilityId = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityId = decodeURIComponent(encodedFacilityId);
  const [createdFacility, setCreatedFacility] = useState({});
  const dispatch = useDispatch();
  const [activeTab, setActiveTab] = useState("ACTIVITY");

  const {
    isLoading, data: facilityData,
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
      dispatch(populateWorkingFacilityDetails(facilityData));
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
      </div>
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
    </div>
  );
};

export default FacilityDetails;
