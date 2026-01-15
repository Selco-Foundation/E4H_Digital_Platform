import React, { useEffect, useState } from "react";
import Summary from "../../components/ActivityDetails/Summary";
import AuditTrail from "../../components/ActivityDetails/AuditTrail";
import { useDispatch } from "react-redux";
import { populateWorkingActivity, populateWorkingFacility } from "../../redux/actions";
import { Loader } from "@egovernments/digit-ui-react-components";
import InfoCard from "../../components/ActivityDetails/InfoCard";
import useActivityDetails from "../../hooks/useActivityDetails";
import useActivityAsset from "../../hooks/useActivityAsset";
import { useTranslation } from "react-i18next";

const ActivityDetails = () => {

  const { t } = useTranslation();
  const [assets, setAssets] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const activityFacilityId = url.split("activities/")[1].split("/")[0].split("?")[0];
  const [facilityDetails, setActivityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [aggregatedAssets, setAggregatedAssets] = useState({});

  const {
    isLoading: facilityDataLoading,
    data: facilityData,
  } = useActivityDetails(activityFacilityId);

  const { isLoading, data: assetData } = useActivityAsset(activityFacilityId);

  useEffect(() => {
    if (assetData) {
      setAssets(assetData);
    }
  }, [assetData]);

  useEffect(() => {
    if (facilityData) {
      setAuditTrail(facilityData.auditTrail);
      setActivityDetails(facilityData.facilityDetails);
      setAggregatedAssets(facilityData.assetAggregation);
      dispatch(populateWorkingFacility(facilityData.facilityDetails));
      dispatch(populateWorkingActivity(facilityData.activityDetails));
    }
  }, [facilityData]);

  if (isLoading || facilityDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
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

    </div>
  );
}

export default ActivityDetails;