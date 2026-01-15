package org.egov.activity.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.Asset;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service to call AMC Scheduler Service APIs for installation completion side effects
 */
@Service
@Slf4j
public class AmcSchedulerService {

    private static final String REQUEST_INFO = "RequestInfo";

    private final ActivityConfiguration activityConfiguration;
    private final ServiceRequestRepository serviceRequest;
    private final ObjectMapper objectMapper;

    @Autowired
    public AmcSchedulerService(
            ActivityConfiguration activityConfiguration,
            ServiceRequestRepository serviceRequest,
            @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.activityConfiguration = activityConfiguration;
        this.serviceRequest = serviceRequest;
        this.objectMapper = objectMapper;
    }

    /**
     * Process installation completion side effects
     *
     * @param projectId Project ID where installation was completed
     * @param facilityId Facility ID where installation was completed
     * @param tenantId Tenant ID
     * @param installedAssets List of installed assets
     * @param installationDate Installation completion date
     * @param requestInfo Request info
     */
    public void processInstallationCompletion(
            String projectId,
            String facilityId,
            String tenantId,
            List<Asset> installedAssets,
            Long installationDate,
            RequestInfo requestInfo) {

        log.info("==================== Processing Installation Completion Side Effects ====================");
        log.info("Project: {}, Facility: {}, Assets Count: {}, TenantId: {}", projectId, facilityId, installedAssets.size(), tenantId);

        try {
            // Step 1: Fetch AMC configurations for the project
            List<Map<String, Object>> amcConfigurations = fetchAmcConfigurations(
                    projectId, facilityId, tenantId, requestInfo);

            if (amcConfigurations == null || amcConfigurations.isEmpty()) {
                log.info("No AMC configurations found for project: {}. Skipping asset AMC creation.", projectId);
                return;
            }

            log.info("Found {} AMC configurations for the project", amcConfigurations.size());

            // Step 2: Create Asset AMCs for matching assets
            Map<String, List<String>> configToAssetMap = createAssetAmcs(
                    amcConfigurations, installedAssets, tenantId, installationDate, requestInfo);

            if (configToAssetMap.isEmpty()) {
                log.info("No Asset AMCs were created. Skipping visit generation.");
                return;
            }

            // Step 3: Generate scheduled visits for each configuration
            generateScheduledVisits(configToAssetMap, tenantId, installationDate, requestInfo);

            log.info("==================== Installation Completion Side Effects Complete ====================");

        } catch (Exception e) {
            log.error("Error processing installation completion side effects: {}", e.getMessage(), e);
            // Don't throw exception - log and continue to avoid blocking workflow
        }
    }

