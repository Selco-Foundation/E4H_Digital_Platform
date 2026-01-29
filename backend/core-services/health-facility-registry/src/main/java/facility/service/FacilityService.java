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
import static facility.config.ServiceConstants.FACILITY_ADMIN;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FacilityService {

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
            EncryptionDecryptionUtil encryptionDecryptionUtil, BoundaryUtil boundaryUtil, FacilityRowMapperV2 facilityRowMapperV2, HRMSUtils hrmsUtils, HRMSService hrmsService) {
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

            log.info("Pushing {} facilities to Kafka for tenant {}", tenantFacilities.size(), tenantId);
            for (Facility facility : tenantFacilities) {
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
                
                // If facility is ONM ready, create POC user and push to Kibana for indexing
                if (Boolean.TRUE.equals(facility.getIsOnmReady())) {
                    log.info("Facility {} is ONM ready, creating POC user and pushing to Kibana", facility.getFacilityId());
                    // Create POC user if not exists (check by phone number uniqueness)
                    createFacilityPOCUserIfNotExists(facility, tenantId, request.getRequestInfo());

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
     * Creates POC user as HRMS employee if not exists (checks by phone number uniqueness).
     * Validates required fields (HFR ID, POC contact, POC name) before attempting creation.
     * Supports both direct fields (facilityPocName, facilityPocPhone, hfrId) and nested facilityDetails.
     *
     * @param facility The facility for which to create POC user
     * @param requestInfo RequestInfo for API calls
     */
    private void createFacilityPOCUserIfNotExists(Facility facility, String tenantId, RequestInfo requestInfo) {
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
        
        if ((facilityDetails.getPocContact() == null || facilityDetails.getPocContact().isBlank()) 
                && facility.getFacilityPocPhone() != null && !facility.getFacilityPocPhone().trim().isBlank()) {
            facilityDetails.setPocContact(facility.getFacilityPocPhone().trim());
        }
        
        if (facilityDetails.getPocName() == null 
                && facility.getFacilityPocName() != null && !facility.getFacilityPocName().trim().isBlank()) {
            facilityDetails.setPocName(facility.getFacilityPocName().trim());
        }
        
        if (facilityDetails.getPocEmail() == null 
                && facility.getFacilityPocEmail() != null && !facility.getFacilityPocEmail().trim().isBlank()) {
            facilityDetails.setPocEmail(facility.getFacilityPocEmail().trim());
        }

        // Validate required fields
        if (facilityDetails.getHfrId() == null || facilityDetails.getHfrId().isBlank() ||
            facilityDetails.getPocContact() == null || facilityDetails.getPocContact().isBlank() ||
            facilityDetails.getPocName() == null || facilityDetails.getPocName().isBlank()) {
            log.warn("Cannot create POC user for facility {}: missing HFR ID, POC contact, or POC name. " +
                    "HFR ID: {}, POC Contact: {}, POC Name: {}",
                    sanitizeForLog(facility.getFacilityId()),
                    sanitizeForLog(facilityDetails.getHfrId()),
                    sanitizeForLog(facilityDetails.getPocContact()),
                    sanitizeForLog(facilityDetails.getPocName() != null ? facilityDetails.getPocName() : "null"));
            return;
        }

        // Check if employee already exists by mobile number
        boolean employeeExists = hrmsService.employeeExistsByMobileNumber(
                facilityDetails.getPocContact(),
                tenantId,
                requestInfo
        );

        if (!employeeExists) {
            // Create POC user as HRMS employee with COMPLAINANT and EMPLOYEE roles
            boolean created = hrmsService.createFacilityPOCEmployee(facility, requestInfo);
            if (created) {
                log.info("Successfully created POC user for facility {} with HFR ID {}",
                        sanitizeForLog(facility.getFacilityId()), sanitizeForLog(facilityDetails.getHfrId()));
            } else {
                log.warn("Failed to create POC user for facility {}", sanitizeForLog(facility.getFacilityId()));
            }
        } else {
            log.info("POC user with mobile number {} already exists for facility {}, skipping creation",
                    sanitizeForLog(facilityDetails.getPocContact()), sanitizeForLog(facility.getFacilityId()));
        }
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
        var userInfo = request.getRequestInfo().getUserInfo();
        if (userInfo.getRoles() != null) {
            boolean isFacilityAdmin = userInfo.getRoles().stream().anyMatch(role -> FACILITY_ADMIN.equalsIgnoreCase(role.getCode()));
            if(!isFacilityAdmin)
                throw new IllegalArgumentException("Only FACILITY_ADMIN role can edit facilities");
        }

        // Check if the facility exists in DB before attempting an update
//        String checkSql = "SELECT COUNT(*) FROM facility WHERE id = ? AND tenant_id = ?";
//        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, update.getFacilityId(), update.getTenantId());
//        if (count == null || count == 0) {
//            return null; // facility not found
//        }

        // Check if the facility exists in DB before attempting an update
        String fetchFullFacilitySql = "SELECT * FROM facility WHERE id = ? AND tenant_id = ?";
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

        Facility facility = new Facility();
        facility.setFacilityId(update.getFacilityId());
        facility.setTenantId(update.getTenantId());
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
        facility.setHfrId(update.getHfrId());
        facility.setNinId(update.getNinId());
        facility.setFacilityStatus(update.getStatus());
        facility.setUserId(update.getUserId());

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
        if (facility.getIsActive() == null) facility.setIsActive(true);

        // If POC details are updated AND facility is isOnmReady=true
        boolean isPocDetailsUpdated = checkPOCDetailsUpdated(existingFacility, facility);
        if(isPocDetailsUpdated){
            updatedHRMSUser(request, existingFacility, facility);
        }

        log.info("Pushing facility update to Kafka");
        facilityRepository.pushUpdateFacility(request);
        
        // If user sent isOnmReady = true, handle POC user creation and Kibana push
        if (Boolean.TRUE.equals(update.getIsOnmReady())) {
            log.info("Facility {} is marked as ONM ready, processing POC user and Kibana push", update.getFacilityId());
            // Merge update request data with existing facility data to get complete facility info
            Facility facilityForProcessing = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .facilitySubtype(facility.getFacilitySubtype() != null ? facility.getFacilitySubtype() : existingFacility.getFacilitySubtype())
                    .facilityName(facility.getFacilityName() != null ? facility.getFacilityName() : existingFacility.getFacilityName())
                    .facilityCategory(existingFacility.getFacilityCategory())
                    .facilityOwnership(existingFacility.getFacilityOwnership())
                    .facilityRegion(existingFacility.getFacilityRegion())
                    .address(facility.getAddress() != null ? facility.getAddress() : existingFacility.getAddress())
                    .facilityDetails(facility.getFacilityDetails() != null ? facility.getFacilityDetails() : existingFacility.getFacilityDetails())
                    .additionalDetails(facility.getAdditionalDetails() != null ? facility.getAdditionalDetails() : existingFacility.getAdditionalDetails())
                    .boundaryCode(facility.getBoundaryCode() != null ? facility.getBoundaryCode() : existingFacility.getBoundaryCode())
                    .isOnmReady(true)
                    .build();

            // Always check/create POC user when isOnmReady is true (whether transitioning or already true)
            // This ensures POC user is created if missing, even if facility was already ONM ready
            createFacilityPOCUserIfNotExists(facilityForProcessing, update.getTenantId(), request.getRequestInfo());

            // Check if facility already exists in Kibana, if not then push
            boolean existsInKibana = facilityKibanaMapper.existsInKibana(
                    update.getFacilityId(), 
                    update.getTenantId(), 
                    request.getRequestInfo()
            );
            
            if (existsInKibana) {
                log.info("Facility {} already exists in Kibana, skipping push", sanitizeForLog(update.getFacilityId()));
                return facility;
            }

            // Fetch full facility from DB only to get missing fields not in update request (like facilityCategory, facilityOwnership, etc.)

            // Merge update request data with existing facility data (prioritize update values)
            Facility facilityForKibana = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .tenantId(facility.getTenantId())
                    .facilityType(facility.getFacilityType() != null ? facility.getFacilityType() : existingFacility.getFacilityType())
                    .facilitySubtype(facility.getFacilitySubtype() != null ? facility.getFacilitySubtype() : existingFacility.getFacilitySubtype())
                    .facilityName(facility.getFacilityName() != null ? facility.getFacilityName() : existingFacility.getFacilityName())
                    .facilityCategory(existingFacility.getFacilityCategory()) // Not in update request, use existing
                    .facilityOwnership(existingFacility.getFacilityOwnership()) // Not in update request, use existing
                    .facilityRegion(existingFacility.getFacilityRegion()) // Not in update request, use existing
                    .address(facility.getAddress() != null ? facility.getAddress() : existingFacility.getAddress())
                    .facilityDetails(facility.getFacilityDetails() != null ? facility.getFacilityDetails() : existingFacility.getFacilityDetails())
                    .additionalDetails(facility.getAdditionalDetails() != null ? facility.getAdditionalDetails() : existingFacility.getAdditionalDetails())
                    .boundaryCode(facility.getBoundaryCode() != null ? facility.getBoundaryCode() : existingFacility.getBoundaryCode())
                    .isOnmReady(true) // Set from update request
                    .facilityPocName(facility.getFacilityPocName()!=null && !facility.getFacilityPocName().isBlank() ? facility.getFacilityPocName(): existingFacility.getFacilityPocEmail())
                    .facilityPocPhone(facility.getFacilityPocPhone()!=null && !facility.getFacilityPocPhone().isBlank() ? facility.getFacilityPocPhone(): existingFacility.getFacilityPocPhone())
                    .facilityPocEmail(facility.getFacilityPocEmail()!=null && !facility.getFacilityPocEmail().isBlank() ? facility.getFacilityPocEmail(): existingFacility.getFacilityPocEmail())
                    .hfrId(facility.getHfrId()!=null && !facility.getHfrId().isBlank() ? facility.getHfrId(): existingFacility.getHfrId())
                    .ninId(facility.getNinId()!=null && !facility.getNinId().isBlank() ? facility.getNinId(): existingFacility.getNinId())
                    .userId(facility.getUserId()!=null && !facility.getUserId().isBlank() ? facility.getUserId(): existingFacility.getUserId())
                    .build();
            
            // Transform to Kibana index format and push
            FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facilityForKibana, request.getRequestInfo());
            facilityRepository.pushToKibana(kibanaIndex);
            log.info("Facility {} pushed to Kibana successfully", sanitizeForLog(update.getFacilityId()));
        }
        
        log.info("Successfully updated facility {}", update.getFacilityId());
        log.trace("Exiting updateFacility method");
        return facility;
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

        StringBuilder query = new StringBuilder("SELECT * FROM facility");
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
        List<String> listFacilityCodes = boundaryUtil.getFacilityCodesFromBoundary(request.getFacilityBulkSearchCriteria());
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

        StringBuilder query = new StringBuilder("SELECT * FROM facility fac");
        query.append(" LEFT JOIN facility_address fa ON fac.addressid = fa.id ");
        query.append(result.getWhereClause());
        query.append(" ORDER BY created_at DESC ");

        List<Object> allParams = new ArrayList<>(result.getParams());
        if (!Boolean.TRUE.equals(request.getFacilityBulkSearchCriteria().getSendNonPaginatedResponse())) {
            query.append(" LIMIT ? OFFSET ?");
            allParams.add(request.getFacilityBulkSearchCriteria().getLimit());
            allParams.add(request.getFacilityBulkSearchCriteria().getOffset());
        }

        log.info("Bulk Search Query: {}", query);
        log.info("Bulk Search Params count: {}", allParams.size());
        List<Facility> facilityList = jdbcTemplate.query(query.toString(), facilityRowMapperV2, allParams.toArray());
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        for (Facility facility: facilityList){
            if (facility.getFacilityPocPhone()!=null && !facility.getFacilityPocPhone().isEmpty()){
                try {
                    String decryptedMobileNumber = decryptMobileNumber(facility.getFacilityPocPhone());
                    if(decryptedMobileNumber!=null && !decryptedMobileNumber.isBlank()){
                        facility.setFacilityPocPhone(decryptedMobileNumber);
                    }
                }
                catch (Exception e){}
            }
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
        StringBuilder query = new StringBuilder("SELECT * FROM facility");
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
            facility.setPocName(facilityDB.getFacilityDetails().getPocName());
            facility.setPocEmail(facilityDB.getFacilityDetails().getPocEmail());
            facility.setHfrId(facilityDB.getFacilityDetails().getHfrId());
            facility.setNinId(facilityDB.getFacilityDetails().getNinId());
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

            HealthFacilityDetails details = facilityDB.getFacilityDetails();
            details.setPocName(null);
            details.setPocContact(null);
            details.setPocEmail(null);
            details.setNinId(null);
            details.setHfrId(null);
            facility.setFacilityDetails(details);

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
        boolean isOnmReady = existingFacilityDetails.getIsOnmReady();
        boolean pocDetailsUpdated = (!Objects.equals(existingFacilityDetails.getFacilityPocPhone(), requestFacilityDetails.getFacilityPocPhone()) ||
                !Objects.equals(existingFacilityDetails.getFacilityPocName(), requestFacilityDetails.getFacilityPocName()) ||
                !Objects.equals(existingFacilityDetails.getFacilityPocEmail(), requestFacilityDetails.getFacilityPocEmail()));
        return isOnmReady && pocDetailsUpdated;
    }

    public void updatedHRMSUser(FacilityUpdateRequest request, Facility existingFacilityDetails, Facility requestFacilityDetails){
        if(existingFacilityDetails.getUserId()!=null && !existingFacilityDetails.getUserId().isEmpty()){
            Employee employee = hrmsUtils.getUserById(request, existingFacilityDetails.getUserId());
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

}
