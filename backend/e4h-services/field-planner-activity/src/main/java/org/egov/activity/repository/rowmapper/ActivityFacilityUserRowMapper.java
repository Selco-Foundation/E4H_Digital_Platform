package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.project.ProjectStaff;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class ActivityFacilityUserRowMapper implements RowMapper<ActivityFacilityUser> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActivityFacilityUser mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return ActivityFacilityUser.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .activityFacilityId(resultSet.getString("activityFacilityId"))
                    .userId(resultSet.getString("userId"))
                    .additionalDetails(
                            resultSet.getString("additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("additionalDetails"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .createdBy(resultSet.getString("createdby"))
                            .createdTime(resultSet.getLong("createdtime"))
                            .lastModifiedBy(resultSet.getString("lastmodifiedby"))
                            .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                            .build())
                    .isDeleted(resultSet.getBoolean("isdeleted"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}