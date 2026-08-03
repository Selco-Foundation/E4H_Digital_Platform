package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.repository.AssessmentFacilityRepository;
import org.egov.field_planner.repository.AssessmentSubmissionRepository;
import org.egov.field_planner.util.AssessmentAdditionalDetailsHelper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssessmentSubmissionService {

    private final AssessmentFacilityRepository facilityRepository;
    private final AssessmentSubmissionRepository submissionRepository;
    private final AssessmentAssessorService assessorService;
    private final AssessmentMdmsService mdmsService;
    private final OutcomeEngineService outcomeEngineService;
    private final AssessmentWorkflowService workflowService;
    private final AllowedActionsService allowedActionsService;
    private final PlanFacilitySearchService searchService;

    public SubmissionQueueSearchResponse searchQueue(SubmissionQueueSearchRequest request) {
        String role = AssessmentConstants.PHASE_PHONE.equals(request.getAssessmentPhase())
                ? AssessmentConstants.ROLE_ENUMERATOR
                : AssessmentConstants.ROLE_FIELD_POC;
        List<String> planIds = assessorService.getAssignedPlanIds(
                request.getRequestInfo(), request.getTenantId(), role);
        List<PlanFacility> facilities = facilityRepository.findQueueFacilities(planIds, request.getAssessmentPhase());
        List<SubmissionQueueItem> queue = facilities.stream()
                .map(f -> SubmissionQueueItem.builder()
                        .planFacilityId(f.getPlanFacilityId())
                        .planId(f.getAssessmentPlanId())
                        .facilityId(f.getFacilityId())
                        .facilityName(f.getFacilityName())
                        .facilityCategory(f.getFacilityCategory())
                        .phoneStatus(f.getPhoneStatus())
                        .fieldStatus(f.getFieldStatus())
                        .build())
                .toList();
        return SubmissionQueueSearchResponse.builder()
                .queue(queue)
                .total(queue.size())
                .build();
    }

    public SubmissionFormResolveResponse resolveForm(SubmissionFormResolveRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));
        if (facility.getFacilityCategory() != null
                && !facility.getFacilityCategory().equalsIgnoreCase(request.getFacilityCategory())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_CATEGORY_MISMATCH,
                    "facilityCategory does not match plan facility snapshot");
        }
        String formType = mdmsService.resolveFormType(request.getFacilityCategory(), request.getAssessmentPhase());
        String tenantId = resolveTenantId(request.getTenantId());
        AssessmentFormSchema schema = mdmsService.getFormSchema(
                request.getRequestInfo(), tenantId, formType);
        if (schema == null || schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_FORM_NOT_AVAILABLE,
                    "Form schema not available for " + formType);
        }
        return SubmissionFormResolveResponse.builder()
                .formType(formType)
                .schema(schema)
                .build();
    }

    @Transactional
    public SubmissionCreateResponse createPhoneSubmission(SubmissionPhoneCreateRequest request) {
        PlanFacility facility = loadAndAuthorizePhone(request);
        if (submissionRepository.existsForPhase(facility.getPlanFacilityId(), AssessmentConstants.PHASE_PHONE)) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_DUPLICATE_PHONE_SUBMISSION,
                    "Phone submission already exists for this plan facility");
        }
        if (!AssessmentConstants.REMOTE_PENDING_STATUSES.contains(facility.getPhoneStatus())
                && !AssessmentConstants.PHONE_PENDING.equals(facility.getPhoneStatus())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_DUPLICATE_PHONE_SUBMISSION,
                    "Remote assessment already completed");
        }

        String formType = mdmsService.resolveFormType(request.getFacilityCategory(), AssessmentConstants.PHASE_PHONE);
        String tenantId = resolveTenantId(request.getTenantId());
        AssessmentFormSchema schema = mdmsService.getFormSchema(
                request.getRequestInfo(), tenantId, formType);
        outcomeEngineService.validateSubmissionData(schema, request.getSubmissionData());
        String outcome = outcomeEngineService.evaluate(
                tenantId, request.getRequestInfo(), formType, request.getSubmissionData());

        workflowService.transitionWorkflow(facility.getPlanFacilityId(), tenantId,
                AssessmentConstants.WF_ACTION_SUBMIT_REMOTE, request.getRequestInfo(), null);

        long now = System.currentTimeMillis();
        AssessmentSubmission submission = persistSubmission(request.getRequestInfo(), facility, formType,
                AssessmentConstants.PHASE_PHONE, outcome, request.getSubmissionData(),
                request.getSubmittedByName(), request.getClientSubmissionTime(), tenantId);

        String phoneStatus = AssessmentConstants.OUTCOME_QUALIFIED.equals(outcome)
                ? AssessmentConstants.PHONE_QUALIFIED
                : AssessmentConstants.PHONE_NOT_QUALIFIED;
        Map<String, Object> assessmentUpdates = new HashMap<>();
        assessmentUpdates.put("phoneOutcome", outcome);
        Map<String, Object> additionalDetails = searchService.buildAssessmentMetadataUpdate(facility, assessmentUpdates);
        additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                additionalDetails,
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_REMOTE_SUBMITTED)
                        .timestamp(now)
                        .actor(request.getRequestInfo().getUserInfo().getUuid())
                        .build()
        );

        facilityRepository.updateFacilityStatuses(
                facility.getPlanFacilityId(),
                phoneStatus,
                facility.getFieldStatus(),
                AssessmentConstants.OVERALL_PENDING,
                null,
                additionalDetails,
                request.getRequestInfo().getUserInfo().getUuid()
        );

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        updated.setAllowedActions(allowedActionsService.compute(updated));
        return SubmissionCreateResponse.builder()
                .submission(submission)
                .facility(updated)
                .autoEligible(false)
                .autoNotEligible(false)
                .build();
    }

    @Transactional
    public SubmissionCreateResponse createFieldSubmission(SubmissionFieldCreateRequest request) {
        PlanFacility facility = loadAndAuthorizeField(request);
        if (submissionRepository.existsForPhase(facility.getPlanFacilityId(), AssessmentConstants.PHASE_FIELD)) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_DUPLICATE_FIELD_SUBMISSION,
                    "Field submission already exists for this plan facility");
        }
        if (!AssessmentConstants.FIELD_PENDING.equals(facility.getFieldStatus())) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_FIELD_NOT_ASSIGNED,
                    "On-site assessment must be assigned (field_status=PENDING) before submit");
        }

        String formType = mdmsService.resolveFormType(request.getFacilityCategory(), AssessmentConstants.PHASE_FIELD);
        String tenantId = resolveTenantId(request.getTenantId());
        AssessmentFormSchema schema = mdmsService.getFormSchema(
                request.getRequestInfo(), tenantId, formType);
        outcomeEngineService.validateSubmissionData(schema, request.getSubmissionData());
        String outcome = outcomeEngineService.evaluate(
                tenantId, request.getRequestInfo(), formType, request.getSubmissionData());

        workflowService.transitionWorkflow(facility.getPlanFacilityId(), tenantId,
                AssessmentConstants.WF_ACTION_SUBMIT_FIELD, request.getRequestInfo(), null);

        long now = System.currentTimeMillis();
        AssessmentSubmission submission = persistSubmission(request.getRequestInfo(), facility, formType,
                AssessmentConstants.PHASE_FIELD, outcome, request.getSubmissionData(),
                request.getSubmittedByName(), request.getClientSubmissionTime(), tenantId);

        String fieldStatus = AssessmentConstants.OUTCOME_QUALIFIED.equals(outcome)
                ? AssessmentConstants.FIELD_QUALIFIED
                : AssessmentConstants.FIELD_NOT_QUALIFIED;
        String phoneOutcome = facility.getPhoneOutcome();
        Map<String, Object> assessmentUpdates = new HashMap<>();
        assessmentUpdates.put("fieldOutcome", outcome);
        Map<String, Object> additionalDetails = searchService.buildAssessmentMetadataUpdate(facility, assessmentUpdates);

        boolean autoEligible = false;
        boolean autoNotEligible = false;
        String overallStatus = facility.getOverallStatus();
        String completionStatus = facility.getAssessmentCompletionStatus();

        if (!Boolean.TRUE.equals(facility.getOverallManuallySet())) {
            if (AssessmentConstants.OUTCOME_QUALIFIED.equals(phoneOutcome)
                    && AssessmentConstants.OUTCOME_QUALIFIED.equals(outcome)) {
                overallStatus = AssessmentConstants.OVERALL_ELIGIBLE;
                completionStatus = AssessmentConstants.COMPLETION_ELIGIBLE;
                autoEligible = true;
                workflowService.transitionWorkflow(facility.getPlanFacilityId(), tenantId,
                        AssessmentConstants.WF_ACTION_AUTO_ELIGIBLE, request.getRequestInfo(), null);
                additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                        additionalDetails,
                        AssessmentAuditEvent.builder()
                                .event(AssessmentConstants.AUDIT_OVERALL_AUTO_ELIGIBLE)
                                .timestamp(now)
                                .actor(AssessmentConstants.ACTOR_SYSTEM)
                                .build()
                );
            } else if (AssessmentConstants.OUTCOME_NOT_QUALIFIED.equals(phoneOutcome)
                    && AssessmentConstants.OUTCOME_NOT_QUALIFIED.equals(outcome)) {
                overallStatus = AssessmentConstants.OVERALL_NOT_ELIGIBLE;
                completionStatus = AssessmentConstants.COMPLETION_NOT_ELIGIBLE;
                autoNotEligible = true;
                workflowService.transitionWorkflow(facility.getPlanFacilityId(), tenantId,
                        AssessmentConstants.WF_ACTION_AUTO_NOT_ELIGIBLE, request.getRequestInfo(), null);
                additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                        additionalDetails,
                        AssessmentAuditEvent.builder()
                                .event(AssessmentConstants.AUDIT_OVERALL_AUTO_NOT_ELIGIBLE)
                                .timestamp(now)
                                .actor(AssessmentConstants.ACTOR_SYSTEM)
                                .build()
                );
            }
        }

        additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                additionalDetails,
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_ONSITE_SUBMITTED)
                        .timestamp(now)
                        .actor(request.getRequestInfo().getUserInfo().getUuid())
                        .build()
        );

        facilityRepository.updateFacilityStatuses(
                facility.getPlanFacilityId(),
                facility.getPhoneStatus(),
                fieldStatus,
                overallStatus,
                completionStatus,
                additionalDetails,
                request.getRequestInfo().getUserInfo().getUuid()
        );

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        updated.setAllowedActions(allowedActionsService.compute(updated));
        return SubmissionCreateResponse.builder()
                .submission(submission)
                .facility(updated)
                .autoEligible(autoEligible)
                .autoNotEligible(autoNotEligible)
                .build();
    }

    @Transactional
    public PlanFacility unableToContact(SubmissionUnableToContactRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));
        assessorService.authorizeAssessor(request.getRequestInfo(), facility,
                AssessmentConstants.ROLE_ENUMERATOR, "in");

        String phoneStatus = AssessmentConstants.UNABLE_REASON_WRONG_NUMBER.equals(request.getReason())
                ? AssessmentConstants.PHONE_PENDING_WRONG_NUMBER
                : AssessmentConstants.PHONE_PENDING_NO_ANSWER;

        workflowService.transitionWorkflow(facility.getPlanFacilityId(), "in",
                AssessmentConstants.WF_ACTION_UNABLE_TO_CONTACT, request.getRequestInfo(), request.getReason());

        long now = System.currentTimeMillis();
        Map<String, Object> additionalDetails = AssessmentAdditionalDetailsHelper.appendAuditEvent(
                facility.getAdditionalDetails(),
                AssessmentAuditEvent.builder()
                        .event(AssessmentConstants.AUDIT_UNABLE_TO_CONTACT)
                        .timestamp(now)
                        .actor(request.getRequestInfo().getUserInfo().getUuid())
                        .build()
        );

        facilityRepository.updateFacilityStatuses(
                facility.getPlanFacilityId(),
                phoneStatus,
                null,
                AssessmentConstants.OVERALL_PENDING,
                null,
                additionalDetails,
                request.getRequestInfo().getUserInfo().getUuid()
        );

        PlanFacility updated = facilityRepository.findById(facility.getPlanFacilityId()).orElseThrow();
        updated.setAllowedActions(allowedActionsService.compute(updated));
        return updated;
    }

    private PlanFacility loadAndAuthorizePhone(SubmissionPhoneCreateRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));
        validateCategory(facility, request.getFacilityCategory());
        assessorService.authorizeAssessor(request.getRequestInfo(), facility,
                AssessmentConstants.ROLE_ENUMERATOR, resolveTenantId(request.getTenantId()));
        return facility;
    }

    private PlanFacility loadAndAuthorizeField(SubmissionFieldCreateRequest request) {
        PlanFacility facility = facilityRepository.findById(request.getPlanFacilityId())
                .orElseThrow(() -> new CustomException(AssessmentConstants.ASSESSMENT_PLAN_FACILITY_NOT_FOUND,
                        "Plan facility not found: " + request.getPlanFacilityId()));
        validateCategory(facility, request.getFacilityCategory());
        assessorService.authorizeAssessor(request.getRequestInfo(), facility,
                AssessmentConstants.ROLE_FIELD_POC, resolveTenantId(request.getTenantId()));
        return facility;
    }

    private void validateCategory(PlanFacility facility, String facilityCategory) {
        if (facility.getFacilityCategory() != null
                && !facility.getFacilityCategory().equalsIgnoreCase(facilityCategory)) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_CATEGORY_MISMATCH,
                    "facilityCategory does not match plan facility snapshot");
        }
    }

    private AssessmentSubmission persistSubmission(RequestInfo requestInfo, PlanFacility facility, String formType,
                                                    String phase, String outcome, Map<String, Object> submissionData,
                                                    String submittedByName, Long clientSubmissionTime, String tenantId) {
        long now = System.currentTimeMillis();
        AssessmentSubmission submission = AssessmentSubmission.builder()
                .tenantId(tenantId)
                .planId(facility.getAssessmentPlanId())
                .planFacilityId(facility.getPlanFacilityId())
                .facilityId(facility.getFacilityId())
                .assessmentPhase(phase)
                .formType(formType)
                .outcome(outcome)
                .submittedBy(requestInfo.getUserInfo().getUuid())
                .submittedByName(StringUtils.defaultIfBlank(submittedByName,
                        requestInfo.getUserInfo().getName()))
                .submissionData(submissionData)
                .clientSubmissionTime(clientSubmissionTime)
                .serverReceivedTime(now)
                .build();
        return submissionRepository.insert(submission);
    }

    private String resolveTenantId(String tenantId) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(tenantId) ? tenantId : "in";
    }
}
