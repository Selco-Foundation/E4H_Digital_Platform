package facility.service;

import facility.config.Configuration;
import facility.repository.FacilityRepository;
import facility.util.*;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static facility.config.ServiceConstants.FACILITY_ADMIN;
import static facility.config.ServiceConstants.SYSTEM_USER;

@Service
@Slf4j
public class FacilityService {
    private static final String CATEGORY_HEALTH = "HEALTH";
    private static final String CATEGORY_ANGANWADI = "ANGANWADI";
    /** When category is HEALTH, MDMS-style rule: at least one of HFR ID or NIN ID must be present. */
    private static final String ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH =
            "When Facility Category is HEALTH, at least one of HFR ID or NIN ID is required.";

    private final FacilityRepository facilityRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FacilityRowMapper facilityRowMapper;
    private final IdgenUtil idgenUtil;
    private final FacilityMdmsValidator facilityMdmsValidator;
    private final BoundaryValidator boundaryValidator;
    private final FacilityQueryDao facilityQueryDao;
    private final BoundaryService boundaryService;
    private final Configuration configs;
    private final FacilityKibanaMapper facilityKibanaMapper;
    private EncryptionDecryptionUtil encryptionDecryptionUtil;
    private BoundaryUtil boundaryUtil;

    private FacilityRowMapperV2 facilityRowMapperV2;

    private final HRMSUtils hrmsUtils;
    private final HRMSService hrmsService;
    private final RestTemplate restTemplate;

    private static final String LOCALIZATION_MODULE = "rainmaker-in";
    private static final String LOCALIZATION_LOCALE = "en_IN";
    // Existing boundary localization upsert uses tenantId="in" (see ingestion-service / im-service migrations)
    private static final String LOCALIZATION_TENANT_ID = "in";

    public FacilityService(
            FacilityRepository facilityRepository,
            JdbcTemplate jdbcTemplate,
            FacilityRowMapper facilityRowMapper,
            IdgenUtil idgenUtil,
            FacilityMdmsValidator facilityMdmsValidator,
            BoundaryValidator boundaryValidator,
            FacilityQueryDao facilityQueryDao,
            BoundaryService boundaryService,
            Configuration configs,
            FacilityKibanaMapper facilityKibanaMapper,
            EncryptionDecryptionUtil encryptionDecryptionUtil,
            BoundaryUtil boundaryUtil,
            FacilityRowMapperV2 facilityRowMapperV2,
            HRMSUtils hrmsUtils,
            HRMSService hrmsService,
            RestTemplate restTemplate) {
        this.facilityRepository = facilityRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.facilityRowMapper = facilityRowMapper;
        this.idgenUtil = idgenUtil;
        this.facilityMdmsValidator = facilityMdmsValidator;
        this.boundaryValidator = boundaryValidator;
        this.facilityQueryDao = facilityQueryDao;
        this.boundaryService = boundaryService;
        this.configs = configs;
        this.facilityKibanaMapper = facilityKibanaMapper;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.boundaryUtil = boundaryUtil;
        this.facilityRowMapperV2 = facilityRowMapperV2;
        this.hrmsUtils = hrmsUtils;
        this.hrmsService = hrmsService;
        this.restTemplate = restTemplate;
    }

