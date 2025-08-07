import React, { useEffect, useState } from "react";
import Summary from "../../components/FacilityDetails/Summary";
import QCActions from "../../components/FacilityDetails/QCActions";
import AuditTrial from "../../components/FacilityDetails/AuditTrial";
import { useDispatch, useSelector } from "react-redux";
import { clearRejectionReasons, setSelectedFacility, setSelectedFieldPlan } from "../../redux/actions";
import { Loader } from "@egovernments/digit-ui-react-components";

const FacilityDetails = ({t}) => {

  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);
  const [fetchedData, setData] = useState([]);
  const dispatch = useDispatch();
  const url = window.location.href;
  const fieldPlanId = url.split("field-plan/")[1].split("/")[0];
  const facilityIdentifier = url.split("facilities/")[1].split("/")[0].split("?")[0];
  const facilityProjectId = facilityIdentifier.split("--")[0];
  const facilityId = decodeURIComponent(facilityIdentifier.split("--")[1]);

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
  const { data: facilityData } = Digit.Hooks.qc.useFacility({
    project : {
      id: [facilityProjectId],
    }
  })
  const { isLoading, data: assetData } = Digit.Hooks.qc.useFacilityDetails(facilityId);

  useEffect(() => {
    if (assetData) {
      setData(assetData);
    }
  }, [assetData]);

  useEffect(() => {
    if (fieldPlanData) {
      dispatch(setSelectedFieldPlan(fieldPlanData.fieldPlans[0]));
    }
  }, [fieldPlanData])

  useEffect(() => {
    if (facilityData) {
      dispatch(setSelectedFacility(facilityData.facilities[0]));
    }
  }, [facilityData]);

  useEffect(() => {
    return () => {
      dispatch(clearRejectionReasons());
    }
  }, []);

  const hospitalDetails = {
    ...selectedFacility,
    healthFacilityType: "Loc 1"
  }

  const auditTrail = [
    {
      status: "Submitted",
      date: "25/05/25",
    },
    {
      status: "Rejected",
      date: "05/05/25",
      reasons: [
        {
          section: "Inverter",
          reasons: [
            { title: "Rejection Reason 1", details: "Additional Details" },
            { title: "Rejection Reason 2", details: "Additional Details" },
          ],
        },
        {
          section: "Panel",
          reasons: [
            { title: "Rejection Reason 1", details: "Additional Details" },
            { title: "Rejection Reason 2", details: "Additional Details" },
          ],
        },
      ],
    },
    {
      status: "Submitted",
      date: "25/04/25",
    },
  ];

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
          {hospitalDetails.facility}
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
          {hospitalDetails.district}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Block</strong></div>
          {hospitalDetails.block}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Health Facility Type</strong></div>
          {hospitalDetails.healthFacilityType}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Status</strong></div>
          {hospitalDetails.status}
        </div>
      </div>

      {auditTrail && <AuditTrial t={t} auditTrial={auditTrail} />}

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

      {selectedFacility?.status && selectedFacility?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && <QCActions />}

    </div>
  );
}

export default FacilityDetails;