package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.field_planner.web.models.PlanFacility;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AssessmentQueueFacilityRowMapper extends AssessmentFacilityRowMapper {

    public AssessmentQueueFacilityRowMapper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public PlanFacility mapRow(ResultSet rs, int rowNum) throws SQLException {
        PlanFacility facility = super.mapRow(rs, rowNum);
        facility.setPlanName(rs.getString("plan_name"));
        facility.setState(readPlanState(rs));
        return facility;
    }

    private String readPlanState(ResultSet rs) throws SQLException {
        Object value = rs.getObject("plan_geography_scope");
        if (value == null) {
            return null;
        }
        try {
            JsonNode geography;
            if (value instanceof PGobject pgObject) {
                geography = objectMapper.readTree(pgObject.getValue());
            } else {
                geography = objectMapper.readTree(value.toString());
            }
            if (geography != null && geography.has("state") && !geography.get("state").isNull()) {
                return geography.get("state").asText();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
