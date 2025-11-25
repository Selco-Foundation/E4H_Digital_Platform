package facility.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
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
public class FacilityRowMapper {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JdbcTemplate jdbcTemplate;

    public FacilityRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final RowMapper<Facility> rowMapper = (rs, rowNum) -> {
        Facility facility = new Facility();

        facility.setFacilityId(rs.getString("id"));
        facility.setTenantId(rs.getString("tenant_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setWfStatus(rs.getString("wf_status"));
        facility.setIsActive(rs.getBoolean("is_active"));
        facility.setFacilityRegion(rs.getString("facility_region"));
        facility.setIsOnmReady(rs.getBoolean("is_onm_ready"));

        String addressId = rs.getString("addressid");

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
            throw new RuntimeException("Error parsing JSON fields in facility record", e);
        }

        return facility;
    };

    private FacilityAddress fetchAddressById(String addressId) {
        String sql = "SELECT * FROM facility_address WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{addressId}, (rs, rowNum) -> {
                FacilityAddress address = new FacilityAddress();
                address.setAddressId(rs.getString("id"));
                address.setTenantId(rs.getString("tenant_id"));
                address.setLatitude(rs.getDouble("latitude"));
                address.setLongitude(rs.getDouble("longitude"));
                address.setAddressLine1(rs.getString("addressLine1"));
                address.setAddressLine2(rs.getString("addressLine2"));
                address.setCity(rs.getString("city"));
                address.setPincode(rs.getString("pincode"));
                address.setLandmark(rs.getString("landmark"));
                return address;
            });
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Address not available");
        }
    }
}
