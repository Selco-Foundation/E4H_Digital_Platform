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
                .tenantId(resultSet.getString("tenant_id"))
                .startDate(resultSet.getLong("start_date"))
                .endDate(resultSet.getLong("end_date"))
                .auditDetails(AuditDetails.builder()
                        .createdBy(resultSet.getString("created_by"))
                        .createdTime(resultSet.getLong("created_time"))
                        .lastModifiedBy(resultSet.getString("last_modified_by"))
                        .lastModifiedTime(resultSet.getLong("last_modified_time"))
                        .build())
                .isDeleted(resultSet.getBoolean("isdeleted"))
                .build();
    }
}
