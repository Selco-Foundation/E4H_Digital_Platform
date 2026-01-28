package org.egov.amc.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.Asset;
import org.egov.amc.web.models.AssetAmc;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class AssetAmcMapper implements RowMapper<AssetAmc> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AssetAmc mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.trace("Entering mapRow method for asset AMC, rowNum: {}", rowNum);

        Asset asset = getAssetObjFromResultSet(rs);
        AmcConfiguration amcConfiguration = getAmcConfigurationObjFromResultSet(rs);
        AssetAmc assetAmc = getAssetAmcObjFromResultSet(rs);

        assetAmc.setAsset(asset);
        assetAmc.setAmcConfiguration(amcConfiguration);

        log.trace("Completed mapping asset AMC row, assetAmcId: {}", assetAmc.getId());
        return assetAmc;
    }

    /* Builds Facility Object from Result Set */
    private Asset getAssetObjFromResultSet(ResultSet rs) throws SQLException {
        Asset asset = new Asset();

        asset.setAssetId(rs.getString("asset_id"));
        asset.setTenantId(rs.getString("asset_tenant_id"));
        asset.setSystem(rs.getString("asset_system"));
        asset.setFacilityID(rs.getString("asset_facility_id"));
        asset.setActivityFacilityID(rs.getString("asset_activity_facility_id"));
        asset.setAssetTypeID(rs.getString("asset_type_id"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setModelNumber(rs.getString("model_number"));
        asset.setBrandID(rs.getString("brand_id"));
        Long startDate = rs.getLong("warranty_start_date");
        Long endDate = rs.getLong("warranty_end_date");
        asset.setWarrantyStartDate(startDate!=null && startDate>0 ? new Date(rs.getLong("warranty_start_date")) : null);
        asset.setWarrantyDuration(rs.getInt("warranty_duration"));
        asset.setWarrantyEndDate(endDate!=null && endDate>0 ? new Date(rs.getLong("warranty_end_date")) : null);
        asset.setWfStatus(rs.getString("asset_wf_status"));
        asset.setIsActive(rs.getBoolean("asset_is_active"));
        asset.setIsOperational(rs.getBoolean("asset_is_operational"));

        String detailsJson = rs.getString("asset_details");
        String addDetails = rs.getString("asset_additional_details");
        try {
            if (detailsJson != null) {
                log.debug("Parsing asset details JSON for assetId: {}", asset.getAssetId());
                asset.setAssetDetails(objectMapper.readValue(detailsJson, new TypeReference<>() {
                }));
            }
            if(addDetails!=null){
                log.debug("Parsing asset additional details JSON for assetId: {}", asset.getAssetId());
                asset.setAdditionalDetails(objectMapper.readValue(addDetails, new TypeReference<Map<String, Object>>() {
                }));
            }

        }catch (JsonProcessingException e) {
            log.error("Error parsing JSONB fields in asset record for assetId: {}", asset.getAssetId(), e);
            throw new RuntimeException("Error parsing JSONB fields", e);
        }

        return asset;
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

        return AmcConfiguration.builder()
                .id(rs.getString("amc_id"))
                .vendorId(rs.getString("amc_vendor_id"))
                .facilityId(rs.getString("amc_facility_id"))
                .tenantId(rs.getString("amc_tenant_id"))
                .projectId(rs.getString("amc_project_id"))
                .durationMonths(rs.getInt("amc_duration_months"))
                .visitFrequencyMonths(rs.getInt("amc_visit_frequency_months"))
                .status(rs.getString("amc_status"))
                .configurationStartDate(rs.getLong("amc_configuration_start_date"))
                .configurationEndDate(rs.getLong("amc_configuration_end_date"))
                .assetTypes(getAssetTypes("amc_asset_types", rs))
                .additionalDetails(objectMapper.convertValue(additionalDetails, Map.class))
                .auditDetails(auditDetails)
                .build();
    }

    /* Builds AssetAmc Object from Result Set */
    private AssetAmc getAssetAmcObjFromResultSet(ResultSet rs) throws SQLException {

        AuditDetails auditDetails = AuditDetails.builder()
                .createdBy(rs.getString("asset_amc_created_by"))
                .createdTime(rs.getLong("asset_amc_created_time"))
                .lastModifiedBy(rs.getString("asset_amc_last_modified_by"))
                .lastModifiedTime(rs.getLong("asset_amc_last_modified_time"))
                .build();

        JsonNode additionalDetails = getAdditionalDetail("asset_amc_additional_details", rs);

        return AssetAmc.builder()
                .id(rs.getString("asset_amc_id"))
                .assetId(rs.getString("asset_amc_asset_id"))
                .amcConfigurationId(rs.getString("asset_amc_configuration_id"))
                .tenantId(rs.getString("asset_amc_tenant_id"))
                .isLegacyAsset(rs.getBoolean("is_legacy_asset"))
                .status(rs.getString("asset_amc_status"))
                .amcStartDate(rs.getLong("amc_start_date"))
                .amcEndDate(rs.getLong("amc_end_date"))
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
            log.error("Failed to parse JSON object for column: {}", columnName, e);
            throw new CustomException("PARSING ERROR", "Failed to parse JSON object for column: " + columnName);
        }
        return null;
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
}
