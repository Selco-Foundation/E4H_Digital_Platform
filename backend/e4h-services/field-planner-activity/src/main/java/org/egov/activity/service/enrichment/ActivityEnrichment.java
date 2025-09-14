package org.egov.activity.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.ActivityRepository;
import org.egov.activity.web.models.Activity;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.common.service.IdGenService;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.web.models.FieldPlan;
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
        Activity existingActivity = activityRepository.getActivityByCode(activityAssignment.getActivityId());
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
        Activity existingActivity = activityRepository.getActivityByCode(activityFacility.getActivityId());
        activityFacility.setActivityId(existingActivity.getId());
        log.info("Activity id set to " + activityFacility.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityFacility.setAuditDetails(auditDetails);
    }

//    public void enrichFieldPlanFacilityOnCreate(List<ActivityFacility> entities, ActivityFacilityBulkRequest request) throws Exception {
//        log.info("starting the enrichment for create project facility");
//
//        log.info("generating IDs using IdGenService");
//        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
//                getTenantId(entities),
//                fieldPlannerConfiguration.getFieldPlanFacilityIdFormat(), "", entities.size());
//
//        enrichForCreate(entities, idList, request.getRequestInfo());
//        log.info("enrichment done");
//    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(ActivityFacility activityFacility, ActivityFacility activityFacilityFromDB, RequestInfo requestInfo) {
        activityFacility.setAuditDetails(activityFacilityFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityFacilityFromDB.getAuditDetails(), false);
        activityFacility.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + activityFacility.getId());
    }


}
