package facility.service;

import facility.repository.FacilityRepository;
import facility.util.IdgenUtil;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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

    public FacilityService(
            FacilityRepository facilityRepository,
            JdbcTemplate jdbcTemplate,
            FacilityRowMapper facilityRowMapper,
            IdgenUtil idgenUtil,
            FacilityMdmsValidator facilityMdmsValidator,
            BoundaryValidator boundaryValidator,
            FacilityQueryDao facilityQueryDao
    ) {
        this.facilityRepository = facilityRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.facilityRowMapper = facilityRowMapper;
        this.idgenUtil = idgenUtil;
        this.facilityMdmsValidator = facilityMdmsValidator;
        this.boundaryValidator = boundaryValidator;
        this.facilityQueryDao = facilityQueryDao;
    }

    /**
     * Creates facilities in the system after validation.
     * Validates against MDMS, ensures boundary codes are valid,
     * generates facility IDs and address IDs if missing, and checks for uniqueness.
     *
     * @param request FacilityCreateRequest containing a list of facilities
     * @return list of successfully validated and pushed facilities
     */
    public List<Facility> createFacility(FacilityCreateRequest request) {
        List<Facility> facilities = request.getFacilities();

        // Group facilities by tenant ID for batch validation and processing
        Map<String, List<Facility>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(Facility::getTenantId));

        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<Facility>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<Facility> tenantFacilities = entry.getValue();

            // Validate facilities against MDMS master data
            facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());

            // Validate boundary codes in bulk
            Set<String> boundaryCodes = tenantFacilities.stream()
                    .map(Facility::getBoundaryCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            boundaryValidator.validateBoundaries(boundaryCodes, tenantId, request.getRequestInfo());

            for (Facility facility : tenantFacilities) {
                // Generate facility ID if not present
                if (facility.getFacilityId() == null) {
                    facility.setFacilityId(idgenUtil.getIdList(
                            request.getRequestInfo(), tenantId, "facility.id", "", 1).get(0));
                }

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
        String checkSql = "SELECT COUNT(*) FROM facility WHERE facility_id = ? AND tenant_id = ?";
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
        return facility;
    }

    /**
     * Searches for facilities using filters like tenantId, name, hfrId, ninId, boundary, etc.
     * Supports pagination using limit and offset.
     *
     * @return List of facilities matching the filter
     */
    public List<Facility> searchFacilities(String tenantId, String facilityId, String facilityName,
                                           String hfrId, String ninId, String boundaryCode,
                                           int limit, int offset) {

        StringBuilder query = new StringBuilder("SELECT * FROM facility WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(tenantId);
        }

        if (facilityId != null && !facilityId.isBlank()) {
            query.append(" AND facility_id::text = ?");
            params.add(facilityId);
        }

        if (facilityName != null && !facilityName.isBlank()) {
            query.append(" AND facility_name ILIKE ?");
            params.add("%" + facilityName + "%");
        }

        if (hfrId != null && !hfrId.isBlank()) {
            query.append(" AND facility_details->>'hfrId' = ?");
            params.add(hfrId);
        }

        if (ninId != null && !ninId.isBlank()) {
            query.append(" AND facility_details->>'ninId' = ?");
            params.add(ninId);
        }

        if (boundaryCode != null && !boundaryCode.isBlank()) {
            query.append(" AND boundary_code = ?");
            params.add(boundaryCode);
        }

        // Add pagination and sort
        query.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(query.toString(), params.toArray(), facilityRowMapper.rowMapper);
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
}