    /**
     * Creates facilities in the system after validation.
     * Validates against MDMS, ensures boundary codes are valid,
     * generates facility IDs and address IDs if missing, and checks for uniqueness.
     *
     * <p><b>Boundary Creation Behavior:</b></p>
     * <ul>
     *   <li>Facility boundaries are created via external boundary service API calls</li>
     *   <li>Boundary codes are validated before creation to ensure parent boundaries exist</li>
     * </ul>
     *
     * @param request FacilityCreateRequest containing a list of facilities
     * @return list of successfully validated and pushed facilities
     * @throws CustomException if validation fails (MDMS, uniqueness, boundary validation)
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Facility> createFacility(FacilityCreateRequest request) {
        log.trace("Entering createFacility method");
        List<FacilityCreate> facilities = request.getFacilities();
        log.info("Processing facility create request for {} facilities", facilities.size());

        // Group facility create requests by tenant ID for batch validation and processing
        Map<String, List<FacilityCreate>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(FacilityCreate::getTenantId));
        log.debug("Grouped facilities into {} tenant groups", facilitiesByTenant.size());

        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<FacilityCreate>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<FacilityCreate> facilityCreateList = entry.getValue();
            log.info("Processing {} facilities for tenant {}", facilityCreateList.size(), tenantId);
            List<Facility> tenantFacilities = new ArrayList<>();

            // Validate block boundary codes in bulk
            Set<String> blockBoundaryCodes = facilityCreateList.stream()
                    .map(FacilityCreate::getBlockBoundaryCode)
                    .collect(Collectors.toSet());
            log.debug("Validating {} unique block boundary codes for tenant {}", blockBoundaryCodes.size(), tenantId);
            boundaryValidator.validateBoundaries(blockBoundaryCodes, tenantId, request.getRequestInfo());

            List<Boundary> boundaryList = new ArrayList<>();
            List<BoundaryRelation> boundaryRelationList = new ArrayList<>();

            for (FacilityCreate facilityCreate : facilityCreateList) {
                Facility facility = Facility.builder()
                        .tenantId(tenantId)
                        .facilityCategory(facilityCreate.getFacilityCategory())
                        .facilityType(facilityCreate.getFacilityType())
                        .facilitySubtype(facilityCreate.getFacilitySubtype())
                        .facilityName(facilityCreate.getFacilityName())
                        .facilityOwnership(facilityCreate.getFacilityOwnership())
                        .facilityPocName(facilityCreate.getFacilityPocName())
                        .facilityPocEmail(facilityCreate.getFacilityPocEmail())
                        .facilityPocPhone(facilityCreate.getFacilityPocPhone())
                        .facilityPocUsername(facilityCreate.getFacilityPocUsername())
                        .hfrId(facilityCreate.getHfrId())
                        .ninId(facilityCreate.getNinId())
                        .userId(facilityCreate.getUserId())
                        .facilityStatus(facilityCreate.getFacilityStatus())
                        .facilityRegion(facilityCreate.getFacilityRegion())
                        .address(facilityCreate.getAddress())
                        .facilityDetails(facilityCreate.getFacilityDetails())
                        .wfStatus(facilityCreate.getWfStatus())
                        .additionalDetails(facilityCreate.getAdditionalDetails())
                        .isActive(true)
                        .isOnmReady(facilityCreate.getIsOnmReady())
                        .build();

                facility.setFacilityId(idgenUtil.getIdList(
                        request.getRequestInfo(), tenantId, "facility.id", "", 1
                ).get(0));

                String facilityBoundaryCode = facilityCreate.getBlockBoundaryCode() + "_" + facility.getFacilityId();

                boundaryList.add(
                        Boundary.builder()
                                .tenantId(tenantId)
                                .code(facilityBoundaryCode)
                                .build()
                );
                boundaryRelationList.add(
                        BoundaryRelation.builder()
                                .tenantId(tenantId)
                                .boundaryType("Facility")
                                .code(facilityBoundaryCode)
                                .parent(facilityCreate.getBlockBoundaryCode())
                                .hierarchyType("SELCO")
                                .build()
                );
                facility.setBoundaryCode(facilityBoundaryCode);

                // Set default workflow status and activation flag
                if (facility.getWfStatus() == null) facility.setWfStatus("CREATED");
                if (facility.getIsActive() == null) facility.setIsActive(true);

                // Generate address ID if missing
                if (facility.getAddress().getAddressId() == null) {
                    facility.getAddress().setAddressId(UUID.randomUUID().toString());
                }

                // HFR/NIN are category-aware: mandatory only for HEALTH.
                validateCategoryBasedIdentifiers(facility.getFacilityCategory(), facility.getHfrId(), facility.getNinId());

                // Check uniqueness for HFR ID or NIN ID
                validateHfrOrNinUniqueness(facility, tenantId);

                // Check uniqueness of facility name + boundaryCode
                validateFacilityNameBoundaryCodeUnique(facility, tenantId);

                tenantFacilities.add(facility);
            }

            // Validate facilities against MDMS master data
            log.info("Validating {} facilities against MDMS for tenant {}", tenantFacilities.size(), tenantId);
            facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());

            //todo: handle boundary or boundary relation creation failure??
            log.info("Creating {} boundaries for tenant {}", boundaryList.size(), tenantId);
            BoundaryCreateRequest boundaryCreateRequest = BoundaryCreateRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .boundary(boundaryList)
                    .build();
            boundaryService.createBoundaries(boundaryCreateRequest);

            log.info("Creating {} boundary relationships for tenant {}", boundaryRelationList.size(), tenantId);
            for (BoundaryRelation boundaryRelation: boundaryRelationList) {
                BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                        .requestInfo(request.getRequestInfo())
                        .boundaryRelationship(boundaryRelation)
                        .build();
                boundaryService.createBoundaryRelationship(boundaryRelationshipRequest);
            }

            // Create localization messages for each facility boundary (code: Boundary_{facilityBoundaryCode})
            upsertFacilityBoundaryLocalizations(tenantFacilities, request.getRequestInfo());

            log.info("Pushing {} facilities to Kafka for tenant {}", tenantFacilities.size(), tenantId);
            for (Facility facility : tenantFacilities) {
                // Keep original (unencrypted) POC mobile number for HRMS user creation
                String originalPocMobileNumber = facility.getFacilityPocPhone();
                try {
                    String encryptedPocMobileNumber = encryptMobileNumber(facility.getFacilityPocPhone());
                    if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                        facility.setFacilityPocPhone(encryptedPocMobileNumber);
                    }
                }
                catch (Exception e){}

                Long time = System.currentTimeMillis();
                facility.setAuditDetails(AuditDetails.builder().createdBy(request.getRequestInfo().getUserInfo().getUuid()).lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid()).createdTime(time).lastModifiedTime(time).build());

                log.trace("Processing facility: {}", facility.getFacilityId());
                // Push to Kafka topic for persistence
                facilityRepository.pushCreateFacility(facility);
                
                FacilityMappedVendorHelper.hydrateFromAdditionalDetails(facility);
                FacilityMappedVendorHelper.syncToAdditionalDetails(facility);

                // If facility is ONM ready, create POC user and push to Kibana for indexing
                if (Boolean.TRUE.equals(facility.getIsOnmReady())) {
                    log.info("Facility {} is ONM ready, creating POC user and pushing to Kibana", facility.getFacilityId());
                    // Create POC user if not exists (check by phone number uniqueness)
                    createFacilityPOCUserIfNotExists(facility, tenantId, request.getRequestInfo(), originalPocMobileNumber);

                    // Push to Kibana for indexing
                    FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                    facilityRepository.pushToKibana(kibanaIndex);
                }
                
                validatedFacilities.add(facility);
            }
        }

        log.info("Successfully created {} facilities", validatedFacilities.size());
        log.trace("Exiting createFacility method");
        return validatedFacilities;
    }

    private void upsertFacilityBoundaryLocalizations(List<Facility> facilities, RequestInfo requestInfo) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }

        // Build localization messages
        List<Map<String, String>> messages = new ArrayList<>();
        for (Facility facility : facilities) {
            if (facility == null) continue;
            String facilityBoundaryCode = facility.getBoundaryCode();
            if (facilityBoundaryCode == null || facilityBoundaryCode.isBlank()) continue;

            String localizationCode = "Boundary_" + facilityBoundaryCode;

            // Display name for this boundary localization: use facility name when available.
            String displayName = facility.getFacilityName();
            if (displayName == null || displayName.isBlank()) {
                displayName = localizationCode;
            }

            messages.add(Map.of(
                    "code", localizationCode,
                    "message", displayName,
                    "module", LOCALIZATION_MODULE,
                    "locale", LOCALIZATION_LOCALE
            ));
        }

        if (messages.isEmpty()) {
            return;
        }

        String localizationHost = configs.getLocalizationHost();
        String localizationContextPath = configs.getLocalizationContextPath();
        if (localizationHost == null || localizationHost.isBlank()
                || localizationContextPath == null || localizationContextPath.isBlank()) {
            log.warn("Localization host/context not configured; skipping facility boundary localization upsert");
            return;
        }

        String upsertUrl = UriComponentsBuilder.fromUriString(localizationHost)
                .path(localizationContextPath)
                .path(configs.getLocalizationUpsertPath())
                .toUriString();

        log.info("Upserting facility boundary localizations: messages={}, module={}, locale={}",
                messages.size(), LOCALIZATION_MODULE, LOCALIZATION_LOCALE);

        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", requestInfo);
        payload.put("tenantId", LOCALIZATION_TENANT_ID);
        payload.put("messages", messages);

        try {
            restTemplate.postForObject(upsertUrl, payload, Map.class);
            log.info("Completed facility boundary localization upsert successfully: messages={}", messages.size());
        } catch (Exception e) {
            // Best-effort: we don't want to fail the entire facility create due to localization.
            log.error("Localization upsert failed for facility boundary localizations: messages={}", messages.size(), e);
        }
    }

    /**
     * Checks whether a facility with the same name and boundary already exists
     * in the given tenant. Throws a CustomException if duplicate found.
     */
    private void validateFacilityNameBoundaryCodeUnique(Facility facility, String tenantId) {
        log.trace("Entering validateFacilityNameBoundaryCodeUnique method");
        if (facility.getFacilityName() != null && facility.getBoundaryCode() != null) {
            log.debug("Checking uniqueness of facility name and boundary code for tenant {}", tenantId);
            boolean exists = facilityQueryDao.existsByFacilityNameAndBoundary(
                    tenantId, facility.getFacilityName(), facility.getBoundaryCode()
            );

            if (exists) {
                log.warn("Duplicate facility found: name={}, boundaryCode={}, tenantId={}",
                        sanitizeForLog(facility.getFacilityName()), facility.getBoundaryCode(), tenantId);
                throw new CustomException("FACILITY_DUPLICATE_NAME_LOCATION",
                        "A facility with the same name and boundary already exists in this tenant");
            }
            log.debug("Facility name and boundary code are unique");
        }
        log.trace("Exiting validateFacilityNameBoundaryCodeUnique method");
    }

    /**
     * Checks whether the HFR ID or NIN ID already exists for another facility
     * in the same tenant. Throws a CustomException if duplicate found.
     */
    private void validateHfrOrNinUniqueness(Facility facility, String tenantId) {
        log.trace("Entering validateHfrOrNinUniqueness method");
        if (facility != null) {
            String hfrId = facility.getHfrId();
            String ninId = facility.getNinId();

            if ((hfrId != null && !hfrId.isBlank()) || (ninId != null && !ninId.isBlank())) {
                log.debug("Checking uniqueness of HFR ID or NIN ID for tenant {}", tenantId);
                boolean exists = facilityQueryDao.existsByHfrIdOrNinId(hfrId, ninId, tenantId);
                if (exists) {
                    log.warn("Duplicate HFR ID or NIN ID found for tenant {}", tenantId);
                    throw new CustomException("FACILITY_DUPLICATE_ID",
                            "Facility with same HFR ID or NIN ID already exists in tenant " + tenantId);
                }
                log.debug("HFR ID and NIN ID are unique");
            }
        }
        log.trace("Exiting validateHfrOrNinUniqueness method");
    }

