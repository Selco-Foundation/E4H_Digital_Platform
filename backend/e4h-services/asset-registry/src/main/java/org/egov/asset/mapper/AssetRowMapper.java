package org.egov.asset.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.asset.web.models.Asset;
import digit.models.coremodels.AuditDetails;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class AssetRowMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public final RowMapper<Asset> rowMapper = (rs, rowNum) -> {
        Asset asset = new Asset();
        asset.setAssetId(rs.getString("asset_id"));
        asset.setTenantId(rs.getString("tenant_id"));
        asset.setSystem(rs.getString("system"));
        asset.setFacilityID(rs.getString("facility_id"));
        asset.setActivityFacilityID(rs.getString("activity_facility_id"));
        asset.setAssetTypeID(rs.getString("asset_type_id"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setModelNumber(rs.getString("model_number"));
        asset.setBrandID(rs.getString("brand_id"));
        Long startDate = rs.getLong("warranty_start_date");
        Long endDate = rs.getLong("warranty_end_date");
        asset.setWarrantyStartDate(startDate!=null && startDate>0 ? new Date(rs.getLong("warranty_start_date")) : null);
        asset.setWarrantyDuration(rs.getInt("warranty_duration"));
        asset.setWarrantyEndDate(endDate!=null && endDate>0 ? new Date(rs.getLong("warranty_end_date")) : null);
        asset.setWfStatus(rs.getString("wf_status"));
        asset.setIsActive(rs.getBoolean("is_active"));
        asset.setIsOperational(rs.getBoolean("is_operational"));

        AuditDetails details = new AuditDetails();
        details.setCreatedBy(rs.getString("created_by"));
        details.setCreatedTime(rs.getLong("created_time"));
        details.setLastModifiedBy(rs.getString("last_modified_by"));
        details.setLastModifiedTime(rs.getLong("last_modified_time"));
        asset.setAuditDetails(details);

        String detailsJson = rs.getString("asset_details");
        String addDetails = rs.getString("additional_details");
        try {
            if (detailsJson != null) {
                asset.setAssetDetails(mapper.readValue(detailsJson, new TypeReference<>() {
                }));
            }
            if(addDetails!=null){
                asset.setAdditionalDetails(mapper.readValue(addDetails, new TypeReference<Map<String, Object>>() {
                }));
            }

        }catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSONB fields", e);
        }

        return asset;
    };
}
