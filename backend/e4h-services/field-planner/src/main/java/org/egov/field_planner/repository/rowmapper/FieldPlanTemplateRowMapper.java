package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.field_planner.web.models.FieldPlanTemplate;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class FieldPlanTemplateRowMapper implements RowMapper<FieldPlanTemplate> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FieldPlanTemplate mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        try {
            return FieldPlanTemplate.builder()
                    .id(resultSet.getString("fpt_id"))
                    .tenantId(resultSet.getString("fpt_tenantId"))
                    .fieldPlanId(resultSet.getString("fpt_fieldPlanId"))
                    .systemType(resultSet.getString("fpt_systemType"))
                    .totalCapacity(resultSet.getString("fpt_totalCapacity"))
                    .fileStoreId(resultSet.getString("fpt_fileStoreId"))
                    .templateData(readJsonMap(resultSet, "fpt_templateData"))
                    .auditDetails(AuditDetails.builder()
                            .createdBy(resultSet.getString("fpt_createdBy"))
                            .lastModifiedBy(resultSet.getString("fpt_lastModifiedBy"))
                            .createdTime(resultSet.getLong("fpt_createdTime"))
                            .lastModifiedTime(resultSet.getLong("fpt_lastModifiedTime"))
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> readJsonMap(ResultSet resultSet, String column) throws SQLException, JsonProcessingException {
        Object value = resultSet.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof PGobject pgObject) {
            return objectMapper.readValue(pgObject.getValue(), Map.class);
        }
        return objectMapper.readValue(value.toString(), Map.class);
    }
}
