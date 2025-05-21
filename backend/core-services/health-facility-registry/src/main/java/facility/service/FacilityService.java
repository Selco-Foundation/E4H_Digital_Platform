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
            BoundaryValidator boundaryValidator, FacilityQueryDao facilityQueryDao
    ) {
        this.facilityRepository = facilityRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.facilityRowMapper = facilityRowMapper;
        this.idgenUtil = idgenUtil;
        this.facilityMdmsValidator = facilityMdmsValidator;
        this.boundaryValidator = boundaryValidator;
        this.facilityQueryDao = facilityQueryDao;
    }

    public List<Facility> createFacility(FacilityCreateRequest request) {
        List<Facility> facilities = request.getFacilities();
        Map<String, List<Facility>> facilitiesByTenant = facilities.stream()
                .collect(Collectors.groupingBy(Facility::getTenantId));

        List<Facility> validatedFacilities = new ArrayList<>();

        for (Map.Entry<String, List<Facility>> entry : facilitiesByTenant.entrySet()) {
            String tenantId = entry.getKey();
            List<Facility> tenantFacilities = entry.getValue();

            // --- Bulk MDMS Validation ---
            facilityMdmsValidator.validateAgainstMDMS(tenantFacilities, tenantId, request.getRequestInfo());

            // --- Collect all boundaryCodes and validate in bulk ---
            Set<String> boundaryCodes = tenantFacilities.stream()
                    .map(Facility::getBoundaryCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            boundaryValidator.validateBoundaries(boundaryCodes, tenantId, request.getRequestInfo());

            for (Facility facility : tenantFacilities) {
                if (facility.getFacilityId() == null) {
                    facility.setFacilityId(idgenUtil.getIdList(
                            request.getRequestInfo(), tenantId, "facility.id", "", 1).get(0));
                }

                if (facility.getWfStatus() == null) facility.setWfStatus("CREATED");
                if (facility.getIsActive() == null) facility.setIsActive(true);

                if (facility.getAddress().getAddressId() == null) {
                    facility.getAddress().setAddressId(UUID.randomUUID().toString());
                }
                validateHfrOrNinUniqueness(facility, tenantId);
                facilityRepository.pushCreateFacility(facility);
                validatedFacilities.add(facility);
            }
        }

        return validatedFacilities;
    }

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



    public Facility updateFacility(FacilityUpdateRequest request) {
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();

        if (update.getFacilityId() == null || update.getTenantId() == null) {
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }

        String checkSql = "SELECT COUNT(*) FROM facility WHERE facility_id = ? AND tenant_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, update.getFacilityId(), update.getTenantId());
        if (count == null || count == 0) {
            return null;
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

        facilityMdmsValidator.validateAgainstMDMS(List.of(facility), update.getTenantId(), request.getRequestInfo());
        boundaryValidator.validateBoundaries(Set.of(facility.getBoundaryCode()), update.getTenantId(), request.getRequestInfo());

        if (facility.getWfStatus() == null) facility.setWfStatus("UPDATED");
        if (facility.getIsActive() == null) facility.setIsActive(true);

        facilityRepository.pushUpdateFacility(request);
        return facility;
    }

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

        query.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(query.toString(), params.toArray(), facilityRowMapper.rowMapper);
    }


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
