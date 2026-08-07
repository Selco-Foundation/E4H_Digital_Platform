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

    // Max project-staff rows per project-service bulk call (was one HTTP call per AMC configuration).
    private static final int PROJECT_STAFF_LINK_CHUNK_SIZE = 500;

    private final AmcConfigurationValidator amcConfigurationValidator;
    private final AmcConfigurationRepository amcConfigurationRepository;
    private final Producer producer;
    private final ScheduledVisitRepository scheduledVisitRepository;
    private final AmcConfigurationEnrichment amcConfigurationEnrichment;

    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;
    private final ServiceRequestRepository requestRepository;
    private final AssetAmcRepository assetAmcRepository;
    private final AmcAnalyticsService amcAnalyticsService;
    private final AmcVisitRegenerationService amcVisitRegenerationService;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public AmcConfigurationService(
            AmcConfigurationRepository amcConfigurationRepository, AmcConfigurationValidator amcConfigurationValidator, ScheduledVisitRepository scheduledVisitRepository, AmcConfigurationEnrichment amcConfigurationEnrichment, AMCServiceConfiguration amcConfigurationConfiguration,
            Producer producer, AmcConfigurationServiceUtil amcConfigurationServiceUtil, ServiceRequestRepository requestRepository, AssetAmcRepository assetAmcRepository,
            AmcAnalyticsService amcAnalyticsService, AmcVisitRegenerationService amcVisitRegenerationService) {
            this.amcConfigurationValidator = amcConfigurationValidator;
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.producer = producer;
            this.amcServiceConfiguration = amcConfigurationConfiguration;
            this.amcConfigurationRepository = amcConfigurationRepository;
            this.amcConfigurationEnrichment = amcConfigurationEnrichment;
            this.amcConfigurationServiceUtil = amcConfigurationServiceUtil;
        this.requestRepository = requestRepository;
        this.assetAmcRepository = assetAmcRepository;
        this.amcAnalyticsService = amcAnalyticsService;
        this.amcVisitRegenerationService = amcVisitRegenerationService;
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

            log.trace("Enriching AMC configuration on create for projectId: {}, facilityId: {}",
                    amcConfiguration.getProjectId(), amcConfiguration.getFacilityId());
            log.info("AMC configuration enriched with project ID: {}, facility ID: {}",
                    amcConfiguration.getProjectId(), amcConfiguration.getFacilityId());
            log.debug("Enriched AMC configuration details - duration: {} months, visitFrequency: {} months",
                    amcConfiguration.getDurationMonths(), amcConfiguration.getVisitFrequencyMonths());
        }

        // Bulk-link project staff: same semantics as per-config createProjectStaff; dedupe (tenant, project, user).
        List<ProjectStaff> distinctStaffToLink = collectDistinctProjectStaffForCreate(request.getAmcConfigurations());
        if (!distinctStaffToLink.isEmpty()) {
            log.info(
                    "Linking {} distinct project staff row(s) for {} AMC configuration(s) (chunk size {})",
                    distinctStaffToLink.size(),
                    request.getAmcConfigurations().size(),
                    PROJECT_STAFF_LINK_CHUNK_SIZE);
            for (int i = 0; i < distinctStaffToLink.size(); i += PROJECT_STAFF_LINK_CHUNK_SIZE) {
                int end = Math.min(i + PROJECT_STAFF_LINK_CHUNK_SIZE, distinctStaffToLink.size());
                List<ProjectStaff> chunk = new ArrayList<>(distinctStaffToLink.subList(i, end));
                amcConfigurationServiceUtil.createProjectStaff(request.getRequestInfo(), chunk);
                log.debug("Project staff bulk link: sent {} row(s)", chunk.size());
            }
        }

        log.info("Pushing {} AMC configuration(s) to kafka", request.getAmcConfigurations().size());
        producer.push(amcServiceConfiguration.getSaveAmcConfigurationTopic(), request);

        // Creating a configuration is the AMC scheduling action - best-effort, never breaks create.
        amcAnalyticsService.publishConfigurationCreateEvents(request);
        return request;
    }

    // One ProjectStaff row per distinct (tenantId, projectId, userId) across all configs in the request.
    private List<ProjectStaff> collectDistinctProjectStaffForCreate(List<AmcConfiguration> configurations) {
        Map<String, ProjectStaff> byKey = new LinkedHashMap<>();
        for (AmcConfiguration amcConfiguration : configurations) {
            if (amcConfiguration.getAssignments() == null || amcConfiguration.getAssignments().isEmpty()) {
                continue;
            }
            String tenantId = amcConfiguration.getTenantId();
            String projectId = amcConfiguration.getProjectId();
            if (tenantId == null || projectId == null) {
                continue;
            }
            for (AmcConfigurationAssignment assignment : amcConfiguration.getAssignments()) {
                String userId = assignment.getAssignedUser();
                if (userId == null) {
                    continue;
                }
                String key = tenantId + "|" + projectId + "|" + userId;
                byKey.putIfAbsent(key, ProjectStaff.builder()
                        .tenantId(tenantId)
                        .projectId(projectId)
                        .userId(userId)
                        .build());
            }
        }
        return new ArrayList<>(byKey.values());
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
         * The contract end date is a function of the start date and the duration, so a caller that
         * changes durationMonths must not be the one deciding the new end date. Recompute it here, in
         * calendar months, before validation - the ids we touched are passed on so the validator knows
         * the decrease is server-derived rather than an arbitrary client shortening.
         */
        Set<String> durationDrivenEndDateIds = applyDurationDrivenEndDates(request.getAmcConfigurations(), amcConfigurationsFromDB);

        /*
         * Validate the update amcConfiguration request against the amcConfigurations fetched from the database
         */
        amcConfigurationValidator.validateUpdateAgainstDB(request.getAmcConfigurations(), amcConfigurationsFromDB, durationDrivenEndDateIds);

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

    /**
     * Recomputes configurationEndDate as startDate + durationMonths (calendar months) for every
     * configuration whose duration changed, and returns their ids.
     *
     * <p>Shortening a contract below what has already been serviced would strand APPROVED visits
     * outside the window, so the derived end date must stay after the last visit that actually took
     * place.
     */
    private Set<String> applyDurationDrivenEndDates(List<AmcConfiguration> amcConfigurationsFromRequest, List<AmcConfiguration> amcConfigurationsFromDB) {
        Set<String> touchedIds = new HashSet<>();
        for (AmcConfiguration amcConfiguration : amcConfigurationsFromRequest) {
            AmcConfiguration amcConfigurationFromDB = findAmcConfigurationById(String.valueOf(amcConfiguration.getId()), amcConfigurationsFromDB);
            if (amcConfigurationFromDB == null
                    || amcConfiguration.getDurationMonths() == null
                    || Objects.equals(amcConfiguration.getDurationMonths(), amcConfigurationFromDB.getDurationMonths())) {
                continue;
            }

            Long startDate = amcConfiguration.getConfigurationStartDate() != null
                    ? amcConfiguration.getConfigurationStartDate()
                    : amcConfigurationFromDB.getConfigurationStartDate();
            if (startDate == null || startDate <= 0) {
                continue;
            }

            long derivedEndDate = amcConfigurationServiceUtil.addMonths(startDate, amcConfiguration.getDurationMonths());
            Long lastServicedVisitDate = getLastServicedVisitDate(amcConfigurationFromDB);
            if (lastServicedVisitDate != null && derivedEndDate < lastServicedVisitDate) {
                throw new CustomException("INVALID_AMC_CONFIGURATION_MODIFY",
                        "The AMC duration cannot be reduced to " + amcConfiguration.getDurationMonths()
                                + " months: visits have already been carried out beyond the resulting end date.");
            }

            log.info("Recomputed configurationEndDate for configurationId: {} from durationMonths {} -> {} (endDate {} -> {})",
                    amcConfiguration.getId(), amcConfigurationFromDB.getDurationMonths(), amcConfiguration.getDurationMonths(),
                    amcConfigurationFromDB.getConfigurationEndDate(), derivedEndDate);
            amcConfiguration.setConfigurationEndDate(derivedEndDate);
            touchedIds.add(amcConfiguration.getId());
        }
        return touchedIds;
    }

    /* Scheduled date of the latest APPROVED visit of a configuration, or null when none exists. */
    private Long getLastServicedVisitDate(AmcConfiguration amcConfiguration) {
        ScheduledVisitSearchCriteria searchCriteria = ScheduledVisitSearchCriteria.builder()
                .tenantId(amcConfiguration.getTenantId())
                .amcConfigurationIds(List.of(amcConfiguration.getId()))
                .statuses(List.of("APPROVED"))
                .build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                .searchCriteria(searchCriteria)
                .build();
        List<ScheduledVisit> visits = scheduledVisitRepository.getScheduledVisit(
                searchRequest, amcServiceConfiguration.getMaxLimit(), 0, amcConfiguration.getTenantId(), null, null);
        if (visits == null || visits.isEmpty()) {
            return null;
        }
        return visits.stream()
                .map(ScheduledVisit::getScheduledDate)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
    }

    /**
     * Hard-deletes AMC configurations (and everything hanging off them: scheduled visits, asset links
     * and assignments - see the delete-amc-configuration mapping in amc-persister.yml).
     * Used by the ingestion service to reconcile a bulk upload: a facility that is inside the selected
     * districts/blocks but no longer present in the uploaded file loses its configuration.
     */
    public AmcConfigurationRequest deleteAmcConfiguration(AmcConfigurationRequest request) {
        log.trace("Entering deleteAmcConfiguration method");
        amcConfigurationValidator.validateDeleteAmcConfigurationRequest(request);

        List<AmcConfiguration> amcConfigurationsFromDB = searchAmcConfiguration(
                getSearchAmcConfigurationRequest(request.getAmcConfigurations(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getAmcConfigurations().get(0).getTenantId(), false, null);
        amcConfigurationValidator.validateDeleteAgainstDB(request.getAmcConfigurations(), amcConfigurationsFromDB);

        // Send back the full DB rows rather than the (possibly minimal) request payload, so callers get
        // the deleted configurations in the response and the audit trail records who removed what.
        for (AmcConfiguration amcConfigurationFromDB : amcConfigurationsFromDB) {
            amcConfigurationEnrichment.enrichAmcConfigurationRequestOnUpdate(
                    amcConfigurationFromDB, amcConfigurationFromDB, request.getRequestInfo());
        }
        request.setAmcConfigurations(amcConfigurationsFromDB);

        log.info("Pushing delete for {} AMC configuration(s) to kafka", amcConfigurationsFromDB.size());
        producer.push(amcServiceConfiguration.getDeleteAmcConfigurationTopic(), request);
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
        enrichAmcConfiguration(request.getRequestInfo(), amcConfigurationList);
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
        Map<String, Object> originalGeographyDetails = amcConfigurationFromDB.getGeographyDetails();
        AuditDetails originalAuditDetails = amcConfigurationFromDB.getAuditDetails();

        /*
         * Geography details may only change districts/blocks; the state is read-only once set
         */
        if (!isValidGeographyDetailsUpdate(originalGeographyDetails, amcConfiguration.getGeographyDetails())) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Cannot change state in geographyDetails during update"
            );
        }

        /*
         * Update the amcConfiguration with new start date, end date, and additional details
         */
        amcConfigurationFromDB.setConfigurationStartDate(amcConfiguration.getConfigurationStartDate());
        amcConfigurationFromDB.setConfigurationEndDate(amcConfiguration.getConfigurationEndDate());
        amcConfigurationFromDB.setAssetTypes(amcConfiguration.getAssetTypes());
        amcConfigurationFromDB.setDurationMonths(amcConfiguration.getDurationMonths());
        amcConfigurationFromDB.setVisitFrequencyMonths(amcConfiguration.getVisitFrequencyMonths());
        amcConfigurationFromDB.setVendorId(amcConfiguration.getVendorId());
        amcConfigurationFromDB.setGeographyDetails(amcConfiguration.getGeographyDetails());
        amcConfigurationFromDB.setAuditDetails(amcConfiguration.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(amcConfigurationFromDB, amcConfiguration)) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Can only update amc configs dates, asset types, vendor, geography details and additional details"
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
        amcConfigurationFromDB.setGeographyDetails(originalGeographyDetails);
        amcConfigurationFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the amcConfiguration
         */
        amcConfigurationEnrichment.enrichAmcConfigurationRequestOnUpdate(amcConfiguration, amcConfigurationFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading amcConfiguration dates and push the update to the message broker
         */
        log.debug("Pushing AMC configuration update to kafka for configurationId: {}", amcConfiguration.getId());
        producer.push(amcServiceConfiguration.getUpdateAmcConfigurationTopic(), request);
        log.info("AMC configuration update pushed to kafka for configurationId: {}", amcConfiguration.getId());

        /*
         * A new duration or visit frequency changes the visit plan, not just the configuration row:
         * rebuild the not-yet-due visits so the schedule matches the contract the user just saved.
         * Best-effort - the configuration update itself is already committed and must not be lost if
         * regeneration fails.
         */
        try {
            amcVisitRegenerationService.regenerateIfCadenceChanged(amcConfigurationFromDB, amcConfiguration, request.getRequestInfo());
        } catch (Exception e) {
            log.error("Failed to regenerate scheduled visits for configurationId: {}", amcConfiguration.getId(), e);
        }
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
            Integer count = getCompletedVisits(requestInfo, amcConfiguration);
            amcConfiguration.setCompletedVisits(count);

            // Enrich amc configuration with linked assets amc
            List<AssetAmc> assetAmcs = getLinkedAssets(amcConfiguration);
            amcConfiguration.setAssetsAmc(assetAmcs);
        }
    }

    private Integer getCompletedVisits(RequestInfo requestInfo, AmcConfiguration amcConfiguration) {
        ScheduledVisitSearchCriteria searchCriteria = ScheduledVisitSearchCriteria.builder().facilityIds(List.of(amcConfiguration.getFacilityId())).statuses(List.of("APPROVED")).build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder().RequestInfo(requestInfo).searchCriteria(searchCriteria).build();
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
        OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().tenantId("in").id(vendorId).build();
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
