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
import InstallationImageReviewCard from "../../components/FacilityDetails/InstallationImageReviewCard";

const FacilityDetails = ({t}) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [assets, setAssets] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const activityAssignmentId = url.split("field-plan/")[1].split("/")[0];
  const activityFacilityId = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [aggregatedDocuments, setAggregatedDocuments] = useState({});
  const [workflowDocuments, setWorkflowDocuments] = useState([]);
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);
  const [installationImages, setInstallationImages] = useState([]);

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
  } = useFacilityDetails(activityFacilityId);

  const { isLoading, data: assetData } = useAsset(activityFacilityId);

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "common-masters",
    [
      {
        name: "InstallationImages",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

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
      setAggregatedDocuments(facilityData.documentAggregation);
      setWorkflowDocuments(facilityData.workflowDocuments);
      dispatch(setSelectedFacility(facilityData.facilityDetails));
    }
  }, [facilityData]);

  useEffect(() => {
    const installationImageCriteria = mdmsResponse?.["common-masters"]?.["InstallationImages"]?.[0]?.["InstallationImage"];
    if (facilityData?.installationImages && installationImageCriteria) {
      setInstallationImages(
        installationImageCriteria.map((criterion) => ({
          code: criterion.code,
          description: criterion.description,
          images: facilityData?.installationImages.filter((image) => image.imageCode === criterion.code),
          providedImagesCount: facilityData?.installationImages.filter((image) => image.imageCode === criterion.code).length,
          requiredImagesCount: criterion["required_count"],
        }))
      )
    }
  }, [facilityData, mdmsResponse]);

  useEffect(() => {
    return () => {
      dispatch(clearRejectionReasons());
    }
  }, []);

  if (isLoading || facilityDataLoading || fieldPlanDataLoading || mdmsLoading) {
    return <Loader />;
  }

  const revalidateData = async () => {
    await revalidateFieldPlans();
    await revalidateFacilities();
    await revalidateFacilityDetails();
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
          images={aggregatedDocuments.images?.[asset.assetType]}
          videos={aggregatedDocuments.videos?.[asset.assetType]}
        />
      })}

      {aggregatedDocuments?.bomCompletionReport && (
        <Summary
          t={t}
          sectionName="InstallationCompletionReport"
          section="INSTALLATION_COMPLETION_REPORT"
          report={{
            ...aggregatedDocuments?.bomCompletionReport,
            name: `${facilityDetails.facilityName}.pdf`
          }}
          supportingDocuments={aggregatedDocuments.installationReportDocuments}
          installationCompletionCertificate={aggregatedDocuments.installationCompletionCertificate}
          assetHandoverDocument={aggregatedDocuments.assetHandoverDocument}
          installationImages={[]}
          isReport={true}
        />
      )}

      {installationImages.map((installationImage, index) => (
        <InstallationImageReviewCard
          key={installationImage.code || index}
          t={t}
          installationImage={installationImage}
          index={index}
        />
      ))}

      {facilityDetails?.status && facilityDetails?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && (
        <QCActions
          t={t}
          revalidateData={revalidateData}
          setUpdatingWorkflow={setUpdatingWorkflow}
          workflowDocuments={workflowDocuments}
        />
      )}

    </div>
  );
}

export default FacilityDetails;