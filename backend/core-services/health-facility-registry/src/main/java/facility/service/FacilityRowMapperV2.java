package facility.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
import org.egov.common.contract.models.Address;
import org.egov.common.contract.models.AuditDetails;
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
public class FacilityRowMapperV2 implements ResultSetExtractor<List<Facility>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Facility> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, Facility> facilityMap = new LinkedHashMap<>();
        while (rs.next()) {
            String facilityId = rs.getString("id");

            if (!facilityMap.containsKey(facilityId)) {
                facilityMap.put(facilityId, createFacilityObj(rs));
            }
        }

        return new ArrayList<>(facilityMap.values());
    }

    private Facility createFacilityObj(ResultSet rs) throws SQLException, DataAccessException {
        FacilityAddress address = getAddress(rs);
        Facility facility = getFacilityObjFromResultSet(rs);
        facility.setAddress(address);
        return facility;
    }

    /* Builds Facility Object from Result Set */
    private Facility getFacilityObjFromResultSet(ResultSet rs) throws SQLException {
        Facility facility = new Facility();

        facility.setFacilityId(rs.getString("id"));
        facility.setTenantId(rs.getString("tenant_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(rs.getString("facility_type"));
        facility.setFacilityCategory(rs.getString("facility_category"));
        facility.setFacilityOwnership(rs.getString("facility_ownership"));
        facility.setBoundaryCode(rs.getString("boundary_code"));
        facility.setFacilityPocName(rs.getString("facility_poc_name"));
        facility.setFacilityPocPhone(rs.getString("facility_poc_phone"));
        facility.setFacilityPocEmail(rs.getString("facility_poc_email"));
        facility.setHfrId(rs.getString("hfr_id"));
        facility.setNinId(rs.getString("nin_id"));
        facility.setFacilityStatus(rs.getString("facility_status"));
        facility.setUserId(rs.getString("user_id"));
        facility.setWfStatus(rs.getString("wf_status"));
        facility.setIsActive(rs.getBoolean("is_active"));
        facility.setFacilityRegion(rs.getString("facility_region"));
        facility.setIsOnmReady(rs.getBoolean("is_onm_ready"));

        try {
            String detailsJson = rs.getString("facility_details");
            if (detailsJson != null) {
                HealthFacilityDetails details = objectMapper.readValue(detailsJson, new TypeReference<HealthFacilityDetails>() {});
                facility.setFacilityDetails(details);
            }

            String additionalJson = rs.getString("additional_details");
            if (additionalJson != null) {
                Map<String, Object> additional = objectMapper.readValue(additionalJson, new TypeReference<Map<String, Object>>() {});
                facility.setAdditionalDetails(additional);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON fields in facility record", e);
        }

        return facility;
    }

    private FacilityAddress getAddress(ResultSet rs) throws SQLException {
        FacilityAddress address = new FacilityAddress();
//        address.setAddressId(rs.getString("id"));
//        address.setTenantId(rs.getString("tenant_id"));
        address.setLatitude(rs.getDouble("latitude"));
        address.setLongitude(rs.getDouble("longitude"));
        address.setAddressLine1(rs.getString("addressLine1"));
        address.setAddressLine2(rs.getString("addressLine2"));
        address.setCity(rs.getString("city"));
        address.setPincode(rs.getString("pincode"));
        address.setLandmark(rs.getString("landmark"));
        return address;
    }
}
