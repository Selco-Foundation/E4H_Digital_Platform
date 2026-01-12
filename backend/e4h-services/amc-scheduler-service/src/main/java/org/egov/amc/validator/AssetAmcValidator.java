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
public class AssetAmcValidator {

    @Autowired
    private final ServiceRequestClient serviceRequestRepository;

    private final ServiceRequestRepository requestRepository;

    public static final String START_DATE_SHOULD_BE_LESS_THAN_END_DATE = "Start date should be less than end date";
    public static final String IS_NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    public static final String TENANT_ID_IS_MANDATORY_IN_ASSETAMC_REQUEST_BODY = "Tenant ID is mandatory in AssetAmc request body";
    public static final String ASSET_TYPES_IS_MANDATORY_IN_AMC_CONFIG_REQUEST_BODY = "Assets Types are mandatory in Amc Configuration request body";
    public static final String DOES_NOT_EXISTS_FOR_THE_ASSETAMC = " that you are trying to update does not exists for the AssetAmc ";
    @Autowired
    MDMSUtils mdmsUtils;

    @Autowired
    AMCServiceConfiguration config;
    private final AmcConfigurationServiceUtil assetAmcServiceUtil;

    private final AmcConfigurationService amcConfigurationService;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public AssetAmcValidator(ServiceRequestClient serviceRequestRepository, ServiceRequestRepository requestRepository, AmcConfigurationServiceUtil assetAmcServiceUtil, AmcConfigurationService amcConfigurationService){
        this.serviceRequestRepository = serviceRequestRepository;
        this.requestRepository = requestRepository;
        this.assetAmcServiceUtil = assetAmcServiceUtil;
        this.amcConfigurationService = amcConfigurationService;
    }

    public void validateCreateAssetAmcRequest(AssetAmcRequest request) {
        log.trace("Entering validateCreateAssetAmcRequest method");
        log.info("Validating create asset AMC request, record count: {}", 
                request.getAssetAmcs() != null ? request.getAssetAmcs().size() : 0);
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if AssetAmc request and mandatory fields are present
        validateAssetAmcRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
        log.debug("Create asset AMC request validation completed successfully");
    }

