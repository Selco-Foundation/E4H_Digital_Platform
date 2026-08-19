import React, { useEffect } from "react";
import { Loader } from "@egovernments/digit-ui-react-components";
import { useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import InfoCard from "../../components/AssessmentDetails/InfoCard";
import ExpandableSection from "../../components/AssessmentDetails/ExpandableSection";
import Section from "../../components/FacilityDetails/Section";
import useAssessmentFacilityDetail from "../../hooks/useAssessmentFacilityDetail";
import { populateWorkingFacility, populateWorkingAssessment } from "../../redux/actions";
import { getAssessmentResponseSections, getPhoneOutcome, getFieldOutcome } from "../../utilities/AssessmentFacilityData";

const mapFacility = (facilityDetail) => (facilityDetail && {
  id: facilityDetail.planFacilityId,
  name: facilityDetail.facilityName,
  facilityType: facilityDetail.facilityType,
  district: facilityDetail.district,
  block: facilityDetail.block,
  remoteStatus: facilityDetail.phoneStatus || "NOT_INITIATED",
  onSiteStatus: facilityDetail.fieldStatus || "NOT_INITIATED",
  result: facilityDetail.overallStatus,
  decisionReason: facilityDetail.remarks,
});

const AssessmentDetails = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const url = window.location.href;
  const planFacilityId = url.split("assessments/")[1].split("/")[0].split("?")[0];

  const {
    isLoading: facilityDetailLoading,
    isFetching: facilityDetailFetching,
    data: facilityDetailData,
  } = useAssessmentFacilityDetail(planFacilityId);

  useEffect(() => {
    if (planFacilityId) {
      dispatch(populateWorkingAssessment({ planFacilityId }));
    }
  }, [planFacilityId]);

  useEffect(() => {
    if (facilityDetailData) {
      dispatch(populateWorkingFacility({
        facilityId: facilityDetailData.facilityId,
        facilityName: facilityDetailData.facilityName,
        facilityType: facilityDetailData.facilityType,
        district: facilityDetailData.district,
        block: facilityDetailData.block,
      }));
    }
  }, [facilityDetailData]);

  const { data: mdmsResponse, isLoading: formSchemaLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "assessment",
    [
      {
        name: "AssessmentMobileFormSchema",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const formSchemas = mdmsResponse?.["assessment"]?.["AssessmentMobileFormSchema"] || [];
  const facility = mapFacility(facilityDetailData);

  if (facilityDetailLoading || formSchemaLoading) {
    return <Loader />;
  }

  const { remoteSections, siteSections } = getAssessmentResponseSections(facilityDetailData, formSchemas);
  const phoneOutcome = getPhoneOutcome(facility);
  const fieldOutcome = getFieldOutcome(facility);

  const SectionFields = ({ fields }) => (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "40px" }}>
      {fields.map((field, index) => (
        <div key={index} style={{ marginBottom: "16px" }}>
          <div style={{ fontSize: "14px", color: "#6B7280", marginBottom: "4px" }}>{t(field.label)}</div>
          <div style={{ fontSize: "16px", fontWeight: 700, color: "#0B0C0C" }}>
            {Array.isArray(field.value)
              ? field.value.map((value) => (field.translateValue ? t(value) : value)).join(", ")
              : (field.translateValue ? t(field.value) : field.value)}
          </div>
        </div>
      ))}
    </div>
  );

  const ResponsePages = ({ sections }) => (
    sections.map((section) => (
      <Section key={section.key} title={t(section.label)}>
        <SectionFields fields={section.fields} />
      </Section>
    ))
  );

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {facilityDetailFetching && (
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

      <div style={{ fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C" }}>
        {facility?.name}
      </div>

      <InfoCard t={t} facility={facility} phoneOutcome={phoneOutcome} fieldOutcome={fieldOutcome} />

      {!!remoteSections.length && (
        <ExpandableSection title={t("PM_ASSESSMENT_RESPONSES")} defaultExpanded={true}>
          <ResponsePages sections={remoteSections} />
        </ExpandableSection>
      )}

      {!!siteSections.length && (
        <ExpandableSection title={t("PM_ASSESSMENT_SITE_RESPONSES")} defaultExpanded={true}>
          <ResponsePages sections={siteSections} />
        </ExpandableSection>
      )}
    </div>
  );
};

export default AssessmentDetails;
