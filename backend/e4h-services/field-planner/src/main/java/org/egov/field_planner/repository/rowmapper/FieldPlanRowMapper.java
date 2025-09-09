package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;

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
            String fieldPlanId = rs.getString("fieldPlanId");

            if (!projectMap.containsKey(fieldPlanId)) {
                projectMap.put(fieldPlanId, createFieldPlanObj(rs));
            }
        }

        return new ArrayList<>(projectMap.values());
    }

    private FieldPlan createFieldPlanObj(ResultSet rs) throws SQLException, DataAccessException {
        FieldPlan project = getProjectObjFromResultSet(rs);
        return project;
    }

    /* Builds Project Object from Result Set and address */
    private FieldPlan getProjectObjFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("fieldPlanId");
        String name = rs.getString("fp_name");
        String tenantId = rs.getString("fp_tenantId");
        String projectId = rs.getString("fp_projectId");
        int healthFacilityNumber = rs.getInt("fp_healthFacilityNumber");
        String status = rs.getString("fp_status");
        long startDate = rs.getLong("fp_startDate");
        long endDate = rs.getLong("fp_endDate");
        JsonNode geographyScope = getAdditionalDetail("fp_geographyScope", rs);
        JsonNode additionalDetails = getAdditionalDetail("fp_additionalDetails", rs);
        List<Map<String, Object>> activities = getSelectedActivities("fp_selectedActivities", rs);
        Boolean isDeleted = rs.getBoolean("fp_isDeleted");
        String createdBy = rs.getString("fp_createdBy");
        String lastModifiedBy = rs.getString("fp_lastModifiedBy");
        Long createdTime = rs.getLong("fp_createdTime");
        Long lastModifiedTime = rs.getLong("fp_lastModifiedTime");

        AuditDetails auditDetails = AuditDetails.builder().createdBy(createdBy).createdTime(createdTime)
                .lastModifiedBy(lastModifiedBy).lastModifiedTime(lastModifiedTime)
                .build();

        FieldPlan fieldPlan = FieldPlan.builder()
                .id(id)
                .name(name)
                .tenantId(tenantId)
                .projectId(projectId)
                .healthFacilityNumber(healthFacilityNumber)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .geographyDetails(objectMapper.convertValue(geographyScope, Map.class))
                .additionalDetails(objectMapper.convertValue(additionalDetails, Map.class))
                .activities(activities)
                .isDeleted(isDeleted)
                .auditDetails(auditDetails)
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

    /**
     * Convertit une colonne JSON/JSONB en List<Map<String,Object>>.
     */
    public List<Map<String, Object>> getSelectedActivities(String columnName, ResultSet rs) throws SQLException {
        try {
            Object obj = rs.getObject(columnName);

            if (obj == null) {
                return null;
            }
            String json;
            if (obj instanceof PGobject) {
                json = ((PGobject) obj).getValue();
            } else {
                json = obj.toString();
            }

            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        }
        catch (IOException e){
            throw new CustomException("PARSING ERROR", "Failed to parse additionalDetail object");
        }
    }
}
