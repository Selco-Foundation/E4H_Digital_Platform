package org.egov.activity.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.activity.web.models.BillOfMaterial;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class BomRowMapper implements RowMapper<BillOfMaterial> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BillOfMaterial mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return BillOfMaterial.builder()
                    .id(resultSet.getString("bom_bomId"))
                    .tenantId(resultSet.getString("bom_tenantId"))
                    .facilityId(resultSet.getString("bom_facilityId"))
                    .name(resultSet.getString("bom_name"))
                    .assignUser(resultSet.getString("bom_assignedUser"))
                    .isActive(resultSet.getBoolean("bom_isActive"))
                    .additionalDetails(
                            resultSet.getString("bom_additionalDetails") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("bom_additionalDetails"), Map.class))
                    .data(
                            resultSet.getString("bom_data") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("bom_data"), Map.class))
                    .auditDetails(AuditDetails.builder()
                            .lastModifiedTime(resultSet.getLong("bom_lastModifiedTime"))
                            .createdTime(resultSet.getLong("bom_createdTime"))
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}