    private void validateCategoryBasedIdentifiers(String facilityCategory, String hfrId, String ninId) {
        String normalizedCategory = facilityCategory == null ? "" : facilityCategory.trim().toUpperCase(Locale.ROOT);
        if (CATEGORY_HEALTH.equals(normalizedCategory)) {
            boolean hasHfr = hfrId != null && !hfrId.isBlank();
            boolean hasNin = ninId != null && !ninId.isBlank();
            if (!hasHfr && !hasNin) {
                throw new IllegalArgumentException(ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH);
            }
            return;
        }
        if (CATEGORY_ANGANWADI.equals(normalizedCategory)) {
            // Explicitly optional for ANGANWADI.
            return;
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    /**
     * Creates POC user as HRMS employee if not exists (checks by employee username / code in HRMS).
     * For {@code ANGANWADI} facilities, username is {@code facilityPocUsername} (HFR not required).
     * Otherwise validates that HFR ID or NIN ID is present (one is always required), plus POC contact and POC name.
     * Supports both direct fields (facilityPocName, facilityPocPhone, hfrId) and nested facilityDetails.
     *
     * @param facility The facility for which to create POC user
     * @param requestInfo RequestInfo for API calls
     */
    private void createFacilityPOCUserIfNotExists(Facility facility, String tenantId, RequestInfo requestInfo,
                                                  String plainPocMobileNumber) {
        HealthFacilityDetails facilityDetails = facility.getFacilityDetails();
        
        // If facilityDetails is null or missing values, populate from direct fields
        if (facilityDetails == null) {
            facilityDetails = HealthFacilityDetails.builder().build();
            facility.setFacilityDetails(facilityDetails);
        }

        // Populate facilityDetails from direct fields if missing (trim whitespace)
        if ((facilityDetails.getHfrId() == null || facilityDetails.getHfrId().isBlank())
                && facility.getHfrId() != null && !facility.getHfrId().trim().isBlank()) {
            facilityDetails.setHfrId(facility.getHfrId().trim());
        }

        if ((facilityDetails.getNinId() == null || facilityDetails.getNinId().isBlank())
                && facility.getNinId() != null && !facility.getNinId().trim().isBlank()) {
            facilityDetails.setNinId(facility.getNinId().trim());
        }

        if ((facilityDetails.getPocContact() == null || facilityDetails.getPocContact().isBlank()) 
                && plainPocMobileNumber != null && !plainPocMobileNumber.trim().isBlank()) {
            facilityDetails.setPocContact(plainPocMobileNumber.trim());
        }
        
        if (facilityDetails.getPocName() == null 
                && facility.getFacilityPocName() != null && !facility.getFacilityPocName().trim().isBlank()) {
            facilityDetails.setPocName(facility.getFacilityPocName().trim());
        }
        
        if (facilityDetails.getPocEmail() == null 
                && facility.getFacilityPocEmail() != null && !facility.getFacilityPocEmail().trim().isBlank()) {
            facilityDetails.setPocEmail(facility.getFacilityPocEmail().trim());
        }

        String normalizedCategory = facility.getFacilityCategory() == null
                ? ""
                : facility.getFacilityCategory().trim().toUpperCase(Locale.ROOT);
        boolean isAnganwadi = CATEGORY_ANGANWADI.equals(normalizedCategory);

        // Validate required fields (ANGANWADI: POC username + contact + name; HEALTH/other: HFR or NIN + contact + name)
        if (isAnganwadi) {
            String pocUsername = facility.getFacilityPocUsername() == null
                    ? ""
                    : facility.getFacilityPocUsername().trim();
            if (pocUsername.isBlank()
                    || facilityDetails.getPocContact() == null || facilityDetails.getPocContact().isBlank()
                    || facilityDetails.getPocName() == null || facilityDetails.getPocName().isBlank()) {
                log.warn("Cannot create POC user for ANGANWADI facility {}: missing facility POC username, POC contact, or POC name. " +
                                "POC username: {}, POC Contact: {}, POC Name: {}",
                        sanitizeForLog(facility.getFacilityId()),
                        sanitizeForLog(pocUsername.isBlank() ? null : pocUsername),
                        sanitizeForLog(facilityDetails.getPocContact()),
                        sanitizeForLog(facilityDetails.getPocName() != null ? facilityDetails.getPocName() : "null"));
                return;
            }
        } else {
            String facilityIdentifier = resolveFacilityIdentifier(facility, facilityDetails);
            if (facilityIdentifier == null || facilityIdentifier.isBlank()
                    || facilityDetails.getPocContact() == null || facilityDetails.getPocContact().isBlank()
                    || facilityDetails.getPocName() == null || facilityDetails.getPocName().isBlank()) {
                log.warn("Cannot create POC user for facility {}: missing facility identifier (HFR or NIN ID), POC contact, or POC name. " +
                        "HFR ID: {}, NIN ID: {}, POC Contact: {}, POC Name: {}",
                        sanitizeForLog(facility.getFacilityId()),
                        sanitizeForLog(facilityDetails.getHfrId()),
                        sanitizeForLog(facilityDetails.getNinId()),
                        sanitizeForLog(facilityDetails.getPocContact()),
                        sanitizeForLog(facilityDetails.getPocName() != null ? facilityDetails.getPocName() : "null"));
                return;
            }
        }

        String username;
        if (isAnganwadi) {
            username = facility.getFacilityPocUsername().trim();
        } else {
            username = resolveFacilityIdentifier(facility, facilityDetails);
        }
        // Check if employee already exists by mobile number
        boolean employeeExists = hrmsService.employeeExistsByUsername(
                username,
                tenantId,
                requestInfo
        );

        if (!employeeExists) {
            // Create POC user as HRMS employee with COMPLAINANT and EMPLOYEE roles
            boolean created = hrmsService.createFacilityPOCEmployee(facility, requestInfo);
            if (created) {
                log.info("Successfully created POC user for facility {} with username {}",
                        sanitizeForLog(facility.getFacilityId()), sanitizeForLog(username));
            } else {
                log.warn("Failed to create POC user for facility {}", sanitizeForLog(facility.getFacilityId()));
            }
        } else {
            log.info("POC user with identifier {} already exists for facility {}, skipping creation",
                    sanitizeForLog(username), sanitizeForLog(facility.getFacilityId()));
        }
    }

    /**
     * Resolves the facility identifier used as HRMS username/employee code.
     * Prefers HFR ID over NIN ID; checks both top-level facility fields and nested facilityDetails.
     */
    private String resolveFacilityIdentifier(Facility facility, HealthFacilityDetails facilityDetails) {
        if (facility.getHfrId() != null && !facility.getHfrId().trim().isBlank()) {
            return facility.getHfrId().trim();
        }
        if (facilityDetails != null && facilityDetails.getHfrId() != null && !facilityDetails.getHfrId().isBlank()) {
            return facilityDetails.getHfrId().trim();
        }
        if (facility.getNinId() != null && !facility.getNinId().trim().isBlank()) {
            return facility.getNinId().trim();
        }
        if (facilityDetails != null && facilityDetails.getNinId() != null && !facilityDetails.getNinId().isBlank()) {
            return facilityDetails.getNinId().trim();
        }
        return null;
    }

    /**
     * Updates a facility after validating existence, MDMS values, and boundaries.
     * Pushes the update request to the Kafka topic for persistence.
     *
     * @param request FacilityUpdateRequest
     * @return updated facility data
     */
    public Facility updateFacility(FacilityUpdateRequest request) {
        log.trace("Entering updateFacility method");
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();

        if (update.getFacilityId() == null || update.getTenantId() == null) {
            log.error("Update request missing facilityId or tenantId");
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }

        log.info("Updating facility {} for tenant {}", update.getFacilityId(), update.getTenantId());
        validateFacilityEditAuthorization(request.getRequestInfo());

        // Check if the facility exists in DB before attempting an update
        String fetchFullFacilitySql = "SELECT fac.*, " +
                " (SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive " +
                " FROM facility fac WHERE fac.id = ? AND fac.tenant_id = ?";
        Facility existingFacility;
        try {
            existingFacility = jdbcTemplate.queryForObject(fetchFullFacilitySql, new Object[]{update.getFacilityId(), update.getTenantId()}, facilityRowMapper.rowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Facility {} not found for tenant {}, returning null", update.getFacilityId(), update.getTenantId());
            return null;
        }
        FacilityAddress address = update.getAddress();
        if(address !=null && existingFacility!=null && existingFacility.getAddress()!=null){
            address.setAddressId(existingFacility.getAddress().getAddressId());
        }

        try{
            String decryptedMobileNumber = decryptMobileNumber(existingFacility.getFacilityPocPhone());
            if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                existingFacility.setFacilityPocPhone(decryptedMobileNumber);
            }
        }
        catch(Exception e){}

        Facility facility = new Facility();
        facility.setFacilityId(update.getFacilityId());
        facility.setTenantId(update.getTenantId());
        facility.setFacilityCategory(update.getFacilityCategory());
        facility.setFacilityType(update.getFacilityType());
        facility.setFacilitySubtype(update.getFacilitySubtype());
        facility.setFacilityName(update.getFacilityName());
        facility.setAddress(update.getAddress());
        facility.setAdditionalDetails(update.getAdditionalDetails());
        facility.setBoundaryCode(update.getBoundaryCode());
        facility.setFacilityDetails(update.getFacilityDetails());
        facility.setFacilityPocName(update.getPocName());
        facility.setFacilityPocPhone(update.getPocContact());
        facility.setFacilityPocEmail(update.getPocEmail());
        facility.setFacilityPocUsername(firstNonBlank(update.getFacilityPocUsername(), existingFacility.getFacilityPocUsername()));
        facility.setHfrId(update.getHfrId());
        facility.setNinId(update.getNinId());
        facility.setFacilityStatus(update.getStatus());
        facility.setIsActive(update.getIsActive());
        facility.setUserId(update.getUserId());
        facility.setIsOnmReady(update.getIsOnmReady());

        FacilityMappedVendorHelper.mergeMappedVendorFromUpdate(facility, update, existingFacility);
        FacilityMappedVendorHelper.syncToAdditionalDetails(facility);
        update.setAdditionalDetails(facility.getAdditionalDetails());
        update.setMappedVendorName(facility.getMappedVendorName());
        update.setMappedVendorUserName(facility.getMappedVendorUserName());

        String effectiveCategory = firstNonBlank(update.getFacilityCategory(), existingFacility.getFacilityCategory());
        String effectiveHfrId = firstNonBlank(update.getHfrId(), existingFacility.getHfrId());
        String effectiveNinId = firstNonBlank(update.getNinId(), existingFacility.getNinId());
        validateCategoryBasedIdentifiers(effectiveCategory, effectiveHfrId, effectiveNinId);

        // Validate with MDMS and boundary APIs
        log.info("Validating facility update against MDMS and boundaries");
        facilityMdmsValidator.validateAgainstMDMS(List.of(facility), update.getTenantId(), request.getRequestInfo());
        if (facility.getBoundaryCode() != null) {
            log.debug("Validating boundary code: {}", facility.getBoundaryCode());
            boundaryValidator.validateBoundaries(
                    Set.of(facility.getBoundaryCode()),
                    update.getTenantId(),
                    request.getRequestInfo());
        }

        if (facility.getWfStatus() == null) facility.setWfStatus("UPDATED");
        if (facility.getIsActive() == null) facility.setIsActive(existingFacility.getIsActive());

        // If POC details are updated AND facility is isOnmReady=true
        boolean isPocDetailsUpdated = checkPOCDetailsUpdated(existingFacility, facility);
        if(isPocDetailsUpdated){
            updatedHRMSUser(request, existingFacility, facility);
        }

        // Create localization messages for each facility boundary (code: Boundary_{facilityBoundaryCode})
        upsertFacilityBoundaryLocalizations(List.of(facility), request.getRequestInfo());

        try {
            String encryptedPocMobileNumber = encryptMobileNumber(request.getFacilityUpdate().getPocContact());
            if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                request.getFacilityUpdate().setPocContact(encryptedPocMobileNumber);
            }
        }
        catch (Exception e){}

        log.info("Pushing facility update to Kafka");
        facilityRepository.pushUpdateFacility(request);
        boolean mappedVendorUpdated = FacilityMappedVendorHelper.hasMappedVendor(facility);
        // If user sent isOnmReady = true, handle POC user creation and Kibana push
        if (Boolean.TRUE.equals(update.getIsOnmReady())) {
            log.info("Facility {} is marked as ONM ready, processing POC user and Kibana push", update.getFacilityId());
            // Merge update request data with existing facility data to get complete facility info
            Facility facilityForProcessing = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityCategory(facility.getFacilityCategory() != null ? facility.getFacilityCategory() : existingFacility.getFacilityCategory())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .facilitySubtype(facility.getFacilitySubtype() != null ? facility.getFacilitySubtype() : existingFacility.getFacilitySubtype())
                    .facilityName(facility.getFacilityName() != null ? facility.getFacilityName() : existingFacility.getFacilityName())
                    .facilityOwnership(existingFacility.getFacilityOwnership())
                    .facilityRegion(existingFacility.getFacilityRegion())
                    .facilityPocName(facility.getFacilityPocName()!=null && !facility.getFacilityPocName().isBlank() ? facility.getFacilityPocName(): existingFacility.getFacilityPocEmail())
                    .facilityPocPhone(facility.getFacilityPocPhone()!=null && !facility.getFacilityPocPhone().isBlank() ? facility.getFacilityPocPhone(): existingFacility.getFacilityPocPhone())
                    .facilityPocEmail(facility.getFacilityPocEmail()!=null && !facility.getFacilityPocEmail().isBlank() ? facility.getFacilityPocEmail(): existingFacility.getFacilityPocEmail())
                    .facilityPocUsername(facility.getFacilityPocUsername()!=null && !facility.getFacilityPocUsername().isBlank() ? facility.getFacilityPocUsername(): existingFacility.getFacilityPocUsername())
                    .hfrId(facility.getHfrId()!=null && !facility.getHfrId().isBlank() ? facility.getHfrId(): existingFacility.getHfrId())
                    .ninId(facility.getNinId()!=null && !facility.getNinId().isBlank() ? facility.getNinId(): existingFacility.getNinId())
                    .userId(facility.getUserId()!=null && !facility.getUserId().isBlank() ? facility.getUserId(): existingFacility.getUserId())
                    .address(facility.getAddress() != null ? facility.getAddress() : existingFacility.getAddress())
                    .facilityDetails(facility.getFacilityDetails() != null ? facility.getFacilityDetails() : existingFacility.getFacilityDetails())
                    .additionalDetails(facility.getAdditionalDetails() != null ? facility.getAdditionalDetails() : existingFacility.getAdditionalDetails())
                    .boundaryCode(facility.getBoundaryCode() != null ? facility.getBoundaryCode() : existingFacility.getBoundaryCode())
                    .isOnmReady(true)
                    .build();

            try{
                String decryptedMobileNumber = decryptMobileNumber(facilityForProcessing.getFacilityPocPhone());
                if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                    facilityForProcessing.setFacilityPocPhone(decryptedMobileNumber);
                }
            }
            catch(Exception e){}

            // Always check/create POC user when isOnmReady is true (whether transitioning or already true)
            // This ensures POC user is created if missing, even if facility was already ONM ready
            createFacilityPOCUserIfNotExists(
                    facilityForProcessing,
                    update.getTenantId(),
                    request.getRequestInfo(),
                    facilityForProcessing.getFacilityPocPhone()
            );

            // Check if facility already exists in Kibana, if not then push
//            boolean existsInKibana = facilityKibanaMapper.existsInKibana(
//                    update.getFacilityId(),
//                    update.getTenantId(),
//                    request.getRequestInfo()
//            );

//            if (existsInKibana) {
//                log.info("Facility {} already exists in Kibana, skipping push", sanitizeForLog(update.getFacilityId()));
//                return facility;
//            }

            // Only update mutable Kibana display fields during facility update.
            Facility facilityForKibanaUpdate = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityCategory(facility.getFacilityCategory() != null ? facility.getFacilityCategory() : existingFacility.getFacilityCategory())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .facilitySubtype(facility.getFacilitySubtype() != null ? facility.getFacilitySubtype() : existingFacility.getFacilitySubtype())
                    .facilityName(facility.getFacilityName() != null ? facility.getFacilityName() : existingFacility.getFacilityName())
                    .facilityOwnership(existingFacility.getFacilityOwnership()) // Not in update request, use existing
                    .facilityRegion(existingFacility.getFacilityRegion()) // Not in update request, use existing
                    .address(facility.getAddress() != null ? facility.getAddress() : existingFacility.getAddress())
                    .facilityDetails(facility.getFacilityDetails() != null ? facility.getFacilityDetails() : existingFacility.getFacilityDetails())
                    .additionalDetails(facility.getAdditionalDetails() != null ? facility.getAdditionalDetails() : existingFacility.getAdditionalDetails())
                    .mappedVendorName(facility.getMappedVendorName() != null ? facility.getMappedVendorName() : existingFacility.getMappedVendorName())
                    .mappedVendorUserName(facility.getMappedVendorUserName() != null ? facility.getMappedVendorUserName() : existingFacility.getMappedVendorUserName())
                    .boundaryCode(facility.getBoundaryCode() != null ? facility.getBoundaryCode() : existingFacility.getBoundaryCode())
                    .isOnmReady(true) // Set from update request
                    .facilityPocName(facility.getFacilityPocName()!=null && !facility.getFacilityPocName().isBlank() ? facility.getFacilityPocName(): existingFacility.getFacilityPocEmail())
                    .facilityPocPhone(facility.getFacilityPocPhone()!=null && !facility.getFacilityPocPhone().isBlank() ? facility.getFacilityPocPhone(): existingFacility.getFacilityPocPhone())
                    .facilityPocEmail(facility.getFacilityPocEmail()!=null && !facility.getFacilityPocEmail().isBlank() ? facility.getFacilityPocEmail(): existingFacility.getFacilityPocEmail())
                    .facilityPocUsername(facility.getFacilityPocUsername()!=null && !facility.getFacilityPocUsername().isBlank() ? facility.getFacilityPocUsername(): existingFacility.getFacilityPocUsername())
                    .hfrId(facility.getHfrId()!=null && !facility.getHfrId().isBlank() ? facility.getHfrId(): existingFacility.getHfrId())
                    .ninId(facility.getNinId()!=null && !facility.getNinId().isBlank() ? facility.getNinId(): existingFacility.getNinId())
                    .userId(facility.getUserId()!=null && !facility.getUserId().isBlank() ? facility.getUserId(): existingFacility.getUserId())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .isActive(facility.getIsActive() != null ? facility.getIsActive() : existingFacility.getIsActive())
                    .build();

            FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndexForFacilityUpdate(
                    facilityForKibanaUpdate, request.getRequestInfo());
            facilityRepository.pushToKibana(kibanaIndex);
            log.info("Facility {} pushed to Kibana successfully", sanitizeForLog(update.getFacilityId()));
        } else if (mappedVendorUpdated) {
            Facility facilityForKibanaUpdate = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityName(firstNonBlank(facility.getFacilityName(), existingFacility.getFacilityName()))
                    .facilityType(firstNonBlank(facility.getFacilityType(), existingFacility.getFacilityType()))
                    .facilityCategory(firstNonBlank(facility.getFacilityCategory(), existingFacility.getFacilityCategory()))
                    .mappedVendorName(facility.getMappedVendorName())
                    .mappedVendorUserName(facility.getMappedVendorUserName())
                    .additionalDetails(facility.getAdditionalDetails())
                    .isActive(facility.getIsActive() != null ? facility.getIsActive() : existingFacility.getIsActive())
                    .build();
            FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndexForFacilityUpdate(
                    facilityForKibanaUpdate, request.getRequestInfo());
            if (kibanaIndex != null) {
                facilityRepository.pushToKibana(kibanaIndex);
                log.info("Facility {} mapped-vendor fields pushed to Kibana", sanitizeForLog(update.getFacilityId()));
            }
        }
        
        log.info("Successfully updated facility {}", update.getFacilityId());
        log.trace("Exiting updateFacility method");
        return facility;
    }

