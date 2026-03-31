package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.web.models.Activity;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ActivityDataRowMapper implements RowMapper<Activity> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Activity mapRow(ResultSet resultSet, int i) throws SQLException {
        return Activity.builder()
                .id(resultSet.getString("id"))
                .tenantId(resultSet.getString("tenant_id"))
                .name(resultSet.getString("name"))
                .code(resultSet.getString("code"))
                .sequenceOrder(resultSet.getInt("sequence_order"))
                .build();
    }
}