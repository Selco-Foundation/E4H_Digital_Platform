package org.egov.amc.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.AmcPlan;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class AmcPlanMapper implements RowMapper<AmcPlan> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AmcPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.trace("Entering mapRow method for AMC plan, rowNum: {}", rowNum);

        AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(rs.getString("ap_created_by"))
                .createdTime(rs.getLong("ap_created_time"))
                .lastModifiedBy(rs.getString("ap_last_modified_by"))
                .lastModifiedTime(rs.getLong("ap_last_modified_time"))
                .build();

        JsonNode geographyScope = getJsonNode("ap_geography_scope", rs);
        JsonNode additionalDetails = getJsonNode("ap_additional_details", rs);
        List<Map<String, Object>> selectedActivities = getSelectedActivities("ap_selected_activities", rs);

        AmcPlan amcPlan = AmcPlan.builder()
                .id(rs.getString("ap_id"))
                .tenantId(rs.getString("ap_tenant_id"))
                .name(rs.getString("ap_name"))
                .projectId(rs.getString("ap_project_id"))
                .healthFacilityNumber(rs.getInt("ap_health_facility_number"))
                .startDate(rs.getLong("ap_start_date"))
                .endDate(rs.getLong("ap_end_date"))
                .status(rs.getString("ap_status"))
                .isDeleted(rs.getBoolean("ap_isdeleted"))
                .geographyScope(objectMapper.convertValue(geographyScope, Map.class))
                .selectedActivities(selectedActivities)
                .additionalDetails(objectMapper.convertValue(additionalDetails, Map.class))
                .auditDetails(auditDetails)
                .build();

        log.trace("Completed mapping AMC plan row, planId: {}", amcPlan.getId());
        return amcPlan;
    }

    private JsonNode getJsonNode(String columnName, ResultSet rs) throws SQLException {
        try {
            PGobject obj = (PGobject) rs.getObject(columnName);
            if (obj != null) {
                return objectMapper.readTree(obj.getValue());
            }
        } catch (IOException e) {
            log.error("Failed to parse JSON object for column: {}", columnName, e);
            throw new CustomException("PARSING ERROR", "Failed to parse JSON object for column: " + columnName);
        }
        return null;
    }

    private List<Map<String, Object>> getSelectedActivities(String columnName, ResultSet rs) throws SQLException {
        try {
            Object obj = rs.getObject(columnName);
            if (obj == null) {
                return null;
            }
            String json = (obj instanceof PGobject) ? ((PGobject) obj).getValue() : obj.toString();
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            log.error("Failed to parse selectedActivities JSON for column: {}", columnName, e);
            throw new CustomException("PARSING ERROR", "Failed to parse selectedActivities");
        }
    }
}
