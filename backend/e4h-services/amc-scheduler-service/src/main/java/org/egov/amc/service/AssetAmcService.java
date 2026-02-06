package org.egov.amc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AssetAmcRepository;
import org.egov.amc.service.enrichment.AssetAmcEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.validator.AssetAmcValidator;
import org.egov.amc.web.models.AssetAmc;
import org.egov.amc.web.models.AssetAmcRequest;
import org.egov.amc.web.models.AssetAmcSearchCriteria;
import org.egov.amc.web.models.AssetAmcSearchRequest;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class AssetAmcService {

    private final AssetAmcValidator assetAmcValidator;
    private final AssetAmcRepository assetAmcRepository;
    private final Producer producer;
    private final AssetAmcEnrichment assetAmcEnrichment;

    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public AssetAmcService(
            AssetAmcRepository assetAmcRepository, AssetAmcValidator assetAmcValidator, AssetAmcEnrichment assetAmcEnrichment, AMCServiceConfiguration assetAmcConfiguration,
            Producer producer, AmcConfigurationServiceUtil assetAmcServiceUtil) {
            this.assetAmcValidator = assetAmcValidator;
            this.producer = producer;
            this.amcServiceConfiguration = assetAmcConfiguration;
            this.assetAmcRepository = assetAmcRepository;
            this.assetAmcEnrichment = assetAmcEnrichment;
            this.amcConfigurationServiceUtil = assetAmcServiceUtil;
    }

    public AssetAmcRequest createAssetAmc(AssetAmcRequest request) {
        log.trace("Entering createAssetAmc method");
        log.info("Creating {} asset AMC record(s)", request.getAssetAmcs().size());
        assetAmcValidator.validateCreateAssetAmcRequest(request);
        for (AssetAmc amcConfiguration : request.getAssetAmcs()) {
            assetAmcEnrichment.enrichAssetAmcOnCreate(amcConfiguration, request.getRequestInfo());
            log.trace("Enriching asset AMC on create for assetId: {}, amcConfigurationId: {}", 
                    amcConfiguration.getAssetId(), amcConfiguration.getAmcConfigurationId());
            log.info("Asset AMC enriched with configuration ID: {}", amcConfiguration.getAmcConfigurationId());
            log.debug("Enriched asset AMC details - assetId: {}, startDate: {}, endDate: {}", 
                    amcConfiguration.getAssetId(), amcConfiguration.getAmcStartDate(), amcConfiguration.getAmcEndDate());
        }
        log.info("Pushing {} asset AMC record(s) to kafka", request.getAssetAmcs().size());
        producer.push(amcServiceConfiguration.getSaveAssetAmcTopic(), request);
        return request;
    }

    public AssetAmcRequest updateAssetAmc(AssetAmcRequest request) {
        log.trace("Entering updateAssetAmc method");
        /*
         * Validate the update assetAmc request
         */
        assetAmcValidator.validateUpdateAssetAmcRequest(request);
        log.info("Update asset AMC request validated, record count: {}", request.getAssetAmcs().size());

        /*
         * Search for asset_amc based on asset_amc IDs provided in the request
         */
        List<AssetAmc> amcConfigurationsFromDB = searchAssetAmc(
                getSearchAssetAmcRequest(request.getAssetAmcs(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getAssetAmcs().get(0).getTenantId(), false, null);
        log.debug("Fetched {} asset AMC record(s) from database for update request", amcConfigurationsFromDB.size());
        log.info("Fetched asset AMC records for update request");

        /*
         * Validate the update asset_amc request against the asset_amcs fetched from the database
         */
        assetAmcValidator.validateUpdateAgainstDB(request.getAssetAmcs(), amcConfigurationsFromDB);

        /*
         * Process each assetAmc in the update request
         */
        log.debug("Processing {} asset AMC record(s) for update", request.getAssetAmcs().size());
        for (AssetAmc amcConfiguration : request.getAssetAmcs()) {
            processAssetAmcUpdate(request, amcConfiguration, amcConfigurationsFromDB);
        }
        log.info("Successfully processed update for {} asset AMC record(s)", request.getAssetAmcs().size());

        return request;
    }

    public Integer countAllAssetAmcs(AssetAmcSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return assetAmcRepository.getAssetAmcCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    /* Construct AssetAmc Request object for search which contains asset_amc id and tenantId */
    private AssetAmcSearchRequest getSearchAssetAmcRequest(List<AssetAmc> amcConfigurations, RequestInfo requestInfo) {
        List<String> assetAmcIds = amcConfigurations.stream().map(AssetAmc::getId).toList();
        AssetAmcSearchCriteria criteria = AssetAmcSearchCriteria.builder().ids(assetAmcIds).tenantId(amcConfigurations.get(0).getTenantId()).build();
        return AssetAmcSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
    }

    public List<AssetAmc> searchAssetAmc(AssetAmcSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        log.trace("Entering searchAssetAmc method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        assetAmcValidator.validateSearchAssetAmcRequest(request, limit, offset, tenantId);
        List<AssetAmc> amcConfigurationList = assetAmcRepository.getAssetAmc(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Found {} asset AMC record(s) matching search criteria", amcConfigurationList.size());
        log.info("Asset AMC search completed");
        return amcConfigurationList;
    }

    private void processAssetAmcUpdate(AssetAmcRequest request, AssetAmc amcConfiguration, List<AssetAmc> amcConfigurationsFromDB) {
        /*
         * Convert asset_amc ID to string for comparison
         */
        String assetAmcId = String.valueOf(amcConfiguration.getId());

        /*
         * Find the assetAmc from the database that matches the current assetAmc ID
         */
        AssetAmc amcConfigurationFromDB = findAssetAmcById(assetAmcId, amcConfigurationsFromDB);

        if (amcConfigurationFromDB != null) {
            /*
             * Merge additional details of the assetAmc from the request and assetAmc from DB
             */
            amcConfigurationServiceUtil.mergeAssetAmcAdditionalDetails(amcConfiguration, amcConfigurationFromDB);

            handleUpdateAssetAmc(request, amcConfiguration, amcConfigurationFromDB);
        }
    }

    private void handleUpdateAssetAmc(AssetAmcRequest request, AssetAmc assetAmc, AssetAmc assetAmcFromDB) {
        /*
         * Save original values of start date, end date, and additional details
         */
        Long originalStartDate = assetAmcFromDB.getAmcStartDate();
        Long originalEndDate = assetAmcFromDB.getAmcEndDate();
        AuditDetails originalAuditDetails = assetAmcFromDB.getAuditDetails();


        /*
         * Update the assetAmc with new start date, end date, and additional details
         */
        assetAmcFromDB.setAmcStartDate(assetAmc.getAmcStartDate());
        assetAmcFromDB.setAmcEndDate(assetAmc.getAmcEndDate());
        assetAmcFromDB.setAuditDetails(assetAmc.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(assetAmcFromDB, assetAmc)) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Can only update amc configs dates, asset types, vendor and additional details"
            );
        }

        /*
         * Restore original values of start date, end date, and additional details
         */
        assetAmcFromDB.setAmcStartDate(originalStartDate);
        assetAmcFromDB.setAmcEndDate(originalEndDate);
        assetAmcFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the assetAmc
         */
        assetAmcEnrichment.enrichAssetAmcRequestOnUpdate(assetAmc, assetAmcFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading assetAmc dates and push the update to the message broker
         */
        log.debug("Pushing asset AMC update to kafka for assetId: {}, amcConfigurationId: {}", 
                assetAmc.getAssetId(), assetAmc.getAmcConfigurationId());
        producer.push(amcServiceConfiguration.getUpdateAssetAmcTopic(), request);
        log.info("Asset AMC update pushed to kafka for assetId: {}", assetAmc.getAssetId());
    }

    private boolean isValidCascadingUpdate(AssetAmc assetAmcFromDB, AssetAmc assetAmc) {
        // Check if only allowed fields are being updated
        return Objects.equals(assetAmcFromDB.getId(), assetAmc.getId()) &&
                Objects.equals(assetAmcFromDB.getTenantId(), assetAmc.getTenantId()) &&
                Objects.equals(assetAmcFromDB.getAssetId(), assetAmc.getAssetId()) &&
                Objects.equals(assetAmcFromDB.getAmcConfigurationId(), assetAmc.getAmcConfigurationId());
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

    private AssetAmc findAssetAmcById(String assetAmcId, List<AssetAmc> amcConfigurationsFromDB) {
        /*
         * Find and return the assetAmc with the matching ID from the list of asset_amc fetched from the database
         */
        return amcConfigurationsFromDB.stream()
                .filter(p -> assetAmcId.equals(String.valueOf(p.getId())))
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
