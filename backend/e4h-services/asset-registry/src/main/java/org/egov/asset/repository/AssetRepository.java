package org.egov.asset.repository;

import org.egov.asset.config.Configuration;
import org.egov.asset.kafka.Producer;
import org.egov.asset.web.models.Asset;
import org.egov.asset.web.models.AssetCreateRequest;
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
        producer.push(configuration.getCreateAssetTopic(), asset);
    }
}
