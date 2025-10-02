package org.egov.activity.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.ActivityRepository;
import org.egov.activity.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.service.IdGenService;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.egov.activity.util.ActivityConstants.*;
import static org.egov.common.utils.CommonUtils.enrichForCreate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final ActivityServiceUtil fieldPlanServiceUtil;

    @Autowired
    ActivityRepository activityRepository;

    private final IdGenService idGenService;

    private final ActivityConfiguration config;

    /* Enrich Project on Create Request */
    public void enrichActivityAssignmentOnCreate(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        //Enrich Project id and audit details
        enrichActivityAssignmentRequestOnCreate(activityAssignment, requestInfo);
        log.info("Enriched FieldPlan request with id and Audit details");

    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichActivityAssignmentRequestOnCreate(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityAssignment.getActivityId())).build();
        Activity existingActivity = activityRepository.getActivityList(criteria);
        if(existingActivity ==null) {
            throw new CustomException("ACTIVITY", "Activity code do not exist on Activity Table");
        }
        activityAssignment.setId(UUID.randomUUID().toString());
        activityAssignment.setStatus(ACTIVE_STATUS);
        activityAssignment.setActivityId(existingActivity.getId());
        log.info("fieldPlan id set to " + activityAssignment.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityAssignment.setAuditDetails(auditDetails);
    }

    public void enrichActivityFacilityRequestOnCreate(ActivityFacility activityFacility, RequestInfo requestInfo) {
        activityFacility.setId(UUID.randomUUID().toString());
        activityFacility.setStatus(SCHEDULED_STATUS);
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityFacility.getActivityId())).build();
        Activity existingActivity = activityRepository.getActivityList(criteria);
        activityFacility.setActivityId(existingActivity.getId());
        log.info("Activity id set to " + activityFacility.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityFacility.setAuditDetails(auditDetails);
    }

    public void enrichActivityOnSearch(ActivityAssignment activityAssignment) {
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityAssignment.getActivityId())).build();
        Activity existingActivity = activityRepository.getActivityList(criteria);
        if(existingActivity ==null) {
            throw new CustomException("ACTIVITY", "Activity code do not exist on Activity Table");
        }
        activityAssignment.setActivityId(existingActivity.getCode());
    }

    public void enrichActivityRequestOnCreate(Activity activity, RequestInfo requestInfo) {
        activity.setId(UUID.randomUUID().toString());
        log.info("Activity id set to " + activity.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activity.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(ActivityFacility activityFacility, ActivityFacility activityFacilityFromDB, RequestInfo requestInfo) {
        activityFacility.setAuditDetails(activityFacilityFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityFacilityFromDB.getAuditDetails(), false);
        activityFacility.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + activityFacility.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnDelete(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        activityAssignment.setIsDeleted(true);
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityAssignment.getAuditDetails(), false);
        activityAssignment.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + activityAssignment.getId());
    }


}
