package facility.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FacilityRowMapper {

    // Jackson ObjectMapper for parsing JSONB columns from the database
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * RowMapper implementation to convert JDBC ResultSet into a Facility object.
     * Handles plain string fields and deserializes JSONB columns like facilityDetails,
     * additionalDetails, and address.
     */
    public final RowMapper<Facility> rowMapper = (rs, rowNum) -> {
        Facility facility = new Facility();

        // Set simple fields from result set
        facility.setFacilityId(rs.getString("facility_id"));
        facility.setTenantId(rs.getString("tenant_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setWfStatus(rs.getString("wf_status"));
        facility.setIsActive(rs.getBoolean("is_active"));

        // Optional field
        String region = rs.getString("facility_region");
        if (region != null) {
            facility.setFacilityRegion(region);
        }

        try {
            // Deserialize JSONB column: facility_details (Map<String, Object>)
            String detailsJson = rs.getString("facility_details");
            if (detailsJson != null) {
                facility.setFacilityDetails(mapper.readValue(detailsJson, new TypeReference<HealthFacilityDetails>() {}));
            }

            // Deserialize JSONB column: additional_details (Map<String, Object>)
            String additionalJson = rs.getString("additional_details");
            if (additionalJson != null) {
                facility.setAdditionalDetails(mapper.readValue(additionalJson, new TypeReference<Map<String, Object>>() {}));
            }

            // Deserialize JSONB column: address (FacilityAddress)
            String addressJson = rs.getString("address");
            if (addressJson != null) {
                facility.setAddress(mapper.readValue(addressJson, FacilityAddress.class));
            }
        } catch (JsonProcessingException e) {
            // Wrap JSON parsing issues in a RuntimeException to fail fast
            throw new RuntimeException("Error parsing JSONB fields", e);
        }

        return facility;
    };
}
