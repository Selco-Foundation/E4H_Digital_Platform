package org.egov.asset.repository;

import org.egov.asset.config.Configuration;
import org.egov.asset.kafka.Producer;
import org.egov.asset.util.ErrorConstants;
import org.egov.asset.web.models.Asset;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssetRepository {

    private final Producer producer;
    private final Configuration configuration;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AssetRepository(Producer producer, Configuration configuration) {
        this.producer = producer;
        this.configuration = configuration;
    }

    public void pushCreateAsset(Asset asset) {
        try {
            producer.push(configuration.getCreateAssetTopic(), asset);
        }catch (Exception e){
            throw new CustomException(ErrorConstants.KAFKA_PUSH_ERROR_CODE,ErrorConstants.KAFKA_PUSH_ERROR_MSG);
        }
    }

    public void pushUpdateAsset(Asset asset) {
        try {
            producer.push(configuration.getUpdateAssetTopic(), asset);
        } catch (Exception e) {
            throw new CustomException(ErrorConstants.UPDATE_ASSET_ERROR_CODE, ErrorConstants.UPDATE_ASSET_ERROR_MSG);
        }
    }

    public void insertAsset(Asset asset) {
        String sql = "INSERT INTO asset (asset_id, tenant_id, system, facility_id, asset_type_id, serial_number, model_number, brand_id, asset_details, warranty_start_date, warranty_duration, warranty_end_date, wf_status, is_active, additional_details, created_by, created_time, last_modified_by, last_modified_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            asset.getAssetId(),
            asset.getTenantId(),
            asset.getSystem(),
            asset.getFacilityID(),
            asset.getAssetTypeID(),
            asset.getSerialNumber(),
            asset.getModelNumber(),
            asset.getBrandID(),
            asset.getAssetDetails() != null ? asset.getAssetDetails().toString() : null,
            asset.getWarrantyStartDate() != null ? asset.getWarrantyStartDate().getTime() : null,
            asset.getWarrantyDuration(),
            asset.getWarrantyEndDate() != null ? asset.getWarrantyEndDate().getTime() : null,
            asset.getWfStatus(),
            asset.getIsActive(),
            asset.getAdditionalDetails() != null ? asset.getAdditionalDetails().toString() : null,
            asset.getAuditDetails() != null ? asset.getAuditDetails().getCreatedBy() : null,
            asset.getAuditDetails() != null ? asset.getAuditDetails().getCreatedTime() : null,
            asset.getAuditDetails() != null ? asset.getAuditDetails().getLastModifiedBy() : null,
            asset.getAuditDetails() != null ? asset.getAuditDetails().getLastModifiedTime() : null
        );
    }
}
