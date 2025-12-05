import React, { useEffect, useState } from "react";
import Summary from "../../components/VisitDetails/Summary";
import AMCReviewerActions from "../../components/VisitDetails/AMCReviewerActions";
import AuditTrail from "../../components/VisitDetails/AuditTrail";
import { useDispatch } from "react-redux";
import {populateWorkingProject, populateWorkingVisit} from "../../redux/actions";
import { Loader } from "@egovernments/digit-ui-react-components";
import useVisitDetails from "../../hooks/useVisitDetails";
import InfoCard from "../../components/VisitDetails/InfoCard";
import useProject from "../../hooks/useProject";

const VisitDetails = ({t}) => {

  const dispatch = useDispatch();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const visitId = url.split("amc-visits/")[1].split("/")[0].split("?")[0];
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [reportDocumentAggregation, setReportDocumentAggregation] = useState({});
  const [workflowDocuments, setWorkflowDocuments] = useState([]);
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);
  const [visitReport, setVisitReport] = useState(null);

  const { data: projectData } = useProject({
    id: [projectId],
  });

  const {
    isLoading,
    isFetching: visitDataFetching,
    data: visitData,
    revalidate: revalidateFacilityDetails,
    revalidateFacilities
  } = useVisitDetails(visitId);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
    }
  }, [projectData])

  useEffect(() => {
    if (visitData) {
      dispatch(populateWorkingVisit(visitData))
      setAuditTrail(visitData.auditTrail);
      setFacilityDetails(visitData.facilityDetails);
      setReportDocumentAggregation(visitData.reportDocumentAggregation);
      setWorkflowDocuments(visitData.workflowDocuments);
      setVisitReport(visitData.visitReport);
      console.debug("visitData.visitReport", visitData.visitReport);
    }
  }, [visitData]);

  if (isLoading) {
    return <Loader />;
  }

  const revalidateData = () => {
    revalidateFacilities();
    revalidateFacilityDetails();
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {(updatingWorkflow || visitDataFetching) && (
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

      {!!visitReport && (
        <Summary
          t={t}
          sectionName="AMC_INSTALLATION_FORM"
          section="AMC_INSTALLATION_FORM"
          data={visitReport}
          images={visitData?.visitImages || []}
        />
      )}

      {!!reportDocumentAggregation?.amcInstallationForm && (
        <Summary
          t={t}
          sectionName="AMC_INSTALLATION_REPORT"
          section="AMC_INSTALLATION_REPORT"
          report={{
            ...reportDocumentAggregation?.amcInstallationForm,
            name: `${facilityDetails.facilityName}.pdf`
          }}
          supportingDocuments={[]}
          isDocument={true}
        />
      )}

      {facilityDetails?.status && facilityDetails?.status.toUpperCase() === "PENDING_APPROVAL" && (
        <AMCReviewerActions
          t={t}
          revalidateData={revalidateData}
          setUpdatingWorkflow={setUpdatingWorkflow}
          aggregatedDocuments={workflowDocuments}
        />
      )}

    </div>
  );
}

export default VisitDetails;