    /**
     * Fetch AMC configurations for the project and facility
     */
    private List<Map<String, Object>> fetchAmcConfigurations(
            String projectId, String facilityId, String tenantId, RequestInfo requestInfo) {

        try {
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put(REQUEST_INFO, requestInfo);

            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("tenantId", tenantId);
            searchCriteria.put("projectIds", List.of(projectId));
            searchCriteria.put("facilityIds", List.of(facilityId));
            searchCriteria.put("statuses", List.of("ACTIVE"));
            searchRequest.put("searchCriteria", searchCriteria);

            StringBuilder url = new StringBuilder(activityConfiguration.getAmcSchedulerHost())
                    .append(activityConfiguration.getAmcConfigurationSearchUrl())
                    .append("?tenantId=").append(tenantId)
                    .append("&limit=").append(activityConfiguration.getMaxLimit())
                    .append("&offset=").append(activityConfiguration.getDefaultOffset());

            Object response = serviceRequest.fetchResult(url, searchRequest);

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                Object amcConfigurations = responseMap.get("AmcConfigurations");
                if (amcConfigurations instanceof List) {
                    return (List<Map<String, Object>>) amcConfigurations;
                }
            }

            return new ArrayList<>();

        } catch (ServiceCallException e) {
            log.error("Service call error fetching AMC configurations: {}", e.getMessage(), e);
            return new ArrayList<>();
        } catch (RuntimeException e) {
            log.error("Error fetching AMC configurations: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Create Asset AMCs for installed assets matching AMC configurations
     */
    private Map<String, List<String>> createAssetAmcs(
            List<Map<String, Object>> amcConfigurations,
            List<Asset> installedAssets,
            String tenantId,
            Long installationDate,
            RequestInfo requestInfo) {

        Map<String, List<String>> configToAssetMap = new HashMap<>();
        List<Map<String, Object>> assetAmcsToCreate = new ArrayList<>();

        for (Map<String, Object> config : amcConfigurations) {
            String configId = (String) config.get("id");
            List<String> configAssetTypes = extractAssetTypes(config.get("assetTypes"));

            if (configAssetTypes.isEmpty()) {
                log.warn("Configuration {} has no asset types defined. Skipping.", configId);
                continue;
            }

            List<Asset> matchingAssets = findMatchingAssets(installedAssets, configAssetTypes, configId);
            if (matchingAssets.isEmpty()) {
                log.info("No matching assets found for configuration: {}", configId);
                continue;
            }

            log.info("Found {} matching assets for configuration: {}", matchingAssets.size(), configId);
            List<String> assetIds = createAssetAmcRecords(matchingAssets, configId, tenantId, 
                    installationDate, assetAmcsToCreate);
            configToAssetMap.put(configId, assetIds);
        }

        bulkCreateAssetAmcs(assetAmcsToCreate, requestInfo);
        return configToAssetMap;
    }

    /**
     * Find assets that match the configuration's asset types
     */
    private List<Asset> findMatchingAssets(List<Asset> installedAssets, 
            List<String> configAssetTypes, String configId) {
        List<Asset> matchingAssets = new ArrayList<>();
        for (Asset asset : installedAssets) {
            String assetTypeId = asset.getAssetTypeID();
            if (configAssetTypes.contains(assetTypeId)) {
                matchingAssets.add(asset);
                log.debug("Asset {} (type: {}) matches configuration {}", asset.getAssetId(), assetTypeId, configId);
            } else {
                log.debug("Asset {} (type: {}) does not match configuration {} (required types: {})", 
                        asset.getAssetId(), assetTypeId, configId, configAssetTypes);
            }
        }
        return matchingAssets;
    }

    /**
     * Create Asset AMC records for matching assets
     */
    private List<String> createAssetAmcRecords(List<Asset> matchingAssets, String configId, 
            String tenantId, Long installationDate, List<Map<String, Object>> assetAmcsToCreate) {
        List<String> assetIds = new ArrayList<>();
        for (Asset asset : matchingAssets) {
            Map<String, Object> assetAmc = new HashMap<>();
            assetAmc.put("tenantId", tenantId);
            assetAmc.put("assetId", asset.getAssetId());
            assetAmc.put("amcConfigurationId", configId);
            assetAmc.put("amcStartDate", installationDate);
            assetAmc.put("status", "ACTIVE");
            assetAmc.put("isLegacyAsset", false);

            assetAmcsToCreate.add(assetAmc);
            assetIds.add(asset.getAssetId());
        }
        return assetIds;
    }

    /**
     * Bulk create Asset AMCs via API
     */
    private void bulkCreateAssetAmcs(List<Map<String, Object>> assetAmcsToCreate, RequestInfo requestInfo) {
        if (assetAmcsToCreate.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put(REQUEST_INFO, requestInfo);
            createRequest.put("AssetAmcs", assetAmcsToCreate);

            StringBuilder url = new StringBuilder(activityConfiguration.getAmcSchedulerHost())
                    .append(activityConfiguration.getAmcAssetCreateUrl());

            serviceRequest.fetchResult(url, createRequest);
            log.info("Successfully created {} Asset AMCs", assetAmcsToCreate.size());

        } catch (ServiceCallException e) {
            log.error("Service call error creating Asset AMCs: {}", e.getMessage(), e);
            throw new CustomException("ASSET_AMC_CREATION_ERROR",
                    "Failed to create Asset AMCs: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error creating Asset AMCs: {}", e.getMessage(), e);
            throw new CustomException("ASSET_AMC_CREATION_ERROR",
                    "Failed to create Asset AMCs: " + e.getMessage());
        }
    }

    /**
     * Generate scheduled visits for all configurations that have assets assigned
     */
    private void generateScheduledVisits(
            Map<String, List<String>> configToAssetMap,
            String tenantId,
            Long installationDate,
            RequestInfo requestInfo) {

        int totalVisitsGenerated = 0;

        for (Map.Entry<String, List<String>> entry : configToAssetMap.entrySet()) {
            String configId = entry.getKey();

            try {
                log.info("Generating scheduled visits for configuration: {} with {} assets",
                        configId, entry.getValue().size());

                Map<String, Object> visitRequest = new HashMap<>();
                visitRequest.put(REQUEST_INFO, requestInfo);
                visitRequest.put("configurationId", configId);
                visitRequest.put("generationStartDate", installationDate);

                StringBuilder url = new StringBuilder(activityConfiguration.getAmcSchedulerHost())
                        .append(activityConfiguration.getAmcVisitGenerateUrl());

                Object response = serviceRequest.fetchResult(url, visitRequest);

                if (response instanceof Map) {
                    Map<String, Object> responseMap = (Map<String, Object>) response;
                    Object scheduledVisits = responseMap.get("scheduledVisits");
                    if (scheduledVisits instanceof List) {
                        int visitCount = ((List<?>) scheduledVisits).size();
                        totalVisitsGenerated += visitCount;
                        log.info("Generated {} scheduled visits in DRAFT state for configuration: {}",
                                visitCount, configId);
                    }
                }

            } catch (Exception e) {
                log.error("Error generating visits for configuration {}: {}",
                        configId, e.getMessage(), e);
                // Continue with other configurations even if one fails
            }
        }

        log.info("Total scheduled visits generated across all configurations: {}", totalVisitsGenerated);
    }

    /**
     * Extract asset type IDs from configuration asset types
     */
    private List<String> extractAssetTypes(Object assetTypesObj) {
        List<String> assetTypeIds = new ArrayList<>();

        if (assetTypesObj == null) {
            return assetTypeIds;
        }

        try {
            List<Map<String, Object>> assetTypes;
            if (assetTypesObj instanceof List) {
                assetTypes = (List<Map<String, Object>>) assetTypesObj;
            } else {
                assetTypes = objectMapper.convertValue(assetTypesObj, new TypeReference<List<Map<String, Object>>>() {});
            }

            for (Map<String, Object> assetType : assetTypes) {
                Object assetTypeId = assetType.get("code");
                if (assetTypeId != null) {
                    assetTypeIds.add(assetTypeId.toString());
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Error extracting asset types: {}", e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error extracting asset types: {}", e.getMessage(), e);
        }

        return assetTypeIds;
    }
}

