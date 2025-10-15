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
import InfoCard from "../../components/FacilityDetails/InfoCard";

const FacilityDetails = ({t}) => {

  const [assets, setAssets] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const activityAssignmentId = url.split("field-plan/")[1].split("/")[0];
  const facilityAssignmentId = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const [facilityId, setFacilityId] = useState("");
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [aggregatedAssets, setAggregatedAssets] = useState({});
  const [aggregatedDocuments, setAggregatedDocuments] = useState([]);
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);

  const {
    isLoading: fieldPlanDataLoading,
    isFetching: fieldPlanDataFetching,
    data: fieldPlanData,
    revalidate: revalidateFieldPlans
  } = useFieldPlan({
    id: [activityAssignmentId]
  });

  const {
    isLoading: facilityDataLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
    revalidate: revalidateFacilityDetails,
    revalidateFacilities
  } = useFacilityDetails(facilityAssignmentId);

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
      setFacilityId(facilityData.facilityId);
      setAuditTrail(facilityData.auditTrail);
      setFacilityDetails(facilityData.facilityDetails);
      setAggregatedAssets(facilityData.assetAggregation);
      setAggregatedDocuments(facilityData.documentAggregation);
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
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      { (updatingWorkflow || fieldPlanDataFetching || facilityDataFetching) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000000,
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
      <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
          {facilityDetails.facilityName}
      </div>

      <InfoCard t={t} facilityDetails={facilityDetails} />

      {auditTrail?.length > 0 && <AuditTrail t={t} auditTrail={auditTrail} />}

      {assets && assets.map((asset) => {
        return <Summary
          t={t}
          key={asset.assetType}
          sectionName={asset?.assetName}
          section={asset.assetType}
          count={asset?.count}
          specifications={asset?.specifications}
          details={asset?.details}
          items={asset?.items}
          images={aggregatedAssets.images?.[asset.assetType]}
          videos={aggregatedAssets.videos?.[asset.assetType]}
        />
      })}

      {aggregatedAssets?.bomCompletionReport && (
        <Summary
          t={t}
          sectionName="InstallationCompletionReport"
          section="INSTALLATION_COMPLETION_REPORT"
          report={{
            ...aggregatedAssets?.bomCompletionReport,
            name: `${facilityDetails.facilityName}.pdf`
          }}
          supportingDocuments={aggregatedAssets.installationReportDocuments}
          isReport={true}
        />
      )}

      {facilityDetails?.status && facilityDetails?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && (
        <QCActions
          revalidateData={revalidateData}
          setUpdatingWorkflow={setUpdatingWorkflow}
          aggregatedDocuments={aggregatedDocuments}
        />
      )}

    </div>
  );
}

export default FacilityDetails;