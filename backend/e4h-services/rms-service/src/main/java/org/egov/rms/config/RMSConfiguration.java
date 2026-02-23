package org.egov.rms.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RMSConfiguration {

    // RMS API Configuration
    @Value("${rms.api.base.url}")
    private String rmsApiBaseUrl;

    @Value("${rms.api.access.token}")
    private String rmsApiAccessToken;

    @Value("${rms.api.center.details.endpoint}")
    private String centerDetailsEndpoint;

    @Value("${rms.api.center.datas.endpoint}")
    private String centerDatasEndpoint;

    @Value("${rms.api.center.mappings.endpoint}")
    private String centerMappingsEndpoint;

    @Value("${rms.api.timeout:30000}")
    private Integer rmsApiTimeout;

    @Value("${rms.api.retry.max.attempts:3}")
    private Integer retryMaxAttempts;

    @Value("${rms.api.retry.backoff.delay:1000}")
    private Long retryBackoffDelay;

    // IM Service Configuration
    @Value("${im.service.base.url}")
    private String imServiceBaseUrl;

    @Value("${im.service.create.endpoint}")
    private String imServiceCreateEndpoint;

    @Value("${im.service.request.timeout:30000}")
    private Integer imServiceTimeout;

    // Rule Engine Configuration
    @Value("${rms.rule.solar.threshold.percent:10}")
    private Integer solarThresholdPercent;

    @Value("${rms.rule.inverter.no.signal.days:2}")
    private Integer inverterNoSignalDays;

    @Value("${rms.rule.inverter.high.voltage.threshold:250}")
    private Integer inverterHighVoltageThreshold;

    @Value("${rms.rule.grid.voltage.low.threshold:200}")
    private Integer gridVoltageLowThreshold;

    @Value("${rms.rule.grid.voltage.high.threshold:250}")
    private Integer gridVoltageHighThreshold;

    // Deduplication Configuration
    @Value("${rms.deduplication.suppression.window.hours:24}")
    private Integer suppressionWindowHours;

    // Facility Service Configuration
    @Value("${facility.service.base.url}")
    private String facilityServiceBaseUrl;

    @Value("${facility.service.search.endpoint}")
    private String facilityServiceSearchEndpoint;

    // User Service Configuration
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.internal.microservice.user.uuid}")
    private String systemUserUuid;

    // Default Tenant
    @Value("${rms.default.tenant.id:pg}")
    private String defaultTenantId;

    // Scheduler Configuration
    @Value("${rms.scheduler.data.collector.enabled:true}")
    private Boolean dataCollectorEnabled;

    @Value("${rms.scheduler.data.collector.cron:0 */15 * * * *}")
    private String dataCollectorCron;

    @Value("${rms.scheduler.rule.engine.enabled:true}")
    private Boolean ruleEngineEnabled;

    @Value("${rms.scheduler.rule.engine.cron:0 */15 * * * *}")
    private String ruleEngineCron;

    @Value("${rms.scheduler.solar.daily.enabled:true}")
    private Boolean solarDailyEnabled;

    @Value("${rms.scheduler.solar.daily.cron:0 0 1 * * *}")
    private String solarDailyCron;

    // Mapping Configuration
    @Value("${rms.mapping.sync.enabled:true}")
    private Boolean mappingSyncEnabled;

    @Value("${rms.mapping.sync.cron:0 0 2 * * 0}")
    private String mappingSyncCron;

    @Value("${rms.mapping.validation.days:7}")
    private Integer mappingValidationDays;

    @Value("${rms.mapping.validation.cron:0 0 3 * * 0}")
    private String mappingValidationCron;

    // Testing Configuration
    @Value("${rms.testing.max.tickets.per.trigger:-1}")
    private Integer maxTicketsPerTrigger; // -1 means unlimited, positive number limits tickets per trigger
}

