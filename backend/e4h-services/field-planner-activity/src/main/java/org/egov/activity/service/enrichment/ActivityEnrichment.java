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

    @Autowired
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
        //Enrich Project id and audit details
        enrichActivityAssignmentRequestOnCreate(activityAssignment, requestInfo);
        log.info("Enriched FieldPlan request with id and Audit details");

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
        log.info("fieldPlan id set to " + activityAssignment.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityAssignment.setAuditDetails(auditDetails);
    }

    public void enrichActivityFacilityRequestOnCreate(ActivityFacility activityFacility, RequestInfo requestInfo) {
        activityFacility.setId(UUID.randomUUID().toString());
        activityFacility.setStatus(SCHEDULED_STATUS);
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().code(List.of(activityFacility.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
        activityFacility.setActivityId(existingActivity.getId());
        log.info("Activity id set to " + activityFacility.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activityFacility.setAuditDetails(auditDetails);
    }

    public void enrichActivityAssignmentOnSearch(ActivityAssignmentSearchRequest request, ActivityAssignment activityAssignment) {
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder().ids(List.of(activityAssignment.getActivityId())).build();
        Activity existingActivity = activityFacilityRepository.getActivityObject(criteria);
        if(existingActivity !=null) {
            activityAssignment.setActivityCode(existingActivity.getCode());
            activityAssignment.setActivityName(existingActivity.getName());
        }

        if (activityAssignment.getFieldPlanId() != null && !activityAssignment.getFieldPlanId().isEmpty()) {
            FieldPlan existingFieldPlan = activityValidator.getFieldPlanById(request.getRequestInfo(), activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (existingFieldPlan != null) {
                activityAssignment.setFieldPlan(existingFieldPlan);
            }

            FieldPlanFacilityBulkResponse fieldPlanFacilityList = activityValidator.getFieldPlanFacilityById(request.getRequestInfo(), activityAssignment.getFieldPlanId(), activityAssignment.getTenantId());
            if (fieldPlanFacilityList != null) {
                Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(activityAssignment.getAdditionalDetails(), "countFieldPlanFacilities", fieldPlanFacilityList.getTotalCount());
                activityAssignment.setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
            }
        }
    }

    public void enrichActivityFacilityOnSearch(ActivityFacilitySearchRequest request, ActivityFacility activityFacility) {
        if(activityFacility.getFacilityId() !=null && !activityFacility.getFacilityId().isEmpty()){
            Facility existingfacility = activityValidator.getFacilityById(activityFacility.getFacilityId());
            if (existingfacility != null) {
                activityFacility.setFacility(existingfacility);
            }
        }

        // Get Full assigned user Infos from HRMS
        if(activityFacility.getAssignedUser() !=null && !activityFacility.getAssignedUser().isEmpty()){
            Employee employee =  activityValidator.getUserById(request, activityFacility);
            if(employee !=null){
                activityFacility.setAssignedEmployeeUser(employee.getUser());
            }
        }
    }

    public void enrichActivityRequestOnCreate(Activity activity, RequestInfo requestInfo) {
        activity.setId(UUID.randomUUID().toString());
        log.info("Activity id set to " + activity.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        activity.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityFacilityRequestOnUpdate(ActivityFacility activityFacility, ActivityFacility activityFacilityFromDB, RequestInfo requestInfo) {
        activityFacility.setAuditDetails(activityFacilityFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityFacilityFromDB.getAuditDetails(), false);
        activityFacility.setAuditDetails(auditDetails);
        log.info("Enriched activity facility audit details for activity " + activityFacility.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityAssignmentRequestOnUpdate(ActivityAssignment activityAssignment, ActivityAssignment activityAssignmentFromDB, RequestInfo requestInfo) {
        activityAssignment.setAuditDetails(activityAssignmentFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityAssignmentFromDB.getAuditDetails(), false);
        activityAssignment.setAuditDetails(auditDetails);
        log.info("Enriched activity assignment audit details for project " + activityAssignment.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnDelete(ActivityAssignment activityAssignment, RequestInfo requestInfo) {
        activityAssignment.setIsDeleted(true);
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), activityAssignment.getAuditDetails(), false);
        activityAssignment.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + activityAssignment.getId());
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
