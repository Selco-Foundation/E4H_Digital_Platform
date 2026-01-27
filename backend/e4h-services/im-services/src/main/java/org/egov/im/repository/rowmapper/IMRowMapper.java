package org.egov.im.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.im.web.models.*;
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
public class IMRowMapper implements ResultSetExtractor<List<Incident>> {


    @Autowired
    private ObjectMapper mapper;



    public List<Incident> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, Incident> serviceMap = new LinkedHashMap<>();

        while (rs.next()) {

            String id = rs.getString("ser_id");
            Incident currentService = serviceMap.get(id);

            if (currentService == null) {
                currentService = mapIncidentFromResultSet(rs);
                serviceMap.put(currentService.getId(), currentService);
            }
            //addChildrenToProperty(rs, currentService);

        }

        return new ArrayList<>(serviceMap.values());


    }

//    private void addChildrenToProperty(ResultSet rs, Incident incident) throws SQLException {
//
//        if(incident.getAddress() == null){
//
//            Double latitude =  rs.getDouble("latitude");
//            Double longitude = rs.getDouble("longitude");
//
//
//            Address address = Address.builder()
//                    .tenantId(rs.getString("ads_tenantId"))
//                    .id(rs.getString("ads_id"))
//                    .district(rs.getString("district"))
//                    .build();
//
//            JsonNode additionalDetails = getAdditionalDetail("ads_additionaldetails",rs);
//
//            if(additionalDetails != null)
//                address.setAdditionDetails(additionalDetails);
//
//            incident.setAddress(address);
//
//        }
//
//    }


    private JsonNode getAdditionalDetail(String columnName, ResultSet rs){

        JsonNode additionalDetail = null;
        try {
            PGobject pgObj = (PGobject) rs.getObject(columnName);
            if(pgObj!=null){
                 additionalDetail = mapper.readTree(pgObj.getValue());
            }
        }
        catch (IOException | SQLException e){
            throw new CustomException("PARSING_ERROR","Failed to parse additionalDetail object");
        }
        return additionalDetail;
    }

    private Incident mapIncidentFromResultSet(ResultSet rs) throws SQLException {
        Incident incident = buildIncidentCore(rs);
        JsonNode additionalDetails = getAdditionalDetail("ser_additionaldetails", rs);
        if (additionalDetails != null) {
            incident.setAdditionalDetail(additionalDetails);
        }
        return incident;
    }

    private Incident buildIncidentCore(ResultSet rs) throws SQLException {
        String id = rs.getString("ser_id");
        String incidentType = rs.getString("IncidentType");
        String incidentSubType = rs.getString("IncidentSubType");
        String phcType = rs.getString("PhcType");
        String reporterType = rs.getString("reporterType");
        String phcSubType = rs.getString("PhcSubType");
        String district = rs.getString("District");
        String block = rs.getString("Block");
        String incidentid = rs.getString("incidentid");
        String comments = rs.getString("comments");
        String applicationStatus = rs.getString("applicationStatus");
        String tenantId = rs.getString("ser_tenantId");
        String systemFunctional = rs.getString("ser_systemfunctional");
        String migrationId = rs.getString("migrationid");
        String legacyId = rs.getString("legacyid");
        Long filedDate = rs.getLong("fileddate");
        String facilityId = rs.getString("facilityid");
        String boundaryCode = rs.getString("boundarycode");

        String accountId = rs.getString("ser_accountid");
        String reporterTenant = rs.getString("ser_reportertenant");
        User reporter = buildReporter(accountId, reporterTenant, rs);

        AuditDetails auditDetails = buildAuditDetails(rs);

        return Incident.builder()
                .id(id)
                .incidentType(incidentType)
                .incidentSubType(incidentSubType)
                .incidentId(incidentid)
                .comments(comments)
                .district(district)
                .block(block)
                .phcType(phcType)
                .phcSubType(phcSubType)
                .applicationStatus(applicationStatus)
                .tenantId(tenantId)
                .accountId(accountId)
                .reporterTenant(reporterTenant)
                .reporter(reporter)
                .reporterType(reporterType)
                .auditDetails(auditDetails)
                .systemFunctional(systemFunctional)
                .migrationId(migrationId)
                .legacyId(legacyId)
                .filedDate(filedDate)
                .facilityId(facilityId)
                .boundaryCode(boundaryCode)
                .build();
    }

    private User buildReporter(String accountId, String reporterTenant, ResultSet rs) throws SQLException {
        User reporter = new User();
        reporter.setTenantId(reporterTenant);
        reporter.setUuid(accountId);
        if (rs.wasNull()) {
            // preserve original behaviour: no-op when last read column was SQL NULL
        }
        return reporter;
    }

    private AuditDetails buildAuditDetails(ResultSet rs) throws SQLException {
        String createdby = rs.getString("ser_createdby");
        Long createdtime = rs.getLong("ser_createdtime");
        String lastmodifiedby = rs.getString("ser_lastmodifiedby");
        Long lastmodifiedtime = rs.getLong("ser_lastmodifiedtime");

        return AuditDetails.builder()
                .createdBy(createdby)
                .createdTime(createdtime)
                .lastModifiedBy(lastmodifiedby)
                .lastModifiedTime(lastmodifiedtime)
                .build();
    }


}
