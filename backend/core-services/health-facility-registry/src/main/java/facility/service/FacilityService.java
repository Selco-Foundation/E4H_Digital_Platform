package facility.service;

import facility.config.Configuration;
import facility.repository.FacilityRepository;
import facility.util.IdgenUtil;
import facility.util.QueryBuilderResult;
import facility.util.QueryBuilderUtil;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            HRMSService hrmsService
    ) {
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

        Map<String, List<FacilityCreate>> facilitiesByTenant = groupFacilitiesByTenant(facilities);
        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<FacilityCreate>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<FacilityCreate> facilityCreateList = entry.getValue();
            log.info("Processing {} facilities for tenant {}", facilityCreateList.size(), tenantId);

            processTenantFacilities(request, tenantId, facilityCreateList, validatedFacilities);
        }

        log.info("Successfully created {} facilities", validatedFacilities.size());
        log.trace("Exiting createFacility method");
        return validatedFacilities;
    }

    private Map<String, List<FacilityCreate>> groupFacilitiesByTenant(List<FacilityCreate> facilities) {
        Map<String, List<FacilityCreate>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(FacilityCreate::getTenantId));
        log.debug("Grouped facilities into {} tenant groups", facilitiesByTenant.size());
        return facilitiesByTenant;
    }

    private void processTenantFacilities(FacilityCreateRequest request,
                                         String tenantId,
                                         List<FacilityCreate> facilityCreateList,
                                         List<Facility> validatedFacilities) {

        validateBlockBoundaries(request, tenantId, facilityCreateList);

        List<Boundary> boundaryList = new ArrayList<>();
        List<BoundaryRelation> boundaryRelationList = new ArrayList<>();
        List<Facility> tenantFacilities = buildFacilitiesForTenant(request, tenantId, facilityCreateList,
                boundaryList, boundaryRelationList);

        validateFacilitiesAgainstMdms(request, tenantId, tenantFacilities);
        createBoundaries(request, tenantId, boundaryList, boundaryRelationList);
        pushFacilitiesForTenant(request, tenantId, tenantFacilities, validatedFacilities);
    }

    private void validateBlockBoundaries(FacilityCreateRequest request,
                                         String tenantId,
                                         List<FacilityCreate> facilityCreateList) {
        Set<String> blockBoundaryCodes = facilityCreateList.stream()
                .map(FacilityCreate::getBlockBoundaryCode)
                .collect(Collectors.toSet());
        log.debug("Validating {} unique block boundary codes for tenant {}", blockBoundaryCodes.size(), tenantId);
        boundaryValidator.validateBoundaries(blockBoundaryCodes, tenantId, request.getRequestInfo());
    }

    private List<Facility> buildFacilitiesForTenant(FacilityCreateRequest request,
                                                    String tenantId,
                                                    List<FacilityCreate> facilityCreateList,
                                                    List<Boundary> boundaryList,
                                                    List<BoundaryRelation> boundaryRelationList) {
        List<Facility> tenantFacilities = new ArrayList<>();

        for (FacilityCreate facilityCreate : facilityCreateList) {
            Facility facility = buildFacilityFromCreate(request, tenantId, facilityCreate);
            String facilityBoundaryCode = facilityCreate.getBlockBoundaryCode() + "_" + facility.getFacilityId();

            boundaryList.add(buildBoundary(tenantId, facilityBoundaryCode));
            boundaryRelationList.add(buildBoundaryRelation(tenantId, facilityCreate, facilityBoundaryCode));
            facility.setBoundaryCode(facilityBoundaryCode);

            applyDefaultStatusAndActivation(facility);
            ensureAddressId(facility);
            validateHfrOrNinUniqueness(facility, tenantId);
            validateFacilityNameBoundaryCodeUnique(facility, tenantId);

            tenantFacilities.add(facility);
        }

        return tenantFacilities;
    }

    private Facility buildFacilityFromCreate(FacilityCreateRequest request,
                                             String tenantId,
                                             FacilityCreate facilityCreate) {
        Facility facility = Facility.builder()
                .tenantId(tenantId)
                .facilityCategory(facilityCreate.getFacilityCategory())
                .facilityType(facilityCreate.getFacilityType())
                .facilitySubtype(facilityCreate.getFacilitySubtype())
                .facilityName(facilityCreate.getFacilityName())
                .facilityOwnership(facilityCreate.getFacilityOwnership())
                .facilityRegion(facilityCreate.getFacilityRegion())
                .address(facilityCreate.getAddress())
                .facilityDetails(facilityCreate.getFacilityDetails())
                .wfStatus(facilityCreate.getWfStatus())
                .additionalDetails(facilityCreate.getAdditionalDetails())
                .isActive(facilityCreate.getIsActive())
                .isOnmReady(facilityCreate.getIsOnmReady())
                .build();

        facility.setFacilityId(idgenUtil.getIdList(
                request.getRequestInfo(), tenantId, "facility.id", "", 1
        ).get(0));

        return facility;
    }

    private Boundary buildBoundary(String tenantId, String facilityBoundaryCode) {
        return Boundary.builder()
                .tenantId(tenantId)
                .code(facilityBoundaryCode)
                .build();
    }

    private BoundaryRelation buildBoundaryRelation(String tenantId,
                                                   FacilityCreate facilityCreate,
                                                   String facilityBoundaryCode) {
        return BoundaryRelation.builder()
                .tenantId(tenantId)
                .boundaryType("Facility")
                .code(facilityBoundaryCode)
                .parent(facilityCreate.getBlockBoundaryCode())
                .hierarchyType("SELCO")
                .build();
    }

    private void applyDefaultStatusAndActivation(Facility facility) {
        if (facility.getWfStatus() == null) {
            facility.setWfStatus("CREATED");
        }
        if (facility.getIsActive() == null) {
            facility.setIsActive(true);
        }
    }

    private void ensureAddressId(Facility facility) {
        if (facility.getAddress().getAddressId() == null) {
            facility.getAddress().setAddressId(UUID.randomUUID().toString());
        }
    }

    private void validateFacilitiesAgainstMdms(FacilityCreateRequest request,
                                               String tenantId,
                                               List<Facility> tenantFacilities) {
        log.info("Validating {} facilities against MDMS for tenant {}", tenantFacilities.size(), tenantId);
        facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());
    }

    private void createBoundaries(FacilityCreateRequest request,
                                  String tenantId,
                                  List<Boundary> boundaryList,
                                  List<BoundaryRelation> boundaryRelationList) {
        log.info("Creating {} boundaries for tenant {}", boundaryList.size(), tenantId);
        BoundaryCreateRequest boundaryCreateRequest = BoundaryCreateRequest.builder()
                .requestInfo(request.getRequestInfo())
                .boundary(boundaryList)
                .build();
        boundaryService.createBoundaries(boundaryCreateRequest);

        log.info("Creating {} boundary relationships for tenant {}", boundaryRelationList.size(), tenantId);
        for (BoundaryRelation boundaryRelation : boundaryRelationList) {
            BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .boundaryRelationship(boundaryRelation)
                    .build();
            boundaryService.createBoundaryRelationship(boundaryRelationshipRequest);
        }
    }

    private void pushFacilitiesForTenant(FacilityCreateRequest request,
                                         String tenantId,
                                         List<Facility> tenantFacilities,
                                         List<Facility> validatedFacilities) {
        log.info("Pushing {} facilities to Kafka for tenant {}", tenantFacilities.size(), tenantId);
        for (Facility facility : tenantFacilities) {
            log.trace("Processing facility: {}", facility.getFacilityId());
            facilityRepository.pushCreateFacility(facility);

            if (Boolean.TRUE.equals(facility.getIsOnmReady())) {
                log.info("Facility {} is ONM ready, creating POC user and pushing to Kibana", facility.getFacilityId());
                createFacilityPOCUserIfNotExists(facility, tenantId, request.getRequestInfo());

                FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                facilityRepository.pushToKibana(kibanaIndex);
            }

            validatedFacilities.add(facility);
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
        HealthFacilityDetails details = facility.getFacilityDetails();

        if (details != null) {
            String hfrId = details.getHfrId();
            String ninId = details.getNinId();

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
     *
     * @param facility The facility for which to create POC user
     * @param requestInfo RequestInfo for API calls
     */
    private void createFacilityPOCUserIfNotExists(Facility facility, String tenantId, RequestInfo requestInfo) {
        HealthFacilityDetails facilityDetails = facility.getFacilityDetails();
        
        if (facilityDetails == null || facilityDetails.getHfrId() == null || 
            facilityDetails.getHfrId().isBlank() || facilityDetails.getPocContact() == null || 
            facilityDetails.getPocContact().isBlank() || facilityDetails.getPocName() == null) {
            log.warn("Cannot create POC user for facility {}: missing HFR ID, POC contact, or POC name", 
                    sanitizeForLog(facility.getFacilityId()));
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

        validateUpdateRequest(update);
        log.info("Updating facility {} for tenant {}", update.getFacilityId(), update.getTenantId());

        Facility existingFacility = fetchExistingFacility(update);
        Facility facility = buildFacilityFromUpdate(update);

        validateUpdateAgainstMdmsAndBoundary(request, update, facility);
        applyDefaultUpdateStatus(facility);

        log.info("Pushing facility update to Kafka");
        facilityRepository.pushUpdateFacility(request);

        if (Boolean.TRUE.equals(update.getIsOnmReady())) {
            handleOnmReadyProcessing(request, update, existingFacility, facility);
        }

        log.info("Successfully updated facility {}", update.getFacilityId());
        log.trace("Exiting updateFacility method");
        return facility;
    }

    private void validateUpdateRequest(FacilityUpdateRequestFacilityUpdate update) {
        if (update.getFacilityId() == null || update.getTenantId() == null) {
            log.error("Update request missing facilityId or tenantId");
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }
    }

    private Facility fetchExistingFacility(FacilityUpdateRequestFacilityUpdate update) {
        String fetchExistingFacilitySql = "SELECT * FROM facility WHERE id = ? AND tenant_id = ?";
        try {
            Facility existingFacility = jdbcTemplate.queryForObject(
                    fetchExistingFacilitySql,
                    facilityRowMapper.rowMapper,
                    update.getFacilityId(),
                    update.getTenantId()
            );
            log.debug("Found existing facility for update");
            return existingFacility;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Facility {} not found for tenant {}, returning null", update.getFacilityId(), update.getTenantId());
            return null;
        }
    }

    private Facility buildFacilityFromUpdate(FacilityUpdateRequestFacilityUpdate update) {
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
        return facility;
    }

    private void validateUpdateAgainstMdmsAndBoundary(FacilityUpdateRequest request,
                                                      FacilityUpdateRequestFacilityUpdate update,
                                                      Facility facility) {
        log.info("Validating facility update against MDMS and boundaries");
        facilityMdmsValidator.validateAgainstMDMS(List.of(facility), update.getTenantId(), request.getRequestInfo());
        if (facility.getBoundaryCode() != null) {
            log.debug("Validating boundary code: {}", facility.getBoundaryCode());
            boundaryValidator.validateBoundaries(
                    Set.of(facility.getBoundaryCode()),
                    update.getTenantId(),
                    request.getRequestInfo());
        }
    }

    private void applyDefaultUpdateStatus(Facility facility) {
        if (facility.getWfStatus() == null) {
            facility.setWfStatus("UPDATED");
        }
        if (facility.getIsActive() == null) {
            facility.setIsActive(true);
        }
    }

    private void handleOnmReadyProcessing(FacilityUpdateRequest request,
                                          FacilityUpdateRequestFacilityUpdate update,
                                          Facility existingFacility,
                                          Facility facility) {
        if (existingFacility == null) {
            log.warn("Existing facility not found; skipping ONM ready processing for facility {}", update.getFacilityId());
            return;
        }

        log.info("Facility {} is marked as ONM ready, processing POC user and Kibana push", update.getFacilityId());

        Facility facilityForProcessing = buildFacilityForProcessing(existingFacility, facility);
        createFacilityPOCUserIfNotExists(facilityForProcessing, update.getTenantId(), request.getRequestInfo());

        boolean existsInKibana = facilityKibanaMapper.existsInKibana(
                update.getFacilityId(),
                update.getTenantId(),
                request.getRequestInfo()
        );

        if (existsInKibana) {
            log.info("Facility {} already exists in Kibana, skipping push", sanitizeForLog(update.getFacilityId()));
            return;
        }

        Facility facilityForKibana = buildFacilityForKibana(existingFacility, facility);
        FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facilityForKibana, request.getRequestInfo());
        facilityRepository.pushToKibana(kibanaIndex);
        log.info("Facility {} pushed to Kibana successfully", sanitizeForLog(update.getFacilityId()));
    }

    private Facility buildFacilityForProcessing(Facility existingFacility, Facility facility) {
        return Facility.builder()
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
    }

    private Facility buildFacilityForKibana(Facility existingFacility, Facility facility) {
        return Facility.builder()
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
        query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<Object> allParams = new ArrayList<>(result.getParams());
        allParams.add(request.getLimit());
        allParams.add(request.getOffset());

        List<Facility> results = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        log.info("Found {} facilities matching search criteria", results.size());
        log.trace("Exiting searchFacilities method");
        return results;
    }

    /**
     * Searches for facilities using filters like multiple tenantIds, names, hfrIds, ninIds, boundaries, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> bulkSearchFacilities(FacilityBulkSearchRequest request) {
        log.trace("Entering bulkSearchFacilities method");
        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );

        StringBuilder query = new StringBuilder("SELECT * FROM facility");
        query.append(result.getWhereClause());

        List<Object> allParams = new ArrayList<>(result.getParams());
        if (!Boolean.TRUE.equals(request.getFacilityBulkSearchCriteria().getSendNonPaginatedResponse())) {
            query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            allParams.add(request.getFacilityBulkSearchCriteria().getLimit());
            allParams.add(request.getFacilityBulkSearchCriteria().getOffset());
        }

        log.debug("Bulk Search Query: {}", query);
        log.debug("Bulk Search Params count: {}", allParams.size());
        List<Facility> results = jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
        log.info("Bulk search found {} facilities", results.size());
        log.trace("Exiting bulkSearchFacilities method");
        return results;
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
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
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
}
