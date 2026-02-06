package facility.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Maps ResultSet rows from the facility table into Facility objects.
 * Also fetches related address data from the facility_address table.
 */
@Service
@Slf4j
public class FacilityRowMapper {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JdbcTemplate jdbcTemplate;

    public FacilityRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final RowMapper<Facility> rowMapper = (rs, rowNum) -> {
        log.trace("Entering rowMapper for row {}", rowNum);
        Facility facility = new Facility();

        facility.setFacilityId(rs.getString("id"));
        facility.setTenantId(rs.getString("tenant_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setFacilityPocName(rs.getString("facility_poc_name"));
        facility.setFacilityPocPhone(rs.getString("facility_poc_phone"));
        facility.setFacilityPocEmail(rs.getString("facility_poc_email"));
        facility.setHfrId(rs.getString("hfr_id"));
        facility.setNinId(rs.getString("nin_id"));
        facility.setFacilityStatus(rs.getString("facility_status"));
        facility.setUserId(rs.getString("user_id"));
        facility.setWfStatus(rs.getString("wf_status"));
        facility.setIsActive(rs.getBoolean("is_active"));
        facility.setFacilityRegion(rs.getString("facility_region"));
        facility.setIsOnmReady(rs.getBoolean("is_onm_ready"));

        String addressId = rs.getString("addressid");
        log.debug("Mapping facility row {} with facilityId: {}", rowNum, facility.getFacilityId());

        try {
            String detailsJson = rs.getString("facility_details");
            if (detailsJson != null) {
                HealthFacilityDetails details = mapper.readValue(detailsJson, new TypeReference<HealthFacilityDetails>() {});
                facility.setFacilityDetails(details);
            }

            String additionalJson = rs.getString("additional_details");
            if (additionalJson != null) {
                Map<String, Object> additional = mapper.readValue(additionalJson, new TypeReference<Map<String, Object>>() {});
                facility.setAdditionalDetails(additional);
            }

            if (addressId != null) {
                FacilityAddress address = fetchAddressById(addressId);
                facility.setAddress(address);
            }

        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON fields in facility record for row {}: {}", rowNum, e.getMessage(), e);
            throw new RuntimeException("Error parsing JSON fields in facility record", e);
        }

        log.trace("Exiting rowMapper for row {}", rowNum);
        return facility;
    };

    private FacilityAddress fetchAddressById(String addressId) {
        log.trace("Entering fetchAddressById method for addressId: {}", addressId);
        String sql = "SELECT * FROM facility_address WHERE id = ?";

        try {
            FacilityAddress address = jdbcTemplate.queryForObject(sql, new Object[]{addressId}, (rs, rowNum) -> {
                FacilityAddress addr = new FacilityAddress();
                addr.setAddressId(rs.getString("id"));
                addr.setTenantId(rs.getString("tenant_id"));
                addr.setLatitude(rs.getDouble("latitude"));
                addr.setLongitude(rs.getDouble("longitude"));
                addr.setAddressLine1(rs.getString("addressLine1"));
                addr.setAddressLine2(rs.getString("addressLine2"));
                addr.setCity(rs.getString("city"));
                addr.setPincode(rs.getString("pincode"));
                addr.setLandmark(rs.getString("landmark"));
                return addr;
            });
            log.debug("Successfully fetched address for addressId: {}", addressId);
            log.trace("Exiting fetchAddressById method");
            return address;
        } catch (EmptyResultDataAccessException e) {
            log.error("Address not found for addressId: {}", addressId);
            throw new RuntimeException("Address not available");
        }
    }
}
