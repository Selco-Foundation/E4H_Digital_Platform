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
        List<FacilityCreate> facilities = request.getFacilities();

        // Group facility create requests by tenant ID for batch validation and processing
        Map<String, List<FacilityCreate>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(FacilityCreate::getTenantId));

        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<FacilityCreate>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<FacilityCreate> facilityCreateList = entry.getValue();
            List<Facility> tenantFacilities = new ArrayList<>();

            // Validate block boundary codes in bulk
            Set<String> blockBoundaryCodes = facilityCreateList.stream()
                    .map(FacilityCreate::getBlockBoundaryCode)
                    .collect(Collectors.toSet());
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
            facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());

            //todo: handle boundary or boundary relation creation failure??
            BoundaryCreateRequest boundaryCreateRequest = BoundaryCreateRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .boundary(boundaryList)
                    .build();
            boundaryService.createBoundaries(boundaryCreateRequest);

            for (BoundaryRelation boundaryRelation: boundaryRelationList) {
                BoundaryRelationshipRequest boundaryRelationshipRequest = BoundaryRelationshipRequest.builder()
                        .requestInfo(request.getRequestInfo())
                        .boundaryRelationship(boundaryRelation)
                        .build();
                boundaryService.createBoundaryRelationship(boundaryRelationshipRequest);
            }

            for (Facility facility : tenantFacilities) {
                // Push to Kafka topic for persistence
                facilityRepository.pushCreateFacility(facility);
                
                // If facility is ONM ready, create POC user and push to Kibana for indexing
                if (Boolean.TRUE.equals(facility.getIsOnmReady())) {
                    // Create POC user if not exists (check by phone number uniqueness)
                    createFacilityPOCUserIfNotExists(facility, tenantId, request.getRequestInfo());
                    
                    // Push to Kibana for indexing
                    FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facility, request.getRequestInfo());
                    facilityRepository.pushToKibana(kibanaIndex);
                }
                
                validatedFacilities.add(facility);
            }
        }

        return validatedFacilities;
    }

    /**
     * Checks whether a facility with the same name and boundary already exists
     * in the given tenant. Throws a CustomException if duplicate found.
     */
    private void validateFacilityNameBoundaryCodeUnique(Facility facility, String tenantId) {
        if (facility.getFacilityName() != null && facility.getBoundaryCode() != null) {
            boolean exists = facilityQueryDao.existsByFacilityNameAndBoundary(
                    tenantId, facility.getFacilityName(), facility.getBoundaryCode()
            );

            if (exists) {
                throw new CustomException("FACILITY_DUPLICATE_NAME_LOCATION",
                        "A facility with the same name and boundary already exists in this tenant");
            }
        }
    }

    /**
     * Checks whether the HFR ID or NIN ID already exists for another facility
     * in the same tenant. Throws a CustomException if duplicate found.
     */
    private void validateHfrOrNinUniqueness(Facility facility, String tenantId) {
        HealthFacilityDetails details = facility.getFacilityDetails();

        if (details != null) {
            String hfrId = details.getHfrId();
            String ninId = details.getNinId();

            if ((hfrId != null && !hfrId.isBlank()) || (ninId != null && !ninId.isBlank())) {
                boolean exists = facilityQueryDao.existsByHfrIdOrNinId(hfrId, ninId, tenantId);
                if (exists) {
                    throw new CustomException("FACILITY_DUPLICATE_ID",
                            "Facility with same HFR ID or NIN ID already exists in tenant " + tenantId);
                }
            }
        }
    }

    /**
     * Creates POC user as HRMS employee if not exists (checks by phone number uniqueness).
     * Validates required fields (HFR ID, POC contact, POC name) before attempting creation.
     *
     * @param facility The facility for which to create POC user
     * @param tenantId The tenant ID
     * @param requestInfo RequestInfo for API calls
     */
    private void createFacilityPOCUserIfNotExists(Facility facility, String tenantId, RequestInfo requestInfo) {
        HealthFacilityDetails facilityDetails = facility.getFacilityDetails();
        
        if (facilityDetails == null || facilityDetails.getHfrId() == null || 
            facilityDetails.getHfrId().isBlank() || facilityDetails.getPocContact() == null || 
            facilityDetails.getPocContact().isBlank() || facilityDetails.getPocName() == null) {
            log.warn("Cannot create POC user for facility {}: missing HFR ID, POC contact, or POC name", 
                    facility.getFacilityId());
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
                        facility.getFacilityId(), facilityDetails.getHfrId());
            } else {
                log.warn("Failed to create POC user for facility {}", facility.getFacilityId());
            }
        } else {
            log.info("POC user with mobile number {} already exists for facility {}, skipping creation", 
                    facilityDetails.getPocContact(), facility.getFacilityId());
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
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();

        if (update.getFacilityId() == null || update.getTenantId() == null) {
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }

        // Fetch existing facility to check current state
        String fetchExistingFacilitySql = "SELECT * FROM facility WHERE id = ? AND tenant_id = ?";
        Facility existingFacility;
        try {
            existingFacility = jdbcTemplate.queryForObject(fetchExistingFacilitySql, new Object[]{update.getFacilityId(), update.getTenantId()}, facilityRowMapper.rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null; // facility not found
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

        // Validate with MDMS and boundary APIs
        facilityMdmsValidator.validateAgainstMDMS(List.of(facility), update.getTenantId(), request.getRequestInfo());
        if (facility.getBoundaryCode() != null) {
            boundaryValidator.validateBoundaries(
                    Set.of(facility.getBoundaryCode()),
                    update.getTenantId(),
                    request.getRequestInfo());
        }

        if (facility.getWfStatus() == null) facility.setWfStatus("UPDATED");
        if (facility.getIsActive() == null) facility.setIsActive(true);

        facilityRepository.pushUpdateFacility(request);
        
        // If user sent isOnmReady = true, handle POC user creation and Kibana push
        if (Boolean.TRUE.equals(update.getIsOnmReady())) {
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
                log.info("Facility {} already exists in Kibana, skipping push", update.getFacilityId());
                return facility;
            }
            
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
                    .build();
            
            // Transform to Kibana index format and push
            FacilityKibanaIndex kibanaIndex = facilityKibanaMapper.toKibanaIndex(facilityForKibana, request.getRequestInfo());
            facilityRepository.pushToKibana(kibanaIndex);
            log.info("Facility {} pushed to Kibana successfully", update.getFacilityId());
        }
        
        return facility;
    }

    /**
     * Searches for facilities using filters like tenantId, name, hfrId, ninId, boundary, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> searchFacilities(FacilitySearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);

        StringBuilder query = new StringBuilder("SELECT * FROM facility");
        query.append(result.getWhereClause());
        query.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<Object> allParams = new ArrayList<>(result.getParams());
        allParams.add(request.getLimit());
        allParams.add(request.getOffset());

        return jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
    }

    /**
     * Searches for facilities using filters like multiple tenantIds, names, hfrIds, ninIds, boundaries, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> bulkSearchFacilities(FacilityBulkSearchRequest request) {
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

        log.info("Bulk Search Query: {}", query);
        log.info("Bulk Search Params: {}", allParams);
        return jdbcTemplate.query(query.toString(), allParams.toArray(), facilityRowMapper.rowMapper);
    }


    /**
     * Fetches a one-line summary of a facility using its ID.
     * If not found, returns null.
     *
     * @param facilityId unique ID of the facility
     * @return a FacilitySummary object
     */
    public FacilitySummary getFacilitySummary(String facilityId) {
        String sql = "SELECT facility_name, facility_type FROM facility WHERE facility_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{facilityId}, (rs, rowNum) -> {
                String name = rs.getString("facility_name");
                String type = rs.getString("facility_type");
                FacilitySummary summary = new FacilitySummary();
                summary.setSummary("Facility '" + name + "' is of type '" + type + "'.");
                return summary;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int countFacilities(FacilitySearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildWhereClause(request);
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
        return jdbcTemplate.queryForObject(query, result.getParams().toArray(), Integer.class);
    }

    public int countFacilitiesForBulkSearch(FacilityBulkSearchRequest request) {
        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                request.getFacilityBulkSearchCriteria(), request.getRequestInfo(), configs.getOnmNonReadyAllowedRoles()
        );
        String query = "SELECT COUNT(*) FROM facility" + result.getWhereClause();
        return jdbcTemplate.queryForObject(query, result.getParams().toArray(), Integer.class);
    }


}
