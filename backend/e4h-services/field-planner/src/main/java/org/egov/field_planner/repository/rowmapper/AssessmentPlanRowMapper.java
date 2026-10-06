package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.field_planner.web.models.AssessmentPlan;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AssessmentPlanRowMapper implements RowMapper<AssessmentPlan> {

    private final ObjectMapper objectMapper;

    public AssessmentPlanRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AssessmentPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
        JsonNode geographyScope = readJson(rs, "geography_scope");
        JsonNode additionalDetails = readJson(rs, "additional_details");
        String state = geographyScope != null && geographyScope.has("state")
                ? geographyScope.get("state").asText()
                : null;

        AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(rs.getString("created_by"))
                .createdTime(rs.getLong("created_time"))
                .lastModifiedBy(rs.getString("last_modified_by"))
                .lastModifiedTime(rs.getLong("last_modified_time"))
                .build();

        Map<String, Object> geographyDetails = geographyScope != null
                ? objectMapper.convertValue(geographyScope, new TypeReference<Map<String, Object>>() {})
                : new HashMap<>();

        return AssessmentPlan.builder()
                .id(rs.getString("id"))
                .tenantId(rs.getString("tenant_id"))
                .projectId(rs.getString("project_id"))
                .name(rs.getString("name"))
                .state(state)
                .startDate(rs.getLong("start_date"))
                .endDate(rs.getLong("end_date"))
                .status(rs.getString("status"))
                .planType(rs.getString("plan_type"))
                .healthFacilityCount(rs.getInt("health_facility_number"))
                .geographyDetails(geographyDetails)
                .auditDetails(auditDetails)
                .build();
    }

    private JsonNode readJson(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof PGobject pgObject) {
                return objectMapper.readTree(pgObject.getValue());
            }
            return objectMapper.readTree(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
