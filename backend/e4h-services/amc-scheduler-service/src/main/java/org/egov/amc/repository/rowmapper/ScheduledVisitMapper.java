package org.egov.amc.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class ScheduledVisitMapper implements RowMapper<ScheduledVisit> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ScheduledVisit mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScheduledVisit visit = new ScheduledVisit();

        visit.setId(rs.getString("sv_visit_id"));
        visit.setTenantId(rs.getString("sv_tenant_id"));
        visit.setAmcConfigurationId(rs.getString("sv_amc_configuration_id"));
        visit.setFacilityId(rs.getString("sv_facility_id"));
        visit.setProjectId(rs.getString("sv_project_id"));
        visit.setVisitNumber(rs.getInt("sv_visit_number"));
        visit.setScheduledDate(rs.getLong("sv_scheduled_date"));
        visit.setActualVisitDate(rs.getLong("sv_actual_visit_date"));
        visit.setLastVisitDate(rs.getLong("sv_last_scheduled_visit_date"));
        visit.setStatus(rs.getString("sv_status"));

        // visit_report (JSONB → POJO)
        String visitReportJson = rs.getString("sv_visit_report");
        if (visitReportJson != null) {
            try {
                VisitReport report = objectMapper.readValue(visitReportJson, VisitReport.class);
                visit.setVisitReport(report);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new SQLException("Error parsing visit_report JSON", e);
            }
        }

        // Audit
        AuditDetails auditDetails = new AuditDetails();
        auditDetails.setCreatedBy(rs.getString("sv_created_by"));
        auditDetails.setCreatedTime(rs.getLong("sv_created_time"));
        auditDetails.setLastModifiedBy(rs.getString("sv_last_modified_by"));
        auditDetails.setLastModifiedTime(rs.getLong("sv_last_modified_time"));
        visit.setAuditDetails(auditDetails);

        // Get AMC configuration Object
        AmcConfiguration amc = new AmcConfiguration();
        amc.setId(rs.getString("amc_id"));
        amc.setTenantId(rs.getString("amc_tenant_id"));
        amc.setVendorId(rs.getString("amc_vendor_id"));
        amc.setFacilityId(rs.getString("amc_facility_id"));
        amc.setProjectId(rs.getString("amc_project_id"));
        amc.setDurationMonths(rs.getInt("amc_duration_months"));
        amc.setVisitFrequencyMonths(rs.getInt("amc_visit_frequency_months"));
        amc.setConfigurationStartDate(rs.getLong("amc_configuration_start_date"));
        amc.setConfigurationEndDate(rs.getLong("amc_configuration_end_date"));
        amc.setStatus(rs.getString("amc_status"));
        amc.setAssetTypes(getAssetTypes("amc_asset_types", rs));

        visit.setAmcConfiguration(amc);

        // -------------------------
        // 🔹 Facility info
        // -------------------------
        Facility facility = new Facility();
        facility.setId(rs.getString("facility_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilitySubtype(rs.getString("facility_subtype"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setFacilityRegion(rs.getString("facility_region"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setIsActive(rs.getBoolean("facility_is_active"));

        String facilityDetailsJson = rs.getString("facility_details");
        if (facilityDetailsJson != null) {
            try {
                facility.setFacilityDetails(objectMapper.readValue(facilityDetailsJson, Map.class));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new SQLException("Error parsing facility_details JSON", e);
            }
        }

        visit.setFacility(facility);

        // -------------------------
        // 🔹 Assignments (JSONB ARRAY)
        // -------------------------
        String assignmentsJson = rs.getString("assignments");

        if (assignmentsJson != null && !assignmentsJson.equals("[]")) {
            try {
                List<ScheduledVisitAssignment> assignments =
                        objectMapper.readValue(
                                assignmentsJson,
                                new TypeReference<List<ScheduledVisitAssignment>>() {}
                        );

                visit.setAssignments(assignments);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new SQLException("Error parsing assignments JSONB array", e);
            }
        } else {
            visit.setAssignments(new ArrayList<>());
        }

        return visit;
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
