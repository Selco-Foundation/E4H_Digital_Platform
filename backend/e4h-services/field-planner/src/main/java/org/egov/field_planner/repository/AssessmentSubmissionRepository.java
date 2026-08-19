package org.egov.field_planner.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.egov.field_planner.repository.rowmapper.AssessmentSubmissionRowMapper;
import org.egov.field_planner.web.models.AssessmentSubmission;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AssessmentSubmissionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AssessmentSubmissionRowMapper rowMapper;
    private final ObjectMapper objectMapper;

    public AssessmentSubmission insert(AssessmentSubmission submission) {
        String id = submission.getId() != null ? submission.getId() : UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        String dataJson = toJson(submission.getSubmissionData());

        jdbcTemplate.update(
                """
                INSERT INTO eg_assessment_submission (
                    id, tenant_id, plan_id, plan_facility_id, facility_id,
                    assessment_phase, form_type, submitted_by, submitted_by_name,
                    submission_data, outcome, client_submission_time, server_received_time, created_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
                """,
                id,
                submission.getTenantId(),
                submission.getPlanId(),
                submission.getPlanFacilityId(),
                submission.getFacilityId(),
                submission.getAssessmentPhase(),
                submission.getFormType(),
                submission.getSubmittedBy(),
                submission.getSubmittedByName(),
                dataJson,
                submission.getOutcome(),
                submission.getClientSubmissionTime(),
                submission.getServerReceivedTime() != null ? submission.getServerReceivedTime() : now,
                now
        );
        submission.setId(id);
        if (submission.getServerReceivedTime() == null) {
            submission.setServerReceivedTime(now);
        }
        return submission;
    }

    public List<AssessmentSubmission> findByPlanFacilityId(String planFacilityId) {
        return jdbcTemplate.query(
                """
                SELECT id, tenant_id, plan_id, plan_facility_id, facility_id, assessment_phase,
                       form_type, submitted_by, submitted_by_name, submission_data, outcome,
                       client_submission_time, server_received_time
                FROM eg_assessment_submission
                WHERE plan_facility_id = ?
                ORDER BY server_received_time ASC
                """,
                rowMapper,
                planFacilityId
        );
    }

    public Optional<AssessmentSubmission> findByPlanFacilityAndPhase(String planFacilityId, String phase) {
        try {
            AssessmentSubmission submission = jdbcTemplate.queryForObject(
                    """
                    SELECT id, tenant_id, plan_id, plan_facility_id, facility_id, assessment_phase,
                           form_type, submitted_by, submitted_by_name, submission_data, outcome,
                           client_submission_time, server_received_time
                    FROM eg_assessment_submission
                    WHERE plan_facility_id = ? AND assessment_phase = ?
                    """,
                    rowMapper,
                    planFacilityId,
                    phase
            );
            return Optional.ofNullable(submission);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsForPhase(String planFacilityId, String phase) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eg_assessment_submission WHERE plan_facility_id = ? AND assessment_phase = ?",
                Integer.class,
                planFacilityId,
                phase
        );
        return count != null && count > 0;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize submission data", e);
        }
    }
}
