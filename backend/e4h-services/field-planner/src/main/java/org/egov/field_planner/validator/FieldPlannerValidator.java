package org.egov.field_planner.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.project.*;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Component
@Slf4j
public class FieldPlannerValidator {

    @Autowired
    FieldPlannerRepository fieldPlannerRepository;

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY = "Tenant ID is mandatory in FieldPlan request body";
    public static final String ACTIVITIES_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY = "Activities are mandatory in FieldPlan request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_FIELDPLAN = " that you are trying to update does not exists for the FieldPlan ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    FieldPlannerConfiguration config;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public FieldPlannerValidator(ServiceRequestClient serviceRequestRepository){
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public void validateCreateFieldPlanRequest(FieldPlanRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if FieldPlan request and mandatory fields are present
        validateFieldPlanRequest(request);

        validateRequestMDMSData(request, request.getFieldPlans().get(0).getTenantId(), errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateFieldPlanRequest(FieldPlanRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getFieldPlans() == null || request.getFieldPlans().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (FieldPlan fieldPlan : request.getFieldPlans()) {
            if (fieldPlan.getProjectId() == null) {
                log.error("Project ID is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "Project ID is mandatory");
            }
            // Get existing project with projectID from project service
            Project existingProject = getProjectById(request, fieldPlan);
            if (existingProject == null) {
                log.error("Project ID do not exist");
                throw new CustomException("FieldPlan", "Project ID do not exist");
            }
            // Check if fieldPlan dates are within project dates
            isFieldPlanWithinProject(existingProject, fieldPlan, errorMap);
            
            if (fieldPlan == null) {
                log.error("FieldPlan is mandatory in FieldPlans");
                throw new CustomException("FieldPlan", "FieldPlan is mandatory");
            }
            if (StringUtils.isBlank(fieldPlan.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (fieldPlan.getActivities() == null || fieldPlan.getActivities().isEmpty()) {
                log.error(ACTIVITIES_IS_MANDATORY_IN_FIELDPLAN_REQUEST_BODY);
                errorMap.put("ACTIVITIES", "Activity is mandatory");
            }
            if ((fieldPlan.getStartDate() != null && fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0) && (fieldPlan.getStartDate().compareTo(fieldPlan.getEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (fieldPlan.getStartDate() != null && fieldPlan.getEndDate() != null && fieldPlan.getEndDate() != 0
                    && fieldPlan.getEndDate().compareTo(Instant.ofEpochMilli(fieldPlan.getStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
        if (requestInfo == null) {
            log.error("Request info is mandatory");
            throw new CustomException("REQUEST_INFO", "Request info is mandatory");
        }
        if (requestInfo.getUserInfo() == null) {
            log.error("UserInfo is mandatory in RequestInfo");
            throw new CustomException("USERINFO", "UserInfo is mandatory");
        }
        if (requestInfo.getUserInfo() != null && StringUtils.isBlank(requestInfo.getUserInfo().getUuid())) {
            log.error("UUID is mandatory in UserInfo");
            throw new CustomException("USERINFO_UUID", "UUID is mandatory");
        }
    }

    /* Validate Project Request MDMS data */
    private void validateRequestMDMSData(FieldPlanRequest request, String tenantId, Map<String, String> errorMap) {
        String rootTenantId = tenantId.split("\\.")[0];

        //Get MDMS data using create project request and tenantId
        Object mdmsData = mdmsUtils.mDMSCall(request, rootTenantId);

        validateMDMSData(request.getFieldPlans(), mdmsData, errorMap);
        log.info("Request data validated with MDMS");
    }

    /* Validates the request data against MDMS data */
    private void validateMDMSData(List<FieldPlan> fieldPlans, Object mdmsData, Map<String, String> errorMap) {
        String mdmsRes = "$.MdmsRes.";
        final String jsonPathForActivities = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_ACTIVITIES + ".*.code";
        final String jsonPathForStateInfo = mdmsRes + MDMS_COMMON_MASTERS_MODULE_NAME + "." + MASTER_STATE_INFO + ".*.name";
        final String jsonPathForTenants = mdmsRes + MDMS_TENANT_MODULE_NAME + "." + MASTER_TENANTS + ".*";

        List<Object> activitiesRes = null;
        List<Object> stateInfoRes = null;
        List<Object> tenantRes = null;
        try {
            activitiesRes = JsonPath.read(mdmsData, jsonPathForActivities);
            stateInfoRes = JsonPath.read(mdmsData, jsonPathForStateInfo);
            tenantRes = JsonPath.read(mdmsData, jsonPathForTenants);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        for (FieldPlan fieldPlan : fieldPlans) {
            log.info("Validate Project type with MDMS");
            Map<String, Object> geographyDetails = fieldPlan.getGeographyDetails();
            List<Map<String, Object>> activities = fieldPlan.getActivities();
            String state = (String)geographyDetails.get("state");
            String mdmsNotPresent = IS_NOT_PRESENT_IN_MDMS;
//            if (!fieldPlan.getActivities().isEmpty() && !typeOfProjectRes.contains(fieldPlan.getActivities())) {
//                log.error("The project type: " + fieldPlan.getActivities() + mdmsNotPresent);
//                errorMap.put("INVALID_PROJECT_TYPE", "The project type: " + fieldPlan.getActivities() + mdmsNotPresent);
//            }
            log.info("Validate Tenant Id with MDMS");
            if (!StringUtils.isBlank(fieldPlan.getTenantId()) && !tenantRes.contains(fieldPlan.getTenantId())) {
                log.error("The tenant: " + fieldPlan.getTenantId() + mdmsNotPresent);
                errorMap.put("INVALID_TENANT", "The tenant: " + fieldPlan.getTenantId() + mdmsNotPresent);
            }
            log.info("Validate stateInfos with MDMS");
            if (!StringUtils.isBlank(state) && !stateInfoRes.contains(state)) {
                log.error("The state code: " + state + mdmsNotPresent);
                errorMap.put("INVALID_STATE_CODE", "The state code: " + state + mdmsNotPresent);
            }
        }
    }

    public Project getProjectById(FieldPlanRequest request, FieldPlan fieldPlan) {
        String projectId = fieldPlan.getProjectId();
        Project project = Project.builder().id(projectId).tenantId(fieldPlan.getTenantId()).build();
        ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(project)).build();
        String url = config.getProjectServiceHost() + config.getProjectServiceSearchUrl()+ "?tenantId="+fieldPlan.getTenantId()+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), projectRequest, Map.class);
        ProjectResponse projectResponse = mapper.convertValue(response, ProjectResponse.class);
        if(projectResponse != null && projectResponse.getProject() !=null && projectResponse.getProject().size() > 0){
            return projectResponse.getProject().get(0);
        }
        return null;
    }

    public void isFieldPlanWithinProject(Project project, FieldPlan fieldPlan, Map<String, String> errorMap) {
        if (project == null || fieldPlan == null) {
            log.error("Project or FieldPlan is null");
            errorMap.put("FIELDPLAN", "Project or FieldPlan is null");
        }

        Long projectStart = project.getStartDate();
        Long projectEnd   = project.getEndDate();
        Long fieldStart   = fieldPlan.getStartDate();
        Long fieldEnd     = fieldPlan.getEndDate();

        if (projectStart == null || projectEnd == null) {
            log.error("Project dates are not mandatory");
            errorMap.put("FIELDPLAN", "Project dates are not mandatory");
        }
        if (fieldStart == null || fieldEnd == null) {
            log.error("FieldPlan dates are not mandatory");
            errorMap.put("FIELDPLAN", "FieldPlan dates are not mandatory");
        }

        if (fieldStart < projectStart) {
            log.error("The FieldPlan start date is earlier than the Project start date");
            errorMap.put("FIELDPLAN", "The FieldPlan start date is earlier than the Project start date");
        }
        if (fieldEnd > projectEnd) {
            log.error("The FieldPlan end date is later than the Project end date");
            errorMap.put("FIELDPLAN", "The FieldPlan end date is later than the Project end date");
        }
    }
}