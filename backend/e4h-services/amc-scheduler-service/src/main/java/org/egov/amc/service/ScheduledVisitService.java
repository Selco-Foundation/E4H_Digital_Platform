package org.egov.amc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.service.enrichment.ScheduledVisitEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.validator.ScheduledVisitValidator;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ScheduledVisitService {

    private final ScheduledVisitValidator scheduledVisitsValidator;
    private final ScheduledVisitRepository scheduledVisitsRepository;
    private final ServiceRequestRepository requestRepository;
    private final Producer producer;
    private final ScheduledVisitEnrichment scheduledVisitsEnrichment;
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public ScheduledVisitService(
            ScheduledVisitRepository scheduledVisitsRepository, ScheduledVisitValidator scheduledVisitsValidator, ServiceRequestRepository requestRepository, ScheduledVisitEnrichment scheduledVisitsEnrichment, AMCServiceConfiguration scheduledVisitsConfiguration,
            Producer producer, AmcConfigurationServiceUtil scheduledVisitsServiceUtil) {
            this.scheduledVisitsValidator = scheduledVisitsValidator;
        this.requestRepository = requestRepository;
        this.producer = producer;
            this.amcServiceConfiguration = scheduledVisitsConfiguration;
            this.scheduledVisitsRepository = scheduledVisitsRepository;
            this.scheduledVisitsEnrichment = scheduledVisitsEnrichment;
            this.amcConfigurationServiceUtil = scheduledVisitsServiceUtil;
    }

    public ScheduledVisitRequest createScheduledVisit(ScheduledVisitRequest request) {
        scheduledVisitsValidator.validateCreateScheduledVisitRequest(request);
        for (ScheduledVisit amcConfiguration : request.getScheduledVisits()) {
            scheduledVisitsEnrichment.enrichScheduledVisitOnCreate(amcConfiguration, request.getRequestInfo());
            log.info("Enriched with AMC Ids and AuditDetails {}", amcConfiguration);
            producer.push(amcServiceConfiguration.getSaveScheduledVisitTopic(), request);
            log.info("Pushed to kafka");
        }
        return request;
    }

    public ScheduledVisitRequest updateScheduledVisit(ScheduledVisitRequest request) {
        /*
         * Validate the update scheduledVisits request
         */
        scheduledVisitsValidator.validateUpdateScheduledVisitRequest(request);
        log.info("Update asset_amc request validated");

        /*
         * Search for asset_amc based on asset_amc IDs provided in the request
         */
        List<ScheduledVisit> amcConfigurationsFromDB = searchScheduledVisit(
                getSearchScheduledVisitRequest(request.getScheduledVisits(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getScheduledVisits().get(0).getTenantId(), false, null);
        log.info("Fetched scheduledVisits for update request");

        /*
         * Validate the update asset_amc request against the asset_amcs fetched from the database
         */
        scheduledVisitsValidator.validateUpdateAgainstDB(request.getScheduledVisits(), amcConfigurationsFromDB);

        /*
         * Process each scheduledVisits in the update request
         */
        for (ScheduledVisit amcConfiguration : request.getScheduledVisits()) {
            processScheduledVisitUpdate(request, amcConfiguration, amcConfigurationsFromDB);
        }

        return request;
    }

    public Integer countAllScheduledVisits(ScheduledVisitSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return scheduledVisitsRepository.getScheduledVisitCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    /* Construct ScheduledVisit Request object for search which contains asset_amc id and tenantId */
    private ScheduledVisitSearchRequest getSearchScheduledVisitRequest(List<ScheduledVisit> amcConfigurations, RequestInfo requestInfo) {
        List<String> scheduledVisitsIds = amcConfigurations.stream().map(ScheduledVisit::getId).toList();
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(scheduledVisitsIds).tenantId(amcConfigurations.get(0).getTenantId()).build();
        return ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
    }

    public List<ScheduledVisit> searchScheduledVisit(ScheduledVisitSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        scheduledVisitsValidator.validateSearchScheduledVisitRequest(request, limit, offset, tenantId);
        List<ScheduledVisit> amcConfigurationList = scheduledVisitsRepository.getScheduledVisit(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        return amcConfigurationList;
    }

    private void processScheduledVisitUpdate(ScheduledVisitRequest request, ScheduledVisit amcConfiguration, List<ScheduledVisit> amcConfigurationsFromDB) {
        /*
         * Convert asset_amc ID to string for comparison
         */
        String scheduledVisitsId = String.valueOf(amcConfiguration.getId());

        /*
         * Find the scheduledVisits from the database that matches the current scheduledVisits ID
         */
        ScheduledVisit amcConfigurationFromDB = findScheduledVisitById(scheduledVisitsId, amcConfigurationsFromDB);

        if (amcConfigurationFromDB != null) {
            /*
             * Merge additional details of the scheduledVisits from the request and scheduledVisits from DB
             */
            amcConfigurationServiceUtil.mergeScheduledVisitAdditionalDetails(amcConfiguration, amcConfigurationFromDB);

//            handleUpdateScheduledVisit(request, amcConfiguration, amcConfigurationFromDB);
        }
    }

//    private void handleUpdateScheduledVisit(ScheduledVisitRequest request, ScheduledVisit scheduledVisits, ScheduledVisit scheduledVisitsFromDB) {
//        /*
//         * Save original values of start date, end date, and additional details
//         */
//        Long originalStartDate = scheduledVisitsFromDB.getAmcStartDate();
//        Long originalEndDate = scheduledVisitsFromDB.getAmcEndDate();
//        AuditDetails originalAuditDetails = scheduledVisitsFromDB.getAuditDetails();
//
//
//        /*
//         * Update the scheduledVisits with new start date, end date, and additional details
//         */
//        scheduledVisitsFromDB.setAmcStartDate(scheduledVisits.getAmcStartDate());
//        scheduledVisitsFromDB.setAmcEndDate(scheduledVisits.getAmcEndDate());
//        scheduledVisitsFromDB.setAuditDetails(scheduledVisits.getAuditDetails());
//
//        /*
//         * Ensure that no other properties are being updated besides the start and end dates
//         */
//        if (!isValidCascadingUpdate(scheduledVisitsFromDB, scheduledVisits)) {
//            throw new CustomException(
//                    "AMC_UPDATE_ERROR",
//                    "Can only update amc configs dates, asset types, vendor and additional details"
//            );
//        }
//
//        /*
//         * Restore original values of start date, end date, and additional details
//         */
//        scheduledVisitsFromDB.setAmcStartDate(originalStartDate);
//        scheduledVisitsFromDB.setAmcEndDate(originalEndDate);
//        scheduledVisitsFromDB.setAuditDetails(originalAuditDetails);
//
//        /*
//         * Update lastModifiedTime and lastModifiedBy for the scheduledVisits
//         */
//        scheduledVisitsEnrichment.enrichScheduledVisitRequestOnUpdate(scheduledVisits, scheduledVisitsFromDB, request.getRequestInfo());
//
//        /*
//         * Check and enrich cascading scheduledVisits dates and push the update to the message broker
//         */
//        producer.push(amcServiceConfiguration.getUpdateScheduledVisitTopic(), request);
//    }

//    private boolean isValidCascadingUpdate(ScheduledVisit scheduledVisitsFromDB, ScheduledVisit scheduledVisits) {
//        // Check if only allowed fields are being updated
//        return Objects.equals(scheduledVisitsFromDB.getId(), scheduledVisits.getId()) &&
//                Objects.equals(scheduledVisitsFromDB.getTenantId(), scheduledVisits.getTenantId()) &&
//                Objects.equals(scheduledVisitsFromDB.getAssetId(), scheduledVisits.getAssetId()) &&
//                Objects.equals(scheduledVisitsFromDB.getAmcConfigurationId(), scheduledVisits.getAmcConfigurationId());
//        // Note: We allow startDate, endDate, vendorId, geographyDetails, activities and auditDetails to be different
//    }

    private ScheduledVisit findScheduledVisitById(String scheduledVisitsId, List<ScheduledVisit> amcConfigurationsFromDB) {
        /*
         * Find and return the scheduledVisits with the matching ID from the list of asset_amc fetched from the database
         */
        return amcConfigurationsFromDB.stream()
                .filter(p -> scheduledVisitsId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    public List<ProcessInstance> getProcessInstanceById(String businessId, String tenantId, RequestInfo requestInfo) {
        String url = amcServiceConfiguration.getWfHost() + amcServiceConfiguration.getWfSearchPath()
                + "?tenantId=" + tenantId
                + "&businessIds=" + businessId
                + "&history=" + true;

        // Wrap RequestInfo in RequestInfoWrapper
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        // POST with requestInfoWrapper as body, query params in URL
        Object response = requestRepository.fetchResult(new StringBuilder(url), requestInfoWrapper);

        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        return (wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty())
                ? null
                : wfResponse.getProcessInstances();
    }

//    public Employee getUserById(Object request, String userId) {
//
//        String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
//        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
//
//        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
//        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
//            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
//        }
//        return employeeResponse.getEmployees().get(0);
//    }

}
