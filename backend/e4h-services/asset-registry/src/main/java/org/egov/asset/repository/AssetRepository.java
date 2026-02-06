package org.egov.asset.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.asset.kafka.Producer;
import org.egov.asset.util.ErrorConstants;
import org.egov.asset.web.models.Asset;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AssetRepository {

    private final Producer producer;
    private final Configuration configuration;

    @Autowired
    public AssetRepository(Producer producer, Configuration configuration) {
        this.producer = producer;
        this.configuration = configuration;
    }

    public void pushCreateAsset(Asset asset) {
        log.trace("AssetRepository::pushCreateAsset called | assetId={} tenantId={}", 
                asset.getAssetId(), asset.getTenantId());
        try {
            log.debug("Pushing create asset to topic={} assetId={}", 
                    configuration.getCreateAssetTopic(), asset.getAssetId());
            producer.push(configuration.getCreateAssetTopic(), asset);
            log.debug("Successfully pushed create asset to topic assetId={}", 
                    asset.getAssetId());
        } catch (Exception e) {
            log.error("Failed to push create asset to Kafka | assetId={} topic={} error={}", 
                    asset.getAssetId(), configuration.getCreateAssetTopic(), e.getMessage(), e);
            throw new CustomException(ErrorConstants.KAFKA_PUSH_ERROR_CODE, ErrorConstants.KAFKA_PUSH_ERROR_MSG);
        }
    }

    public void pushUpdateAsset(Asset asset) {
        log.trace("AssetRepository::pushUpdateAsset called | assetId={} tenantId={}", 
                asset.getAssetId(), asset.getTenantId());
        try {
            log.debug("Pushing update asset to topic={} assetId={}", 
                    configuration.getUpdateAssetTopic(), asset.getAssetId());
            producer.push(configuration.getUpdateAssetTopic(), asset);
            log.debug("Successfully pushed update asset to topic assetId={}", 
                    asset.getAssetId());
        } catch (Exception e) {
            log.error("Failed to push update asset to Kafka | assetId={} topic={} error={}", 
                    asset.getAssetId(), configuration.getUpdateAssetTopic(), e.getMessage(), e);
            throw new CustomException(ErrorConstants.UPDATE_ASSET_ERROR_CODE, ErrorConstants.UPDATE_ASSET_ERROR_MSG);
        }
    }
}
