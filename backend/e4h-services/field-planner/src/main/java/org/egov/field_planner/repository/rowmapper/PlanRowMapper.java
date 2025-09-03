package org.egov.field_planner.repository.rowmapper;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Project;
import org.egov.field_planner.web.models.FieldPlan;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PlanRowMapper implements RowMapper<FieldPlan> {

    @Override
    public FieldPlan mapRow(ResultSet resultSet, int i) throws SQLException {
        return FieldPlan.builder()
                .id(resultSet.getString("id"))
                .tenantId(resultSet.getString("tenantid"))
                .startDate(resultSet.getLong("startDate"))
                .endDate(resultSet.getLong("endDate"))
                .auditDetails(AuditDetails.builder()
                        .createdBy(resultSet.getString("createdby"))
                        .createdTime(resultSet.getLong("createdtime"))
                        .lastModifiedBy(resultSet.getString("lastmodifiedby"))
                        .lastModifiedTime(resultSet.getLong("lastmodifiedtime"))
                        .build())
                .isDeleted(resultSet.getBoolean("isdeleted"))
                .build();
    }
}
