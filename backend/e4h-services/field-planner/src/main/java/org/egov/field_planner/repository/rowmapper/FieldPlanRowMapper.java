package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.AddressType;
import org.egov.common.models.project.Project;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FieldPlanRowMapper implements ResultSetExtractor<List<FieldPlan>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<FieldPlan> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, FieldPlan> projectMap = new LinkedHashMap<>();
        while (rs.next()) {
            String fieldPlanName = rs.getString("name");

            if (!projectMap.containsKey(fieldPlanName)) {
                projectMap.put(fieldPlanName, createProjectObj(rs));
            }
        }

        return new ArrayList<>(projectMap.values());
    }

    private FieldPlan createProjectObj(ResultSet rs) throws SQLException, DataAccessException {
        FieldPlan project = getProjectObjFromResultSet(rs);
        return project;
    }

    /* Builds Project Object from Result Set and address */
    private FieldPlan getProjectObjFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String tenantId = rs.getString("tenant_id");

        FieldPlan fieldPlan = FieldPlan.builder()
                .name(name)
                .tenantId(tenantId)
                .build();

        return fieldPlan;
    }

    private JsonNode getAdditionalDetail(String columnName, ResultSet rs)    throws SQLException {
        JsonNode additionalDetails = null;
        try {
            PGobject obj = (PGobject) rs.getObject(columnName);
            if (obj != null) {
                additionalDetails = objectMapper.readTree(obj.getValue());
            }
        } catch (IOException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse additionalDetail object");
        }
        if (additionalDetails == null || additionalDetails.isEmpty())
            additionalDetails = null;
        return additionalDetails;
    }
}
