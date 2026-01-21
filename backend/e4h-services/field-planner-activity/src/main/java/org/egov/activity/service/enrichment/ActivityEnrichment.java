package org.egov.activity.service.enrichment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.service.ActivityService;
import org.egov.activity.validator.ActivityValidator;
import org.egov.activity.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.egov.activity.util.ActivityConstants.*;
import static org.egov.common.utils.CommonUtils.enrichForCreate;

@Service
@Slf4j
//@RequiredArgsConstructor
public class ActivityEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final ActivityServiceUtil fieldPlanServiceUtil;
    private final ActivityValidator activityValidator;

    private final ObjectMapper objectMapper;

    @Autowired
    ActivityFacilityRepository activityFacilityRepository;

    public ActivityEnrichment(ActivityServiceUtil fieldPlanServiceUtil, ActivityValidator activityValidator, ObjectMapper objectMapper){
        this.fieldPlanServiceUtil = fieldPlanServiceUtil;
        this.activityValidator = activityValidator;
        this.objectMapper = objectMapper;
    }

    /* Enrich Project on Create Request */
    public void enrichActivityAssignmentOnCreate(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        log.trace("enrichActivityAssignmentOnCreate method invoked for activityAssignmentId: {}", activityAssignment.getId());
        //Enrich Project id and audit details
        enrichActivityAssignmentRequestOnCreate(activityAssignment, requestInfo);
        log.debug("Enriched activity assignment with id and audit details, id: {}", activityAssignment.getId());
    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichActivityAssignmentRequestOnCreate(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityAssignment.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
        if(existingActivity ==null) {
            throw new CustomException("ACTIVITY", "Activity code do not exist on Activity Table");
        }
        activityAssignment.setId(UUID.randomUUID().toString());
        activityAssignment.setStatus(ACTIVE_STATUS);
        activityAssignment.setActivityId(existingActivity.getId());
        log.debug("Activity assignment id set to: {}", activityAssignment.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityAssignment.setAuditDetails(auditDetails);
    }

    public void enrichActivityFacilityRequestOnCreate(ActivityFacility activityFacility, RequestInfo requestInfo) {
        log.trace("enrichActivityFacilityRequestOnCreate method invoked");
        activityFacility.setId(UUID.randomUUID().toString());
        activityFacility.setStatus(SCHEDULED_STATUS);
        activityFacility.setIsDeleted(false);
        log.debug("Setting activity facility id: {}, status: {}", activityFacility.getId(), SCHEDULED_STATUS);
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityFacility.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
        activityFacility.setActivityId(existingActivity.getId());
        log.debug("Activity facility enriched with activityId: {}", existingActivity.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityFacility.setAuditDetails(auditDetails);
    }

    public void enrichActivityAssignmentOnSearch(RequestInfo requestInfo, ActivityAssignment activityAssignment) {
        log.trace("enrichActivityAssignmentOnSearch method invoked for activityAssignmentId: {}", activityAssignment.getId());
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityAssignment.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
        if(existingActivity !=null) {
            log.debug("Enriching activity assignment with activity code and name, activityAssignmentId: {}", activityAssignment.getId());
            activityAssignment.setActivityCode(existingActivity.getCode());
            activityAssignment.setActivityName(existingActivity.getName());
        }

        if (activityAssignment.getFieldPlanId() != null && !activityAssignment.getFieldPlanId().isEmpty()) {
            log.debug("Enriching activity assignment with field plan details, fieldPlanId: {}", activityAssignment.getFieldPlanId());
            FieldPlan existingFieldPlan = activityValidator.getFieldPlanById(requestInfo, activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (existingFieldPlan != null) {
                activityAssignment.setFieldPlan(existingFieldPlan);
            }

            FieldPlanFacilityBulkResponse fieldPlanFacilityList = activityValidator.getFieldPlanFacilityById(requestInfo, activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (fieldPlanFacilityList != null) {
                log.debug("Adding field plan facility count to additional details, count: {}", fieldPlanFacilityList.getTotalCount());
                Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(activityAssignment.getAdditionalDetails(), "countFieldPlanFacilities", fieldPlanFacilityList.getTotalCount());
                activityAssignment.setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
            }
        }
    }

    public void enrichActivityFacilityOnSearch(ActivityFacilitySearchRequest request, ActivityFacility activityFacility) {
        log.trace("enrichActivityFacilityOnSearch method invoked for activityFacilityId: {}", activityFacility.getId());
        if(activityFacility.getFacilityId() !=null && !activityFacility.getFacilityId().isEmpty()){
            log.debug("Enriching activity facility with facility details, facilityId: {}", activityFacility.getFacilityId());
            Facility existingfacility = activityValidator.getFacilityById(activityFacility.getFacilityId());
            if (existingfacility != null) {
                activityFacility.setFacility(existingfacility);
            }
        }

        // Get Full assigned user Infos from HRMS
        if(activityFacility.getAssignedUser() !=null && !activityFacility.getAssignedUser().isEmpty()){
            log.debug("Enriching activity facility with assigned user details, userId: {}", activityFacility.getAssignedUser());
            Employee employee =  activityValidator.getUserById(request, activityFacility.getAssignedUser());
            if(employee !=null){
                activityFacility.setAssignedEmployeeUser(employee.getUser());
            }
        }
    }

    public void enrichActivityRequestOnCreate(Activity activity, RequestInfo requestInfo) {
        log.trace("enrichActivityRequestOnCreate method invoked");
        activity.setId(UUID.randomUUID().toString());
        log.debug("Activity id set to: {}", activity.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activity.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityFacilityRequestOnUpdate(ActivityFacility activityFacility, ActivityFacility activityFacilityFromDB, RequestInfo requestInfo) {
        log.trace("enrichActivityFacilityRequestOnUpdate method invoked for activityFacilityId: {}", activityFacility.getId());
        activityFacility.setAuditDetails(activityFacilityFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityFacilityFromDB.getAuditDetails(), false);
        activityFacility.setAuditDetails(auditDetails);
        log.debug("Enriched activity facility audit details for activityFacilityId: {}", activityFacility.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityAssignmentRequestOnUpdate(ActivityAssignment activityAssignment, ActivityAssignment activityAssignmentFromDB, RequestInfo requestInfo) {
        log.trace("enrichActivityAssignmentRequestOnUpdate method invoked for activityAssignmentId: {}", activityAssignment.getId());
        activityAssignment.setAuditDetails(activityAssignmentFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityAssignmentFromDB.getAuditDetails(), false);
        activityAssignment.setAuditDetails(auditDetails);
        log.debug("Enriched activity assignment audit details for activityAssignmentId: {}", activityAssignment.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnDelete(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        log.trace("enrichFieldPlanRequestOnDelete method invoked for activityAssignmentId: {}", activityAssignment.getId());
        activityAssignment.setIsDeleted(true);
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityAssignment.getAuditDetails(), false);
        activityAssignment.setAuditDetails(auditDetails);
        log.debug("Enriched activity assignment audit details for deletion, activityAssignmentId: {}", activityAssignment.getId());
    }

    private Object mergeIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        if (additionalDetails instanceof ObjectNode) {
            ((ObjectNode) additionalDetails).put(key, objectMapper.valueToTree(value));
            return additionalDetails;
        } else if (additionalDetails instanceof Map) {
            ((Map<String, Object>) additionalDetails).put(key, value);
            return additionalDetails;
        } else {
            // default to HashMap if null or unknown type
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
    }


}
