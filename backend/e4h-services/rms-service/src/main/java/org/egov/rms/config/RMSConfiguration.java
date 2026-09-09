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

    @Value("${rms.api.dashboard.data.endpoint}")
    private String dashboardDataEndpoint;

    @Value("${rms.api.dashboard.table.data.endpoint}")
    private String dashboardTableDataEndpoint;

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

    /**
     * When true, panel low-solar tickets require recent RMS activity (last sync within {@link #panelIdleMaxHours}).
     * Idle centers (no sync for longer than that window) are excluded even if 7-day low solar data is present.
     */
    @Value("${rms.rule.panel.low.solar.activity.gating.enabled:true}")
    private boolean panelLowSolarActivityGatingEnabled;

    /** A center is treated as idle if last_sync_time is older than this many hours (default 72). */
    @Value("${rms.rule.panel.idle.max.hours:72}")
    private int panelIdleMaxHours;

    @Value("${rms.rule.inverter.no.signal.days:2}")
    private Integer inverterNoSignalDays;

    @Value("${rms.rule.inverter.high.voltage.threshold:250}")
    private Integer inverterHighVoltageThreshold;

    /**
     * Inclusive lower bound (volts) of the reverse voltage range.
     * A grid voltage in [reverseMin, reverseMax] triggers a "Reverse Voltage" alert.
     */
    @Value("${rms.rule.grid.voltage.reverse.min.threshold:50}")
    private Integer gridVoltageReverseMinThreshold;

    /** Inclusive upper bound (volts) of the reverse voltage range. */
    @Value("${rms.rule.grid.voltage.reverse.max.threshold:150}")
    private Integer gridVoltageReverseMaxThreshold;

    @Value("${rms.rule.grid.voltage.high.threshold:250}")
    private Integer gridVoltageHighThreshold;

    /** When false, grid high voltage (&gt; threshold) facilities do not produce alerts or tickets. Reverse voltage is unaffected by this flag. */
    @Value("${rms.rule.grid.high.voltage.tickets.enabled:true}")
    private boolean gridHighVoltageTicketsEnabled;

    /** When false, battery deep discharge facilities do not produce alerts or tickets. Overcharge is unaffected by this flag. */
    @Value("${rms.rule.battery.deep.discharge.tickets.enabled:true}")
    private boolean batteryDeepDischargeTicketsEnabled;

    // Deduplication Configuration
    @Value("${rms.deduplication.suppression.window.hours:24}")
    private Integer suppressionWindowHours;

    // Facility Service Configuration
    @Value("${facility.service.base.url}")
    private String facilityServiceBaseUrl;

    @Value("${facility.service.search.endpoint}")
    private String facilityServiceSearchEndpoint;

    // MDMS Configuration
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;

    @Value("${rms.mdms.facility.sync.limit:200}")
    private Integer mdmsFacilitySyncLimit;

    // User Service Configuration
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.internal.microservice.user.uuid}")
    private String systemUserUuid;

    // Default Tenant
    @Value("${rms.default.tenant.id:in}")
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

    // Ticket Pause Configuration
    @Value("${rms.ticket.pause.enabled:true}")
    private Boolean ticketPauseEnabled;

    @Value("${rms.kafka.ticket.pause.topic.indexer:save-rms-ticket-pause-indexer}")
    private String ticketPauseAuditTopicIndexer;
}

