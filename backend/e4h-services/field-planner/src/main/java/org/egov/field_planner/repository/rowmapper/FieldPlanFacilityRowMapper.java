package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.project.ProjectFacility;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FieldPlanFacilityRowMapper implements RowMapper<FieldPlanFacility> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FieldPlanFacility mapRow(ResultSet resultSet, int i) throws SQLException {
        try {
            return FieldPlanFacility.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .fieldPlanId(resultSet.getString("field_plan_id"))
                    .facilityId(resultSet.getString("facility_id"))
                    .additionalFields(
                            resultSet.getString("additional_details") == null
                                    ? null
                                    : objectMapper.readValue(resultSet.getString("additional_details"), AdditionalFields.class))
                    .auditDetails(AuditDetails.builder()
                            .createdTime(resultSet.getLong("created_time"))
                            .lastModifiedBy(resultSet.getString("last_modified_by"))
                            .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                            .build())
                    .isDeleted(resultSet.getBoolean("isdeleted"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}