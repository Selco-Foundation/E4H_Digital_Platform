package facility.service;

import facility.repository.FacilityRepository;
import facility.util.IdgenUtil;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FacilityRowMapper facilityRowMapper;
    private final IdgenUtil idgenUtil;
    private final FacilityMdmsValidator facilityMdmsValidator;
    private final BoundaryValidator boundaryValidator;

    public FacilityService(
            FacilityRepository facilityRepository,
            JdbcTemplate jdbcTemplate,
            FacilityRowMapper facilityRowMapper,
            IdgenUtil idgenUtil,
            FacilityMdmsValidator facilityMdmsValidator,
            BoundaryValidator boundaryValidator
    ) {
        this.facilityRepository = facilityRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.facilityRowMapper = facilityRowMapper;
        this.idgenUtil = idgenUtil;
        this.facilityMdmsValidator = facilityMdmsValidator;
        this.boundaryValidator = boundaryValidator;
    }

    public List<Facility> createFacility(FacilityCreateRequest request) {
        List<Facility> facilities = new ArrayList<>();
        for (Facility facility : request.getFacilities()) {
            String tenantId = facility.getTenantId();

            facilityMdmsValidator.validateAgainstMDMS(facility, tenantId, request.getRequestInfo());
            boundaryValidator.validateBoundary(facility.getBoundaryCode(), tenantId, request.getRequestInfo());

            if (facility.getFacilityId() == null) {
                facility.setFacilityId(idgenUtil.getIdList(request.getRequestInfo(), tenantId, "facility.id", "", 1).get(0));
            }

            if (facility.getWfStatus() == null) facility.setWfStatus("CREATED");
            if (facility.getIsActive() == null) facility.setIsActive(true);

            facilityRepository.pushCreateFacility(facility);
            facilities.add(facility);
        }
        return facilities;
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

        facilityMdmsValidator.validateAgainstMDMS(facility, update.getTenantId(), request.getRequestInfo());
        boundaryValidator.validateBoundary(facility.getBoundaryCode(), update.getTenantId(), request.getRequestInfo());

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
