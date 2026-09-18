package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.field_planner.web.models.AssessmentSubmission;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class AssessmentSubmissionRowMapper implements RowMapper<AssessmentSubmission> {

    private final ObjectMapper objectMapper;

    public AssessmentSubmissionRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AssessmentSubmission mapRow(ResultSet rs, int rowNum) throws SQLException {
        return AssessmentSubmission.builder()
                .id(rs.getString("id"))
                .planFacilityId(rs.getString("plan_facility_id"))
                .planId(rs.getString("plan_id"))
                .facilityId(rs.getString("facility_id"))
                .assessmentPhase(rs.getString("assessment_phase"))
                .formType(rs.getString("form_type"))
                .outcome(rs.getString("outcome"))
                .submittedBy(rs.getString("submitted_by"))
                .submittedByName(rs.getString("submitted_by_name"))
                .submissionData(readMap(rs, "submission_data"))
                .clientSubmissionTime(rs.getObject("client_submission_time") != null
                        ? rs.getLong("client_submission_time") : null)
                .serverReceivedTime(rs.getLong("server_received_time"))
                .build();
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
