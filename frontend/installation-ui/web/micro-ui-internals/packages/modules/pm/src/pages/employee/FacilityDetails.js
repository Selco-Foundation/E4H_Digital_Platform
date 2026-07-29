import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { Loader } from "@egovernments/digit-ui-react-components";
import useFieldPlan from "../../hooks/useFieldPlan";
import useFacilityDetails, { getAssetAggregation } from "../../hooks/useFacilityDetails";
import useAsset from "../../hooks/useAsset";
import InfoCard from "../../components/FacilityDetails/InfoCard";
import AuditTrail from "../../components/FacilityDetails/AuditTrail";
import Summary from "../../components/FacilityDetails/Summary";
import InstallationImageReviewCard from "../../components/FacilityDetails/InstallationImageReviewCard";
import { populateWorkingFieldPlan, populateWorkingFacility } from "../../redux/actions";

const sectionLoaderStyle = {
  width: "95%",
  minWidth: "900px",
  marginTop: "15px",
  padding: "20px",
  background: "white",
  borderRadius: "4px",
  boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
  border: "1px solid #eee",
  color: "#0B4B66",
  fontSize: "20px",
  fontWeight: "bold",
};

const FacilityDetails = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const dispatch = useDispatch();
  const [assets, setAssets] = useState([]);
  const url = window.location.href;
  const fieldPlanId = url.split("field-plans/")[1].split("/")[0];
  const facilityId = url.split("facilities/")[1].split("/")[0];
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [aggregatedDocuments, setAggregatedDocuments] = useState({});
  const [workflowDocuments, setWorkflowDocuments] = useState([]);
  const [installationImages, setInstallationImages] = useState([]);
  const [loadedDocumentSections, setLoadedDocumentSections] = useState({});
  const [loadingDocumentSections, setLoadingDocumentSections] = useState({});

  const {
    isLoading: fieldPlanDataLoading,
    data: fieldPlanData,
  } = useFieldPlan({
    ids: [fieldPlanId]
  });

  const {
    isLoading: facilityDataLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
  } = useFacilityDetails(facilityId);

  const { isLoading: assetDataLoading, data: assetData } = useAsset(facilityId);

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
    const fieldPlan = fieldPlanData?.fieldPlans?.[0];
    if (fieldPlan) {
      dispatch(populateWorkingFieldPlan(fieldPlan));
    }
  }, [fieldPlanData]);

  useEffect(() => {
    if (facilityData) {
      setAuditTrail(facilityData.auditTrail);
      setFacilityDetails(facilityData.facilityDetails);
      setWorkflowDocuments(facilityData.workflow?.[0]?.documents || []);
      dispatch(populateWorkingFacility(facilityData.facilityDetails));
    }
  }, [facilityData]);

  const updateDocumentAggregation = (documentAggregation = {}) => {
    setAggregatedDocuments((prev) => ({
      ...prev,
      images: {
        ...(prev.images || {}),
        ...(documentAggregation.images || {}),
      },
      videos: {
        ...(prev.videos || {}),
        ...(documentAggregation.videos || {}),
      },
      installationReportDocuments: documentAggregation.installationReportDocuments?.length ? documentAggregation.installationReportDocuments : prev.installationReportDocuments,
      installationCompletionCertificate: documentAggregation.installationCompletionCertificate?.length ? documentAggregation.installationCompletionCertificate : prev.installationCompletionCertificate,
      assetHandoverDocument: documentAggregation.assetHandoverDocument?.length ? documentAggregation.assetHandoverDocument : prev.assetHandoverDocument,
      bomCompletionReport: documentAggregation.bomCompletionReport || prev.bomCompletionReport,
    }));
  };

  const loadSectionDocuments = async (section) => {
    if (!section || loadedDocumentSections[section] || loadingDocumentSections[section]) {
      return;
    }

    setLoadingDocumentSections((prev) => ({ ...prev, [section]: true }));
    try {
      const sectionDocuments = await getAssetAggregation(facilityData?.workflow, section);
      updateDocumentAggregation(sectionDocuments.documentAggregation);

      if (sectionDocuments.installationImages?.length > 0) {
        setInstallationImages((prev) => {
          const loadedImageCodes = sectionDocuments.installationImages.map((image) => image.imageCode?.toUpperCase());
          const nextImages = prev.filter((image) => !loadedImageCodes.includes(image.imageCode?.toUpperCase()));
          return [...nextImages, ...sectionDocuments.installationImages];
        });
      }
      setLoadedDocumentSections((prev) => ({ ...prev, [section]: true }));
    } catch (error) {
      console.error(`Failed to load documents for section ${section}:`, error);
    } finally {
      setLoadingDocumentSections((prev) => ({ ...prev, [section]: false }));
    }
  };

  const installationImageCriteria = mdmsResponse?.["common-masters"]?.["InstallationImages"]?.[0]?.["InstallationImage"] || [];
  const hasInstallationReport = facilityData?.workflow?.[0]?.documents?.some((document) => (
    [
      "INSTALLATION_REPORT",
      "INSTALLATION_REPORT_BOM",
      "INSTALLATION_COMPLETION_CERTIFICATE",
      "ASSET_HANDOVER_DOCUMENT",
    ].includes(document.documentType?.toUpperCase())
  ));

  if (facilityDataLoading || fieldPlanDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      { facilityDataFetching && (
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

      {assetDataLoading && <div style={sectionLoaderStyle}>{t("CORE_COMMON_LOADING")}</div>}

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
          isLoadingContent={loadingDocumentSections[asset.assetType]}
          onExpand={() => loadSectionDocuments(asset.assetType)}
        />
      })}

      {mdmsLoading && (
        <div style={sectionLoaderStyle}>{t("CORE_COMMON_LOADING")}</div>
      )}

      {hasInstallationReport && (
        <Summary
          t={t}
          sectionName="InstallationCompletionReport"
          section="INSTALLATION_COMPLETION_REPORT"
          report={aggregatedDocuments?.bomCompletionReport ? {
            ...aggregatedDocuments?.bomCompletionReport,
            name: `${facilityDetails.facilityName}.pdf`
          } : null}
          supportingDocuments={aggregatedDocuments.installationReportDocuments}
          installationCompletionCertificate={aggregatedDocuments.installationCompletionCertificate}
          assetHandoverDocument={aggregatedDocuments.assetHandoverDocument}
          installationImages={[]}
          isReport={true}
          isLoadingContent={loadingDocumentSections.INSTALLATION_COMPLETION_REPORT}
          onExpand={() => loadSectionDocuments("INSTALLATION_COMPLETION_REPORT")}
        />
      )}

      {installationImageCriteria.map((criterion, index) => {
        const section = `INSTALLATION_IMAGE_${criterion.code}`.toUpperCase();
        const images = installationImages.filter((image) => image.imageCode?.toUpperCase() === criterion.code?.toUpperCase());
        return (
          <InstallationImageReviewCard
            key={criterion.code || index}
            t={t}
            installationImage={{
              code: criterion.code,
              description: criterion.description,
              images,
              providedImagesCount: images.length,
              requiredImagesCount: criterion["required_count"],
            }}
            index={index}
            isLoadingContent={loadingDocumentSections[section]}
            onExpand={() => loadSectionDocuments(section)}
          />
        );
      })}

    </div>
  );
}

export default FacilityDetails;
