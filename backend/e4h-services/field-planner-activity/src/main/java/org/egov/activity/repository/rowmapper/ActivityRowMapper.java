package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.activity.web.models.ActivityFacility;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ActivityRowMapper implements RowMapper<ActivityFacility> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActivityFacility mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            List<String> linkedUsers = null;
            Array linkedUsersArray = resultSet.getArray("fa_linkedUsers");
            if (linkedUsersArray != null && linkedUsersArray.getArray() instanceof String[] users) {
                linkedUsers = Arrays.asList(users);
            }

            return ActivityFacility.builder()
                    .id(resultSet.getString("fa_facilityActivityId"))
                    .tenantId(resultSet.getString("fa_tenantId"))
                    .fieldPlanId(resultSet.getString("fa_fieldPlanId"))
                    .facilityId(resultSet.getString("fa_facilityId"))
                    .activityId(resultSet.getString("fa_activityId"))
                    .activityType(resultSet.getString("activity_type"))
                    .scheduledAt(resultSet.getLong("fa_scheduledAt"))
//                    .assignedUser(resultSet.getString("fu.userid"))
                    .assignedUser(resultSet.getString("fa_assignedUser"))
                    .activatedAt(resultSet.getLong("fa_activatedAt"))
                    .completedAt(resultSet.getLong("fa_completedAt"))
                    .status(resultSet.getString("fa_status"))
                    .linkedUsers(linkedUsers)
                    .additionalDetails(
                            resultSet.getString("fa_additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("fa_additionalDetails"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .lastModifiedTime(resultSet.getLong("fa_lastModifiedTime"))
                            .createdTime(resultSet.getLong("fa_createdTime"))
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}