    @Transactional(rollbackFor = Exception.class)
    public Facility updateFacilityBlockBoundary(FacilityBlockUpdateRequest request) {
        log.trace("Entering updateFacilityBlockBoundary method");
        FacilityBlockUpdate blockUpdate = request.getFacilityBlockUpdate();
        if (blockUpdate == null) {
            throw new IllegalArgumentException("FacilityBlockUpdate payload is required");
        }
        if (blockUpdate.getFacilityId() == null || blockUpdate.getFacilityId().isBlank()
                || blockUpdate.getTenantId() == null || blockUpdate.getTenantId().isBlank()
                || blockUpdate.getNewBlockBoundaryCode() == null || blockUpdate.getNewBlockBoundaryCode().isBlank()) {
            throw new IllegalArgumentException("facility_id, tenant_id and new_block_boundary_code must be provided");
        }

        validateFacilityEditAuthorization(request.getRequestInfo());

        Facility existingFacility = getFacilityFromDb(blockUpdate.getFacilityId(), blockUpdate.getTenantId());
        if (existingFacility == null) {
            log.warn("Facility {} not found for tenant {}", blockUpdate.getFacilityId(), blockUpdate.getTenantId());
            return null;
        }
        String oldFacilityBoundaryCode = existingFacility.getBoundaryCode();

        // Validate that requested target block exists in boundary service.
        boundaryValidator.validateBoundaries(
                Set.of(blockUpdate.getNewBlockBoundaryCode()),
                blockUpdate.getTenantId(),
                request.getRequestInfo()
        );

        String updatedFacilityBoundaryCode = blockUpdate.getNewBlockBoundaryCode() + "_" + blockUpdate.getFacilityId();
        if (updatedFacilityBoundaryCode.equals(existingFacility.getBoundaryCode())) {
            log.info("No boundary update needed for facility {} (boundary code unchanged)", blockUpdate.getFacilityId());
            return existingFacility;
        }

        ensureFacilityBoundaryExists(updatedFacilityBoundaryCode, blockUpdate.getNewBlockBoundaryCode(), blockUpdate.getTenantId(), request.getRequestInfo());

        int updatedRows = jdbcTemplate.update(
                "UPDATE facility SET boundary_code = ? WHERE id = ? AND tenant_id = ?",
                updatedFacilityBoundaryCode,
                blockUpdate.getFacilityId(),
                blockUpdate.getTenantId()
        );
        log.info("{} Rows updated for facility {} and tenant {}", updatedRows, blockUpdate.getFacilityId(), blockUpdate.getTenantId());
        if (updatedRows == 0) {
            log.warn("No rows updated for facility {} and tenant {}", blockUpdate.getFacilityId(), blockUpdate.getTenantId());
            return null;
        }

        existingFacility.setBoundaryCode(updatedFacilityBoundaryCode);
        upsertFacilityBoundaryLocalizations(List.of(existingFacility), request.getRequestInfo());
        cleanupOldFacilityBoundaryIfUnused(oldFacilityBoundaryCode, blockUpdate.getTenantId(), request.getRequestInfo());
        syncImIncidentBoundaryCodesForFacility(
                blockUpdate.getTenantId(),
                blockUpdate.getFacilityId(),
                updatedFacilityBoundaryCode,
                blockUpdate.getNewBlockBoundaryCode(),
                request.getRequestInfo()
        );
        log.info("Updated boundary code for facility {} to {}", blockUpdate.getFacilityId(), updatedFacilityBoundaryCode);
        log.trace("Exiting updateFacilityBlockBoundary method");
        return existingFacility;
    }

