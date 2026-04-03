package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class ActivityAssignmentRowMapper implements RowMapper<ActivityAssignment> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActivityAssignment mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return ActivityAssignment.builder()
                    .id(resultSet.getString("aa_activityAssignmentId"))
                    .tenantId(resultSet.getString("aa_tenantId"))
                    .fieldPlanId(resultSet.getString("aa_fieldPlanId"))
                    .activityId(resultSet.getString("aa_activityId"))
                    .status(resultSet.getString("aa_status"))
                    .assignedTo(resultSet.getString("aa_assignedTo"))
                    .assignedBy(resultSet.getString("aa_assignedBy"))
                    .startDate(resultSet.getLong("aa_startDate"))
                    .endDate(resultSet.getLong("aa_endDate"))
                    .isDeleted(resultSet.getBoolean("aa_isdeleted"))
                    .isEmailSent(resultSet.getBoolean("aa_emailSent"))
                    .pocNumber(resultSet.getString("aa_pocNumber"))
                    .additionalDetails(
                            resultSet.getString("aa_additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("aa_additionalDetails"), Map.class))
                    .role(
                            resultSet.getString("aa_role") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("aa_role"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .lastModifiedTime(resultSet.getLong("aa_lastModifiedTime"))
                            .createdTime(resultSet.getLong("aa_createdTime"))
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}