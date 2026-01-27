package org.egov.amc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.producer.Producer;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AmcConfigurationRepository;
import org.egov.amc.service.enrichment.AmcConfigurationEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.validator.AmcConfigurationValidator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AmcConfigurationService {

    private final AmcConfigurationValidator amcConfigurationValidator;
    private final AmcConfigurationRepository amcConfigurationRepository;
    private final Producer producer;
    private final AmcConfigurationEnrichment amcConfigurationEnrichment;

    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public AmcConfigurationService(
            AmcConfigurationRepository amcConfigurationRepository, AmcConfigurationValidator amcConfigurationValidator, AmcConfigurationEnrichment amcConfigurationEnrichment, AMCServiceConfiguration amcConfigurationConfiguration,
            Producer producer, AmcConfigurationServiceUtil amcConfigurationServiceUtil) {
            this.amcConfigurationValidator = amcConfigurationValidator;
            this.producer = producer;
            this.amcServiceConfiguration = amcConfigurationConfiguration;
            this.amcConfigurationRepository = amcConfigurationRepository;
            this.amcConfigurationEnrichment = amcConfigurationEnrichment;
            this.amcConfigurationServiceUtil = amcConfigurationServiceUtil;
    }

    public AmcConfigurationRequest createAmcConfiguration(AmcConfigurationRequest request) {
        log.trace("Entering createAmcConfiguration method");
        log.info("Creating {} AMC configuration(s)", request.getAmcConfigurations().size());
        amcConfigurationValidator.validateCreateAmcConfigurationRequest(request);
        for (AmcConfiguration amcConfiguration : request.getAmcConfigurations()) {
            // remove Duplicate Assignments if the same user is AMC_STAFF and AMC_REVIEWER
            Set<String> seenUsers = new HashSet<>();
            List<AmcConfigurationAssignment> assignments = amcConfiguration.getAssignments().stream().filter(a -> seenUsers.add(a.getAssignedUser()))
                    .toList();
            amcConfiguration.setAssignments(assignments);
            amcConfigurationEnrichment.enrichAmcConfigurationOnCreate(amcConfiguration, request.getRequestInfo());

            // Link the AMC_REVIEWER and AMC_STAFF to project
            if (amcConfiguration.getAssignments() != null && !amcConfiguration.getAssignments().isEmpty()) {
                List<ProjectStaff> staffs = amcConfiguration.getAssignments().stream()
                        .map(assignment -> ProjectStaff.builder()
                                .tenantId(amcConfiguration.getTenantId())
                                .projectId(amcConfiguration.getProjectId())
                                .userId(assignment.getAssignedUser())
                                .build())
                        .collect(Collectors.toList());
                amcConfigurationServiceUtil.createProjectStaff(request.getRequestInfo(), staffs);
                log.debug("Created {} project staff assignment(s) for configuration", staffs.size());
            }
            log.trace("Enriching AMC configuration on create for projectId: {}, facilityId: {}", 
                    amcConfiguration.getProjectId(), amcConfiguration.getFacilityId());
            log.info("AMC configuration enriched with project ID: {}, facility ID: {}", 
                    amcConfiguration.getProjectId(), amcConfiguration.getFacilityId());
            log.debug("Enriched AMC configuration details - duration: {} months, visitFrequency: {} months", 
                    amcConfiguration.getDurationMonths(), amcConfiguration.getVisitFrequencyMonths());
        }
        log.info("Pushing {} AMC configuration(s) to kafka", request.getAmcConfigurations().size());
        producer.push(amcServiceConfiguration.getSaveAmcConfigurationTopic(), request);
        return request;
    }

    public AmcConfigurationRequest updateAmcConfiguration(AmcConfigurationRequest request) {
        log.trace("Entering updateAmcConfiguration method");
        /*
         * Validate the update amcConfiguration request
         */
        amcConfigurationValidator.validateUpdateAmcConfigurationRequest(request);
        log.info("Update AMC configuration request validated, configuration count: {}", request.getAmcConfigurations().size());

        /*
         * Search for amcConfiguration based on amcConfiguration IDs provided in the request
         */
        List<AmcConfiguration> amcConfigurationsFromDB = searchAmcConfiguration(
                getSearchAmcConfigurationRequest(request.getAmcConfigurations(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getAmcConfigurations().get(0).getTenantId(), false, null);
        log.debug("Fetched {} AMC configuration(s) from database for update request", amcConfigurationsFromDB.size());
        log.info("Fetched AMC configurations for update request");

        /*
         * Validate the update amcConfiguration request against the amcConfigurations fetched from the database
         */
        amcConfigurationValidator.validateUpdateAgainstDB(request.getAmcConfigurations(), amcConfigurationsFromDB);

        /*
         * Process each amcConfiguration in the update request
         */
        log.debug("Processing {} AMC configuration(s) for update", request.getAmcConfigurations().size());
        for (AmcConfiguration amcConfiguration : request.getAmcConfigurations()) {
            processamcConfigurationUpdate(request, amcConfiguration, amcConfigurationsFromDB);
        }
        log.info("Successfully processed update for {} AMC configuration(s)", request.getAmcConfigurations().size());

        return request;
    }

    public Integer countAllAmcConfiguration(AmcConfigurationSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return amcConfigurationRepository.getAmcConfigurationCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    /* Construct AmcConfiguration Request object for search which contains amcConfiguration id and tenantId */
    private AmcConfigurationSearchRequest getSearchAmcConfigurationRequest(List<AmcConfiguration> amcConfigurations, RequestInfo requestInfo) {
        List<String> amcConfigurationIds = amcConfigurations.stream().map(AmcConfiguration::getId).toList();
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder().ids(amcConfigurationIds).tenantId(amcConfigurations.get(0).getTenantId()).build();
        return AmcConfigurationSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
    }

    public List<AmcConfiguration> searchAmcConfiguration(AmcConfigurationSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering searchAmcConfiguration method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        amcConfigurationValidator.validateSearchAmcConfigurationRequest(request, limit, offset, tenantId);
        List<AmcConfiguration> amcConfigurationList = amcConfigurationRepository.getAmcConfiguration(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Found {} AMC configuration(s) matching search criteria", amcConfigurationList.size());
        log.info("AMC configuration search completed");
        return amcConfigurationList;
    }

    private void processamcConfigurationUpdate(AmcConfigurationRequest request, AmcConfiguration amcConfiguration, List<AmcConfiguration> amcConfigurationsFromDB) {
        /*
         * Convert amcConfiguration ID to string for comparison
         */
        String amcConfigurationId = String.valueOf(amcConfiguration.getId());

        /*
         * Find the amcConfiguration from the database that matches the current amcConfiguration ID
         */
        AmcConfiguration amcConfigurationFromDB = findAmcConfigurationById(amcConfigurationId, amcConfigurationsFromDB);

        if (amcConfigurationFromDB == null) {
            return;
        }

        /*
         * Merge additional details of the amcConfiguration from the request and amcConfiguration from DB
         */
        amcConfigurationServiceUtil.mergeAdditionalDetails(amcConfiguration, amcConfigurationFromDB);

        handleUpdateamcConfiguration(request, amcConfiguration, amcConfigurationFromDB);
    }

    private void handleUpdateamcConfiguration(AmcConfigurationRequest request, AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDB) {
        OriginalAmcSnapshot originalSnapshot = captureOriginalSnapshot(amcConfigurationFromDB);

        applyRequestedChanges(amcConfiguration, amcConfigurationFromDB);

        validateAllowedUpdate(amcConfigurationFromDB, amcConfiguration);

        restoreOriginalSnapshot(amcConfigurationFromDB, originalSnapshot);

        amcConfigurationEnrichment.enrichAmcConfigurationRequestOnUpdate(
                amcConfiguration,
                amcConfigurationFromDB,
                request.getRequestInfo()
        );

        log.debug("Pushing AMC configuration update to kafka for configurationId: {}", amcConfiguration.getId());
        producer.push(amcServiceConfiguration.getUpdateAmcConfigurationTopic(), request);
        log.info("AMC configuration update pushed to kafka for configurationId: {}", amcConfiguration.getId());
    }

    private void validateAllowedUpdate(AmcConfiguration updatedFromDB, AmcConfiguration requestConfiguration) {
        if (!isValidCascadingUpdate(updatedFromDB, requestConfiguration)) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Can only update amc configs dates, asset types, vendor and additional details"
            );
        }
    }

    private OriginalAmcSnapshot captureOriginalSnapshot(AmcConfiguration amcConfigurationFromDB) {
        return new OriginalAmcSnapshot(
                amcConfigurationFromDB.getConfigurationStartDate(),
                amcConfigurationFromDB.getConfigurationEndDate(),
                amcConfigurationFromDB.getAssetTypes(),
                amcConfigurationFromDB.getDurationMonths(),
                amcConfigurationFromDB.getVisitFrequencyMonths(),
                amcConfigurationFromDB.getVendorId(),
                amcConfigurationFromDB.getAuditDetails()
        );
    }

    private void applyRequestedChanges(AmcConfiguration requestConfiguration, AmcConfiguration amcConfigurationFromDB) {
        amcConfigurationFromDB.setConfigurationStartDate(requestConfiguration.getConfigurationStartDate());
        amcConfigurationFromDB.setConfigurationEndDate(requestConfiguration.getConfigurationEndDate());
        amcConfigurationFromDB.setAssetTypes(requestConfiguration.getAssetTypes());
        amcConfigurationFromDB.setDurationMonths(requestConfiguration.getDurationMonths());
        amcConfigurationFromDB.setVisitFrequencyMonths(requestConfiguration.getVisitFrequencyMonths());
        amcConfigurationFromDB.setVendorId(amcConfigurationFromDB.getId());
        amcConfigurationFromDB.setAuditDetails(requestConfiguration.getAuditDetails());
    }

    private void restoreOriginalSnapshot(AmcConfiguration amcConfigurationFromDB, OriginalAmcSnapshot snapshot) {
        amcConfigurationFromDB.setConfigurationStartDate(snapshot.originalStartDate());
        amcConfigurationFromDB.setConfigurationEndDate(snapshot.originalEndDate());
        amcConfigurationFromDB.setAssetTypes(snapshot.originalAssetTypes());
        amcConfigurationFromDB.setDurationMonths(snapshot.originalDurationMonths());
        amcConfigurationFromDB.setVisitFrequencyMonths(snapshot.originalVisitFrequencyMonths());
        amcConfigurationFromDB.setVendorId(snapshot.originalVendorId());
        amcConfigurationFromDB.setAuditDetails(snapshot.originalAuditDetails());
    }

    private record OriginalAmcSnapshot(
            Long originalStartDate,
            Long originalEndDate,
            List<Map<String, Object>> originalAssetTypes,
            int originalDurationMonths,
            int originalVisitFrequencyMonths,
            String originalVendorId,
            AuditDetails originalAuditDetails
    ) {
    }

    private boolean isValidCascadingUpdate(AmcConfiguration amcConfigurationFromDB, AmcConfiguration amcConfiguration) {
        // Check if only allowed fields are being updated
        return Objects.equals(amcConfigurationFromDB.getId(), amcConfiguration.getId()) &&
                Objects.equals(amcConfigurationFromDB.getTenantId(), amcConfiguration.getTenantId()) &&
                Objects.equals(amcConfigurationFromDB.getFacilityId(), amcConfiguration.getFacilityId()) &&
                Objects.equals(amcConfigurationFromDB.getProjectId(), amcConfiguration.getProjectId());
        // Note: We allow startDate, endDate, vendorId, geographyDetails, activities and auditDetails to be different
    }

    /**
     * Validates if only allowed fields in additionalDetails are being updated
     * Allowed: geographyDetails (districts, blocks)
     * Read-only: justificationCode field
     */
    private boolean isValidGeographyDetailsUpdate(Object originalGeographyDetails, Object newGeographyDetails) {
        if (originalGeographyDetails == null && newGeographyDetails == null) {
            return true;
        }
        if (originalGeographyDetails == null || newGeographyDetails == null) {
            return false;
        }

        try {
            // Convert to JsonNode for easier comparison
            JsonNode originalNode = mapper.valueToTree(originalGeographyDetails);
            JsonNode newNode = mapper.valueToTree(newGeographyDetails);

            // Check if state is unchanged (read-only)
            JsonNode originalState = originalNode.get("state");
            JsonNode newState = newNode.get("state");
            if (!Objects.equals(originalState, newState)) {
                log.warn("State cannot be changed during cascading update - original: {}, new: {}", originalState, newState);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating geographyDetails update", e);
            return false;
        }
    }

    private AmcConfiguration findAmcConfigurationById(String amcConfigurationId, List<AmcConfiguration> amcConfigurationsFromDB) {
        /*
         * Find and return the amcConfiguration with the matching ID from the list of amcConfiguration fetched from the database
         */
        return amcConfigurationsFromDB.stream()
                .filter(p -> amcConfigurationId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
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
