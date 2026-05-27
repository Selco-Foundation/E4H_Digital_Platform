package org.selco.e4h.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CarbonEmissionProperties {

    @Value("${kafka.topics.carbon-emission-calculate}")
    private String carbonEmissionTopic;

    @Value("${co2.es.index.actual}")
    private String actualIndex;

    @Value("${co2.es.index.projection}")
    private String projectionIndex;

    @Value("${kafka.topics.co2-monthly-facility-indexer}")
    private String co2MonthlyFacilityIndexerTopic;

    @Value("${kafka.topics.co2-monthly-projection-indexer}")
    private String co2MonthlyProjectionIndexerTopic;

    @Value("${co2.batch.facility.size}")
    private int facilityBatchSize;

    @Value("${egov.rms.host}")
    private String rmsHost;

    @Value("${egov.rms.co2.reference.path}")
    private String rmsCo2ReferencePath;

    @Value("${egov.rms.co2.consumption.batch.path}")
    private String rmsCo2ConsumptionBatchPath;

    @Value("${co2.default.tenant.id}")
    private String defaultTenantId;

    @Value("${egov.facility.host}")
    private String facilityHost;

    @Value("${egov.facility.bulk-search.path}")
    private String facilityBulkSearchPath;

    @Value("${egov.project.host}")
    private String projectHost;

    @Value("${egov.project.fetch-by-facilities.path}")
    private String projectFetchByFacilitiesPath;

    @Value("${co2.growth.rate}")
    private double growthRate;

    @Value("${co2.lifecycle.years}")
    private int lifecycleYears;

    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${co2.localization.boundary.module}")
    private String localizationBoundaryModule;

    @Value("${co2.localization.locale}")
    private String localizationLocale;
}
