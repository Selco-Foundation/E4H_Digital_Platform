package org.egov.amc.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.project.*;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AmcConfigurationRepository;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.util.MDMSUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Component
@Slf4j
public class AmcConfigurationValidator {

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    private final ServiceRequestRepository requestRepository;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_AmcConfiguration_REQUEST_BODY = "Tenant ID is mandatory in AmcConfiguration request body";
    public static final String ASSET_TYPES_IS_MANDATORY_IN_AMC_CONFIG_REQUEST_BODY = "Assets Types are mandatory in Amc Configuration request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_AmcConfiguration = " that you are trying to update does not exists for the AmcConfiguration ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    AMCServiceConfiguration config;
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public AmcConfigurationValidator(ServiceRequestClient serviceRequestRepository, ServiceRequestRepository requestRepository, AmcConfigurationServiceUtil amcConfigurationServiceUtil){
        this.serviceRequestRepository = serviceRequestRepository;
        this.requestRepository = requestRepository;
        this.amcConfigurationServiceUtil = amcConfigurationServiceUtil;
    }

    public void validateCreateAmcConfigurationRequest(AmcConfigurationRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if AmcConfiguration request and mandatory fields are present
        validateAmcConfigurationRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateAmcConfigurationRequest(AmcConfigurationRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getAmcConfigurations() == null || request.getAmcConfigurations().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("AmcConfiguration", "Field Plans are mandatory");
        }

        for (AmcConfiguration amcConfiguration : request.getAmcConfigurations()) {
            if (amcConfiguration == null) {
                log.error("AmcConfiguration is mandatory in AmcConfiguration");
                throw new CustomException("AmcConfiguration", "AmcConfiguration is mandatory");
            }

            if (amcConfiguration.getProjectId() == null || amcConfiguration.getProjectId().isEmpty()) {
                log.error("Project ID is mandatory in AmcConfiguration");
                throw new CustomException("AmcConfiguration", "Project ID is mandatory");
            }
            // Get existing amcConfiguration with projectID from amcConfiguration service
            Project existingProject = getProjectById(request.getRequestInfo(), amcConfiguration.getProjectId(), amcConfiguration.getTenantId());
            if (existingProject == null) {
                log.error("Project ID do not exist");
                throw new CustomException("AmcConfiguration", "Project ID do not exist");
            }

            if (amcConfiguration.getVendorId() == null || amcConfiguration.getVendorId().isEmpty()) {
                log.error("Vendor ID is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Vendor ID is mandatory");
            }

            if (amcConfiguration.getFacilityId() == null || amcConfiguration.getFacilityId().isEmpty()) {
                log.error("Facility ID is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Facility ID is mandatory");
            }
            // Get existing facility with facilityID from facility service
            Facility existingFacility = getFacilityById(amcConfiguration.getFacilityId());
            if (existingFacility == null) {
                log.error("Facility ID do not exist");
                throw new CustomException("AMC Configuration", "Facility ID do not exist");
            }

            if (amcConfiguration.getDurationMonths() == null || amcConfiguration.getDurationMonths() <= 0) {
                log.error("Duration is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Duration should be greater than 0");
            }

            if (amcConfiguration.getVisitFrequencyMonths() == null || amcConfiguration.getVisitFrequencyMonths() <= 0) {
                log.error("Visit Frequency is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Visit Frequency should be greater than 0");
            }

            if (amcConfiguration.getConfigurationStartDate() == null || amcConfiguration.getConfigurationStartDate() == 0) {
                log.error("Start Date is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Visit Frequency should be greater than 0");
            }

            // Check if amcConfiguration dates are within amcConfiguration dates
//            isAmcConfigurationWithinProject(existingProject, amcConfiguration, errorMap);

            if (StringUtils.isBlank(amcConfiguration.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_AmcConfiguration_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (amcConfiguration.getAssetTypes() == null || amcConfiguration.getAssetTypes().isEmpty()) {
                log.error(ASSET_TYPES_IS_MANDATORY_IN_AMC_CONFIG_REQUEST_BODY);
                errorMap.put("AMC Configuration", "ASSET_TYPES_IS_MANDATORY_IN_AMC_CONFIG_REQUEST_BODY");
            }
            if ((amcConfiguration.getConfigurationStartDate() != null && amcConfiguration.getConfigurationEndDate() != null && amcConfiguration.getConfigurationEndDate() != 0) && (amcConfiguration.getConfigurationStartDate().compareTo(amcConfiguration.getConfigurationEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE_ERROR", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (amcConfiguration.getConfigurationStartDate() != null && amcConfiguration.getConfigurationEndDate() != null && amcConfiguration.getConfigurationEndDate() != 0
                    && amcConfiguration.getConfigurationEndDate().compareTo(Instant.ofEpochMilli(amcConfiguration.getConfigurationStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }

            // Validate assignment users data
            validateAmcConfigurationAssignmentRequest(amcConfiguration.getAssignments(), errorMap) ;
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateAmcConfigurationAssignmentRequest(List<AmcConfigurationAssignment> request, Map<String, String> errorMap) {

        if (request == null || request.size() == 0) {
            log.error("Assignment list is empty. Assignment is mandatory");
            throw new CustomException("Amc Configuration Assignment", "Assignment are mandatory");
        }

        for (AmcConfigurationAssignment assignment : request) {
            if (assignment == null) {
                log.error("Amc Configuration Assignment is mandatory in AmcConfiguration");
                throw new CustomException("Amc Configuration Assignment", "Amc Configuration Assignment is mandatory");
            }

            if (assignment.getAssignedUser() == null || assignment.getAssignedUser().isEmpty()) {
                log.error("AssignUser ID is mandatory in Amc Configuration Assignment");
                throw new CustomException("AMC Configuration Assignment", "AssignUser ID is mandatory");
            }

            if (StringUtils.isBlank(assignment.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_AmcConfiguration_REQUEST_BODY);
                errorMap.put("TENANT_ID_ASSIGNMENT", "Tenant ID is mandatory");
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

    public Project getProjectById(RequestInfo requestInfo, String projectId, String tenantId) {
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(requestInfo).projects(List.of(project)).build();
        String url = config.getProjectServiceHost() + config.getProjectServiceSearchUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), projectRequest, Map.class);
        ProjectResponse projectResponse = mapper.convertValue(response, ProjectResponse.class);
        if(projectResponse != null && projectResponse.getProject() !=null && projectResponse.getProject().size() > 0){
            return projectResponse.getProject().get(0);
        }
        return null;
    }

    public Facility getFacilityById(String facilityId) {

        String url = config.getFacilityServiceHost() + config.getFacilityServiceSearchUrlV2()+ "?facilityId="+facilityId;
        Object response = requestRepository.fetchResult(new StringBuilder(url));

        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
            return facilityList.getFacilities().get(0);
        }
        return null;
    }

    public void isAmcConfigurationWithinProject(Project project, AmcConfiguration amcConfiguration, Map<String, String> errorMap) {
        if (project == null || amcConfiguration == null) {
            log.error("Project or AMC configuration is null");
            errorMap.put("AMC Configuration", "Project or AmcConfiguration is null");
        }

        Long projectStart = project.getStartDate();
        Long projectEnd   = project.getEndDate();
        Long amcConfigStart   = amcConfiguration.getConfigurationStartDate();
        Long amcConfigEnd     = amcConfiguration.getConfigurationEndDate();

        if (projectStart == null || projectEnd == null) {
            log.error("Project dates are not mandatory");
            errorMap.put("AMC Configuration", "Project dates are not mandatory");
        }
        if (amcConfigStart == null || amcConfigEnd == null) {
            log.error("AMC Configuration dates are not mandatory");
            errorMap.put("AMC Configuration", "AmcConfiguration dates are not mandatory");
        }

        if (amcConfigStart < projectStart) {
            log.error("The AMC Configuration start date is earlier than the Project start date");
            errorMap.put("AMC_STARTDATE", "The AMC Configuration start date is earlier than the Project start date");
        }
        if (amcConfigEnd > projectEnd) {
            log.error("The AMC Configuration end date is later than the Project end date");
            errorMap.put("AMC_ENDDATE", "The AMC Configuration end date is later than the Project end date");
        }
    }

    /* Validates Update Project request body */
    public void validateUpdateAmcConfigurationRequest(AmcConfigurationRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify Project request and if mandatory fields are present
        validateAmcConfigurationRequest(request);
        //Verify if project request have multiple tenant Ids
        validateMultipleTenantIds(request);

        //Verify if AmcConfiguration id is present
        for (AmcConfiguration amcConfiguration : request.getAmcConfigurations()) {
            if (StringUtils.isBlank(amcConfiguration.getId())) {
                log.error("AMC_Id is mandatory");
                throw new CustomException("UPDATE_AMC_Configuration", "Amc Configuration Id is mandatory");
            }
        }


        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /* Validates search AmcConfiguration request body and parameters*/
    public void validateSearchAmcConfigurationRequest(AmcConfigurationSearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search AmcConfiguration request parameters are valid
        validateSearchAmcConfigurationRequestParams(limit, offset, tenantId);
        //Verify if search AmcConfiguration request is valid
        validateSearchAmcConfiguration(request.getSearchCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchAmcConfigurationRequestParams(Integer limit, Integer offset, String tenantId) {
        if (limit == null) {
            log.error("limit is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.LIMIT", "limit is mandatory for Project Search");
        }

        if (offset == null) {
            log.error("offset is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.OFFSET", "offset is mandatory for Project Search");
        }

        if (StringUtils.isBlank(tenantId)) {
            log.error("tenantId is mandatory parameter in Project search");
            throw new CustomException("SEARCH_PROJECT.TENANT_ID", "tenantId is mandatory for Project Search");
        }
    }

    /* Validates Search Project Request body */
    private void validateSearchAmcConfiguration(AmcConfigurationSearchCriteria amcConfiguration, String tenantId) {
//        checkAmcConfigurationIfEmpty(amcConfigurations);
        doNullAndEmptyChecks(tenantId, amcConfiguration);
//
        if ((amcConfiguration.getConfigurationStartDate() != null && amcConfiguration.getConfigurationStartDate() != null && amcConfiguration.getConfigurationEndDate() != 0) && (amcConfiguration.getConfigurationEndDate().compareTo(amcConfiguration.getConfigurationEndDate()) > 0)) {
            log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            throw new CustomException("INVALID_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
        }

        if ((amcConfiguration.getConfigurationStartDate() == null || amcConfiguration.getConfigurationStartDate() == 0) && (amcConfiguration.getConfigurationEndDate() != null && amcConfiguration.getConfigurationEndDate() != 0)) {
            log.error("Start date is required if end date is passed");
            throw new CustomException("INVALID_DATE", "Start date is required if end date is passed");
        }
    }

//    private static void checkAmcConfigurationIfEmpty(List<AmcConfiguration> amcConfigurations) {
//        if (amcConfigurations == null || amcConfigurations.size() == 0) {
//            log.error("amcConfiguration list is empty. AmcConfiguration is mandatory");
//            throw new CustomException("amcConfiguration", "AmcConfiguration are mandatory");
//        }
//    }

    private static void doNullAndEmptyChecks(String tenantId, AmcConfigurationSearchCriteria amcConfiguration) {
        if (amcConfiguration == null) {
            log.error("amcConfiguration is mandatory in AmcConfiguration");
            throw new CustomException("AmcConfiguration", "AmcConfiguration is mandatory");
        }
        if (StringUtils.isBlank(amcConfiguration.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_AmcConfiguration_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((amcConfiguration.getIds()==null || amcConfiguration.getIds().isEmpty()) && (amcConfiguration.getProjectIds()==null || amcConfiguration.getProjectIds().isEmpty())
                && (amcConfiguration.getStatuses()==null || amcConfiguration.getStatuses().isEmpty()) && (amcConfiguration.getVendorIds()==null || amcConfiguration.getVendorIds().isEmpty())
                && (amcConfiguration.getFacilityIds()==null || amcConfiguration.getFacilityIds().isEmpty()) && (amcConfiguration.getAssignedUsers()==null || amcConfiguration.getAssignedUsers().isEmpty())
                && (amcConfiguration.getConfigurationStartDate() == null || amcConfiguration.getConfigurationStartDate() == 0)
                && (amcConfiguration.getConfigurationEndDate() == null || amcConfiguration.getConfigurationEndDate() == 0)) {
            log.error("Any one amcConfiguration search field is required for AmcConfiguration Search");
            throw new CustomException("AMC_CONFIGURATION_SEARCH_FIELDS", "Any one amc configuration search field is required");
        }

        if (!amcConfiguration.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as AMC CONFIGURATION request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and AMC CONFIGURATION request");
        }
    }

    /* Validates if all AmcConfiguration have same tenant Id */
    private void validateMultipleTenantIds(AmcConfigurationRequest request) {
        List<AmcConfiguration> amcConfigurations = request.getAmcConfigurations();
        String firstTenantId = amcConfigurations.get(0).getTenantId();
        if (amcConfigurations.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All amcConfigurations in AmcConfiguration request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All amcConfigurations must have same tenant Id. Please create new request for different tentant id");
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<AmcConfiguration> amcConfigurationsFromRequest, List<AmcConfiguration> amcConfigurationsFromDB) {
        if (CollectionUtils.isEmpty(amcConfigurationsFromDB)) {
            log.error("The amcConfiguration records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_AmcConfiguration_MODIFY", "The records that you are trying to update does not exists in the system");
        }
        Long currentTimestamp = Instant.now().toEpochMilli();
        // Calculate the timestamp for midnight (12:00 AM) of the next date, plus 24 hours, in UTC
        Instant nextDateInstantUTC = Instant.ofEpochMilli(currentTimestamp)
                .plus(Duration.ofDays(1))  // Add 1 day to get the next date
                .atZone(ZoneOffset.UTC)
                .toLocalDate()  // Extract the date part
                .atStartOfDay(ZoneOffset.UTC)  // Set the time to midnight
                .toInstant()// Convert to Instant
                .plus(Duration.ofDays(1));  // Add 1 day

        Long nextDateTimestampUTC = nextDateInstantUTC.toEpochMilli();
        for (AmcConfiguration amcConfiguration : amcConfigurationsFromRequest) {
            AmcConfiguration amcConfigurationFromDB = amcConfigurationsFromDB.stream().filter(p -> p.getId().equals(amcConfiguration.getId())).findFirst().orElse(null);

            if (amcConfigurationFromDB == null) {
                log.error("The amcConfiguration id " + amcConfiguration.getId() + " that you are trying to update does not exists for the amcConfiguration");
                throw new CustomException("INVALID_AmcConfiguration_MODIFY", "The amcConfiguration id " + amcConfiguration.getId() + " that you are trying to update does not exists for the amcConfiguration");
            }

            validateStartDateAndEndDateAgainstDB(amcConfiguration, amcConfigurationFromDB, currentTimestamp, nextDateTimestampUTC);

//            validateUpdateAddressAgainstDB(project, projectFromDB);
        }
    }

    private void validateStartDateAndEndDateAgainstDB(AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
        String errorMessage = "";
        // Check if the amcConfiguration start date is not null and whether it's different from the one in the database
        errorMessage = getErrorMessage(amcConfiguration, amcConfigurationFromDB, currentTimestamp, nextDateTimestampUTC, errorMessage);
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_AMC_CONFIGURATION_MODIFY", errorMessage);
        }

        errorMessage = "";
        // Check if the project end date is not null and whether it's different from the one in the database
        if (amcConfiguration.getConfigurationEndDate() != null) {
            // Check if the project end date is before the current timestamp or within 24 hours from the next date's midnight
            if (amcConfiguration.getConfigurationEndDate().compareTo(amcConfigurationFromDB.getConfigurationEndDate()) < 0) {
                if (amcConfiguration.getConfigurationEndDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The amc configuration end date cannot be updated as it has already ended. The amc configuration end date cannot be decreased to a past date.";
                } else if (amcConfiguration.getConfigurationEndDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The amc configuration end date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The amc configuration end date cannot be updated as it is null.";
        }
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
        }
    }

    private static String getErrorMessage(AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
        if (amcConfiguration.getConfigurationStartDate() != null) {
            // Check if the project start date is different from the one in the database
            if (amcConfiguration.getConfigurationStartDate().compareTo(amcConfigurationFromDB.getConfigurationStartDate()) != 0) {
                // Check if the project start date is before the current timestamp or within 24 hours from the next date's midnight
                if (amcConfigurationFromDB.getConfigurationStartDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The amcConfiguration start date cannot be updated as the amcConfiguration has already started.";
                } else if (amcConfiguration.getConfigurationStartDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The amcConfiguration start date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The project start date cannot be updated as it is null.";
        }
        return errorMessage;
    }
}