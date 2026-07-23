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
    public static final String CATEGORY_ANGANWADI = "ANGANWADI";
    /** When category is HEALTH, MDMS-style rule: at least one of HFR ID or NIN ID must be present. */
    private static final String ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH =
            "When Facility Category is HEALTH, at least one of HFR ID or NIN ID is required.";
    private static final String ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI =
            "PoC Username is required when Facility Category is ANGANWADI.";

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
    private final FacilityProjectClient facilityProjectClient;
    private EncryptionDecryptionUtil encryptionDecryptionUtil;
    private BoundaryUtil boundaryUtil;

    private FacilityRowMapperV2 facilityRowMapperV2;

    private final HRMSUtils hrmsUtils;
    private final HRMSService hrmsService;
    private final VendorOrganisationService vendorOrganisationService;
    private final RestTemplate restTemplate;

    private static final String LOCALIZATION_MODULE = "rainmaker-in";
    private static final String LOCALIZATION_LOCALE = "en_IN";
    // Existing boundary localization upsert uses tenantId="in" (see ingestion-service / im-service migrations)
    private static final String LOCALIZATION_TENANT_ID = "in";
    /** Tenant used for boundary entity and boundary_relationship (all facilities). */
    private static final String BOUNDARY_TENANT_ID = "in";

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
            FacilityProjectClient facilityProjectClient,
            EncryptionDecryptionUtil encryptionDecryptionUtil,
            BoundaryUtil boundaryUtil,
            FacilityRowMapperV2 facilityRowMapperV2,
            HRMSUtils hrmsUtils,
            HRMSService hrmsService,
            VendorOrganisationService vendorOrganisationService,
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
        this.facilityProjectClient = facilityProjectClient;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.boundaryUtil = boundaryUtil;
        this.facilityRowMapperV2 = facilityRowMapperV2;
        this.hrmsUtils = hrmsUtils;
        this.hrmsService = hrmsService;
        this.vendorOrganisationService = vendorOrganisationService;
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

        // Normalize facility names (trim + collapse multiple spaces) before any validation
        if (facilities != null) {
            for (FacilityCreate fc : facilities) {
                if (fc != null && fc.getFacilityName() != null) {
                    fc.setFacilityName(
                            fc.getFacilityName()
                                    .trim()
                                    .replaceAll("\\s+", " ")
                    );
                }
            }
        }

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
                        .facilityName(facilityCreate.getFacilityName()!=null ? facilityCreate.getFacilityName().trim() : facilityCreate.getFacilityName())
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
                        .solarInstallationDate(facilityCreate.getSolarInstallationDate())
                        .rmsInstallationDate(facilityCreate.getRmsInstallationDate())
                        .solarSystemCapacityKwp(facilityCreate.getSolarSystemCapacityKwp())
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

                validateFacilityPocUsernameUnique(
                        facility.getFacilityCategory(), facility.getFacilityPocUsername(), tenantId, null
                );

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
                    // Create POC user if not exists (check by phone number uniqueness). Swallow
                    // failures so a transient HRMS/user-service outage doesn't fail the whole
                    // create request after the facility itself has already been persisted.
                    try {
                        createFacilityPOCUserIfNotExists(facility, tenantId, request.getRequestInfo(), originalPocMobileNumber);
                    } catch (Exception e) {
                        log.error("Error syncing POC user for facility {}: {}",
                                sanitizeForLog(facility.getFacilityId()), e.getMessage(), e);
                    }

                    // Push to Kibana for indexing
                    FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                    facilityRepository.pushToKibana(kibanaIndex);
                }

                validatedFacilities.add(facility);
            }

            if (!Boolean.TRUE.equals(request.getSkipVendorJurisdictionAssignment())) {
                vendorOrganisationService.assignFacilityJurisdictionsBulk(
                        tenantFacilities, tenantId, request.getRequestInfo());
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

    /**
     * Validates facility POC username: required for ANGANWADI, unique within tenant when provided.
     * Throws CustomException if duplicate found.
     */
    private void validateFacilityPocUsernameUnique(
            String facilityCategory, String facilityPocUsername, String tenantId, String excludeFacilityId
    ) {
        log.trace("Entering validateFacilityPocUsernameUnique method");
        String normalizedCategory = facilityCategory == null ? "" : facilityCategory.trim().toUpperCase(Locale.ROOT);
        if (CATEGORY_ANGANWADI.equals(normalizedCategory)
                && (facilityPocUsername == null || facilityPocUsername.isBlank())) {
            log.warn("Missing facility POC username for ANGANWADI facility in tenant {}", tenantId);
            throw new IllegalArgumentException(ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI);
        }
        if (facilityPocUsername != null && !facilityPocUsername.isBlank()) {
            String normalizedUsername = facilityPocUsername.trim();
            log.debug("Checking uniqueness of facility POC username for tenant {}", tenantId);
            boolean exists = facilityQueryDao.existsByFacilityPocUsername(
                    tenantId, normalizedUsername, excludeFacilityId
            );
            if (exists) {
                log.warn("Duplicate facility POC username found for tenant {}", tenantId);
                throw new CustomException("FACILITY_DUPLICATE_POC_USERNAME",
                        "A facility with the same POC username already exists in this tenant");
            }
            log.debug("Facility POC username is unique");
        }
        log.trace("Exiting validateFacilityPocUsernameUnique method");
    }

    private String extractVendorCode(Facility facility) {
        if (facility.getFacilityDetails() == null) {
            return null;
        }
        String vendorCode = facility.getFacilityDetails().getVendorCode();
        return vendorCode != null ? vendorCode.trim() : null;
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

        // Find the (at most one, first match taken) employee - active or inactive - assigned to
        // this facility's boundary. Search is boundary-based (not by identifier) so it's resilient
        // to the facility's stored HFR/NIN/POC username having drifted out of date, and so an
        // existing-but-inactive POC employee is found (and reactivated) rather than duplicated.
        String boundaryCode = facility.getBoundaryCode();
        if (boundaryCode == null || boundaryCode.isBlank()) {
            log.warn("Cannot sync POC user for facility {}: missing boundary code", sanitizeForLog(facility.getFacilityId()));
            return;
        }
        Employee matchedEmployee = hrmsUtils.getEmployeeByBoundaryCode(requestInfo, boundaryCode);

        if (isAnganwadi) {
            String pocUsername = facility.getFacilityPocUsername().trim();
            if (matchedEmployee == null) {
                boolean created = hrmsService.createFacilityPOCEmployee(facility, requestInfo);
                if (created) {
                    log.info("Successfully created POC user for facility {} with username {}",
                            sanitizeForLog(facility.getFacilityId()), sanitizeForLog(pocUsername));
                } else {
                    log.warn("Failed to create POC user for facility {}", sanitizeForLog(facility.getFacilityId()));
                }
                return;
            }
            String matchedCode = matchedEmployee.getCode() == null ? "" : matchedEmployee.getCode().trim();
            if (pocUsername.equals(matchedCode)) {
                if (isPocEmployeeInactive(matchedEmployee)) {
                    log.info("POC user {} matches for facility {} but is inactive; reactivating",
                            sanitizeForLog(pocUsername), sanitizeForLog(facility.getFacilityId()));
                    activatePocUserAndResetPassword(matchedEmployee, pocUsername, facility.getFacilityId(), requestInfo);
                } else {
                    log.info("POC user {} already in sync and active for facility {}, nothing to update",
                            sanitizeForLog(pocUsername), sanitizeForLog(facility.getFacilityId()));
                }
                return;
            }
            renamePocEmployeeAndSyncFacility(matchedEmployee, pocUsername, facility, tenantId, requestInfo, true, false);
            activatePocUserAndResetPassword(matchedEmployee, pocUsername, facility.getFacilityId(), requestInfo);
        } else {
            String hfr = facilityDetails.getHfrId();
            String nin = facilityDetails.getNinId();
            boolean hasHfr = hfr != null && !hfr.isBlank();
            boolean hasNin = nin != null && !nin.isBlank();
            String resolvedId = hasHfr ? hfr.trim() : (hasNin ? nin.trim() : null);
            if (resolvedId == null) {
                log.warn("Cannot sync POC user for facility {}: both HFR and NIN ID are missing",
                        sanitizeForLog(facility.getFacilityId()));
                return;
            }

            if (matchedEmployee == null) {
                boolean created = hrmsService.createFacilityPOCEmployee(facility, requestInfo);
                if (created) {
                    log.info("Successfully created POC user for facility {} with username {}",
                            sanitizeForLog(facility.getFacilityId()), sanitizeForLog(resolvedId));
                } else {
                    log.warn("Failed to create POC user for facility {}", sanitizeForLog(facility.getFacilityId()));
                }
                return;
            }

            String matchedCode = matchedEmployee.getCode() == null ? "" : matchedEmployee.getCode().trim();
            boolean matchesNin = hasNin && matchedCode.equals(nin.trim());
            boolean matchesHfr = hasHfr && matchedCode.equals(hfr.trim());
            if (matchesNin || matchesHfr) {
                if (isPocEmployeeInactive(matchedEmployee)) {
                    log.info("POC user {} matches for facility {} but is inactive; reactivating",
                            sanitizeForLog(matchedCode), sanitizeForLog(facility.getFacilityId()));
                    activatePocUserAndResetPassword(matchedEmployee, matchedCode, facility.getFacilityId(), requestInfo);
                } else {
                    log.info("POC user {} already in sync and active for facility {}, nothing to update",
                            sanitizeForLog(matchedCode), sanitizeForLog(facility.getFacilityId()));
                }
                return;
            }
            renamePocEmployeeAndSyncFacility(matchedEmployee, resolvedId, facility, tenantId, requestInfo, false, hasHfr);
            activatePocUserAndResetPassword(matchedEmployee, resolvedId, facility.getFacilityId(), requestInfo);
        }
    }

    /**
     * True when the employee, its user record, or its employeeStatus indicate an inactive POC.
     */
    private boolean isPocEmployeeInactive(Employee employee) {
        boolean employeeInactive = Boolean.FALSE.equals(employee.getIsActive());
        boolean userInactive = employee.getUser() == null
                || employee.getUser().getActive() == null || !employee.getUser().getActive();
        boolean statusInactive = "INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
        return employeeInactive || userInactive || statusInactive;
    }

    /**
     * Ensures an existing POC user is active (re-activating it in HRMS when needed) and resets its
     * credentials to the configured default password. Invoked when a facility is marked ONM ready
     * and a POC user with the resolved HFR/NIN already exists.
     */
    private void activatePocUserAndResetPassword(Employee employee, String username, String facilityId,
                                                 RequestInfo requestInfo) {
        try {
            if (isPocEmployeeInactive(employee)) {
                employee.getUser().setActive(true);
                employee.setIsActive(true);
                employee.setEmployeeStatus("EMPLOYED");
                employee.setReActivateEmployee(true);

                EmployeeRequest employeeRequest = EmployeeRequest.builder()
                        .requestInfo(requestInfo)
                        .employees(List.of(employee))
                        .build();
                List<Employee> updatedEmployees = hrmsUtils.updateHRMSUser(employeeRequest);
                if (updatedEmployees != null && !updatedEmployees.isEmpty()) {
                    log.info("Activated existing HRMS POC user {} for facility {}",
                            sanitizeForLog(username), sanitizeForLog(facilityId));
                } else {
                    log.warn("Failed to activate existing HRMS POC user {} for facility {}",
                            sanitizeForLog(username), sanitizeForLog(facilityId));
                }
            } else {
                log.info("Existing HRMS POC user {} is already active for facility {}",
                        sanitizeForLog(username), sanitizeForLog(facilityId));
            }

            // Always reset to the default password so the POC lands on known credentials once ONM ready
            hrmsService.resetUserPasswordToDefault(employee.getUser(), requestInfo);
        } catch (Exception e) {
            log.error("Error activating/resetting password for HRMS POC user {} for facility {}: {}",
                    sanitizeForLog(username), sanitizeForLog(facilityId), e.getMessage(), e);
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

        // Normalize facility name (trim + collapse multiple spaces) before mapping
        if (update.getFacilityName() != null) {
            update.setFacilityName(
                    update.getFacilityName()
                            .trim()
                            .replaceAll("\\s+", " ")
            );
        }

        Facility facility = new Facility();
        facility.setFacilityId(update.getFacilityId());
        facility.setTenantId(update.getTenantId());
        facility.setFacilityCategory(update.getFacilityCategory());
        facility.setFacilityType(update.getFacilityType());
        facility.setFacilitySubtype(update.getFacilitySubtype());
        facility.setFacilityName(update.getFacilityName()!=null ? update.getFacilityName().trim() : update.getFacilityName());
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
        facility.setSolarInstallationDate(
                update.getSolarInstallationDate() != null ? update.getSolarInstallationDate() : existingFacility.getSolarInstallationDate());
        facility.setRmsInstallationDate(
                update.getRmsInstallationDate() != null ? update.getRmsInstallationDate() : existingFacility.getRmsInstallationDate());
        facility.setSolarSystemCapacityKwp(
                update.getSolarSystemCapacityKwp() != null ? update.getSolarSystemCapacityKwp() : existingFacility.getSolarSystemCapacityKwp());

        // Preserve CO2 fields on partial update payloads sent to persister
        if (update.getSolarInstallationDate() == null) {
            update.setSolarInstallationDate(existingFacility.getSolarInstallationDate());
        }
        if (update.getRmsInstallationDate() == null) {
            update.setRmsInstallationDate(existingFacility.getRmsInstallationDate());
        }
        if (update.getSolarSystemCapacityKwp() == null) {
            update.setSolarSystemCapacityKwp(existingFacility.getSolarSystemCapacityKwp());
        }

        FacilityMappedVendorHelper.mergeMappedVendorFromUpdate(facility, update, existingFacility);
        FacilityMappedVendorHelper.syncToAdditionalDetails(facility);
        update.setAdditionalDetails(facility.getAdditionalDetails());
        update.setMappedVendorName(facility.getMappedVendorName());
        update.setMappedVendorUserName(facility.getMappedVendorUserName());

        String effectiveCategory = firstNonBlank(update.getFacilityCategory(), existingFacility.getFacilityCategory());
        String effectiveHfrId = firstNonBlank(update.getHfrId(), existingFacility.getHfrId());
        String effectiveNinId = firstNonBlank(update.getNinId(), existingFacility.getNinId());
        validateCategoryBasedIdentifiers(effectiveCategory, effectiveHfrId, effectiveNinId);

        validateFacilityPocUsernameUnique(
                effectiveCategory, facility.getFacilityPocUsername(), update.getTenantId(), update.getFacilityId()
        );

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
        // Keep the persisted payload in sync with the resolved is_active so an update
        // without isActive does not overwrite the existing value with null.
        update.setIsActive(facility.getIsActive());

        // If the facility is being deactivated (is_active=false), it can no longer be ONM ready.
        // Force is_onm_ready=false so it is persisted, and so the update flows through the
        // non ONM-ready branch (POC/complainant deactivation + Kibana index deletion).
        if (Boolean.FALSE.equals(facility.getIsActive())) {
            log.info("Facility {} is being deactivated (is_active=false); forcing is_onm_ready=false",
                    sanitizeForLog(update.getFacilityId()));
            facility.setIsOnmReady(false);
            update.setIsOnmReady(false);
        }

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
        boolean mappedVendorUpdated = FacilityMappedVendorHelper.hasMappedVendorUpdateInPayload(update);
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
                    .isActive(facility.getIsActive() != null ? facility.getIsActive() : existingFacility.getIsActive())
                    .facilityStatus(facility.getFacilityStatus() != null ? facility.getFacilityStatus() : existingFacility.getFacilityStatus())
                    .build();

            try{
                String decryptedMobileNumber = decryptMobileNumber(facilityForProcessing.getFacilityPocPhone());
                if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                    facilityForProcessing.setFacilityPocPhone(decryptedMobileNumber);
                }
            }
            catch(Exception e){}

            // Always check/create POC user when isOnmReady is true (whether transitioning or already true)
            // This ensures POC user is created if missing, even if facility was already ONM ready.
            // Swallow failures so a transient HRMS/user-service outage doesn't fail the whole
            // update request after the facility itself has already been persisted.
            try {
                createFacilityPOCUserIfNotExists(
                        facilityForProcessing,
                        update.getTenantId(),
                        request.getRequestInfo(),
                        facilityForProcessing.getFacilityPocPhone()
                );
            } catch (Exception e) {
                log.error("Error syncing POC user for facility {}: {}",
                        sanitizeForLog(update.getFacilityId()), e.getMessage(), e);
            }

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
                    .mappedVendorName(facility.getMappedVendorName())
                    .mappedVendorUserName(facility.getMappedVendorUserName())
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
        } else if (Boolean.FALSE.equals(update.getIsOnmReady())) {
            // Facility marked as non ONM-ready: deactivate the HRMS POC user (if any) and
            // remove the facility document from the Kibana/Elasticsearch index (if present).
            log.info("Facility {} marked as non ONM-ready, deactivating POC user and removing Kibana index",
                    sanitizeForLog(update.getFacilityId()));
            deactivateFacilityPOCUser(request, existingFacility);
            facilityKibanaMapper.deleteKibanaIndexByFacilityId(update.getFacilityId(), update.getTenantId());
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

        boundaryValidator.validateBoundaries(
                Set.of(blockUpdate.getNewBlockBoundaryCode()),
                blockUpdate.getTenantId(),
                request.getRequestInfo()
        );

        Facility updated = applyFacilityBlockBoundaryChange(
                existingFacility,
                blockUpdate.getFacilityId(),
                blockUpdate.getTenantId(),
                blockUpdate.getNewBlockBoundaryCode(),
                request.getRequestInfo()
        );
        log.trace("Exiting updateFacilityBlockBoundary method");
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public Facility updateFacilityDistrictBoundary(FacilityDistrictUpdateRequest request) {
        log.trace("Entering updateFacilityDistrictBoundary method");
        FacilityDistrictUpdate districtUpdate = request.getFacilityDistrictUpdate();
        if (districtUpdate == null) {
            throw new IllegalArgumentException("FacilityDistrictUpdate payload is required");
        }
        if (districtUpdate.getFacilityId() == null || districtUpdate.getFacilityId().isBlank()
                || districtUpdate.getTenantId() == null || districtUpdate.getTenantId().isBlank()
                || districtUpdate.getNewDistrictBoundaryCode() == null || districtUpdate.getNewDistrictBoundaryCode().isBlank()
                || districtUpdate.getNewBlockBoundaryCode() == null || districtUpdate.getNewBlockBoundaryCode().isBlank()) {
            throw new IllegalArgumentException("facility_id, tenant_id, new_district_boundary_code and new_block_boundary_code must be provided");
        }

        String districtPrefix = districtUpdate.getNewDistrictBoundaryCode() + "_";
        if (!districtUpdate.getNewBlockBoundaryCode().startsWith(districtPrefix)) {
            throw new IllegalArgumentException(
                    "new_block_boundary_code must belong to new_district_boundary_code (expected prefix: " + districtPrefix + ")"
            );
        }

        validateFacilityEditAuthorization(request.getRequestInfo());

        Facility existingFacility = getFacilityFromDb(districtUpdate.getFacilityId(), districtUpdate.getTenantId());
        if (existingFacility == null) {
            log.warn("Facility {} not found for tenant {}", districtUpdate.getFacilityId(), districtUpdate.getTenantId());
            return null;
        }

        boundaryValidator.validateBoundaries(
                Set.of(districtUpdate.getNewDistrictBoundaryCode(), districtUpdate.getNewBlockBoundaryCode()),
                districtUpdate.getTenantId(),
                request.getRequestInfo()
        );

        Facility updated = applyFacilityBlockBoundaryChange(
                existingFacility,
                districtUpdate.getFacilityId(),
                districtUpdate.getTenantId(),
                districtUpdate.getNewBlockBoundaryCode(),
                request.getRequestInfo()
        );
        log.trace("Exiting updateFacilityDistrictBoundary method");
        return updated;
    }

    private Facility applyFacilityBlockBoundaryChange(
            Facility existingFacility,
            String facilityId,
            String tenantId,
            String newBlockBoundaryCode,
            RequestInfo requestInfo) {
        String oldFacilityBoundaryCode = existingFacility.getBoundaryCode();
        String updatedFacilityBoundaryCode = newBlockBoundaryCode + "_" + facilityId;
        if (updatedFacilityBoundaryCode.equals(existingFacility.getBoundaryCode())) {
            log.info("No boundary update needed for facility {} (boundary code unchanged)", facilityId);
            return existingFacility;
        }

        ensureFacilityBoundaryExists(updatedFacilityBoundaryCode, newBlockBoundaryCode, tenantId, requestInfo);

        int updatedRows = jdbcTemplate.update(
                "UPDATE facility SET boundary_code = ? WHERE id = ? AND tenant_id = ?",
                updatedFacilityBoundaryCode,
                facilityId,
                tenantId
        );
        log.info("{} Rows updated for facility {} and tenant {}", updatedRows, facilityId, tenantId);
        if (updatedRows == 0) {
            log.warn("No rows updated for facility {} and tenant {}", facilityId, tenantId);
            return null;
        }

        existingFacility.setBoundaryCode(updatedFacilityBoundaryCode);
        upsertFacilityBoundaryLocalizations(List.of(existingFacility), requestInfo);
        cleanupOldFacilityBoundaryIfUnused(oldFacilityBoundaryCode, tenantId, requestInfo);
        syncImIncidentBoundaryCodesForFacility(
                tenantId,
                facilityId,
                updatedFacilityBoundaryCode,
                newBlockBoundaryCode,
                requestInfo
        );
        log.info("Updated boundary code for facility {} to {}", facilityId, updatedFacilityBoundaryCode);
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
        query.append(buildBulkSearchOrderBy(criteria));

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
    /**
     * One-off migration: when HFR is absent ({@code NULL} or blank), sets indexer {@code code} via Kafka:
     * {@code nin_id} if present, otherwise {@code facility_poc_username} when both HFR and NIN are absent.
     *
     * @return short summary string for operators
     */
    public String syncKibanaFacilityCodeFromNinWhereHfrMissing() {
        log.info("Starting Kibana code sync for facilities without HFR (nin or poc_username fallback)");
        String sql = "SELECT fac.*, "
                + "(SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r "
                + "WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive "
                + "FROM facility fac "
                + "WHERE (fac.hfr_id IS NULL OR TRIM(fac.hfr_id) = '') "
                + "AND ("
                + "  (fac.nin_id IS NOT NULL AND TRIM(fac.nin_id) <> '') "
                + "  OR ("
                + "    (fac.nin_id IS NULL OR TRIM(fac.nin_id) = '') "
                + "    AND fac.facility_poc_username IS NOT NULL "
                + "    AND TRIM(fac.facility_poc_username) <> ''"
                + "  )"
                + ")";

        RequestInfo migrationRequestInfo = new RequestInfo();
        List<Facility> facilities = jdbcTemplate.query(sql, facilityRowMapper.rowMapper);
        int pushed = 0;
        int failed = 0;
        int fromNin = 0;
        int fromPocUsername = 0;
        for (Facility facility : facilities) {
            try {
                String code = resolveIndexerCodeForHfrMissingMigration(facility);
                if (code == null) {
                    continue;
                }
                if (hasNonBlankTrimmed(facility.getNinId())) {
                    fromNin++;
                } else {
                    fromPocUsername++;
                }
                FacilityKibanaIndex patch = facilityKibanaMapper.toKibanaIndexPatchCode(
                        facility,
                        code,
                        migrationRequestInfo);
                if (patch != null) {
                    facilityRepository.pushToKibana(patch);
                    pushed++;
                    log.debug("Queued Kibana code patch for facilityId {}", sanitizeForLog(facility.getFacilityId()));
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed Kibana code sync for facilityId {} tenantId {}: {}",
                        sanitizeForLog(facility.getFacilityId()),
                        sanitizeForLog(facility.getTenantId()),
                        e.getMessage(),
                        e);
            }
        }
        String summary = String.format(
                "Matched %d facilities (hfr absent; code from nin=%d, poc_username=%d); "
                        + "queued to indexer=%d; errors=%d",
                facilities.size(), fromNin, fromPocUsername, pushed, failed);
        log.info("Completed Kibana code sync: {}", summary);
        return summary;
    }

    /**
     * Operator reindex: rebuilds full Kibana/Elasticsearch payloads (including boundary hierarchy)
     * for existing facilities and pushes them to the indexer topic.
     */
    public FacilityKibanaReindexResponse reindexFacilitiesInKibana(FacilityKibanaReindexRequest request) {
        if (!configs.isFacilityKibanaReindexEnabled()) {
            throw new IllegalArgumentException(
                    "Facility Kibana reindex is disabled. Set facility.kibana.reindex.enabled=true to run."
            );
        }
        if (request == null || request.getRequestInfo() == null) {
            throw new IllegalArgumentException("RequestInfo is required");
        }

        boolean onmReadyOnly = request.getOnmReadyOnly() == null || Boolean.TRUE.equals(request.getOnmReadyOnly());
        List<Facility> facilities = loadFacilitiesForKibanaReindex(
                request.getTenantId(),
                request.getFacilityIds(),
                onmReadyOnly
        );

        log.info("Kibana reindex: tenantId={}, facilityIds={}, onmReadyOnly={}, scanned={}",
                request.getTenantId(),
                request.getFacilityIds() != null ? request.getFacilityIds().size() : 0,
                onmReadyOnly,
                facilities.size());

        FacilityKibanaReindexResponse response = FacilityKibanaReindexResponse.builder()
                .scanned(facilities.size())
                .errors(new ArrayList<>())
                .build();

        for (Facility facility : facilities) {
            if (facility.getBoundaryCode() == null || facility.getBoundaryCode().isBlank()) {
                response.setSkipped(response.getSkipped() + 1);
                log.warn("Skipping Kibana reindex for facility {}: boundary_code is blank", facility.getFacilityId());
                continue;
            }
            try {
                FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                if (kibanaIndex == null) {
                    response.setSkipped(response.getSkipped() + 1);
                    continue;
                }
                facilityRepository.pushToKibana(kibanaIndex);
                response.setReindexed(response.getReindexed() + 1);
                log.debug("Queued Kibana reindex for facilityId {}", sanitizeForLog(facility.getFacilityId()));
            } catch (Exception e) {
                response.setFailed(response.getFailed() + 1);
                String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                log.error("Kibana reindex failed for facilityId {} tenantId {}: {}",
                        sanitizeForLog(facility.getFacilityId()),
                        sanitizeForLog(facility.getTenantId()),
                        message,
                        e);
                response.getErrors().add(FacilityBoundaryBackfillErrorItem.builder()
                        .facilityId(facility.getFacilityId())
                        .tenantId(facility.getTenantId())
                        .boundaryCode(facility.getBoundaryCode())
                        .message(message)
                        .build());
            }
        }

        log.info("Kibana reindex complete: scanned={}, reindexed={}, skipped={}, failed={}",
                response.getScanned(), response.getReindexed(), response.getSkipped(), response.getFailed());
        return response;
    }

    /**
     * Backfills {@code projectName} on facilities already present in the health facility index.
     * Scans the index, resolves each facility's project name from the project service (batched),
     * and patches only {@code Data.projectName} on documents whose value is missing or stale.
     */
    public FacilityProjectNameBackfillResponse backfillFacilityProjectNames(FacilityProjectNameBackfillRequest request) {
        if (!configs.isFacilityProjectNameBackfillEnabled()) {
            throw new IllegalArgumentException(
                    "Facility projectName backfill is disabled. Set facility.project-name.backfill.enabled=true to run.");
        }
        if (request == null || request.getRequestInfo() == null) {
            throw new IllegalArgumentException("RequestInfo is required");
        }

        final int projectFetchBatchSize = 100;
        String tenantId = request.getTenantId();
        int pageSize = (request.getBatchSize() != null && request.getBatchSize() > 0) ? request.getBatchSize() : 500;

        List<FacilityKibanaMapper.IndexedFacilityRef> refs =
                facilityKibanaMapper.fetchAllIndexedFacilities(tenantId, pageSize);

        FacilityProjectNameBackfillResponse response = FacilityProjectNameBackfillResponse.builder()
                .scanned(refs.size())
                .errors(new ArrayList<>())
                .build();

        if (refs.isEmpty()) {
            return response;
        }

        // Resolve project names in batches, grouped by tenant (project lookup is tenant-scoped).
        Map<String, String> projectNamesByFacilityId = new HashMap<>();
        Map<String, List<String>> facilityIdsByTenant = new LinkedHashMap<>();
        for (FacilityKibanaMapper.IndexedFacilityRef ref : refs) {
            String refTenant = ref.getTenantId() != null ? ref.getTenantId() : tenantId;
            facilityIdsByTenant.computeIfAbsent(refTenant, k -> new ArrayList<>()).add(ref.getFacilityId());
        }
        for (Map.Entry<String, List<String>> entry : facilityIdsByTenant.entrySet()) {
            String batchTenant = entry.getKey();
            List<String> ids = entry.getValue();
            for (int i = 0; i < ids.size(); i += projectFetchBatchSize) {
                List<String> batch = ids.subList(i, Math.min(i + projectFetchBatchSize, ids.size()));
                projectNamesByFacilityId.putAll(
                        facilityProjectClient.fetchProjectNamesByFacility(request.getRequestInfo(), batchTenant, batch));
            }
        }

        for (FacilityKibanaMapper.IndexedFacilityRef ref : refs) {
            String projectName = projectNamesByFacilityId.get(ref.getFacilityId());
            // No mapping found, or projectName already up to date → nothing to write.
            if (projectName == null || projectName.isBlank() || projectName.equals(ref.getProjectName())) {
                response.setSkipped(response.getSkipped() + 1);
                continue;
            }
            int updated = facilityKibanaMapper.updateProjectNameByFacilityId(
                    ref.getFacilityId(), ref.getTenantId(), projectName);
            if (updated > 0) {
                response.setUpdated(response.getUpdated() + 1);
            } else if (updated == 0) {
                response.setSkipped(response.getSkipped() + 1);
            } else {
                response.setFailed(response.getFailed() + 1);
                response.getErrors().add(FacilityBoundaryBackfillErrorItem.builder()
                        .facilityId(ref.getFacilityId())
                        .tenantId(ref.getTenantId())
                        .message("Elasticsearch _update_by_query failed")
                        .build());
            }
        }

        log.info("projectName backfill complete: scanned={}, updated={}, skipped={}, failed={}",
                response.getScanned(), response.getUpdated(), response.getSkipped(), response.getFailed());
        return response;
    }

    private List<Facility> loadFacilitiesForKibanaReindex(
            String tenantId,
            List<String> facilityIds,
            boolean onmReadyOnly
    ) {
        StringBuilder query = new StringBuilder(
                "SELECT fac.*, "
                        + "(SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r "
                        + "WHERE r.facilityid = fac.id AND r.tenantid = fac.tenant_id)) AS rms_inactive "
                        + "FROM facility fac WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (onmReadyOnly) {
            query.append(" AND fac.is_onm_ready = true");
        }
        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND fac.tenant_id = ?");
            params.add(tenantId.trim());
        }
        if (facilityIds != null && !facilityIds.isEmpty()) {
            List<String> distinctIds = facilityIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            if (!distinctIds.isEmpty()) {
                query.append(" AND fac.id IN (")
                        .append(distinctIds.stream().map(id -> "?").collect(Collectors.joining(", ")))
                        .append(")");
                params.addAll(distinctIds);
            }
        }

        return jdbcTemplate.query(query.toString(), params.toArray(), facilityRowMapper.rowMapper);
    }

    /**
     * When HFR is missing: prefer {@code nin_id}, else {@code facility_poc_username}.
     */
    private String resolveIndexerCodeForHfrMissingMigration(Facility facility) {
        if (facility == null) {
            return null;
        }
        if (hasNonBlankTrimmed(facility.getHfrId())) {
            return null;
        }
        if (hasNonBlankTrimmed(facility.getNinId())) {
            return facility.getNinId().trim();
        }
        if (hasNonBlankTrimmed(facility.getFacilityPocUsername())) {
            return facility.getFacilityPocUsername().trim();
        }
        return null;
    }

    private static boolean hasNonBlankTrimmed(String value) {
        return value != null && !value.trim().isEmpty();
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

    /**
     * Deactivates the HRMS POC user associated with a facility when the facility is marked as
     * non ONM-ready. The username is resolved the same way it is created (POC username for ANGANWADI,
     * otherwise HFR ID falling back to NIN ID). If no HRMS user exists, this is a no-op.
     */
    public void deactivateFacilityPOCUser(FacilityUpdateRequest request, Facility existingFacilityDetails) {
        String normalizedCategory = existingFacilityDetails.getFacilityCategory() == null
                ? ""
                : existingFacilityDetails.getFacilityCategory().trim().toUpperCase(Locale.ROOT);
        boolean isAnganwadi = CATEGORY_ANGANWADI.equals(normalizedCategory);
        String username;
        if (isAnganwadi) {
            username = existingFacilityDetails.getFacilityPocUsername() != null && !existingFacilityDetails.getFacilityPocUsername().trim().isBlank()
                    ? existingFacilityDetails.getFacilityPocUsername().trim() : "";
        } else {
            username = existingFacilityDetails.getHfrId() != null && !existingFacilityDetails.getHfrId().trim().isBlank()
                    ? existingFacilityDetails.getHfrId().trim()
                    : existingFacilityDetails.getNinId();
        }

        if (username == null || username.isBlank()) {
            log.info("Skipping POC user deactivation for facility {}: no resolvable HRMS username",
                    existingFacilityDetails.getFacilityId());
            return;
        }

        try {
            Employee employee = hrmsUtils.getUserByUsername(request, username);
            if (employee == null || employee.getUser() == null) {
                log.info("No HRMS POC user found for username {}, nothing to deactivate", username);
                return;
            }

            boolean employeeInactive = Boolean.FALSE.equals(employee.getIsActive());
            boolean userInactive = employee.getUser().getActive() == null || !employee.getUser().getActive();
            if (employeeInactive && userInactive) {
                log.info("HRMS POC user {} is already inactive for facility {}, skipping deactivation",
                        username, existingFacilityDetails.getFacilityId());
                return;
            }

            employee.getUser().setActive(false);
            employee.setIsActive(false);
            employee.setEmployeeStatus("INACTIVE");
            employee.setReActivateEmployee(false);

            EmployeeRequest employeeRequest = EmployeeRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .employees(List.of(employee))
                    .build();
            List<Employee> updatedEmployees = hrmsUtils.updateHRMSUser(employeeRequest);
            if (updatedEmployees != null && !updatedEmployees.isEmpty()) {
                log.info("Deactivated HRMS POC user {} for facility {}", username, existingFacilityDetails.getFacilityId());
            } else {
                log.warn("Failed to deactivate HRMS POC user {} for facility {}", username, existingFacilityDetails.getFacilityId());
            }
        } catch (CustomException e) {
            // getUserByUsername raises EMPLOYEE_NOT_FOUND when no matching user exists
            log.info("No HRMS POC user to deactivate for username {} (facility {}): {}",
                    username, existingFacilityDetails.getFacilityId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error deactivating HRMS POC user {} for facility {}: {}",
                    username, existingFacilityDetails.getFacilityId(), e.getMessage(), e);
        }
    }

    /**
     * Renames a mismatched HRMS POC employee (found via boundary code) to {@code newCode}, then
     * syncs the facility table's corresponding identifier column to the same value via a dedicated
     * update-facility push (carrying the rest of the facility's current fields forward unchanged so
     * the blanket persister UPDATE doesn't null out unrelated columns), and finally patches the
     * Kibana index's {@code code} field.
     */
    private void renamePocEmployeeAndSyncFacility(Employee matchedEmployee, String newCode, Facility facility,
            String tenantId, RequestInfo requestInfo, boolean isAnganwadi, boolean updatingHfr) {
        log.info("POC identifier mismatch for facility {} (HRMS user code={}, expected={}); renaming HRMS user and syncing facility",
                sanitizeForLog(facility.getFacilityId()), sanitizeForLog(matchedEmployee.getCode()), sanitizeForLog(newCode));

        hrmsUtils.updateHrmsUsername(requestInfo, matchedEmployee.getUuid(), newCode, tenantId);
        // Keep the in-memory Employee consistent with the rename so any subsequent HRMS _update
        // call using this same object (e.g. activatePocUserAndResetPassword) doesn't push the
        // stale code back via delete+insert and undo the rename.
        matchedEmployee.setCode(newCode);

        FacilityUpdateRequestFacilityUpdate updatePayload = FacilityUpdateRequestFacilityUpdate.builder()
                .tenantId(facility.getTenantId())
                .facilityId(facility.getFacilityId())
                .facilityName(facility.getFacilityName())
                .facilityType(facility.getFacilityType())
                .facilitySubtype(facility.getFacilitySubtype())
                .address(facility.getAddress())
                .facilityDetails(facility.getFacilityDetails())
                .additionalDetails(facility.getAdditionalDetails())
                .isOnmReady(facility.getIsOnmReady())
                .pocName(facility.getFacilityPocName())
                .pocContact(facility.getFacilityPocPhone())
                .pocEmail(facility.getFacilityPocEmail())
                .facilityPocUsername(facility.getFacilityPocUsername())
                .hfrId(facility.getHfrId())
                .ninId(facility.getNinId())
                .userId(facility.getUserId())
                .status(facility.getFacilityStatus())
                .isActive(facility.getIsActive())
                .build();

        if (isAnganwadi) {
            updatePayload.setFacilityPocUsername(newCode);
            facility.setFacilityPocUsername(newCode);
        } else if (updatingHfr) {
            updatePayload.setHfrId(newCode);
            facility.setHfrId(newCode);
        } else {
            updatePayload.setNinId(newCode);
            facility.setNinId(newCode);
        }

        facilityRepository.pushUpdateFacility(FacilityUpdateRequest.builder()
                .requestInfo(requestInfo)
                .facilityUpdate(updatePayload)
                .build());

        FacilityKibanaIndex patch = facilityKibanaMapper.toKibanaIndexPatchCode(facility, newCode, requestInfo);
        if (patch != null) {
            facilityRepository.pushToKibana(patch);
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

    /**
     * Backfills missing boundary entities and SELCO Facility boundary-relationships for facilities that
     * were persisted when relationship create failed (e.g. varchar length on code/parent).
     */
    public FacilityBoundaryBackfillResponse backfillMissingFacilityBoundaryRelationships(
            FacilityBoundaryBackfillRequest request
    ) {
        if (!configs.isFacilityBoundaryBackfillEnabled()) {
            throw new IllegalArgumentException(
                    "Facility boundary backfill is disabled. Set facility.boundary.backfill.enabled=true to run."
            );
        }
//        validateSystemUserAuthorization(request.getRequestInfo());

        String hierarchyType = configs.getBoundaryHierarchyType();

        List<FacilityBoundaryBackfillRow> rows = loadFacilitiesMissingBoundaryRelationship(hierarchyType);
        log.info("Boundary backfill: boundaryTenantId={}, hierarchyType={}, scanned={}",
                BOUNDARY_TENANT_ID, hierarchyType, rows.size());

        FacilityBoundaryBackfillResponse response = FacilityBoundaryBackfillResponse.builder()
                .scanned(rows.size())
                .errors(new ArrayList<>())
                .build();

        if (rows.isEmpty()) {
            return response;
        }

        List<FacilityBoundaryBackfillRow> validRows = new ArrayList<>();
        for (FacilityBoundaryBackfillRow row : rows) {
            String parent = deriveParentBlockBoundaryCode(row.boundaryCode(), row.facilityId());
            if (parent == null || parent.isBlank()) {
                response.setSkippedInvalid(response.getSkippedInvalid() + 1);
                response.getErrors().add(FacilityBoundaryBackfillErrorItem.builder()
                        .facilityId(row.facilityId())
                        .tenantId(row.tenantId())
                        .boundaryCode(row.boundaryCode())
                        .message("boundary_code does not match expected pattern {blockCode}_" + row.facilityId())
                        .build());
                continue;
            }
            validRows.add(row);
        }

        List<FacilityBoundaryBackfillRow> toBackfill = new ArrayList<>(validRows);
        response.setMissing(toBackfill.size());

        for (FacilityBoundaryBackfillRow row : toBackfill) {
            String parent = deriveParentBlockBoundaryCode(row.boundaryCode(), row.facilityId());
            try {
                ensureFacilityBoundaryExists(row.boundaryCode(), parent, BOUNDARY_TENANT_ID, request.getRequestInfo());
                response.setCreated(response.getCreated() + 1);
            } catch (Exception e) {
                response.setFailed(response.getFailed() + 1);
                String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                log.error("Boundary backfill failed for facility {}: {}", row.facilityId(), message, e);
                response.getErrors().add(FacilityBoundaryBackfillErrorItem.builder()
                        .facilityId(row.facilityId())
                        .tenantId(row.tenantId())
                        .boundaryCode(row.boundaryCode())
                        .message(message)
                        .build());
            }
        }

        log.info("Boundary backfill complete: created={}, failed={}, skippedInvalid={}",
                response.getCreated(), response.getFailed(), response.getSkippedInvalid());
        return response;
    }

    /**
     * Loads only facilities whose {@code boundary_code} has no matching row in {@code boundary_relationship}
     * for the given hierarchy (typically SELCO). Requires facility and boundary-service tables in the same DB.
     */
    private List<FacilityBoundaryBackfillRow> loadFacilitiesMissingBoundaryRelationship(String hierarchyType) {
        String sql =
                "SELECT f.id, f.tenant_id, f.boundary_code FROM facility f " +
                "WHERE f.boundary_code IS NOT NULL " +
                "AND NOT EXISTS ( " +
                "  SELECT 1 FROM boundary_relationship br " +
                "  WHERE br.tenantid = ? " +
                "    AND br.code = f.boundary_code " +
                "    AND br.hierarchytype = ? " +
                ") ORDER BY f.id DESC";
        List<Object> params = List.of(BOUNDARY_TENANT_ID, hierarchyType);
        return jdbcTemplate.query(
                sql,
                params.toArray(),
                (rs, rowNum) -> new FacilityBoundaryBackfillRow(
                        rs.getString("id"),
                        rs.getString("tenant_id"),
                        rs.getString("boundary_code")
                )
        );
    }

    private String deriveParentBlockBoundaryCode(String boundaryCode, String facilityId) {
        if (boundaryCode == null || facilityId == null) {
            return null;
        }
        String suffix = "_" + facilityId;
        if (!boundaryCode.endsWith(suffix)) {
            return null;
        }
        return boundaryCode.substring(0, boundaryCode.length() - suffix.length());
    }

    private void validateSystemUserAuthorization(RequestInfo requestInfo) {
        var userInfo = requestInfo != null ? requestInfo.getUserInfo() : null;
        if (userInfo == null || userInfo.getRoles() == null) {
            throw new IllegalArgumentException("Only SYSTEM_USER role can run facility boundary backfill");
        }
        boolean isSystemUser = userInfo.getRoles().stream()
                .anyMatch(role -> SYSTEM_USER.equalsIgnoreCase(role.getCode()));
        if (!isSystemUser) {
            throw new IllegalArgumentException("Only SYSTEM_USER role can run facility boundary backfill");
        }
    }

    private record FacilityBoundaryBackfillRow(String facilityId, String tenantId, String boundaryCode) {}

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

    private String buildBulkSearchOrderBy(FacilityBulkSearchCriteria criteria) {
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy().trim().toLowerCase() : "updated_at";
        String column = "created_at".equals(sortBy) ? "fac.created_at" : "fac.updated_at";
        boolean asc = "asc".equalsIgnoreCase(criteria.getSortOrder());
        return " ORDER BY " + column + (asc ? " ASC " : " DESC ") + " NULLS LAST ";
    }

}
