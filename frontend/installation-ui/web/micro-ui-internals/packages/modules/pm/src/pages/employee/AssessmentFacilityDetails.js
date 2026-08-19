import React, { useEffect, useState } from "react";
import { Loader, Toast } from "@egovernments/digit-ui-react-components";
import { useDispatch } from "react-redux";
import { useQueryClient } from "react-query";
import { useTranslation } from "react-i18next";
import InfoCard from "../../components/AssessmentFacilityDetails/InfoCard";
import ExpandableSection from "../../components/AssessmentFacilityDetails/ExpandableSection";
import Section from "../../components/AssessmentFacilityDetails/Section";
import ActionBar from "../../components/AssessmentFacilityDetails/ActionBar";
import ConfirmActionModal from "../../components/AssessmentDetails/ConfirmActionModal";
import ReasonRequiredModal from "../../components/AssessmentDetails/ReasonRequiredModal";
import useAssessmentPlan from "../../hooks/useAssessmentPlan";
import useAssessmentFacilityDetail from "../../hooks/useAssessmentFacilityDetail";
import useProject from "../../hooks/useProject";
import { populateWorkingProject, populateWorkingAssessmentPlan, populateWorkingAssessmentFacility } from "../../redux/actions";
import { AssessmentFacilityService } from "../../services/AssessmentFacility";
import {
  canAssignForOnSiteAssessment,
  evaluateMarkResultScenario,
  getAssessmentResponseSections,
  getPhoneOutcome,
  getFieldOutcome,
} from "../../utilities/AssessmentPlanData";
import CommonUtils from "../../utilities/CommonUtils";

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