    /**
     * Searches for facilities using filters like tenantId, name, hfrId, ninId, boundary, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> searchFacilities(FacilitySearchRequest request) {
        log.trace("Entering searchFacilities method");
        log.info("Searching facilities with limit={}, offset={}", request.getLimit(), request.getOffset());
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);
        log.debug("Built query with {} parameters", result.getParams().size());

        StringBuilder query = new StringBuilder(
                "SELECT facility.*, (SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r WHERE r.facilityid = facility.id AND r.tenantid = facility.tenant_id)) AS rms_inactive FROM facility");
        query.append(result.getWhereClause());
        query.append(" ORDER BY created_at DESC NULLS LAST LIMIT ? OFFSET ?");

        List<Object> allParams = new ArrayList<>(result.getParams());
        allParams.add(request.getLimit());
        allParams.add(request.getOffset());

        List<Facility> facilityList = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        log.info("Found {} facilities matching search criteria", facilityList.size());
        log.trace("Exiting searchFacilities method");
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        for (Facility facility: facilityList){
            try{
                String decryptedMobileNumber = decryptMobileNumber(facility.getFacilityPocPhone());
                if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                    facility.setFacilityPocPhone(decryptedMobileNumber);
                }
            }
            catch(Exception e){}
            String boundaryCode = facility.getBoundaryCode();
            if (boundaryCode != null && listBlock != null) {
                Boundary boundary = listBlock.get(boundaryCode);
                if (boundary != null) {
                    log.debug("✨ Enriching facility={} with state={}, district={} and block={}", facility.getFacilityId(), boundary.getState(), boundary.getDistrict(), boundary.getBlock());
                    boundary.setCode(boundaryCode);
                    facility.setBoundary(boundary);
                } else {
                    log.warn("⚠️ No boundary found for code={} in facility boundary={}", boundaryCode, facility.getFacilityId());
                }
            }
        }

        return facilityList;
    }

    /**
     * Searches for facilities using filters like multiple tenantIds, names, hfrIds, ninIds, boundaries, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> bulkSearchFacilities(FacilityBulkSearchRequest request) {
        log.trace("Entering bulkSearchFacilities method");
        List<Facility> facilityList = loadBulkFacilitiesWithAddressJoin(request);
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        enrichFacilitiesWithBoundaries(facilityList, listBlock);
        log.trace("Exiting bulkSearchFacilities method");
        return facilityList;
    }

    /**
     * Same bulk search as {@link #bulkSearchFacilities(FacilityBulkSearchRequest)} (SQL joins facility + address),
     * but resolves boundary hierarchy only for {@code boundary_code} values present on the result rows
     * via the boundary v2 API in batches — avoids loading the full boundary tree for large clients.
     * Request/response models are unchanged from bulk search.
     */
    public List<Facility> bulkSearchFacilitiesWithAddressAndBoundary(FacilityBulkSearchRequest request) {
        log.trace("Entering bulkSearchFacilitiesWithAddressAndBoundary method");
        List<Facility> facilityList = loadBulkFacilitiesWithAddressJoin(request);
        Set<String> boundaryCodesOnRows = facilityList.stream()
                .map(Facility::getBoundaryCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryMapForFacilityCodes(boundaryCodesOnRows);
        enrichFacilitiesWithBoundaries(facilityList, listBlock);
        log.trace("Exiting bulkSearchFacilitiesWithAddressAndBoundary method");
        return facilityList;
    }

    /**
     * Shared bulk SQL: facility rows with address fields from {@code facility_address}, POC phone decrypted.
     */
    private List<Facility> loadBulkFacilitiesWithAddressJoin(FacilityBulkSearchRequest request) {
        FacilityBulkSearchCriteria criteria = request.getFacilityBulkSearchCriteria();
        List<String> listFacilityCodes = boundaryUtil.getFacilityCodesFromBoundary(criteria);
        // When searching by state, district, or block with no facilities in that boundary, return empty list
        boolean isBoundarySearch = (criteria.getState() != null && !criteria.getState().isEmpty())
                || (criteria.getDistrict() != null && !criteria.getDistrict().isEmpty())
                || (criteria.getBlock() != null && !criteria.getBlock().isEmpty());
        if (isBoundarySearch && (listFacilityCodes == null || listFacilityCodes.isEmpty())) {
            return Collections.emptyList();
        }
        if(listFacilityCodes !=null && !listFacilityCodes.isEmpty()){
            if(request.getFacilityBulkSearchCriteria().getBoundaryCodes()==null)
                request.getFacilityBulkSearchCriteria().setBoundaryCodes(new ArrayList<>());

            // Remove any facility code duplicates
            List<String> uniqueListFacilityCodes = new ArrayList<>(new LinkedHashSet<>(listFacilityCodes));
            request.getFacilityBulkSearchCriteria().getBoundaryCodes().addAll(uniqueListFacilityCodes);
        }

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );

        StringBuilder query = new StringBuilder(
                "SELECT fac.*, " +
                        "fa.latitude AS latitude, " +
                        "fa.longitude AS longitude, " +
                        "fa.addressLine1 AS addressLine1, " +
                        "fa.addressLine2 AS addressLine2, " +
                        "fa.city AS city, " +
                        "fa.pincode AS pincode, " +
                        "fa.landmark AS landmark, " +
                        "(SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r " +
                        "WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive " +
                        "FROM facility fac");
        query.append(" LEFT JOIN facility_address fa ON fac.addressid = fa.id ");
        query.append(result.getWhereClause());
        query.append(" ORDER BY updated_at DESC NULLS LAST ");

        List<Object> allParams = new ArrayList<>(result.getParams());
        if (!Boolean.TRUE.equals(request.getFacilityBulkSearchCriteria().getSendNonPaginatedResponse())) {
            query.append(" LIMIT ? OFFSET ?");
            allParams.add(request.getFacilityBulkSearchCriteria().getLimit());
            allParams.add(request.getFacilityBulkSearchCriteria().getOffset());
        }

        log.info("Bulk Search Query: {}", query);
        log.info("Bulk Search Params count: {}", allParams.size());
        List<Facility> facilityList = jdbcTemplate.query(query.toString(), facilityRowMapperV2, allParams.toArray());
        for (Facility facility : facilityList) {
            if (facility.getFacilityPocPhone() != null && !facility.getFacilityPocPhone().isEmpty()) {
                try {
                    String decryptedMobileNumber = decryptMobileNumber(facility.getFacilityPocPhone());
                    if (decryptedMobileNumber != null && !decryptedMobileNumber.isBlank()) {
                        facility.setFacilityPocPhone(decryptedMobileNumber);
                    }
                } catch (Exception e) {
                    log.trace("Decrypt POC phone skipped for facility {}", facility.getFacilityId());
                }
            }
        }
        return facilityList;
    }

