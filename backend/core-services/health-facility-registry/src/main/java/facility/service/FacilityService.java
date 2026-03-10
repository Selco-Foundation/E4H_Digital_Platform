package facility.service;

import facility.config.Configuration;
import facility.repository.FacilityRepository;
import facility.util.*;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
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

    public FacilityService(
            FacilityRepository facilityRepository,
            JdbcTemplate jdbcTemplate,
            FacilityRowMapper facilityRowMapper,
            IdgenUtil idgenUtil,
            FacilityMdmsValidator facilityMdmsValidator,
            BoundaryValidator boundaryValidator,
            FacilityQueryDao facilityQueryDao,
            BoundaryService boundaryService,
            Configuration configs
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

        // Check if the facility exists in DB before attempting an update
        String checkSql = "SELECT COUNT(*) FROM facility WHERE id = ? AND tenant_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, update.getFacilityId(), update.getTenantId());
        if (count == null || count == 0) {
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

        facilityRepository.pushUpdateFacility(request);
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

        StringBuilder query = new StringBuilder(
                "SELECT facility.*, (SELECT EXISTS(SELECT 1 FROM facility_rms_inactive_incident r WHERE r.facilityid = facility.id AND r.tenantid = facility.tenant_id)) AS rms_inactive FROM facility");
        query.append(result.getWhereClause());
        query.append(" ORDER BY created_at DESC NULLS LAST LIMIT ? OFFSET ?");

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


}