const AssessmentFacilityDetails = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const queryClient = useQueryClient();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const assessmentId = url.split("assessment/")[1].split("/")[0];
  const facilityId = url.split("facilities/")[1].split("/")[0].split("?")[0];

  const [pendingAction, setPendingAction] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const {
    isLoading: projectDataLoading,
    data: projectData,
  } = useProject({
    id: [projectId]
  });

  const {
    isLoading: assessmentPlanDataLoading,
    data: assessmentPlanData,
  } = useAssessmentPlan({
    id: [assessmentId]
  });

  const {
    isLoading: facilityDetailLoading,
    isFetching: facilityDetailFetching,
    data: facilityDetailData,
    revalidate: revalidateFacilityDetail,
  } = useAssessmentFacilityDetail(facilityId);

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

  const assessmentPlan = assessmentPlanData?.assessmentPlans?.[0];
  const planCompleted = assessmentPlan?.status === "CLOSED";
  const facility = mapFacility(facilityDetailData);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
    }
  }, [projectData]);

  useEffect(() => {
    if (assessmentPlan) {
      dispatch(populateWorkingAssessmentPlan(assessmentPlan));
    }
  }, [assessmentPlanData]);

  useEffect(() => {
    if (facility) {
      dispatch(populateWorkingAssessmentFacility(facility));
    }
  }, [facilityDetailData]);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const openAssignOnSiteConfirm = () => {
    setPendingAction({ type: "ASSIGN_ONSITE" });
  };

  // With a single facility, "BULK_NOT_SUPPORTED" can never occur (that scenario only applies
  // to a mixed selection), so it's not handled here.
  const openMarkResultConfirm = (targetResult) => {
    const scenario = evaluateMarkResultScenario([facility], targetResult);

    if (scenario === "BLOCK_REMOTE_PENDING") {
      setPendingAction({ type: "BLOCK_REMOTE_PENDING" });
      return;
    }

    setPendingAction({
      type: scenario === "REASON_REQUIRED" ? "MARK_RESULT_REASON_REQUIRED" : scenario,
      targetResult,
    });
  };

  const closePendingAction = () => setPendingAction(null);

  const handleConfirmPendingAction = async (reason) => {
    if (!pendingAction) return;

    setActionLoading(true);
    try {
      const decisionFields = pendingAction.type === "ASSIGN_ONSITE"
        ? { assignForField: true }
        : { overallStatus: pendingAction.targetResult, ...(reason ? { remarks: reason } : {}) };

      await AssessmentFacilityService.updateFacilityDecision(assessmentPlan, facility.id, decisionFields);

      await revalidateFacilityDetail();
      await queryClient.invalidateQueries(["ASSESSMENT_FACILITY"]);
      await queryClient.invalidateQueries(["ASSESSMENT_PLAN_DETAIL", assessmentId]);
      setPendingAction(null);
      setToast({ key: "success", label: t("PM_ASSESSMENT_ACTION_SUCCESS") });
    } catch (error) {
      console.error("Error updating assessment facility", error);
      setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("PM_ASSESSMENT_ACTION_ERROR") });
    } finally {
      setActionLoading(false);
    }
  };

  const getConfirmModalProps = () => {
    if (pendingAction?.type === "ASSIGN_ONSITE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_ONSITE_TITLE"),
        description: t("PM_ASSESSMENT_CONFIRM_ONSITE_DESC"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${facility?.name} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_PENDING")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_ASSIGN_ONSITE"),
        confirmColor: "#0B4B66",
      };
    }

    if (pendingAction?.type === "BLOCK_REMOTE_PENDING") {
      return {
        title: t("PM_ASSESSMENT_BLOCK_REMOTE_PENDING_TITLE"),
        description: t("PM_ASSESSMENT_BLOCK_REMOTE_PENDING_DESC"),
        confirmLabel: t("CORE_COMMON_DISMISS"),
        confirmColor: "#0B4B66",
        singleAction: true,
        informational: true,
      };
    }

    if (pendingAction?.type === "WARN_ONSITE_PENDING") {
      return {
        title: t("PM_ASSESSMENT_WARN_ONSITE_PENDING_TITLE"),
        description: t("PM_ASSESSMENT_WARN_ONSITE_PENDING_DESC"),
        confirmLabel: t("CORE_COMMON_CONFIRM"),
        confirmColor: pendingAction.targetResult === "NOT_ELIGIBLE" ? "#B91900" : "#1B8354",
      };
    }

    if (pendingAction?.type === "WARN_ONSITE_NOT_INITIATED") {
      return {
        title: t("PM_ASSESSMENT_WARN_ONSITE_NOT_INITIATED_TITLE"),
        description: t("PM_ASSESSMENT_WARN_ONSITE_NOT_INITIATED_DESC"),
        confirmLabel: t("CORE_COMMON_CONFIRM"),
        confirmColor: pendingAction.targetResult === "NOT_ELIGIBLE" ? "#B91900" : "#1B8354",
      };
    }

    if (pendingAction?.type === "PROCEED" && pendingAction?.targetResult === "ELIGIBLE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_RESULT_TITLE"),
        description: t("PM_ASSESSMENT_CONFIRM_ELIGIBLE_DESC"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${facility?.name} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_ELIGIBLE")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_MARK_ELIGIBLE"),
        confirmColor: "#1B8354",
      };
    }

    if (pendingAction?.type === "PROCEED" && pendingAction?.targetResult === "NOT_ELIGIBLE") {
      return {
        title: t("PM_ASSESSMENT_CONFIRM_RESULT_TITLE"),
        message: `${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_PREFIX")} ${facility?.name} ${t("PM_ASSESSMENT_CONFIRM_ACTION_MESSAGE_TO")} '${t("PM_ASSESSMENT_FACILITY_STATUS_NOT_ELIGIBLE")}'.`,
        confirmLabel: t("PM_ASSESSMENT_ACTION_MARK_NOT_ELIGIBLE"),
        confirmColor: "#B91900",
      };
    }

    return null;
  };

  const getReasonModalDescription = () => (
    pendingAction?.targetResult === "ELIGIBLE"
      ? t("PM_ASSESSMENT_REASON_REQUIRED_ELIGIBLE_DESC")
      : t("PM_ASSESSMENT_REASON_REQUIRED_NOT_ELIGIBLE_DESC")
  );

  if (projectDataLoading || assessmentPlanDataLoading || facilityDetailLoading || formSchemaLoading) {
    return <Loader />;
  }

  const { remoteSections, siteSections } = getAssessmentResponseSections(facilityDetailData, formSchemas);
  const phoneOutcome = getPhoneOutcome(facility);
  const fieldOutcome = getFieldOutcome(facility);
  const confirmModalProps = getConfirmModalProps();

  const SectionFields = ({ fields }) => (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "40px" }}>
      {fields.map((field, index) => (
        <div key={index} style={{ marginBottom: "16px" }}>
          <div style={{ fontSize: "14px", color: "#6B7280", marginBottom: "4px" }}>{t(field.label)}</div>
          <div style={{ fontSize: "16px", fontWeight: 700, color: "#0B0C0C" }}>
            {Array.isArray(field.value) ? field.value.map((value) => t(value)).join(", ") : (field.translateValue ? t(field.value) : field.value)}
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
    <div style={{ marginTop: "20px", padding: planCompleted ? "0px 10px" : "0px 10px 80px", overflow: "auto" }}>
      {(actionLoading || facilityDetailFetching) && (
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

      {!planCompleted && (
        <ActionBar
          t={t}
          canAssignOnSite={canAssignForOnSiteAssessment(facility)}
          canMarkEligible={facility?.result !== "ELIGIBLE"}
          canMarkNotEligible={facility?.result !== "NOT_ELIGIBLE"}
          onAssignOnSite={openAssignOnSiteConfirm}
          onMarkEligible={() => openMarkResultConfirm("ELIGIBLE")}
          onMarkNotEligible={() => openMarkResultConfirm("NOT_ELIGIBLE")}
        />
      )}

      {confirmModalProps && (
        <ConfirmActionModal
          t={t}
          title={confirmModalProps.title}
          description={confirmModalProps.description}
          message={confirmModalProps.message}
          confirmLabel={confirmModalProps.confirmLabel}
          confirmColor={confirmModalProps.confirmColor}
          singleAction={confirmModalProps.singleAction}
          loading={actionLoading}
          onConfirm={() => (confirmModalProps.informational ? closePendingAction() : handleConfirmPendingAction())}
          onClose={closePendingAction}
        />
      )}

      {pendingAction?.type === "MARK_RESULT_REASON_REQUIRED" && (
        <ReasonRequiredModal
          t={t}
          description={getReasonModalDescription()}
          loading={actionLoading}
          onConfirm={(reason) => handleConfirmPendingAction(reason)}
          onClose={closePendingAction}
        />
      )}

      {toast && (
        <div style={{ position: "fixed", zIndex: 10000001 }}>
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            style={toast.key === "error" ? { backgroundColor: "#B91900" } : {}}
            label={toast.label}
            isDleteBtn={true}
            onClose={() => setToast(null)}
          />
        </div>
      )}
    </div>
  );
};

export default AssessmentFacilityDetails;
