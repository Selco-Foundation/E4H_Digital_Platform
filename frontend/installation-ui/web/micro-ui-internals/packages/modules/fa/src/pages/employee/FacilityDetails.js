import React, { useEffect, useState } from "react";
import { Loader, Button } from "@egovernments/digit-ui-react-components";
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

const FacilityDetails = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const url = window.location.href;
  const encodedFacilityId = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityId = decodeURIComponent(encodedFacilityId);
  const [createdFacility, setCreatedFacility] = useState({});
  const [showEditFacilityModal, setShowEditFacilityModal] = useState(false);
  const dispatch = useDispatch();
  const [activeTab, setActiveTab] = useState("ACTIVITY");

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
        solarSolutionDesignType: solarSolutionDesignTypes.find((type) => type.code === facilityData?.solarDesignCode) || {},
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
          onSubmit={(data) => {
            console.debug("data", data);
            setShowEditFacilityModal(false);
          }}
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
    </div>
  );
};

export default FacilityDetails;
