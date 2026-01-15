package org.egov.amc.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationAssignment;
import org.egov.amc.web.models.Facility;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Project;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AmcConfigurationMapper implements RowMapper<AmcConfiguration> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AmcConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {

        Facility facility = getFacilityObjFromResultSet(rs);
        Project project = getProjectObjFromResultSet(rs);
        AmcConfiguration amcConfiguration = getAmcConfigurationObjFromResultSet(rs);

        amcConfiguration.setFacility(facility);
        amcConfiguration.setProject(project);

        return amcConfiguration;
    }

    /* Builds Facility Object from Result Set */
    private Facility getFacilityObjFromResultSet(ResultSet rs) throws SQLException {
        Facility facility = new Facility();

        facility.setFacilityId(rs.getString("facility_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setIsActive(rs.getBoolean("facility_is_active"));
        facility.setFacilityRegion(rs.getString("facility_region"));

        try {
            String json = rs.getString("facility_details");
            if (json != null) {
                Map<String, Object> details =
                        objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                facility.setFacilityDetails(details);
            }
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON fields in facility record", e);
        }

        return facility;
    }

    /* Builds Project Object from Result Set */
    private Project getProjectObjFromResultSet(ResultSet rs) throws SQLException {
        JsonNode additionalDetails = getAdditionalDetail("project_additionalDetails", rs);

        return Project.builder()
                .id(rs.getString("project_id"))
                .projectNumber(rs.getString("project_number"))
                .name(rs.getString("project_name"))
                .projectType(rs.getString("project_type"))
                .projectSubType(rs.getString("project_subtype"))
                .description(rs.getString("project_description"))
                .startDate(rs.getLong("project_start_date"))
                .endDate(rs.getLong("project_end_date"))
                .additionalDetails(additionalDetails)
                .build();
    }

    /* Builds AmcConfiguration Object from Result Set */
    private AmcConfiguration getAmcConfigurationObjFromResultSet(ResultSet rs) throws SQLException {

        AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(rs.getString("amc_created_by"))
                .createdTime(rs.getLong("amc_created_time"))
                .lastModifiedBy(rs.getString("amc_last_modified_by"))
                .lastModifiedTime(rs.getLong("amc_last_modified_time"))
                .build();

        JsonNode additionalDetails = getAdditionalDetail("amc_additional_details", rs);
        String assignmentsJson = rs.getString("assignments");
        List<AmcConfigurationAssignment> assignments = new ArrayList<>();
        if (assignmentsJson != null && !assignmentsJson.equals("[]")) {
            try {
                assignments =
                        objectMapper.readValue(
                                assignmentsJson,
                                new TypeReference<List<AmcConfigurationAssignment>>() {}
                        );

            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new SQLException("Error parsing assignments JSONB array", e);
            }
        }

        return AmcConfiguration.builder()
                .id(rs.getString("amc_id"))
                .vendorId(rs.getString("amc_vendor_id"))
                .facilityId(rs.getString("amc_facility_id"))
                .tenantId(rs.getString("amc_tenant_id"))
                .projectId(rs.getString("amc_project_id"))
                .durationMonths(rs.getInt("amc_duration_months"))
                .visitFrequencyMonths(rs.getInt("amc_visit_frequency_months"))
                .status(rs.getString("amc_status"))
                .assignments(assignments)
                .configurationStartDate(rs.getLong("amc_configuration_start_date"))
                .configurationEndDate(rs.getLong("amc_configuration_end_date"))
                .assetTypes(getAssetTypes("amc_asset_types", rs))
                .additionalDetails(objectMapper.convertValue(additionalDetails, Map.class))
                .auditDetails(auditDetails)
                .build();
    }

    private JsonNode getAdditionalDetail(String columnName, ResultSet rs) throws SQLException {
        try {
            PGobject obj = (PGobject) rs.getObject(columnName);
            if (obj != null) {
                return objectMapper.readTree(obj.getValue());
            }
        } catch (IOException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse JSON object for column: " + columnName);
        }
        return null;
    }

    /**
     * Convert JSONB column into List<Map<String,Object>>
     */
    public List<Map<String, Object>> getAssetTypes(String columnName, ResultSet rs) throws SQLException {
        try {
            Object obj = rs.getObject(columnName);
            if (obj == null) return null;

            String json = (obj instanceof PGobject)
                    ? ((PGobject) obj).getValue()
                    : obj.toString();

            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
        }
        catch (IOException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse assetTypes");
        }
    }
}
