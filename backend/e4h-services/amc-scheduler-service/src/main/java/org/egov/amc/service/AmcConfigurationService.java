package org.egov.amc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.repository.AssetAmcRepository;
import org.egov.amc.repository.ScheduledVisitRepository;
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
    private final ScheduledVisitRepository scheduledVisitRepository;
    private final AmcConfigurationEnrichment amcConfigurationEnrichment;

    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;
    private final ServiceRequestRepository requestRepository;
    private final AssetAmcRepository assetAmcRepository;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public AmcConfigurationService(
            AmcConfigurationRepository amcConfigurationRepository, AmcConfigurationValidator amcConfigurationValidator, ScheduledVisitRepository scheduledVisitRepository, AmcConfigurationEnrichment amcConfigurationEnrichment, AMCServiceConfiguration amcConfigurationConfiguration,
            Producer producer, AmcConfigurationServiceUtil amcConfigurationServiceUtil, ServiceRequestRepository requestRepository, AssetAmcRepository assetAmcRepository) {
            this.amcConfigurationValidator = amcConfigurationValidator;
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.producer = producer;
            this.amcServiceConfiguration = amcConfigurationConfiguration;
            this.amcConfigurationRepository = amcConfigurationRepository;
            this.amcConfigurationEnrichment = amcConfigurationEnrichment;
            this.amcConfigurationServiceUtil = amcConfigurationServiceUtil;
        this.requestRepository = requestRepository;
        this.assetAmcRepository = assetAmcRepository;
    }

    public AmcConfigurationRequest createAmcConfiguration(AmcConfigurationRequest request) {
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
            }
            log.info("Enriched with AMC Ids and AuditDetails {}", amcConfiguration);
            log.info("Pushed to kafka");
        }
        producer.push(amcServiceConfiguration.getSaveAmcConfigurationTopic(), request);
        return request;
    }

    public AmcConfigurationRequest updateAmcConfiguration(AmcConfigurationRequest request) {
        /*
         * Validate the update amcConfiguration request
         */
        amcConfigurationValidator.validateUpdateAmcConfigurationRequest(request);
        log.info("Update amcConfiguration request validated");

        /*
         * Search for amcConfiguration based on amcConfiguration IDs provided in the request
         */
        List<AmcConfiguration> amcConfigurationsFromDB = searchAmcConfiguration(
                getSearchAmcConfigurationRequest(request.getAmcConfigurations(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getAmcConfigurations().get(0).getTenantId(), false, null);
        log.info("Fetched amcConfiguration for update request");

        /*
         * Validate the update amcConfiguration request against the amcConfigurations fetched from the database
         */
        amcConfigurationValidator.validateUpdateAgainstDB(request.getAmcConfigurations(), amcConfigurationsFromDB);

        /*
         * Process each amcConfiguration in the update request
         */
        for (AmcConfiguration amcConfiguration : request.getAmcConfigurations()) {
            processamcConfigurationUpdate(request, amcConfiguration, amcConfigurationsFromDB);
        }

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
        amcConfigurationValidator.validateSearchAmcConfigurationRequest(request, limit, offset, tenantId);
        List<AmcConfiguration> amcConfigurationList = amcConfigurationRepository.getAmcConfiguration(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        enrichAmcConfiguration(request.getRequestInfo(), amcConfigurationList);
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

        if (amcConfigurationFromDB != null) {
            /*
             * Merge additional details of the amcConfiguration from the request and amcConfiguration from DB
             */
            amcConfigurationServiceUtil.mergeAdditionalDetails(amcConfiguration, amcConfigurationFromDB);

            handleUpdateamcConfiguration(request, amcConfiguration, amcConfigurationFromDB);
        }
    }

    private void handleUpdateamcConfiguration(AmcConfigurationRequest request, AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDB) {
        /*
         * Save original values of start date, end date, and additional details
         */
        Long originalStartDate = amcConfigurationFromDB.getConfigurationStartDate();
        Long originalEndDate = amcConfigurationFromDB.getConfigurationEndDate();
        List<Map<String, Object>> originalAssetTypes = amcConfigurationFromDB.getAssetTypes();
        int originalDurationMonths = amcConfigurationFromDB.getDurationMonths();
        int originalVisitFrequencyMonths = amcConfigurationFromDB.getVisitFrequencyMonths();
        String originalVendorId = amcConfigurationFromDB.getVendorId();
        AuditDetails originalAuditDetails = amcConfigurationFromDB.getAuditDetails();


        /*
         * Update the amcConfiguration with new start date, end date, and additional details
         */
        amcConfigurationFromDB.setConfigurationStartDate(amcConfiguration.getConfigurationStartDate());
        amcConfigurationFromDB.setConfigurationEndDate(amcConfiguration.getConfigurationEndDate());
        amcConfigurationFromDB.setAssetTypes(amcConfiguration.getAssetTypes());
        amcConfigurationFromDB.setDurationMonths(amcConfiguration.getDurationMonths());
        amcConfigurationFromDB.setVisitFrequencyMonths(amcConfiguration.getVisitFrequencyMonths());
        amcConfigurationFromDB.setVendorId(amcConfigurationFromDB.getId());
        amcConfigurationFromDB.setAuditDetails(amcConfiguration.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(amcConfigurationFromDB, amcConfiguration)) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Can only update amc configs dates, asset types, vendor and additional details"
            );
        }

        /*
         * Restore original values of start date, end date, and additional details
         */
        amcConfigurationFromDB.setConfigurationStartDate(originalStartDate);
        amcConfigurationFromDB.setConfigurationEndDate(originalEndDate);
        amcConfigurationFromDB.setAssetTypes(originalAssetTypes);
        amcConfigurationFromDB.setDurationMonths(originalDurationMonths);
        amcConfigurationFromDB.setVisitFrequencyMonths(originalVisitFrequencyMonths);
        amcConfigurationFromDB.setVendorId(originalVendorId);
        amcConfigurationFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the amcConfiguration
         */
        amcConfigurationEnrichment.enrichAmcConfigurationRequestOnUpdate(amcConfiguration, amcConfigurationFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading amcConfiguration dates and push the update to the message broker
         */
        producer.push(amcServiceConfiguration.getUpdateAmcConfigurationTopic(), request);
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
                log.warn("State cannot be changed during cascading update");
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

    private void enrichAmcConfiguration(RequestInfo requestInfo, List<AmcConfiguration> amcConfigurationList){
        for (AmcConfiguration amcConfiguration : amcConfigurationList){
            // Enrich amc configuration with vendor details
            Organisation organisation = getVendorById(requestInfo, amcConfiguration.getVendorId());
            if(organisation != null){
                amcConfiguration.setVendor(organisation);
            }

            // Enrich amc configuration with total visit
            Integer totalVisit = amcConfiguration.getDurationMonths()/amcConfiguration.getVisitFrequencyMonths();
            amcConfiguration.setTotalVisits(totalVisit);

            // Enrich amc configuration with completed visit
            Integer count = getCompletedVisits(amcConfiguration);
            amcConfiguration.setCompletedVisits(count);

            // Enrich amc configuration with linked assets amc
            List<AssetAmc> assetAmcs = getLinkedAssets(amcConfiguration);
            amcConfiguration.setAssetsAmc(assetAmcs);
        }
    }

    private Integer getCompletedVisits(AmcConfiguration amcConfiguration) {
        ScheduledVisitSearchCriteria searchCriteria = ScheduledVisitSearchCriteria.builder().facilityIds(List.of(amcConfiguration.getFacilityId())).statuses(List.of("APPROVED")).build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder().searchCriteria(searchCriteria).build();
        Integer count = scheduledVisitRepository.getScheduledVisitCount(searchRequest, amcConfiguration.getTenantId(), null, null);
        return count;
    }

    private List<AssetAmc> getLinkedAssets(AmcConfiguration amcConfiguration) {
        AssetAmcSearchCriteria searchCriteria = AssetAmcSearchCriteria.builder().amcConfigurationIds(List.of(amcConfiguration.getId())).build();
        AssetAmcSearchRequest searchRequest = AssetAmcSearchRequest.builder().searchCriteria(searchCriteria).build();
        List<AssetAmc> assetAmcs = assetAmcRepository.getAssetAmc(searchRequest, 1000, 0, amcConfiguration.getTenantId(), null, null);
        return assetAmcs;
    }

    public Organisation getVendorById(RequestInfo requestInfo, String vendorId) {
        OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().id(vendorId).build();
        OrgSearchRequest searchRequest = OrgSearchRequest.builder().requestInfo(requestInfo).searchCriteria(searchCriteria).build();
        String url = amcServiceConfiguration.getVendorHost() + amcServiceConfiguration.getVendorSearchUrl()+ "?tenantId=in&limit=1";

        Object response = requestRepository.fetchResult(new StringBuilder(url), searchRequest);

        OrgServiceResponse orgServiceResponse = mapper.convertValue(response, OrgServiceResponse.class);
        if (orgServiceResponse != null && !orgServiceResponse.getOrganisations().isEmpty()) {
            return orgServiceResponse.getOrganisations().get(0);
        }
        return null;
    }



}
