package org.egov.asset.repository;

import org.egov.asset.config.Configuration;
import org.egov.asset.kafka.Producer;
import org.egov.asset.util.ErrorConstants;
import org.egov.asset.web.models.Asset;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AssetRepository {

    private final Producer producer;
    private final Configuration configuration;

    @Autowired
    public AssetRepository(Producer producer, Configuration configuration) {
        this.producer = producer;
        this.configuration = configuration;
    }

    public void pushCreateAsset(Asset asset) {
        try {
            producer.push(configuration.getCreateAssetTopic(), asset);
        } catch (RuntimeException e) {
            throw new CustomException(ErrorConstants.KAFKA_PUSH_ERROR_CODE, ErrorConstants.KAFKA_PUSH_ERROR_MSG + ": " + e.getMessage());
        }
    }

    public void pushUpdateAsset(Asset asset) {
        try {
            producer.push(configuration.getUpdateAssetTopic(), asset);
        } catch (RuntimeException e) {
            throw new CustomException(ErrorConstants.UPDATE_ASSET_ERROR_CODE, ErrorConstants.UPDATE_ASSET_ERROR_MSG + ": " + e.getMessage());
        }
    }
}
