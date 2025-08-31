import React, { useEffect, useState } from "react";
import Summary from "../../components/FacilityDetails/Summary";
import QCActions from "../../components/FacilityDetails/QCActions";
import AuditTrail from "../../components/FacilityDetails/AuditTrail";
import { useDispatch } from "react-redux";
import { clearRejectionReasons, setSelectedFacility, setSelectedFieldPlan } from "../../redux/actions";
import { Loader } from "@egovernments/digit-ui-react-components";
import useFieldPlan from "../../hooks/useFieldPlan";
import useFacilityDetails from "../../hooks/useFacilityDetails";
import useAsset from "../../hooks/useAsset";

const FacilityDetails = ({t}) => {

  const [assets, setAssets] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const fieldPlanId = url.split("field-plan/")[1].split("/")[0];
  const facilityIdentifier = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityProjectId = facilityIdentifier.split("--")[0];
  const facilityId = decodeURIComponent(facilityIdentifier.split("--")[1]);
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [aggregatedAssets, setAggregatedAssets] = useState({});
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);

  const {
    isLoading: fieldPlanDataLoading,
    isFetching: fieldPlanDataFetching,
    data: fieldPlanData,
    revalidate: revalidateFieldPlans
  } = useFieldPlan({
    Project : {
      projectTypeId: "FieldPlan",
      id: [fieldPlanId]
    }
  });

  const {
    isLoading: facilityDataLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
    revalidate: revalidateFacilityDetails,
    revalidateFacilities
  } = useFacilityDetails(facilityProjectId);

  const { isLoading, data: assetData } = useAsset(facilityId);

  useEffect(() => {
    if (assetData) {
      setAssets(assetData);
    }
  }, [assetData]);

  useEffect(() => {
    if (fieldPlanData) {
      dispatch(setSelectedFieldPlan(fieldPlanData.fieldPlans[0]));
    }
  }, [fieldPlanData]);

  useEffect(() => {
    if (facilityData) {
      setAuditTrail(facilityData.auditTrail);
      setFacilityDetails(facilityData.facilityDetails);
      setAggregatedAssets(facilityData.assetAggregation);
      dispatch(setSelectedFacility(facilityData.facilityDetails));
    }
  }, [facilityData]);

  useEffect(() => {
    return () => {
      dispatch(clearRejectionReasons());
    }
  }, []);

  if (isLoading || facilityDataLoading || fieldPlanDataLoading) {
    return <Loader />;
  }

  const revalidateData = () => {
    revalidateFieldPlans();
    revalidateFacilities();
    revalidateFacilityDetails();
  }

  return (
    <div style={{marginTop: "20px"}}>
      { (updatingWorkflow || fieldPlanDataFetching || facilityDataFetching) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 5,
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
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
          {facilityDetails.facilityName}
      </div>
      <div style={{
        marginTop: "15px",
        width: "95%",
        padding: "20px",
        background: "white",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        border: "1px solid #eee",
      }}>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>District</strong></div>
          { facilityDetails.district ? t(`DISTRICT_${facilityDetails.district.toUpperCase()}`) : "-" }
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Block</strong></div>
          { facilityDetails.block ? t(`BLOCK_${facilityDetails.block.toUpperCase()}`) : "-" }
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Health Facility Type</strong></div>
          { facilityDetails.facilityType ? facilityDetails.facilityType : "-" }
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Status</strong></div>
          { facilityDetails.status ? t(`CS_${facilityDetails.status}`) : "-" }
        </div>
      </div>

      {auditTrail?.length > 0 && <AuditTrail t={t} auditTrail={auditTrail} />}

      {assets && assets.map((asset) => {
        return <Summary
          sectionName={asset?.assetName}
          count={asset?.count}
          specifications={asset?.specifications}
          details={asset?.details}
          items={asset?.items}
          images={aggregatedAssets.images?.[asset.assetType]}
          videos={aggregatedAssets.videos?.[asset.assetType]}
        />
      })}

      {aggregatedAssets?.installationReport && (
        <Summary
          sectionName="InstallationCompletionReport"
          report={{
            ...aggregatedAssets?.installationReport,
            name: facilityDetails.facilityName
          }}
          isReport={true}
        />
      )}

      {facilityDetails?.status && facilityDetails?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && (
        <QCActions
          revalidateData={revalidateData}
          setUpdatingWorkflow={setUpdatingWorkflow}
        />
      )}

    </div>
  );
}

export default FacilityDetails;
