import React, { useEffect, useState } from "react";
import Summary from "../../components/FacilityDetails/Summary";
import QCActions from "../../components/FacilityDetails/QCActions";
import AuditTrail from "../../components/FacilityDetails/AuditTrail";
import { useDispatch } from "react-redux";
import { clearRejectionReasons, setSelectedFacility, setSelectedFieldPlan } from "../../redux/actions";
import { Loader } from "@egovernments/digit-ui-react-components";

const FacilityDetails = ({t}) => {

  const [fetchedData, setData] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const fieldPlanId = url.split("field-plan/")[1].split("/")[0];
  const facilityIdentifier = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityProjectId = facilityIdentifier.split("--")[0];
  const facilityId = decodeURIComponent(facilityIdentifier.split("--")[1]);
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);

  const [pdfFile, setPdfFile] = useState({
    name: "Alkod.pdf",
    size: "3.5 MB"
  });

  const { data: fieldPlanData } = Digit.Hooks.qc.useFieldPlan({
    Project : {
      projectTypeId: "FieldPlan",
      id: [fieldPlanId]
    }
  });
  const { data: facilityData } = Digit.Hooks.qc.useFacilityDetails(facilityProjectId);
  const { isLoading, data: assetData } = Digit.Hooks.qc.useAsset(facilityId);

  useEffect(() => {
    if (assetData) {
      setData(assetData);
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
      dispatch(setSelectedFacility(facilityData.facilityDetails));
    }
  }, [facilityData]);

  useEffect(() => {
    return () => {
      dispatch(clearRejectionReasons());
    }
  }, []);

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
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

      {fetchedData && fetchedData.map((asset) => {
        return <Summary
          sectionName={asset?.assetName}
          count={asset?.count}
          specifications={asset?.specifications}
          details={asset?.details}
          items={asset?.items}
        />
      })}

      {pdfFile && <Summary sectionName="InstallationCompletionReport" pdf={pdfFile} isReport={true} />}

      {facilityDetails?.status && facilityDetails?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && <QCActions />}

    </div>
  );
}

export default FacilityDetails;