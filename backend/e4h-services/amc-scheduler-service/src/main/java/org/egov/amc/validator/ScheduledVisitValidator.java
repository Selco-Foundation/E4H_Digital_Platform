package org.egov.amc.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.AmcConfigurationService;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.util.MDMSUtils;
import org.egov.amc.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ScheduledVisitValidator {

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    private final ServiceRequestRepository requestRepository;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_scheduledVisit_REQUEST_BODY = "Tenant ID is mandatory in ScheduledVisit request body";
    public static final String ASSET_TYPES_IS_MANDATORY_IN_AMC_CONFIG_REQUEST_BODY = "Assets Types are mandatory in Amc Configuration request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_scheduledVisit = " that you are trying to update does not exists for the ScheduledVisit ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    AMCServiceConfiguration config;

    private final AmcConfigurationService amcConfigurationService;
    private final AmcConfigurationValidator amcConfigurationValidator;
    private final AmcConfigurationServiceUtil scheduledVisitServiceUtil;
    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public ScheduledVisitValidator(ServiceRequestClient serviceRequestRepository, ServiceRequestRepository requestRepository, AmcConfigurationService amcConfigurationService, AmcConfigurationValidator amcConfigurationValidator, AmcConfigurationServiceUtil scheduledVisitServiceUtil){
        this.serviceRequestRepository = serviceRequestRepository;
        this.requestRepository = requestRepository;
        this.amcConfigurationService = amcConfigurationService;
        this.amcConfigurationValidator = amcConfigurationValidator;
        this.scheduledVisitServiceUtil = scheduledVisitServiceUtil;
    }

    public void validateCreateScheduledVisitRequest(ScheduledVisitRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ScheduledVisit request and mandatory fields are present
        validateScheduledVisitRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateScheduledVisitRequest(ScheduledVisitRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getScheduledVisits() == null || request.getScheduledVisits().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("scheduledVisit", "Field Plans are mandatory");
        }

        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            if (scheduledVisit == null) {
                log.error("ScheduledVisit is mandatory in ScheduledVisits");
                throw new CustomException("ScheduledVisit", "ScheduledVisit is mandatory");
            }

            if (scheduledVisit.getFacilityId() == null || scheduledVisit.getFacilityId().isEmpty()) {
                log.error("Facility ID is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Facility ID is mandatory");
            }
            // Get existing facility with facilityID from facility service
            Facility existingFacility = amcConfigurationValidator.getFacilityById(scheduledVisit.getFacilityId());
            if (existingFacility == null) {
                log.error("Facility ID do not exist");
                throw new CustomException("AMC Configuration", "Facility ID do not exist");
            }

            if (scheduledVisit.getAmcConfigurationId() == null || scheduledVisit.getAmcConfigurationId().isEmpty()) {
                log.error("Amc Configuration ID is mandatory in Amc Configuration");
                throw new CustomException("AMC Configuration", "Amc Configuration ID is mandatory");
            }
            // Get existing amcConfiguration from amcConfiguration service
            String amcConfigurationIds = scheduledVisit.getAmcConfigurationId();
            AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder().ids(new ArrayList<>(List.of(amcConfigurationIds))).tenantId(scheduledVisit.getTenantId()).build();
            AmcConfigurationSearchRequest searchRequest = AmcConfigurationSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
            List<AmcConfiguration> amcConfigurationList = amcConfigurationService.searchAmcConfiguration(searchRequest, 10, 0, scheduledVisit.getTenantId(), false, null );
            if (amcConfigurationList ==null || amcConfigurationList.isEmpty()){
                log.error("AMC Configuration ID do not exist");
                throw new CustomException("ScheduledVisit", "AMC Configuration do not exist");
            }

            if (scheduledVisit.getVisitNumber() == null || scheduledVisit.getVisitNumber() == 0) {
                log.error("Visit Number is mandatory in Scheduled Visit");
                throw new CustomException("Scheduled Visit", "Visit Number is mandatory");
            }

            if (scheduledVisit.getVisitNumber() == null || scheduledVisit.getVisitNumber() == 0) {
                log.error("Start Date is mandatory in Scheduled Visit");
                throw new CustomException("Scheduled Visit", "Start Date Scheduled Visit is mandatory");
            }

            if (StringUtils.isBlank(scheduledVisit.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_scheduledVisit_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
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

    /* Validates Update Project request body */
    public void validateUpdateScheduledVisitRequest(ScheduledVisitRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify Project request and if mandatory fields are present
        validateScheduledVisitRequest(request);
        //Verify if project request have multiple tenant Ids
        validateMultipleTenantIds(request);

        //Verify if ScheduledVisit id is present
        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            if (StringUtils.isBlank(scheduledVisit.getId())) {
                log.error("AMC_Id is mandatory");
                throw new CustomException("UPDATE_AMC_Configuration", "Amc Configuration Id is mandatory");
            }
        }


        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /* Validates search ScheduledVisit request body and parameters*/
    public void validateSearchScheduledVisitRequest(ScheduledVisitSearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search ScheduledVisit request parameters are valid
        validateSearchScheduledVisitRequestParams(limit, offset, tenantId);
        //Verify if search ScheduledVisit request is valid
        validateSearchScheduledVisit(request.getSearchCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchScheduledVisitRequestParams(Integer limit, Integer offset, String tenantId) {
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
    private void validateSearchScheduledVisit(ScheduledVisitSearchCriteria scheduledVisit, String tenantId) {
//        checkScheduledVisitsIfEmpty(scheduledVisits);
        doNullAndEmptyChecks(tenantId, scheduledVisit);
//
        if ((scheduledVisit.getScheduledDateFrom() != null && scheduledVisit.getScheduledDateTo() != null && scheduledVisit.getScheduledDateTo() != 0) && (scheduledVisit.getScheduledDateFrom().compareTo(scheduledVisit.getScheduledDateTo()) > 0)) {
            log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            throw new CustomException("INVALID_SCHEDULED_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
        }

        if ((scheduledVisit.getActualDateFrom() != null && scheduledVisit.getActualDateTo() != null && scheduledVisit.getActualDateTo() != 0) && (scheduledVisit.getActualDateFrom().compareTo(scheduledVisit.getActualDateTo()) > 0)) {
            log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            throw new CustomException("INVALID_ACTUAL_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
        }

        if ((scheduledVisit.getScheduledDateFrom() == null || scheduledVisit.getScheduledDateFrom() == 0) && (scheduledVisit.getScheduledDateTo() != null && scheduledVisit.getScheduledDateTo() != 0)) {
            log.error("Start date is required if end date is passed");
            throw new CustomException("INVALID_SCHEDULED_DATE", "Start date is required if end date is passed");
        }

        if ((scheduledVisit.getScheduledDateFrom() != null && scheduledVisit.getScheduledDateFrom() != 0) && (scheduledVisit.getScheduledDateTo() == null || scheduledVisit.getScheduledDateTo() != 0)) {
            log.error("End date is required if Start date is passed");
            throw new CustomException("INVALID_SCHEDULED_DATE", "End date is required if Start date is passed");
        }

        if ((scheduledVisit.getActualDateFrom() == null || scheduledVisit.getActualDateFrom() == 0) && (scheduledVisit.getActualDateTo() != null && scheduledVisit.getActualDateTo() != 0)) {
            log.error("Start date is required if end date is passed");
            throw new CustomException("INVALID_ACTUAL_DATE", "Start date is required if end date is passed");
        }

        if ((scheduledVisit.getActualDateFrom() != null && scheduledVisit.getActualDateFrom() != 0) && (scheduledVisit.getActualDateTo() == null && scheduledVisit.getActualDateTo() != 0)) {
            log.error("End date is required if Start date is passed");
            throw new CustomException("INVALID_ACTUAL_DATE", "End date is required if Start date is passed");
        }
    }

//    private static void checkScheduledVisitsIfEmpty(List<ScheduledVisit> scheduledVisits) {
//        if (scheduledVisits == null || scheduledVisits.size() == 0) {
//            log.error("scheduledVisit list is empty. ScheduledVisits is mandatory");
//            throw new CustomException("scheduledVisit", "ScheduledVisits are mandatory");
//        }
//    }

    private static void doNullAndEmptyChecks(String tenantId, ScheduledVisitSearchCriteria scheduledVisit) {
        if (scheduledVisit == null) {
            log.error("scheduledVisit is mandatory in ScheduledVisits");
            throw new CustomException("scheduledVisit", "ScheduledVisit is mandatory");
        }
        if (StringUtils.isBlank(scheduledVisit.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_scheduledVisit_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((scheduledVisit.getIds()==null || scheduledVisit.getIds().isEmpty()) && (scheduledVisit.getAmcConfigurationIds()==null || scheduledVisit.getAmcConfigurationIds().isEmpty())
                && (scheduledVisit.getStatuses()==null || scheduledVisit.getStatuses().isEmpty()) && (scheduledVisit.getFacilityIds()==null || scheduledVisit.getFacilityIds().isEmpty())
                && (scheduledVisit.getScheduledDateFrom() == null || scheduledVisit.getScheduledDateFrom() == 0) && (scheduledVisit.getScheduledDateTo() == null || scheduledVisit.getScheduledDateTo() == 0)
                && (scheduledVisit.getActualDateFrom() == null || scheduledVisit.getActualDateFrom() == 0) && (scheduledVisit.getActualDateTo() == null || scheduledVisit.getActualDateTo() == 0)
                && (scheduledVisit.getVisitNumbers() == null || scheduledVisit.getVisitNumbers().isEmpty()) && (scheduledVisit.getAssignedUsers() == null || scheduledVisit.getAssignedUsers().isEmpty())
                && (scheduledVisit.getIncludeExpired() == null)){
            log.error("Any one scheduledVisit search field is required for ScheduledVisit Search");
            throw new CustomException("ASSET_AMC_SEARCH_FIELDS", "Any one asset_amc search field is required");
        }

        if (!scheduledVisit.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as Scheduled Visit request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and Scheduled Visit request");
        }
    }

    /* Validates if all ScheduledVisits have same tenant Id */
    private void validateMultipleTenantIds(ScheduledVisitRequest request) {
        List<ScheduledVisit> scheduledVisits = request.getScheduledVisits();
        String firstTenantId = scheduledVisits.get(0).getTenantId();
        if (scheduledVisits.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All Scheduled Visit in ScheduledVisit request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All Scheduled Visit must have same tenant Id. Please create new request for different tentant id");
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<ScheduledVisit> scheduledVisitsFromRequest, List<ScheduledVisit> scheduledVisitsFromDB) {
        if (CollectionUtils.isEmpty(scheduledVisitsFromDB)) {
            log.error("The Scheduled Visit records that you are trying to update does not exists in the system");
            throw new CustomException("INVALID_scheduledVisit_MODIFY", "The records that you are trying to update does not exists in the system");
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
        for (ScheduledVisit scheduledVisit : scheduledVisitsFromRequest) {
            ScheduledVisit scheduledVisitFromDB = scheduledVisitsFromDB.stream().filter(p -> p.getId().equals(scheduledVisit.getId())).findFirst().orElse(null);

            if (scheduledVisitFromDB == null) {
                log.error("The Scheduled Visit id " + scheduledVisit.getId() + " that you are trying to update does not exists for the Scheduled Visit");
                throw new CustomException("INVALID_scheduledVisit_MODIFY", "The Scheduled Visit id " + scheduledVisit.getId() + " that you are trying to update does not exists for the Scheduled Visit");
            }

        }
    }

//    private void validateStartDateAndEndDateAgainstDB(ScheduledVisit scheduledVisit, ScheduledVisit scheduledVisitFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
//        String errorMessage = "";
//        // Check if the Scheduled Visit start date is not null and whether it's different from the one in the database
//        errorMessage = getErrorMessage(scheduledVisit, scheduledVisitFromDB, currentTimestamp, nextDateTimestampUTC, errorMessage);
//        // If there's an error message, log it and throw a CustomException
//        if (!errorMessage.trim().isEmpty()) {
//            log.error(errorMessage);
//            throw new CustomException("INVALID_ASSET_AMC_MODIFY", errorMessage);
//        }
//
//        errorMessage = "";
//        // Check if the project end date is not null and whether it's different from the one in the database
//        if (scheduledVisit.getAmcEndDate() != null) {
//            // Check if the project end date is before the current timestamp or within 24 hours from the next date's midnight
//            if (scheduledVisit.getAmcEndDate().compareTo(scheduledVisitFromDB.getAmcEndDate()) < 0) {
//                if (scheduledVisit.getAmcEndDate().compareTo(currentTimestamp) < 0) {
//                    errorMessage = "The asset_amc end date cannot be updated as it has already ended. The asset_amc end date cannot be decreased to a past date.";
//                } else if (scheduledVisit.getAmcEndDate().compareTo(nextDateTimestampUTC) < 0) {
//                    errorMessage = "The asset_amc end date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
//                }
//            }
//        } else {
//            errorMessage = "The asset_amc end date cannot be updated as it is null.";
//        }
//        // If there's an error message, log it and throw a CustomException
//        if (!errorMessage.trim().isEmpty()) {
//            log.error(errorMessage);
//            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
//        }
//    }
//
//    private static String getErrorMessage(ScheduledVisit scheduledVisit, ScheduledVisit scheduledVisitFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
//        if (scheduledVisit.getAmcStartDate() != null) {
//            // Check if the project start date is different from the one in the database
//            if (scheduledVisit.getAmcStartDate().compareTo(scheduledVisitFromDB.getAmcStartDate()) != 0) {
//                // Check if the project start date is before the current timestamp or within 24 hours from the next date's midnight
//                if (scheduledVisitFromDB.getAmcStartDate().compareTo(currentTimestamp) < 0) {
//                    errorMessage = "The Scheduled Visit start date cannot be updated as the Scheduled Visit has already started.";
//                } else if (scheduledVisit.getAmcStartDate().compareTo(nextDateTimestampUTC) < 0) {
//                    errorMessage = "The Scheduled Visit start date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
//                }
//            }
//        } else {
//            errorMessage = "The project start date cannot be updated as it is null.";
//        }
//        return errorMessage;
//    }
}