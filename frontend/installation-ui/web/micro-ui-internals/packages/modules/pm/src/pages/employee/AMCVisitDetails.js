import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { Loader } from "@egovernments/digit-ui-react-components";
import useAMCConfigurationList from "../../hooks/useAMCConfigurationList";
import useAMCVisitDetails from "../../hooks/useAMCVisitDetails";
import InfoCard from "../../components/AMCVisitDetails/InfoCard";
import AuditTrail from "../../components/AMCVisitDetails/AuditTrail";
import Summary from "../../components/AMCVisitDetails/Summary";
import { populateWorkingAMCConfiguration, populateWorkingAMCVisit } from "../../redux/actions";

const AMCVisitDetails = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const url = window.location.href;
  const configurationId = url.split("amc-configurations/")[1].split("/")[0];
  const visitId = url.split("visits/")[1].split("/")[0].split("?")[0];
  const [facilityDetails, setFacilityDetails] = useState({});
  const [auditTrail, setAuditTrail] = useState([]);
  const [reportDocumentAggregation, setReportDocumentAggregation] = useState({});
  const [visitReport, setVisitReport] = useState(null);

  const { isLoading: configurationDataLoading, data: configurationData } = useAMCConfigurationList({
    ids: [configurationId],
  });

  const {
    isLoading,
    isFetching: visitDataFetching,
    data: visitData,
  } = useAMCVisitDetails(visitId);

  useEffect(() => {
    const amcConfiguration = configurationData?.amcConfigurations?.[0];
    if (amcConfiguration) {
      dispatch(populateWorkingAMCConfiguration(amcConfiguration));
    }
  }, [configurationData])

  useEffect(() => {
    if (visitData) {
      dispatch(populateWorkingAMCVisit(visitData))
      setAuditTrail(visitData.auditTrail);
      setFacilityDetails(visitData.facilityDetails);
      setReportDocumentAggregation(visitData.reportDocumentAggregation);
      setVisitReport(visitData.visitReport);
    }
  }, [visitData]);

  if (isLoading || configurationDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {visitDataFetching && (
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
          section="AMC_INSTALLATION_FORM"
          data={visitReport}
          images={visitData?.visitImages || []}
        />
      )}

      {!!reportDocumentAggregation?.amcInstallationForm && (
        <Summary
          t={t}
          section="AMC_INSTALLATION_REPORT"
          report={{
            ...reportDocumentAggregation?.amcInstallationForm,
            name: `${facilityDetails.facilityName}.pdf`
          }}
          supportingDocuments={[]}
          isDocument={true}
        />
      )}

    </div>
  );
}

export default AMCVisitDetails;