    private void enrichFacilitiesWithBoundaries(List<Facility> facilityList, Map<String, Boundary> listBlock) {
        for (Facility facility : facilityList) {
            String boundaryCode = facility.getBoundaryCode();
            if (boundaryCode != null && listBlock != null) {
                Boundary boundary = listBlock.get(boundaryCode);
                if (boundary != null) {
                    log.debug("✨ Enriching facility={} with state={}, district={} and block={}", facility.getFacilityId(), boundary.getState(), boundary.getDistrict(), boundary.getBlock());
                    boundary.setCode(boundaryCode);
                    facility.setBoundary(boundary);
                } else {
                    log.warn("⚠️ No boundary found for code={} in facility boundary={}", boundaryCode, facility.getFacilityId());
                }
            }
        }
    }


    /**
     * Fetches a one-line summary of a facility using its ID.
     * If not found, returns null.
     *
     * @param facilityId unique ID of the facility
     * @return a FacilitySummary object
     */
    public FacilitySummary getFacilitySummary(String facilityId) {
        log.trace("Entering getFacilitySummary method for facility: {}", facilityId);
        String sql = "SELECT facility_name, facility_type FROM facility WHERE facility_id = ?";
        try {
            FacilitySummary summary = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String name = rs.getString("facility_name");
                String type = rs.getString("facility_type");
                FacilitySummary result = new FacilitySummary();
                result.setSummary("Facility '" + name + "' is of type '" + type + "'.");
                return result;
            }, facilityId);
            log.debug("Retrieved facility summary for facility: {}", facilityId);
            log.trace("Exiting getFacilitySummary method");
            return summary;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Facility summary not found for facility: {}", facilityId);
            return null;
        }
    }

    public int countFacilities(FacilitySearchRequest request) {
        log.trace("Entering countFacilities method");
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
        int count = jdbcTemplate.queryForObject(query, Integer.class, result.getParams().toArray());
        log.debug("Facility count: {}", count);
        log.trace("Exiting countFacilities method");
        return count;
    }

    public int countFacilitiesForBulkSearch(FacilityBulkSearchRequest request) {
        log.trace("Entering countFacilitiesForBulkSearch method");
        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );
        String query = "SELECT COUNT(*) FROM facility fac" + result.getWhereClause();
        int count = jdbcTemplate.queryForObject(query, Integer.class, result.getParams().toArray());
        log.debug("Bulk search facility count: {}", count);
        log.trace("Exiting countFacilitiesForBulkSearch method");
        return count;
    }

    /**
     * Sanitizes a string value for safe logging by removing control characters
     * that could be used for log injection attacks (newlines, carriage returns).
     *
     * @param value The string value to sanitize
     * @return null if input is null, otherwise the sanitized string with \r and \n replaced by spaces
     */
    private String sanitizeForLog(String value) {
        log.trace("Entering sanitizeForLog method");
        if (value == null) {
            log.trace("Exiting sanitizeForLog method, input was null");
            return null;
        }
        String result = value.replace('\r', ' ').replace('\n', ' ');
        log.trace("Exiting sanitizeForLog method");
        return result;
    }
    public void migrateFacilityData() {
        StringBuilder query = new StringBuilder(
                "SELECT fac.*, " +
                        "(SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r " +
                        "WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive " +
                        "FROM facility fac"
        );
        List<Object> allParams = new ArrayList<>();
        List<Facility> facilities = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        for (Facility facilityDB : facilities){
            log.info("Before HF to migrate : {}", facilityDB);
            FacilityUpdateRequestFacilityUpdate facility = new FacilityUpdateRequestFacilityUpdate();
            facility.setFacilityId(facilityDB.getFacilityId());
            facility.setTenantId(facilityDB.getTenantId());
            facility.setFacilityType(facilityDB.getFacilityType());
            facility.setFacilitySubtype(facilityDB.getFacilitySubtype());
            facility.setFacilityName(facilityDB.getFacilityName());
            facility.setAddress(facilityDB.getAddress());
            facility.setAdditionalDetails(facilityDB.getAdditionalDetails());
            facility.setBoundaryCode(facilityDB.getBoundaryCode());
            HealthFacilityDetails details = facilityDB.getFacilityDetails();
            if (details != null) {
                if (facilityDB.getFacilityDetails().getPocName() != null
                        && !facilityDB.getFacilityDetails().getPocName().isBlank()) {
                    facility.setPocName(facilityDB.getFacilityDetails().getPocName());
                }
                if (facilityDB.getFacilityDetails().getPocEmail() != null
                        && !facilityDB.getFacilityDetails().getPocEmail().isBlank()) {
                    facility.setPocEmail(facilityDB.getFacilityDetails().getPocEmail());
                }
                if (facilityDB.getFacilityDetails().getHfrId() != null
                        && !facilityDB.getFacilityDetails().getHfrId().isBlank()) {
                    facility.setHfrId(facilityDB.getFacilityDetails().getHfrId());
                }
                if (facilityDB.getFacilityDetails().getNinId() != null
                        && !facilityDB.getFacilityDetails().getNinId().isBlank()) {
                    facility.setNinId(facilityDB.getFacilityDetails().getNinId());
                }
            }
            facility.setStatus("ACTIVE");
            facility.setUserId(facilityDB.getUserId());
            facility.setIsOnmReady(facilityDB.getIsOnmReady());

            if(facilityDB.getFacilityDetails()!=null && facilityDB.getFacilityDetails().getPocContact()!=null && !facilityDB.getFacilityDetails().getPocContact().isBlank()){
                String encryptedMobileNumber = encryptMobileNumber(facilityDB.getFacilityDetails().getPocContact());
                if (encryptedMobileNumber!=null && !encryptedMobileNumber.isBlank()){
                    log.info("mobile number {} encrypted to : {}", facilityDB.getFacilityDetails().getPocContact(), encryptedMobileNumber);
                    facility.setPocContact(encryptedMobileNumber);
                }
            }

            if (details != null) {
                details.setPocName(null);
                details.setPocContact(null);
                details.setPocEmail(null);
                details.setNinId(null);
                details.setHfrId(null);
                facility.setFacilityDetails(details);
            }

            FacilityUpdateRequest request = FacilityUpdateRequest.builder()
                    .facilityUpdate(facility)
                    .build();
            log.info("Final HF to migrate : {}", request.getFacilityUpdate());
            facilityRepository.pushUpdateFacility(request);
        }
    }

    public String encryptMobileNumber(String mobileNumber){
        String encryptedMobileNumber = null;
        if(mobileNumber!=null && !mobileNumber.isBlank()){
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            EncReqObject encReqObject = EncReqObject.builder()
                    .tenantId(configs.getEncServiceTenantId())
                    .type("Normal")
                    .value(userMap)
                    .build();
            EncryptionRequest encryptionRequest = EncryptionRequest.builder()
                    .encryptionRequests(List.of(encReqObject))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.encryptObject(encryptionRequest);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject"); // clé du JSON
                if (user != null) {
                    log.info("Mobile crypté : {}", user.getMobileNumber());
                    encryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return encryptedMobileNumber;
    }

    public String decryptMobileNumber(String mobileNumber){
        String decryptedMobileNumber = null;
        if(mobileNumber!=null && !mobileNumber.isBlank()){
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            DecryptionRequest request = DecryptionRequest.builder()
                    .decryptionRequests(List.of(userMap))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.decryptObject(request);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject"); // clé du JSON
                if (user != null) {
                    log.info("Mobile decrypté : {}", user.getMobileNumber());
                    decryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return decryptedMobileNumber;
    }

    public boolean checkPOCDetailsUpdated(Facility existingFacilityDetails, Facility requestFacilityDetails) {
        boolean isOnmReady = requestFacilityDetails.getIsOnmReady();
        boolean pocDetailsUpdated = (!Objects.equals(existingFacilityDetails.getFacilityPocPhone(), requestFacilityDetails.getFacilityPocPhone()) ||
                !Objects.equals(existingFacilityDetails.getFacilityPocName(), requestFacilityDetails.getFacilityPocName()) ||
                !Objects.equals(existingFacilityDetails.getFacilityPocEmail(), requestFacilityDetails.getFacilityPocEmail()));
        return isOnmReady && pocDetailsUpdated;
    }

    public void updatedHRMSUser(FacilityUpdateRequest request, Facility existingFacilityDetails, Facility requestFacilityDetails){
        String normalizedCategory = existingFacilityDetails.getFacilityCategory() == null
                ? ""
                : existingFacilityDetails.getFacilityCategory().trim().toUpperCase(Locale.ROOT);
        boolean isAnganwadi = CATEGORY_ANGANWADI.equals(normalizedCategory);
        String username;
        if (isAnganwadi) {
            username = existingFacilityDetails.getFacilityPocUsername() !=null && !existingFacilityDetails.getFacilityPocUsername().trim().isBlank() ?
                    existingFacilityDetails.getFacilityPocUsername().trim() : "";
        } else {
            username = existingFacilityDetails.getHfrId() != null && !existingFacilityDetails.getHfrId().trim().isBlank()
                    ? existingFacilityDetails.getHfrId().trim()
                    : existingFacilityDetails.getNinId();
        }

        if(username!=null && !username.isEmpty()){
            Employee employee = hrmsUtils.getUserByUsername(request, username);
            if (employee != null) {
                User existingUser = employee.getUser();
                existingUser.setName(requestFacilityDetails.getFacilityPocName());
                existingUser.setMobileNumber(requestFacilityDetails.getFacilityPocPhone());
                existingUser.setEmailId(requestFacilityDetails.getFacilityPocEmail());
                EmployeeRequest employeeRequest = EmployeeRequest.builder().requestInfo(request.getRequestInfo()).employees(List.of(employee)).build();
                List<Employee> updatedEmployees = hrmsUtils.updateHRMSUser(employeeRequest);
                if (updatedEmployees != null && !updatedEmployees.isEmpty()) {
                    // User updated successfully
                    Employee employeeResp = updatedEmployees.get(0);
                    log.info("User with userId {} updated successfully", existingFacilityDetails.getUserId());
                }
            }
        }
    }

    private void validateFacilityEditAuthorization(RequestInfo requestInfo) {
        var userInfo = requestInfo != null ? requestInfo.getUserInfo() : null;
        if (userInfo == null || userInfo.getRoles() == null) {
            throw new IllegalArgumentException("Only FACILITY_ADMIN or SYSTEM_USER roles can edit facilities");
        }

        boolean isFacilityAdmin = userInfo.getRoles().stream()
                .anyMatch(role -> FACILITY_ADMIN.equalsIgnoreCase(role.getCode()));
        boolean isSystemUser = userInfo.getRoles().stream()
                .anyMatch(role -> SYSTEM_USER.equalsIgnoreCase(role.getCode()));
        if (!isFacilityAdmin && !isSystemUser) {
            throw new IllegalArgumentException("Only FACILITY_ADMIN or SYSTEM_USER roles can edit facilities");
        }
    }

    private Facility getFacilityFromDb(String facilityId, String tenantId) {
        String fetchFullFacilitySql = "SELECT fac.*, " +
                " (SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive " +
                " FROM facility fac WHERE fac.id = ? AND fac.tenant_id = ?";
        try {
            return jdbcTemplate.queryForObject(fetchFullFacilitySql, new Object[]{facilityId, tenantId}, facilityRowMapper.rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void ensureFacilityBoundaryExists(String facilityBoundaryCode, String parentBlockBoundaryCode, String tenantId, RequestInfo requestInfo) {
        boolean boundaryExists = false;
        try {
            boundaryValidator.validateBoundaries(Set.of(facilityBoundaryCode), tenantId, requestInfo);
            boundaryExists = true;
        } catch (Exception exception) {
            log.info("Facility boundary code {} not found, creating it under parent {}", facilityBoundaryCode, parentBlockBoundaryCode);
        }

        if (!boundaryExists) {
            BoundaryCreateRequest boundaryCreateRequest = BoundaryCreateRequest.builder()
                    .requestInfo(requestInfo)
                    .boundary(List.of(
                            Boundary.builder()
                                    .tenantId(tenantId)
                                    .code(facilityBoundaryCode)
                                    .build()
                    ))
                    .build();
            boundaryService.createBoundaries(boundaryCreateRequest);
        }

        BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                .requestInfo(requestInfo)
                .boundaryRelationship(
                        BoundaryRelation.builder()
                                .tenantId(tenantId)
                                .boundaryType("Facility")
                                .code(facilityBoundaryCode)
                                .parent(parentBlockBoundaryCode)
                                .hierarchyType("SELCO")
                                .build()
                )
                .build();
        try {
            boundaryService.createBoundaryRelationship(boundaryRelationshipRequest);
        } catch (Exception e) {
            String errMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errMsg.contains("DUPLICATE_RECORD")) {
                log.info("Boundary relationship already exists for code {}. Skipping create.", facilityBoundaryCode);
                return;
            }
            throw e;
        }
    }

    private void cleanupOldFacilityBoundaryIfUnused(String oldFacilityBoundaryCode, String tenantId, RequestInfo requestInfo) {
        try {
            if (oldFacilityBoundaryCode == null || oldFacilityBoundaryCode.isBlank()) {
                return;
            }
            Integer usageCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM facility WHERE tenant_id = ? AND boundary_code = ?",
                    Integer.class,
                    tenantId,
                    oldFacilityBoundaryCode
            );
            if (usageCount != null && usageCount > 0) {
                log.info("Skipping old facility boundary cleanup for code {} because {} facilities still reference it",
                        oldFacilityBoundaryCode, usageCount);
                return;
            }

            log.info("No facilities reference old boundary code {}. Deleting relationship and boundary entity", oldFacilityBoundaryCode);
            BoundaryRelationshipRequest deleteRelationshipRequest = BoundaryRelationshipRequest.builder()
                    .requestInfo(requestInfo)
                    .boundaryRelationship(
                            BoundaryRelation.builder()
                                    .tenantId(tenantId)
                                    .hierarchyType("SELCO")
                                    .boundaryType("Facility")
                                    .code(oldFacilityBoundaryCode)
                                    .build()
                    )
                    .build();
            boundaryService.deleteBoundaryRelationship(deleteRelationshipRequest);

            BoundaryCreateRequest deleteBoundaryRequest = BoundaryCreateRequest.builder()
                    .requestInfo(requestInfo)
                    .boundary(List.of(
                            Boundary.builder()
                                    .tenantId(tenantId)
                                    .code(oldFacilityBoundaryCode)
                                    .build()
                    ))
                    .build();
            boundaryService.deleteBoundaries(deleteBoundaryRequest);
        } catch (Exception e) {
            log.info("Skipping old boundary cleanup for code {} due to exception: {}", oldFacilityBoundaryCode, e.getMessage(), e);
        }
    }

    /**
     * Notifies im-services to set {@code eg_incident_v2.boundarycode} for all incidents with the given facility id.
     */
    private void syncImIncidentBoundaryCodesForFacility(String tenantId, String facilityId, String newBoundaryCode,
                                                        String newBlockBoundaryCode, RequestInfo requestInfo) {
        String host = configs.getImServicesHost();
        String path = configs.getImIncidentBoundaryByFacilityUpdatePath();
        if (host == null || host.isBlank()) {
            log.warn("egov.im.services.host is not configured; skipping IM incident boundary sync for facility {}", facilityId);
            return;
        }
        if (path == null || path.isBlank()) {
            log.warn("egov.im.services.incident.boundary-by-facility.path is blank; skipping IM incident boundary sync for facility {}", facilityId);
            return;
        }

        String url = UriComponentsBuilder.fromUriString(host.trim()).path(path).toUriString();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("RequestInfo", requestInfo);
        payload.put("tenant_id", tenantId);
        payload.put("facility_id", facilityId);
        payload.put("new_boundary_code", newBoundaryCode);
        payload.put("new_block_code", deriveBlockFromBoundaryCode(newBlockBoundaryCode));

        try {
            restTemplate.postForObject(url, payload, Map.class);
            log.info("IM incident boundary sync completed for facilityId={}, tenantId={}", facilityId, tenantId);
        } catch (Exception e) {
            log.error("IM incident boundary sync failed for facilityId={}: {}", facilityId, e.getMessage(), e);
            throw new CustomException(
                    "INCIDENT_BOUNDARY_SYNC_FAILED",
                    "Facility boundary was updated but incident boundary sync to im-services failed: " + e.getMessage()
            );
        }
    }

    private String deriveBlockFromBoundaryCode(String newBlockBoundaryCode) {
        if (newBlockBoundaryCode == null || newBlockBoundaryCode.isBlank()) {
            return newBlockBoundaryCode;
        }
        String[] segments = newBlockBoundaryCode.split("_");
        return segments[segments.length - 1];
    }

}