    private void validateAssetAmcRequest(AssetAmcRequest request) {
        log.trace("Entering validateAssetAmcRequest method");
        Map<String, String> errorMap = new HashMap<>();

        if (request.getAssetAmcs() == null || request.getAssetAmcs().size() == 0) {
            log.error("Asset AMC list is empty. Asset AMCs are mandatory");
            throw new CustomException("ASSETAMC", "Field Plans are mandatory");
        }

        log.debug("Validating {} asset AMC record(s)", request.getAssetAmcs().size());
        for (AssetAmc assetAmc : request.getAssetAmcs()) {
            if (assetAmc == null) {
                log.error("AssetAmc is mandatory in AssetAmcs");
                throw new CustomException("AssetAmc", "AssetAmc is mandatory");
            }

            if (assetAmc.getAssetId() == null || assetAmc.getAssetId().isEmpty()) {
                log.error("Asset ID is mandatory in AssetAmcs");
                throw new CustomException("AssetAmc", "Project ID is mandatory");
            }
            // Get existing assetAmc with projectID from assetAmc service
            log.debug("Validating asset ID: {} for tenantId: {}", assetAmc.getAssetId(), assetAmc.getTenantId());
            Asset existingAsset = getAssetById(request, assetAmc);
            if (existingAsset == null) {
                log.error("Asset ID {} does not exist for tenantId: {}", assetAmc.getAssetId(), assetAmc.getTenantId());
                throw new CustomException("AssetAmc", "Asset ID do not exist");
            }

            if (assetAmc.getAmcConfigurationId() == null || assetAmc.getAmcConfigurationId().isEmpty()) {
                log.error("Amc Configuration ID is mandatory in Amc Configuration");
                throw new CustomException("Asset Amc", "Amc Configuration ID is mandatory");
            }

            // Get existing amcConfiguration from amcConfiguration service
            log.debug("Validating AMC configuration ID: {} for tenantId: {}", assetAmc.getAmcConfigurationId(), assetAmc.getTenantId());
            String amcConfigurationIds = assetAmc.getAmcConfigurationId();
            AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder().ids(new ArrayList<>(List.of(amcConfigurationIds))).tenantId(assetAmc.getTenantId()).build();
            AmcConfigurationSearchRequest searchRequest = AmcConfigurationSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
            List<AmcConfiguration> amcConfigurationList = amcConfigurationService.searchAmcConfiguration(searchRequest, 10, 0, assetAmc.getTenantId(), false, null );
            if (amcConfigurationList ==null || amcConfigurationList.isEmpty()){
                log.error("AMC Configuration ID {} does not exist for tenantId: {}", assetAmc.getAmcConfigurationId(), assetAmc.getTenantId());
                throw new CustomException("Asset Amc", "AMC Configuration do not exist");
            }

            if (assetAmc.getAmcStartDate() == null || assetAmc.getAmcStartDate() == 0) {
                log.error("Start Date is mandatory in Asset AMC");
                throw new CustomException("Asset AMC", "Start Date Asset AMC is mandatory");
            }

            if (StringUtils.isBlank(assetAmc.getTenantId())) {
                log.error(TENANT_ID_IS_MANDATORY_IN_ASSETAMC_REQUEST_BODY);
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if ((assetAmc.getAmcStartDate() != null && assetAmc.getAmcEndDate() != null && assetAmc.getAmcEndDate() != 0) && (assetAmc.getAmcStartDate().compareTo(assetAmc.getAmcEndDate()) > 0)) {
                log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
                errorMap.put("INVALID_DATE_ERROR", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            }
            if (assetAmc.getAmcStartDate() != null && assetAmc.getAmcEndDate() != null && assetAmc.getAmcEndDate() != 0
                    && assetAmc.getAmcEndDate().compareTo(Instant.ofEpochMilli(assetAmc.getAmcStartDate()).plus(Duration.ofDays(1)).toEpochMilli()) < 0) {
                log.error("Start date and end date difference should at least be 1 day.");
                errorMap.put("INVALID_DATE", "Start date and end date difference should at least be 1 day.");
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
        log.trace("Entering validateRequestInfo method");
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
        log.debug("RequestInfo validation successful");
    }

    public Asset getAssetById(AssetAmcRequest request, AssetAmc assetAmc) {
        log.trace("Entering getAssetById method for assetId: {}, tenantId: {}", assetAmc.getAssetId(), assetAmc.getTenantId());
        String assetId = assetAmc.getAssetId();
        AssetSearchCriteria criteria = AssetSearchCriteria.builder().assetID(assetId).tenantId(assetAmc.getTenantId()).build();
        AssetSearchRequest assetSearchRequest = AssetSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(criteria).build();
        String url = config.getAssetServiceHost() + config.getAssetServiceSearchUrl()+ "?tenantId="+assetAmc.getTenantId()+"&offset=0&limit=100";
        log.debug("Calling asset service to fetch asset at URL: {}", url);
        List<Asset> assetList = requestRepository.fetchResult(new StringBuilder(url), assetSearchRequest, new TypeReference<List<Asset>>() {});
        if(assetList != null && !assetList.isEmpty()){
            log.debug("Asset found for assetId: {}", assetId);
            return assetList.get(0);
        }
        log.debug("Asset not found for assetId: {}", assetId);
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

//    public void isAssetAmcWithinProject(Project project, AssetAmc assetAmc, Map<String, String> errorMap) {
//        if (project == null || assetAmc == null) {
//            log.error("Project or AMC configuration is null");
//            errorMap.put("AMC Configuration", "Project or AssetAmc is null");
//        }
//
//        Long projectStart = project.getStartDate();
//        Long projectEnd   = project.getEndDate();
//        Long amcConfigStart   = assetAmc.getConfigurationStartDate();
//        Long amcConfigEnd     = assetAmc.getConfigurationEndDate();
//
//        if (projectStart == null || projectEnd == null) {
//            log.error("Project dates are not mandatory");
//            errorMap.put("AMC Configuration", "Project dates are not mandatory");
//        }
//        if (amcConfigStart == null || amcConfigEnd == null) {
//            log.error("AMC Configuration dates are not mandatory");
//            errorMap.put("AMC Configuration", "AssetAmc dates are not mandatory");
//        }
//
//        if (amcConfigStart < projectStart) {
//            log.error("The AMC Configuration start date is earlier than the Project start date");
//            errorMap.put("AMC_STARTDATE", "The AMC Configuration start date is earlier than the Project start date");
//        }
//        if (amcConfigEnd > projectEnd) {
//            log.error("The AMC Configuration end date is later than the Project end date");
//            errorMap.put("AMC_ENDDATE", "The AMC Configuration end date is later than the Project end date");
//        }
//    }

    /* Validates Update Project request body */
    public void validateUpdateAssetAmcRequest(AssetAmcRequest request) {
        log.trace("Entering validateUpdateAssetAmcRequest method");
        log.info("Validating update asset AMC request, record count: {}", 
                request.getAssetAmcs() != null ? request.getAssetAmcs().size() : 0);
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify Project request and if mandatory fields are present
        validateAssetAmcRequest(request);
        //Verify if project request have multiple tenant Ids
        validateMultipleTenantIds(request);

        //Verify if AssetAmc id is present
        for (AssetAmc assetAmc : request.getAssetAmcs()) {
            if (StringUtils.isBlank(assetAmc.getId())) {
                log.error("Asset AMC ID is mandatory for update");
                throw new CustomException("UPDATE_AMC_Configuration", "Amc Configuration Id is mandatory");
            }
        }
        log.debug("Update asset AMC request validation completed successfully");


        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


    /* Validates search AssetAmc request body and parameters*/
    public void validateSearchAssetAmcRequest(AssetAmcSearchRequest request, Integer limit, Integer offset, String tenantId) {
        log.trace("Entering validateSearchAssetAmcRequest method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search AssetAmc request parameters are valid
        validateSearchAssetAmcRequestParams(limit, offset, tenantId);
        //Verify if search AssetAmc request is valid
        validateSearchAssetAmc(request.getSearchCriteria(), tenantId);
        log.debug("Search asset AMC request validation completed successfully");
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates if search Project request parameters are valid */
    private void validateSearchAssetAmcRequestParams(Integer limit, Integer offset, String tenantId) {
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
    private void validateSearchAssetAmc(AssetAmcSearchCriteria assetAmc, String tenantId) {
//        checkAssetAmcsIfEmpty(assetAmcs);
        doNullAndEmptyChecks(tenantId, assetAmc);
//
        if ((assetAmc.getStartDateFrom() != null && assetAmc.getStartDateFrom() != null && assetAmc.getStartDateTo() != 0) && (assetAmc.getEndDateTo().compareTo(assetAmc.getEndDateTo()) > 0)) {
            log.error(START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
            throw new CustomException("INVALID_DATE", START_DATE_SHOULD_BE_LESS_THAN_END_DATE);
        }

        if ((assetAmc.getStartDateFrom() == null || assetAmc.getStartDateFrom() == 0) && (assetAmc.getEndDateTo() != null && assetAmc.getEndDateTo() != 0)) {
            log.error("Start date is required if end date is passed");
            throw new CustomException("INVALID_DATE", "Start date is required if end date is passed");
        }
    }

//    private static void checkAssetAmcsIfEmpty(List<AssetAmc> assetAmcs) {
//        if (assetAmcs == null || assetAmcs.size() == 0) {
//            log.error("AssetAmc list is empty. AssetAmcs is mandatory");
//            throw new CustomException("AssetAmc", "AssetAmcs are mandatory");
//        }
//    }

    private static void doNullAndEmptyChecks(String tenantId, AssetAmcSearchCriteria assetAmc) {
        if (assetAmc == null) {
            log.error("assetAmc is mandatory in AssetAmcs");
            throw new CustomException("ASSETAMC", "AssetAmc is mandatory");
        }
        if (StringUtils.isBlank(assetAmc.getTenantId())) {
            log.error(TENANT_ID_IS_MANDATORY_IN_ASSETAMC_REQUEST_BODY);
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((assetAmc.getIds()==null || assetAmc.getIds().isEmpty()) && (assetAmc.getAssetIds()==null || assetAmc.getAssetIds().isEmpty())
                && (assetAmc.getStatuses()==null || assetAmc.getStatuses().isEmpty()) && (assetAmc.getAmcConfigurationIds()==null || assetAmc.getAmcConfigurationIds().isEmpty())
                && (assetAmc.getStartDateFrom() == null || assetAmc.getStartDateFrom() == 0)
                && (assetAmc.getEndDateTo() == null || assetAmc.getEndDateTo() == 0)) {
            log.error("Any one assetAmc search field is required for AssetAmc Search");
            throw new CustomException("ASSET_AMC_SEARCH_FIELDS", "Any one asset_amc search field is required");
        }

        if (!assetAmc.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as AMC CONFIGURATION request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and AMC CONFIGURATION request");
        }
    }

    /* Validates if all AssetAmcs have same tenant Id */
    private void validateMultipleTenantIds(AssetAmcRequest request) {
        List<AssetAmc> assetAmcs = request.getAssetAmcs();
        String firstTenantId = assetAmcs.get(0).getTenantId();
        if (assetAmcs.stream().anyMatch(p -> !p.getTenantId().equals(firstTenantId))) {
            log.error("All assetAmcs in AssetAmc request must have same tenant Id");
            throw new CustomException("MULTIPLE_TENANTS", "All assetAmcs must have same tenant Id. Please create new request for different tentant id");
        }
    }

    /* Validates projects data in update request against projects data fetched from database */
    public void validateUpdateAgainstDB(List<AssetAmc> assetAmcsFromRequest, List<AssetAmc> assetAmcsFromDB) {
        log.trace("Entering validateUpdateAgainstDB method, request count: {}, DB count: {}", 
                assetAmcsFromRequest != null ? assetAmcsFromRequest.size() : 0,
                assetAmcsFromDB != null ? assetAmcsFromDB.size() : 0);
        if (CollectionUtils.isEmpty(assetAmcsFromDB)) {
            log.error("The asset AMC records that you are trying to update do not exist in the system, request count: {}", 
                    assetAmcsFromRequest != null ? assetAmcsFromRequest.size() : 0);
            throw new CustomException("INVALID_ASSETAMC_MODIFY", "The records that you are trying to update does not exists in the system");
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
        for (AssetAmc assetAmc : assetAmcsFromRequest) {
            AssetAmc assetAmcFromDB = assetAmcsFromDB.stream().filter(p -> p.getId().equals(assetAmc.getId())).findFirst().orElse(null);

            if (assetAmcFromDB == null) {
                log.error("Asset AMC ID {} does not exist in the system", assetAmc.getId());
                throw new CustomException("INVALID_ASSETAMC_MODIFY", "The assetAmc id " + assetAmc.getId() + " that you are trying to update does not exists for the assetAmc");
            }
        log.debug("Update against DB validation completed successfully for {} asset AMC record(s)", assetAmcsFromRequest.size());

            validateStartDateAndEndDateAgainstDB(assetAmc, assetAmcFromDB, currentTimestamp, nextDateTimestampUTC);

//            validateUpdateAddressAgainstDB(project, projectFromDB);
        }
    }

    private void validateStartDateAndEndDateAgainstDB(AssetAmc assetAmc, AssetAmc assetAmcFromDB, Long currentTimestamp, Long nextDateTimestampUTC) {
        String errorMessage = "";
        // Check if the assetAmc start date is not null and whether it's different from the one in the database
        errorMessage = getErrorMessage(assetAmc, assetAmcFromDB, currentTimestamp, nextDateTimestampUTC, errorMessage);
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_ASSET_AMC_MODIFY", errorMessage);
        }

        errorMessage = "";
        // Check if the project end date is not null and whether it's different from the one in the database
        if (assetAmc.getAmcEndDate() != null) {
            // Check if the project end date is before the current timestamp or within 24 hours from the next date's midnight
            if (assetAmc.getAmcEndDate().compareTo(assetAmcFromDB.getAmcEndDate()) < 0) {
                if (assetAmc.getAmcEndDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The asset_amc end date cannot be updated as it has already ended. The asset_amc end date cannot be decreased to a past date.";
                } else if (assetAmc.getAmcEndDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The asset_amc end date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The asset_amc end date cannot be updated as it is null.";
        }
        // If there's an error message, log it and throw a CustomException
        if (!errorMessage.trim().isEmpty()) {
            log.error(errorMessage);
            throw new CustomException("INVALID_PROJECT_MODIFY", errorMessage);
        }
    }

    private static String getErrorMessage(AssetAmc assetAmc, AssetAmc assetAmcFromDB, Long currentTimestamp, Long nextDateTimestampUTC, String errorMessage) {
        if (assetAmc.getAmcStartDate() != null) {
            // Check if the project start date is different from the one in the database
            if (assetAmc.getAmcStartDate().compareTo(assetAmcFromDB.getAmcStartDate()) != 0) {
                // Check if the project start date is before the current timestamp or within 24 hours from the next date's midnight
                if (assetAmcFromDB.getAmcStartDate().compareTo(currentTimestamp) < 0) {
                    errorMessage = "The assetAmc start date cannot be updated as the assetAmc has already started.";
                } else if (assetAmc.getAmcStartDate().compareTo(nextDateTimestampUTC) < 0) {
                    errorMessage = "The assetAmc start date cannot be updated as it should be at least 24 hours in advance from the current time and start after the next day onwards.";
                }
            }
        } else {
            errorMessage = "The project start date cannot be updated as it is null.";
        }
        return errorMessage;
    }
}