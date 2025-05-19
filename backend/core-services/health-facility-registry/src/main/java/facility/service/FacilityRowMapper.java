package facility.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class FacilityRowMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public final RowMapper<Facility> rowMapper = (rs, rowNum) -> {
        Facility facility = new Facility();
        facility.setFacilityId(rs.getString("facility_id"));
        facility.setTenantId(rs.getString("tenant_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));

        String region = rs.getString("facility_region");
        if (region != null) {
            facility.setFacilityRegion(region);
        }

        facility.setWfStatus(rs.getString("wf_status"));
        facility.setIsActive(rs.getBoolean("is_active"));

        try {
            String detailsJson = rs.getString("facility_details");
            if (detailsJson != null) {
                facility.setFacilityDetails(mapper.readValue(detailsJson, new TypeReference<>() {
                }));
            }

            String additionalJson = rs.getString("additional_details");
            if (additionalJson != null) {
                facility.setAdditionalDetails(mapper.readValue(additionalJson, new TypeReference<>() {
                }));
            }

            String addressJson = rs.getString("address");
            if (addressJson != null) {
                facility.setAddress(mapper.readValue(addressJson, FacilityAddress.class));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSONB fields", e);
        }

        return facility;
    };
}

