package org.egov.amc.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ScheduledVisitMapper implements RowMapper<ScheduledVisit> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ScheduledVisit mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.trace("Entering mapRow method for scheduled visit, rowNum: {}", rowNum);
        ScheduledVisit visit = new ScheduledVisit();

        populateBasicVisitFields(rs, visit);
        populateVisitReport(rs, visit);
        populateAuditDetails(rs, visit);

        AmcConfiguration amcConfiguration = mapAmcConfiguration(rs);
        visit.setAmcConfiguration(amcConfiguration);

        Facility facility = mapFacility(rs, visit.getId());
        visit.setFacility(facility);

        List<ScheduledVisitAssignment> assignments = mapAssignments(rs, visit.getId());
        visit.setAssignments(assignments);

        log.trace("Completed mapping scheduled visit row, visitId: {}", visit.getId());
        return visit;
    }

    /**
     * Convert JSONB column into List<Map<String,Object>>
     */
    public List<Map<String, Object>> getAssetTypes(String columnName, ResultSet rs) throws SQLException {
        log.trace("Entering getAssetTypes method for column: {}", columnName);
        try {
            Object obj = rs.getObject(columnName);
            if (obj == null) {
                log.debug("Asset types column {} is null", columnName);
                return null;
            }

            String json = (obj instanceof PGobject)
                    ? ((PGobject) obj).getValue()
                    : obj.toString();

            List<Map<String, Object>> assetTypes = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            log.debug("Parsed {} asset type(s) from column: {}", assetTypes != null ? assetTypes.size() : 0, columnName);
            return assetTypes;
        }
        catch (IOException e) {
            log.error("Failed to parse assetTypes JSON for column: {}", columnName, e);
            throw new CustomException("PARSING ERROR", "Failed to parse assetTypes");
        }
    }

    private void populateBasicVisitFields(ResultSet rs, ScheduledVisit visit) throws SQLException {
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
    }

    private void populateVisitReport(ResultSet rs, ScheduledVisit visit) throws SQLException {
        String visitReportJson = rs.getString("sv_visit_report");
        if (visitReportJson == null) {
            return;
        }
        try {
            log.debug("Parsing visit report JSON for scheduled visit ID: {}", visit.getId());
            VisitReport report = objectMapper.readValue(visitReportJson, VisitReport.class);
            visit.setVisitReport(report);
        } catch (Exception e) {
            log.error("Error parsing visit_report JSON for scheduled visit ID: {}", visit.getId(), e);
            throw new SQLException("Error parsing visit_report JSON", e);
        }
    }

    private void populateAuditDetails(ResultSet rs, ScheduledVisit visit) throws SQLException {
        AuditDetails auditDetails = new AuditDetails();
        auditDetails.setCreatedBy(rs.getString("sv_created_by"));
        auditDetails.setCreatedTime(rs.getLong("sv_created_time"));
        auditDetails.setLastModifiedBy(rs.getString("sv_last_modified_by"));
        auditDetails.setLastModifiedTime(rs.getLong("sv_last_modified_time"));
        visit.setAuditDetails(auditDetails);
    }

    private AmcConfiguration mapAmcConfiguration(ResultSet rs) throws SQLException {
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
        return amc;
    }

    private Facility mapFacility(ResultSet rs, String visitId) throws SQLException {
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
        if (facilityDetailsJson == null) {
            return facility;
        }

        try {
            log.debug("Parsing facility details JSON for scheduled visit ID: {}", visitId);
            facility.setFacilityDetails(objectMapper.readValue(facilityDetailsJson, Map.class));
        } catch (Exception e) {
            log.error("Error parsing facility_details JSON for scheduled visit ID: {}", visitId, e);
            throw new SQLException("Error parsing facility_details JSON", e);
        }
        return facility;
    }

    private List<ScheduledVisitAssignment> mapAssignments(ResultSet rs, String visitId) throws SQLException {
        String assignmentsJson = rs.getString("assignments");
        if (assignmentsJson == null || assignmentsJson.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            log.debug("Parsing assignments JSON for scheduled visit ID: {}", visitId);
            List<ScheduledVisitAssignment> assignments =
                    objectMapper.readValue(
                            assignmentsJson,
                            new TypeReference<List<ScheduledVisitAssignment>>() {}
                    );

            log.debug("Parsed {} assignment(s) for scheduled visit ID: {}", assignments.size(), visitId);
            return assignments;
        } catch (Exception e) {
            log.error("Error parsing assignments JSONB array for scheduled visit ID: {}", visitId, e);
            throw new SQLException("Error parsing assignments JSONB array", e);
        }
    }
}
