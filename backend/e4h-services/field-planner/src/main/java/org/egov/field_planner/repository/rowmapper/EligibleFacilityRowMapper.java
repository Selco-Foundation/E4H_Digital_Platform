package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.field_planner.web.models.EligibleFacility;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class EligibleFacilityRowMapper implements RowMapper<EligibleFacility> {

    private final ObjectMapper objectMapper;

    public EligibleFacilityRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public EligibleFacility mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> additionalDetails = readMap(rs, "additional_details");
        String facilityName = additionalDetails != null && additionalDetails.get("facilityName") != null
                ? additionalDetails.get("facilityName").toString() : null;
        return EligibleFacility.builder()
                .planFacilityId(rs.getString("id"))
                .assessmentPlanId(rs.getString("field_plan_id"))
                .assessmentPlanName(getString(rs, "plan_name"))
                .facilityId(rs.getString("facility_id"))
                .facilityName(facilityName)
                .assessmentCompletionStatus(rs.getString("assessment_completion_status"))
                .installationFieldPlanId(rs.getString("installation_field_plan_id"))
                .overallStatus(rs.getString("overall_status"))
                .projectId(rs.getString("project_id"))
                .build();
    }

    private String getString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Map<String, Object> readMap(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof PGobject pgObject) {
                return objectMapper.readValue(pgObject.getValue(), new TypeReference<Map<String, Object>>() {});
            }
            return objectMapper.readValue(value.toString(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
