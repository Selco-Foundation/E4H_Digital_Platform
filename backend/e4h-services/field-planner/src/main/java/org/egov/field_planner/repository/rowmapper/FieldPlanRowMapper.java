package org.egov.field_planner.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.Project;
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
        Project project = getProjectObjFromResultSet(rs);
        FieldPlan fieldPlan = getFieldPlanObjFromResultSet(rs);
        fieldPlan.setProject(project);
        return fieldPlan;
    }

    /* Builds FieldPlan Object from Result Set */
    private FieldPlan getFieldPlanObjFromResultSet(ResultSet rs) throws SQLException {
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

    private Project getProjectObjFromResultSet(ResultSet rs) throws SQLException {
        String project_id = rs.getString("projectId");
        String project_tenantId = rs.getString("project_tenantId");
        String project_projectNumber = rs.getString("project_projectNumber");
        String project_name = rs.getString("project_name");
        String project_projectType = rs.getString("project_projectType");
        String project_projectTypeId = rs.getString("project_projectTypeId");
        String project_projectSubtype = rs.getString("project_projectSubtype");
        String project_department = rs.getString("project_department");
        String project_description = rs.getString("project_description");
        String project_referenceId = rs.getString("project_referenceId");
        Long project_startDate = rs.getLong("project_startDate");
        Long project_endDate = rs.getLong("project_endDate");
        Boolean project_isTaskEnabled = rs.getBoolean("project_isTaskEnabled");
        String project_projectHierarchy = rs.getString("project_projectHierarchy");
        String project_parent = rs.getString("project_parent");
        JsonNode project_additionalDetails = getAdditionalDetail("project_additionalDetails", rs);
        String project_natureOfWork = rs.getString("project_natureOfWork");
        Boolean project_isDeleted = rs.getBoolean("project_isDeleted");
        Integer project_rowVersion = rs.getInt("project_rowVersion");
        String project_createdBy = rs.getString("project_createdBy");
        String project_lastModifiedBy = rs.getString("project_lastModifiedBy");
        Long project_createdTime = rs.getLong("project_createdTime");
        Long project_lastModifiedTime = rs.getLong("project_lastModifiedTime");

        AuditDetails projectAuditDetails = AuditDetails.builder().createdBy(project_createdBy).createdTime(project_createdTime)
                .lastModifiedBy(project_lastModifiedBy).lastModifiedTime(project_lastModifiedTime)
                .build();

        Project project = Project.builder()
                .id(project_id)
                .tenantId(project_tenantId)
                .projectNumber(project_projectNumber)
                .name(project_name)
                .projectType(project_projectType)
                .projectTypeId(project_projectTypeId)
                .projectSubType(project_projectSubtype)
                .department(project_department)
                .description(project_description)
                .referenceID(project_referenceId)
                .startDate(project_startDate)
                .endDate(project_endDate)
                .isTaskEnabled(project_isTaskEnabled)
                .parent(project_parent)
                .projectHierarchy(project_projectHierarchy)
                .additionalDetails(project_additionalDetails)
                .natureOfWork(project_natureOfWork)
                .isDeleted(project_isDeleted)
                .rowVersion(project_rowVersion)
                .auditDetails(projectAuditDetails)
                .build();

        return project;
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
