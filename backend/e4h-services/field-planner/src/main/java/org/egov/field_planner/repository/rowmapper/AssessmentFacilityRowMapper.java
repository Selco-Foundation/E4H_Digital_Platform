package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.field_planner.util.AssessmentAdditionalDetailsHelper;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.PlanFacility;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class AssessmentFacilityRowMapper implements RowMapper<PlanFacility> {

    protected final ObjectMapper objectMapper;

    public AssessmentFacilityRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PlanFacility mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> additionalDetails = readMap(rs, "additional_details");
        String facilityName = getDetail(additionalDetails, "facilityName");
        String facilityCategory = getDetail(additionalDetails, "facilityCategory");
        String facilityType = getDetail(additionalDetails, "facilityType");
        String district = getDetail(additionalDetails, "district");
        String block = getDetail(additionalDetails, "block");

        String phoneStatus = rs.getString("phone_status");
        String fieldStatus = rs.getString("field_status");
        String phoneOutcome = resolveOutcome(AssessmentAdditionalDetailsHelper.getPhoneOutcome(additionalDetails),
                phoneStatus);
        String fieldOutcome = resolveOutcome(AssessmentAdditionalDetailsHelper.getFieldOutcome(additionalDetails),
                fieldStatus);

        return PlanFacility.builder()
                .planFacilityId(rs.getString("id"))
                .assessmentPlanId(rs.getString("field_plan_id"))
                .planId(rs.getString("field_plan_id"))
                .facilityId(rs.getString("facility_id"))
                .facilityName(facilityName)
                .facilityCategory(facilityCategory)
                .facilityType(facilityType)
                .district(district)
                .block(block)
                .phoneStatus(phoneStatus)
                .fieldStatus(fieldStatus)
                .overallStatus(rs.getString("overall_status"))
                .phoneOutcome(phoneOutcome)
                .fieldOutcome(fieldOutcome)
                .overallManuallySet(AssessmentAdditionalDetailsHelper.isOverallManuallySet(additionalDetails))
                .remarks(AssessmentAdditionalDetailsHelper.getRemarks(additionalDetails))
                .assessmentCompletionStatus(rs.getString("assessment_completion_status"))
                .installationFieldPlanId(rs.getString("installation_field_plan_id"))
                .lastActionTime(rs.getLong("last_modified_time"))
                .auditTrail(AssessmentAdditionalDetailsHelper.getAuditTrail(additionalDetails))
                .additionalDetails(additionalDetails)
                .build();
    }

    private String resolveOutcome(String storedOutcome, String phaseStatus) {
        if (storedOutcome != null) {
            return storedOutcome;
        }
        if (phaseStatus == null) {
            return null;
        }
        if (AssessmentConstants.REMOTE_DONE_STATUSES.contains(phaseStatus)
                || AssessmentConstants.FIELD_QUALIFIED.equals(phaseStatus)
                || AssessmentConstants.FIELD_NOT_QUALIFIED.equals(phaseStatus)) {
            return phaseStatus;
        }
        return null;
    }

    private String getDetail(Map<String, Object> additionalDetails, String key) {
        return additionalDetails != null && additionalDetails.get(key) != null
                ? additionalDetails.get(key).toString() : null;
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
